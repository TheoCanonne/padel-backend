package com.padel.repository

import com.padel.model.Event
import com.padel.model.EventStatus
import com.padel.model.Sport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface EventRepository : JpaRepository<Event, UUID> {

    // Trouver tous les événements d'un organisateur
    fun findByOrganizerId(organizerId: UUID): List<Event>

    // Trouver tous les événements d'un club
    fun findByClubId(clubId: UUID): List<Event>

    // Trouver les événements par sport
    fun findBySport(sport: Sport): List<Event>

    // Trouver les événements par statut
    fun findByStatus(status: EventStatus): List<Event>

    // Trouver les événements futurs ouverts
    fun findByStatusAndStartDateTimeAfter(status: EventStatus, dateTime: LocalDateTime): List<Event>

    // Trouver les événements d'un club avec un statut donné
    fun findByClubIdAndStatus(clubId: UUID, status: EventStatus): List<Event>

    // Trouver les événements d'un organisateur avec un statut donné
    fun findByOrganizerIdAndStatus(organizerId: UUID, status: EventStatus): List<Event>
}
