package com.padel.controller

import com.padel.controller.dto.CreateEventRequest
import com.padel.controller.dto.EventResponse
import com.padel.security.ClerkAuthenticationPrincipal
import com.padel.service.UserService
import com.padel.service.event.usecase.CreateEventUseCase
import com.padel.service.event.usecase.GetEventByIdUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Gestion des événements (parties, tournois, etc.)")
class EventController(
    private val createEventUseCase: CreateEventUseCase,
    private val getEventByIdUseCase: GetEventByIdUseCase,
    private val userService: UserService
) {

    @PostMapping
    @Operation(
        summary = "Créer un événement",
        description = """
            Crée un nouvel événement (partie, tournoi, entraînement, etc.).

            L'utilisateur authentifié devient automatiquement l'organisateur de l'événement
            et occupe une place (occupiedSlots = 1 par défaut).

            Validations :
            - endDateTime doit être après startDateTime
            - minLevel <= maxLevel (si spécifiés)
            - totalSlots entre 2 et 50
            - Le club doit exister dans la base
        """,
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    fun createEvent(
        @Valid @RequestBody request: CreateEventRequest,
        authentication: Authentication
    ): ResponseEntity<EventResponse> {
        val clerkUserId = ClerkAuthenticationPrincipal.getClerkUserId(authentication)
        val user = userService.findByClerkId(clerkUserId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        return try {
            val event = createEventUseCase(request, user)
            ResponseEntity.status(HttpStatus.CREATED).body(event)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest().build()
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtenir un événement par ID",
        description = """
            Récupère les détails complets d'un événement par son identifiant UUID.

            Retourne :
            - Les informations de l'événement
            - Le résumé du club (nom, adresse, localisation)
            - Le résumé de l'organisateur (nom, photo)
            - Le nombre de places disponibles/occupées
        """
    )
    fun getEventById(
        @Parameter(description = "ID de l'événement")
        @PathVariable id: UUID
    ): ResponseEntity<EventResponse> {
        return try {
            val event = getEventByIdUseCase(id)
            ResponseEntity.ok(event)
        } catch (e: NoSuchElementException) {
            ResponseEntity.notFound().build()
        }
    }
}
