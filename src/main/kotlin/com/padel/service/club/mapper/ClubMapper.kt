package com.padel.service.club.mapper

import com.fasterxml.jackson.databind.ObjectMapper
import com.padel.controller.dto.AddressComponentsResponse
import com.padel.controller.dto.ClubResponse
import com.padel.controller.dto.LocationResponse
import com.padel.model.Club
import com.padel.service.dto.GooglePlaceDetails
import org.springframework.stereotype.Component

@Component
class ClubMapper(
    private val objectMapper: ObjectMapper
) {

    fun toClubResponse(club: Club, distance: Double? = null): ClubResponse {
        val openingHours = club.openingHours?.let {
            objectMapper.readValue(it, Any::class.java)
        }

        return ClubResponse(
            id = club.id,
            googlePlaceId = club.googlePlaceId,
            name = club.name,
            type = club.type,
            formattedAddress = club.formattedAddress,
            location = LocationResponse(
                latitude = club.location.y,
                longitude = club.location.x
            ),
            addressComponents = AddressComponentsResponse(
                streetNumber = club.streetNumber,
                route = club.route,
                locality = club.locality,
                postalCode = club.postalCode,
                country = club.country
            ),
            rating = club.rating,
            totalRatings = club.totalRatings,
            phoneNumber = club.phoneNumber,
            website = club.website,
            googleMapsUrl = club.googleMapsUrl,
            photoUrl = club.photoUrl,
            openingHours = openingHours,
            isVerified = club.isVerified,
            distance = distance,
            createdAt = club.createdAt,
            updatedAt = club.updatedAt
        )
    }

    fun extractAddressComponents(placeDetails: GooglePlaceDetails): Map<String, String?> {
        val components = mutableMapOf<String, String?>()

        placeDetails.addressComponents?.forEach { component ->
            when {
                component.types.contains("street_number") -> components["streetNumber"] = component.longName
                component.types.contains("route") -> components["route"] = component.longName
                component.types.contains("locality") -> components["locality"] = component.longName
                component.types.contains("postal_code") -> components["postalCode"] = component.longName
                component.types.contains("country") -> components["country"] = component.longName
            }
        }

        return components
    }

    /**
     * Calculate distance between two points using Haversine formula
     * Returns distance in meters
     */
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371000.0 // meters

        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)

        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return earthRadius * c
    }
}
