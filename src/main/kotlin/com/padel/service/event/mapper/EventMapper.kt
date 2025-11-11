package com.padel.service.event.mapper

import com.padel.controller.dto.*
import com.padel.model.Club
import com.padel.model.Event
import com.padel.model.User
import org.springframework.stereotype.Component

@Component
class EventMapper {

    fun toEventResponse(event: Event): EventResponse {
        return EventResponse(
            id = event.id,
            eventType = event.eventType,
            sport = event.sport,
            club = toClubSummary(event.club),
            organizer = toUserSummary(event.organizer),
            startDateTime = event.startDateTime,
            endDateTime = event.endDateTime,
            totalSlots = event.totalSlots,
            occupiedSlots = event.occupiedSlots,
            remainingSlots = event.remainingSlots(),
            minLevel = event.minLevel,
            maxLevel = event.maxLevel,
            visibility = event.visibility,
            status = event.status,
            description = event.description,
            waitingListEnabled = event.waitingListEnabled,
            autoAccept = event.autoAccept,
            createdAt = event.createdAt,
            updatedAt = event.updatedAt,
            cancelledAt = event.cancelledAt,
            cancelReason = event.cancelReason
        )
    }

    private fun toClubSummary(club: Club): ClubSummaryResponse {
        return ClubSummaryResponse(
            id = club.id,
            name = club.name,
            formattedAddress = club.formattedAddress,
            location = LocationResponse(
                latitude = club.location.y,
                longitude = club.location.x
            )
        )
    }

    private fun toUserSummary(user: User): UserSummaryResponse {
        return UserSummaryResponse(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            photoUrl = user.photoUrl
        )
    }
}
