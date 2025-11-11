package com.padel.service.dto

import com.fasterxml.jackson.annotation.JsonProperty

// Google Places API response structures

data class GooglePlacesSearchResponse(
    val results: List<GooglePlaceResult>,
    val status: String,
    @JsonProperty("next_page_token")
    val nextPageToken: String? = null
)

data class GooglePlaceResult(
    @JsonProperty("place_id")
    val placeId: String,
    val name: String,
    val vicinity: String? = null,
    @JsonProperty("formatted_address")
    val formattedAddress: String? = null,
    val geometry: GooglePlaceGeometry,
    val rating: Double? = null,
    @JsonProperty("user_ratings_total")
    val userRatingsTotal: Int? = null,
    val photos: List<GooglePlacePhoto>? = null,
    val types: List<String>? = null
)

data class GooglePlaceGeometry(
    val location: GooglePlaceLocation
)

data class GooglePlaceLocation(
    val lat: Double,
    val lng: Double
)

data class GooglePlacePhoto(
    @JsonProperty("photo_reference")
    val photoReference: String,
    val height: Int,
    val width: Int,
    @JsonProperty("html_attributions")
    val htmlAttributions: List<String>? = null
)

// Place Details API response

data class GooglePlaceDetailsResponse(
    val result: GooglePlaceDetails,
    val status: String
)

data class GooglePlaceDetails(
    @JsonProperty("place_id")
    val placeId: String,
    val name: String,
    @JsonProperty("formatted_address")
    val formattedAddress: String,
    @JsonProperty("address_components")
    val addressComponents: List<GoogleAddressComponent>? = null,
    val geometry: GooglePlaceGeometry,
    @JsonProperty("formatted_phone_number")
    val formattedPhoneNumber: String? = null,
    @JsonProperty("international_phone_number")
    val internationalPhoneNumber: String? = null,
    val website: String? = null,
    val url: String? = null,
    val rating: Double? = null,
    @JsonProperty("user_ratings_total")
    val userRatingsTotal: Int? = null,
    val photos: List<GooglePlacePhoto>? = null,
    @JsonProperty("opening_hours")
    val openingHours: GooglePlaceOpeningHours? = null,
    val types: List<String>? = null
)

data class GoogleAddressComponent(
    @JsonProperty("long_name")
    val longName: String,
    @JsonProperty("short_name")
    val shortName: String,
    val types: List<String>
)

data class GooglePlaceOpeningHours(
    @JsonProperty("open_now")
    val openNow: Boolean? = null,
    val periods: List<GooglePlaceOpeningPeriod>? = null,
    @JsonProperty("weekday_text")
    val weekdayText: List<String>? = null
)

data class GooglePlaceOpeningPeriod(
    val open: GooglePlaceOpeningTime,
    val close: GooglePlaceOpeningTime? = null
)

data class GooglePlaceOpeningTime(
    val day: Int,
    val time: String
)

// Autocomplete API response

data class GooglePlacesAutocompleteResponse(
    val predictions: List<GooglePlacePrediction>,
    val status: String
)

data class GooglePlacePrediction(
    val description: String,
    @JsonProperty("place_id")
    val placeId: String,
    @JsonProperty("structured_formatting")
    val structuredFormatting: GooglePlaceStructuredFormatting? = null,
    val types: List<String>? = null
)

data class GooglePlaceStructuredFormatting(
    @JsonProperty("main_text")
    val mainText: String,
    @JsonProperty("secondary_text")
    val secondaryText: String? = null
)
