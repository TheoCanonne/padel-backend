package com.padel.controller

import com.padel.controller.dto.*
import com.padel.model.ClubType
import com.padel.security.ClerkAuthenticationPrincipal
import com.padel.service.UserService
import com.padel.service.club.usecase.CreateClubUseCase
import com.padel.service.club.usecase.GetClubByIdUseCase
import com.padel.service.club.usecase.ListClubsUseCase
import com.padel.service.club.usecase.SearchGooglePlacesUseCase
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
@RequestMapping("/api/v1/clubs")
@Tag(name = "Clubs", description = "Gestion des clubs et lieux de sport")
class ClubController(
    private val searchGooglePlacesUseCase: SearchGooglePlacesUseCase,
    private val createClubUseCase: CreateClubUseCase,
    private val listClubsUseCase: ListClubsUseCase,
    private val getClubByIdUseCase: GetClubByIdUseCase,
    private val userService: UserService
) {

    @GetMapping("/search/google")
    @Operation(
        summary = "Rechercher des lieux dans Google Places",
        description = """
            Recherche des lieux via l'API Google Places.
            Les résultats incluent un flag 'isKnown' indiquant si le lieu est déjà dans notre base.

            Exemples de requêtes :
            - "club de padel paris"
            - "terrain de tennis lyon"
            - "salle de futsal bordeaux"
        """
    )
    fun searchGooglePlaces(
        @Parameter(description = "Terme de recherche", example = "club de padel paris")
        @RequestParam query: String,

        @Parameter(description = "Latitude pour biaiser les résultats", example = "48.8566")
        @RequestParam(required = false) latitude: Double?,

        @Parameter(description = "Longitude pour biaiser les résultats", example = "2.3522")
        @RequestParam(required = false) longitude: Double?,

        @Parameter(description = "Rayon de recherche en mètres", example = "5000")
        @RequestParam(required = false) radius: Int?
    ): ResponseEntity<SearchGooglePlacesResponse> {
        val request = SearchGooglePlacesRequest(
            query = query,
            latitude = latitude,
            longitude = longitude,
            radius = radius
        )

        val response = searchGooglePlacesUseCase(request)
        return ResponseEntity.ok(response)
    }

    @PostMapping
    @Operation(
        summary = "Créer un club depuis Google Places",
        description = """
            Crée un club dans notre base de données en le référençant depuis Google Places.
            Les détails complets du lieu sont automatiquement récupérés depuis l'API Google Places.

            Le club créé sera marqué comme "connu" (isKnown=true) dans les futures recherches.
        """,
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    fun createClub(
        @Valid @RequestBody request: CreateClubRequest,
        authentication: Authentication
    ): ResponseEntity<ClubResponse> {
        val clerkUserId = ClerkAuthenticationPrincipal.getClerkUserId(authentication)
        val user = userService.findByClerkId(clerkUserId)
            ?: return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()

        try {
            val club = createClubUseCase(request, user)
            return ResponseEntity.status(HttpStatus.CREATED).body(club)
        } catch (e: IllegalArgumentException) {
            return ResponseEntity.badRequest().build()
        }
    }

    @GetMapping
    @Operation(
        summary = "Lister les clubs de notre base",
        description = """
            Liste UNIQUEMENT les clubs présents dans notre base de données.

            Filtres optionnels :
            - Par type de sport (PADEL, etc.)
            - Par géolocalisation et rayon (en km)

            Sans paramètres, retourne tous les clubs actifs triés par nom.
            Avec géolocalisation, les clubs sont triés par distance croissante.

            Note: Pour rechercher des lieux dans Google Maps, utilisez l'endpoint /clubs/search/google
        """
    )
    fun listClubs(
        @Parameter(description = "Type de club (PADEL)")
        @RequestParam(required = false) type: ClubType?,

        @Parameter(description = "Latitude du point de recherche", example = "48.8566")
        @RequestParam(required = false) latitude: Double?,

        @Parameter(description = "Longitude du point de recherche", example = "2.3522")
        @RequestParam(required = false) longitude: Double?,

        @Parameter(description = "Rayon de recherche en kilomètres", example = "10")
        @RequestParam(required = false) radiusInKm: Double?
    ): ResponseEntity<ListClubsResponse> {
        val request = ListClubsRequest(
            type = type,
            latitude = latitude,
            longitude = longitude,
            radiusInKm = radiusInKm
        )

        val response = listClubsUseCase(request)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtenir un club par ID",
        description = "Récupère les détails complets d'un club par son identifiant UUID."
    )
    fun getClubById(
        @Parameter(description = "ID du club")
        @PathVariable id: UUID
    ): ResponseEntity<ClubResponse> {
        return try {
            val club = getClubByIdUseCase(id)
            ResponseEntity.ok(club)
        } catch (e: IllegalArgumentException) {
            ResponseEntity.notFound().build()
        }
    }
}
