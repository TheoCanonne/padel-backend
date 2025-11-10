package com.padel.controller

import com.padel.model.User
import com.padel.security.ClerkAuthenticationPrincipal
import com.padel.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints d'authentification et gestion utilisateur")
class AuthController(
    private val userService: UserService
) {

    @GetMapping("/me")
    @Operation(
        summary = "Obtenir l'utilisateur courant",
        description = """
            Récupère les informations de l'utilisateur authentifié via Clerk JWT.

            Note : Les utilisateurs sont automatiquement synchronisés via le webhook Clerk.
            Si l'utilisateur n'existe pas encore en base, il sera créé lors de son premier événement Clerk.
        """,
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    fun getCurrentUser(authentication: Authentication): ResponseEntity<UserResponse> {
        val clerkUserId = ClerkAuthenticationPrincipal.getClerkUserId(authentication)

        val user = userService.findByClerkId(clerkUserId)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(UserResponse.fromUser(user))
    }
}

data class UserResponse(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val photoUrl: String?,
    val emailVerified: Boolean
) {
    companion object {
        fun fromUser(user: User): UserResponse {
            return UserResponse(
                id = user.id.toString(),
                email = user.email,
                firstName = user.firstName,
                lastName = user.lastName,
                photoUrl = user.photoUrl,
                emailVerified = user.emailVerified
            )
        }
    }
}
