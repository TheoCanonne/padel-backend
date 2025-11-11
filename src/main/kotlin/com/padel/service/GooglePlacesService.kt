package com.padel.service

import com.padel.service.dto.GooglePlaceDetails
import com.padel.service.dto.GooglePlaceDetailsResponse
import com.padel.service.dto.GooglePlacesAutocompleteResponse
import com.padel.service.dto.GooglePlacesSearchResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Service
class GooglePlacesService(
    @Value("\${external.google-maps.api-key}")
    private val apiKey: String,
    private val webClient: WebClient = WebClient.builder()
        .baseUrl("https://maps.googleapis.com/maps/api")
        .build()
) {
    private val logger = LoggerFactory.getLogger(GooglePlacesService::class.java)

    /**
     * Search places using Text Search API
     * @param query Search query (e.g., "padel clubs in Paris")
     * @param location Optional location bias as "lat,lng"
     * @param radius Optional radius in meters
     */
    fun searchPlaces(
        query: String,
        location: String? = null,
        radius: Int? = null
    ): GooglePlacesSearchResponse? {
        try {
            val uri = buildSearchUri(query, location, radius)
            logger.info("Searching Google Places: $uri")

            return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GooglePlacesSearchResponse::class.java)
                .onErrorResume { error ->
                    logger.error("Error searching places: ${error.message}", error)
                    Mono.empty()
                }
                .block()
        } catch (e: Exception) {
            logger.error("Failed to search places: ${e.message}", e)
            return null
        }
    }

    /**
     * Get place details by place ID
     */
    fun getPlaceDetails(placeId: String): GooglePlaceDetails? {
        try {
            val uri = "/place/details/json?place_id=$placeId&key=$apiKey&fields=" +
                    "place_id,name,formatted_address,address_components,geometry," +
                    "formatted_phone_number,international_phone_number,website,url," +
                    "rating,user_ratings_total,photos,opening_hours,types"

            logger.info("Fetching place details for: $placeId")

            val response = webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GooglePlaceDetailsResponse::class.java)
                .onErrorResume { error ->
                    logger.error("Error fetching place details: ${error.message}", error)
                    Mono.empty()
                }
                .block()

            if (response?.status != "OK") {
                logger.warn("Google Places API returned status: ${response?.status}")
                return null
            }

            return response.result
        } catch (e: Exception) {
            logger.error("Failed to fetch place details: ${e.message}", e)
            return null
        }
    }

    /**
     * Autocomplete search for places
     */
    fun autocompletePlaces(
        input: String,
        location: String? = null,
        radius: Int? = null,
        types: String? = "establishment"
    ): GooglePlacesAutocompleteResponse? {
        try {
            var uri = "/place/autocomplete/json?input=$input&key=$apiKey"
            if (location != null) uri += "&location=$location"
            if (radius != null) uri += "&radius=$radius"
            if (types != null) uri += "&types=$types"

            logger.info("Autocompleting places: $input")

            return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(GooglePlacesAutocompleteResponse::class.java)
                .onErrorResume { error ->
                    logger.error("Error in autocomplete: ${error.message}", error)
                    Mono.empty()
                }
                .block()
        } catch (e: Exception) {
            logger.error("Failed to autocomplete places: ${e.message}", e)
            return null
        }
    }

    /**
     * Get photo URL from photo reference
     */
    fun getPhotoUrl(photoReference: String, maxWidth: Int = 400): String {
        return "https://maps.googleapis.com/maps/api/place/photo?" +
                "maxwidth=$maxWidth&photo_reference=$photoReference&key=$apiKey"
    }

    private fun buildSearchUri(query: String, location: String?, radius: Int?): String {
        var uri = "/place/textsearch/json?query=$query&key=$apiKey"
        if (location != null) uri += "&location=$location"
        if (radius != null) uri += "&radius=$radius"
        return uri
    }
}
