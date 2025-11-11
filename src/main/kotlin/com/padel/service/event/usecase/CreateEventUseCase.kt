package com.padel.service.event.usecase

import com.padel.controller.dto.CreateEventRequest
import com.padel.controller.dto.EventResponse
import com.padel.model.Event
import com.padel.model.EventStatus
import com.padel.model.User
import com.padel.repository.ClubRepository
import com.padel.repository.EventRepository
import com.padel.service.event.mapper.EventMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
@Transactional
class CreateEventUseCase(
    private val eventRepository: EventRepository,
    private val clubRepository: ClubRepository,
    private val eventMapper: EventMapper
) {
    private val logger = LoggerFactory.getLogger(CreateEventUseCase::class.java)

    operator fun invoke(request: CreateEventRequest, organizer: User): EventResponse {
        // Validate dates
        if (request.endDateTime.isBefore(request.startDateTime)) {
            throw IllegalArgumentException("End date must be after start date")
        }

        // Validate levels
        if (request.minLevel != null && request.maxLevel != null && request.minLevel > request.maxLevel) {
            throw IllegalArgumentException("Minimum level cannot be greater than maximum level")
        }

        // Find club
        val club = clubRepository.findById(request.clubId)
            .orElseThrow { IllegalArgumentException("Club not found with ID: ${request.clubId}") }

        // Create event
        val event = Event(
            eventType = request.eventType,
            sport = request.sport,
            club = club,
            organizer = organizer,
            startDateTime = request.startDateTime,
            endDateTime = request.endDateTime,
            totalSlots = request.totalSlots,
            occupiedSlots = 1, // L'organisateur occupe une place
            minLevel = request.minLevel,
            maxLevel = request.maxLevel,
            visibility = request.visibility,
            status = EventStatus.OPEN,
            description = request.description,
            waitingListEnabled = request.waitingListEnabled,
            autoAccept = request.autoAccept,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val savedEvent = eventRepository.save(event)
        logger.info("Event created: ${savedEvent.id} by organizer ${organizer.id}")

        return eventMapper.toEventResponse(savedEvent)
    }
}
