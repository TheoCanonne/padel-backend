package com.padel.controller.dto

import com.padel.model.EventStatus
import com.padel.model.EventType
import com.padel.model.EventVisibility
import com.padel.model.Sport
import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.UUID

// Request DTOs

data class CreateEventRequest(
    @field:NotNull(message = "Event type is required")
    val eventType: EventType,

    @field:NotNull(message = "Sport is required")
    val sport: Sport,

    @field:NotNull(message = "Club ID is required")
    val clubId: UUID,

    @field:NotNull(message = "Start date and time is required")
    val startDateTime: LocalDateTime,

    @field:NotNull(message = "End date and time is required")
    val endDateTime: LocalDateTime,

    @field:Positive(message = "Total slots must be positive")
    @field:Min(value = 2, message = "Total slots must be at least 2")
    @field:Max(value = 50, message = "Total slots cannot exceed 50")
    val totalSlots: Int = 4,

    @field:Min(value = 1, message = "Minimum level must be between 1 and 10")
    @field:Max(value = 10, message = "Minimum level must be between 1 and 10")
    val minLevel: Int? = null,

    @field:Min(value = 1, message = "Maximum level must be between 1 and 10")
    @field:Max(value = 10, message = "Maximum level must be between 1 and 10")
    val maxLevel: Int? = null,

    @field:NotNull(message = "Visibility is required")
    val visibility: EventVisibility = EventVisibility.PUBLIC,

    @field:Size(max = 2000, message = "Description cannot exceed 2000 characters")
    val description: String? = null,

    val waitingListEnabled: Boolean = true,

    val autoAccept: Boolean = true
)

data class UpdateEventRequest(
    val eventType: EventType? = null,
    val startDateTime: LocalDateTime? = null,
    val endDateTime: LocalDateTime? = null,
    val totalSlots: Int? = null,
    val minLevel: Int? = null,
    val maxLevel: Int? = null,
    val visibility: EventVisibility? = null,
    val description: String? = null,
    val waitingListEnabled: Boolean? = null,
    val autoAccept: Boolean? = null,
    val status: EventStatus? = null
)

// Response DTOs

data class EventResponse(
    val id: UUID,
    val eventType: EventType,
    val sport: Sport,
    val club: ClubSummaryResponse,
    val organizer: UserSummaryResponse,
    val startDateTime: LocalDateTime,
    val endDateTime: LocalDateTime,
    val totalSlots: Int,
    val occupiedSlots: Int,
    val remainingSlots: Int,
    val minLevel: Int? = null,
    val maxLevel: Int? = null,
    val visibility: EventVisibility,
    val status: EventStatus,
    val description: String? = null,
    val waitingListEnabled: Boolean,
    val autoAccept: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
    val cancelledAt: Instant? = null,
    val cancelReason: String? = null
)

data class ClubSummaryResponse(
    val id: UUID,
    val name: String,
    val formattedAddress: String,
    val location: LocationResponse
)

data class UserSummaryResponse(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val photoUrl: String? = null
)

data class ListEventsResponse(
    val events: List<EventResponse>,
    val total: Int
)
