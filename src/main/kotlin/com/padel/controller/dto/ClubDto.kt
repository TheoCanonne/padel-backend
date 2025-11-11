package com.padel.controller.dto

import com.padel.model.ClubType
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

// Request DTOs

data class CreateClubRequest(
    @field:NotBlank(message = "Google Place ID is required")
    val googlePlaceId: String,

    val type: ClubType = ClubType.PADEL
)

data class SearchGooglePlacesRequest(
    @field:NotBlank(message = "Search query is required")
    val query: String,

    val latitude: Double? = null,
    val longitude: Double? = null,
    val radius: Int? = null // in meters
)

data class ListClubsRequest(
    val type: ClubType? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusInKm: Double? = null // in kilometers
)

// Response DTOs

data class ClubResponse(
    val id: UUID,
    val googlePlaceId: String,
    val name: String,
    val type: ClubType,
    val formattedAddress: String,
    val location: LocationResponse,
    val addressComponents: AddressComponentsResponse? = null,
    val rating: BigDecimal? = null,
    val totalRatings: Int? = null,
    val phoneNumber: String? = null,
    val website: String? = null,
    val googleMapsUrl: String? = null,
    val photoUrl: String? = null,
    val openingHours: Any? = null, // JSON object
    val isVerified: Boolean,
    val distance: Double? = null, // in meters, only when querying with location
    val createdAt: Instant,
    val updatedAt: Instant
)

data class LocationResponse(
    val latitude: Double,
    val longitude: Double
)

data class AddressComponentsResponse(
    val streetNumber: String? = null,
    val route: String? = null,
    val locality: String? = null,
    val postalCode: String? = null,
    val country: String? = null
)

data class GooglePlaceSearchResult(
    val placeId: String,
    val name: String,
    val formattedAddress: String,
    val location: LocationResponse,
    val rating: Double? = null,
    val totalRatings: Int? = null,
    val photoUrl: String? = null,
    val types: List<String>? = null,
    val isKnown: Boolean // true if already in our database
)

data class SearchGooglePlacesResponse(
    val results: List<GooglePlaceSearchResult>,
    val status: String
)

data class ListClubsResponse(
    val clubs: List<ClubResponse>,
    val total: Int
)
