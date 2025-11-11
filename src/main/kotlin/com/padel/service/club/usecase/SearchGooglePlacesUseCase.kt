package com.padel.service.club.usecase

import com.padel.controller.dto.GooglePlaceSearchResult
import com.padel.controller.dto.LocationResponse
import com.padel.controller.dto.SearchGooglePlacesRequest
import com.padel.controller.dto.SearchGooglePlacesResponse
import com.padel.repository.ClubRepository
import com.padel.service.GooglePlacesService
import org.springframework.stereotype.Service

@Service
class SearchGooglePlacesUseCase(
    private val googlePlacesService: GooglePlacesService,
    private val clubRepository: ClubRepository
) {

    operator fun invoke(request: SearchGooglePlacesRequest): SearchGooglePlacesResponse {
        val location = if (request.latitude != null && request.longitude != null) {
            "${request.latitude},${request.longitude}"
        } else null

        val searchResponse = googlePlacesService.searchPlaces(
            query = request.query,
            location = location,
            radius = request.radius
        )

        if (searchResponse == null || searchResponse.status != "OK") {
            return SearchGooglePlacesResponse(
                results = emptyList(),
                status = searchResponse?.status ?: "ERROR"
            )
        }

        // Check which places are already in our database
        val placeIds = searchResponse.results.map { it.placeId }
        val knownPlaceIds = placeIds.mapNotNull { placeId ->
            if (clubRepository.existsByGooglePlaceId(placeId)) placeId else null
        }.toSet()

        val results = searchResponse.results.map { result ->
            val photoUrl = result.photos?.firstOrNull()?.let { photo ->
                googlePlacesService.getPhotoUrl(photo.photoReference)
            }

            GooglePlaceSearchResult(
                placeId = result.placeId,
                name = result.name,
                formattedAddress = result.formattedAddress ?: result.vicinity ?: "",
                location = LocationResponse(
                    latitude = result.geometry.location.lat,
                    longitude = result.geometry.location.lng
                ),
                rating = result.rating,
                totalRatings = result.userRatingsTotal,
                photoUrl = photoUrl,
                types = result.types,
                isKnown = knownPlaceIds.contains(result.placeId)
            )
        }

        return SearchGooglePlacesResponse(
            results = results,
            status = "OK"
        )
    }
}
