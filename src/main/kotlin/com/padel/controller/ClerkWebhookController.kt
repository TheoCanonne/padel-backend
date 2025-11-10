package com.padel.controller

import com.padel.controller.dto.ClerkWebhookEvent
import com.padel.security.ClerkWebhookValidator
import com.padel.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhooks/clerk")
@Tag(name = "Webhooks", description = "Webhooks pour la synchronisation automatique avec les services externes")
class ClerkWebhookController(
    private val userService: UserService,
    private val webhookValidator: ClerkWebhookValidator
) {

    private val logger = LoggerFactory.getLogger(ClerkWebhookController::class.java)

    @PostMapping
    @Operation(
        summary = "Webhook Clerk pour la synchronisation des utilisateurs",
        description = """
            Reçoit les événements Clerk et synchronise automatiquement les utilisateurs dans notre base de données.

            **Événements gérés :**
            - `user.created` : Crée un nouvel utilisateur
            - `user.updated` : Met à jour les informations utilisateur
            - `user.deleted` : Soft-delete de l'utilisateur

            **Configuration :**
            1. Allez dans Clerk Dashboard → Webhooks
            2. Créez un endpoint : `https://your-api.com/api/v1/webhooks/clerk`
            3. Sélectionnez les événements : user.created, user.updated, user.deleted
            4. Copiez le Signing Secret dans votre variable d'environnement `CLERK_WEBHOOK_SECRET`

            **Sécurité :**
            La signature du webhook est validée via HMAC SHA256 pour garantir l'authenticité.
        """
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Webhook traité avec succès",
                content = [Content(schema = Schema(example = "{\"status\": \"success\"}"))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Signature invalide",
                content = [Content(schema = Schema(example = "{\"error\": \"Invalid signature\"}"))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Données invalides",
                content = [Content(schema = Schema(example = "{\"error\": \"No email found\"}"))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Erreur serveur",
                content = [Content(schema = Schema(example = "{\"error\": \"Internal server error\"}"))]
            )
        ]
    )
    fun handleWebhook(
        @Parameter(description = "Payload JSON de l'événement Clerk", required = true)
        @RequestBody payload: String,

        @Parameter(description = "Signature Svix pour validation (format: v1,signature)")
        @RequestHeader("svix-signature", required = false) signature: String?,

        @Parameter(description = "Timestamp de l'événement (Unix timestamp)")
        @RequestHeader("svix-timestamp", required = false) timestamp: String?,

        @Parameter(description = "ID unique de l'événement")
        @RequestHeader("svix-id", required = false) id: String?
    ): ResponseEntity<Map<String, String>> {
        logger.info("Received webhook event - ID: $id")

        // Validate webhook signature
        if (!webhookValidator.validateSignature(payload, signature, timestamp, id)) {
            logger.warn("Invalid webhook signature received")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(mapOf("error" to "Invalid signature"))
        }

        return try {
            // Parse the webhook event
            val event = parseWebhookEvent(payload)

            when (event.type) {
                "user.created", "user.updated" -> handleUserCreatedOrUpdated(event)
                "user.deleted" -> handleUserDeleted(event)
                else -> {
                    logger.info("Unhandled webhook event type: ${event.type}")
                    ResponseEntity.ok(mapOf("status" to "ignored"))
                }
            }
        } catch (e: Exception) {
            logger.error("Error processing webhook", e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(mapOf("error" to "Internal server error"))
        }
    }

    private fun handleUserCreatedOrUpdated(event: ClerkWebhookEvent): ResponseEntity<Map<String, String>> {
        val data = event.data

        // Find primary email
        val primaryEmail = data.emailAddresses.firstOrNull {
            it.id == data.primaryEmailAddressId
        } ?: data.emailAddresses.firstOrNull()

        if (primaryEmail == null) {
            // Cas des webhooks de test Clerk (email_addresses vide)
            logger.info("No email found for user ${data.id} - likely a test webhook, ignoring")
            return ResponseEntity.ok(mapOf("status" to "ignored", "reason" to "test webhook"))
        }

        val emailVerified = primaryEmail.verification?.status == "verified"

        // Sync user to database
        userService.syncUserFromClerk(
            clerkUserId = data.id,
            email = primaryEmail.emailAddress,
            firstName = data.firstName ?: "",
            lastName = data.lastName ?: "",
            photoUrl = data.profileImageUrl,
            emailVerified = emailVerified
        )

        logger.info("User ${data.id} synchronized successfully")
        return ResponseEntity.ok(mapOf("status" to "success"))
    }

    private fun handleUserDeleted(event: ClerkWebhookEvent): ResponseEntity<Map<String, String>> {
        userService.deleteUserByClerkId(event.data.id)
        logger.info("User ${event.data.id} deleted successfully")
        return ResponseEntity.ok(mapOf("status" to "success"))
    }

    private fun parseWebhookEvent(payload: String): ClerkWebhookEvent {
        val objectMapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
        return objectMapper.readValue(payload, ClerkWebhookEvent::class.java)
    }
}
