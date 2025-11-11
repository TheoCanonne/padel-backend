package com.padel.service.event.usecase

import com.padel.controller.dto.EventResponse
import com.padel.repository.EventRepository
import com.padel.service.event.mapper.EventMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class GetEventByIdUseCase(
    private val eventRepository: EventRepository,
    private val eventMapper: EventMapper
) {
    private val logger = LoggerFactory.getLogger(GetEventByIdUseCase::class.java)

    operator fun invoke(eventId: UUID): EventResponse {
        val event = eventRepository.findById(eventId)
            .orElseThrow { NoSuchElementException("Event not found with ID: $eventId") }

        logger.debug("Retrieved event: ${event.id}")
        return eventMapper.toEventResponse(event)
    }
}
