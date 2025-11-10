package com.padel.controller.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClerkWebhookEvent(
    val type: String,
    val data: ClerkUserData
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClerkUserData(
    val id: String,
    @JsonProperty("email_addresses")
    val emailAddresses: List<ClerkEmailAddress> = emptyList(),
    @JsonProperty("first_name")
    val firstName: String?,
    @JsonProperty("last_name")
    val lastName: String?,
    @JsonProperty("profile_image_url")
    val profileImageUrl: String?,
    @JsonProperty("primary_email_address_id")
    val primaryEmailAddressId: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClerkEmailAddress(
    val id: String,
    @JsonProperty("email_address")
    val emailAddress: String,
    val verification: ClerkVerification?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ClerkVerification(
    val status: String
)
