package com.padel.model

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "events")
class Event(
    @Id
    val id: UUID = UUID.randomUUID(),

    // Type d'événement (partie, tournoi, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var eventType: EventType = EventType.MATCH,

    // Sport pratiqué
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var sport: Sport = Sport.PADEL,

    // Lieu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "club_id", nullable = false)
    var club: Club,

    // Organisateur
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    var organizer: User,

    // Date et heure
    @Column(nullable = false)
    var startDateTime: LocalDateTime,

    @Column(nullable = false)
    var endDateTime: LocalDateTime,

    // Capacité
    @Column(nullable = false)
    var totalSlots: Int = 4, // Par défaut 4 joueurs pour le padel

    @Column(nullable = false)
    var occupiedSlots: Int = 0,

    // Règles d'éligibilité
    var minLevel: Int? = null, // Niveau minimum (1-10)
    var maxLevel: Int? = null, // Niveau maximum (1-10)

    // Visibilité
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var visibility: EventVisibility = EventVisibility.PUBLIC,

    // État
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: EventStatus = EventStatus.OPEN,

    // Description optionnelle
    @Column(columnDefinition = "TEXT")
    var description: String? = null,

    // Liste d'attente activée
    @Column(nullable = false)
    var waitingListEnabled: Boolean = true,

    // Validation automatique ou manuelle
    @Column(nullable = false)
    var autoAccept: Boolean = true,

    // Metadata
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),

    var cancelledAt: Instant? = null,
    var cancelReason: String? = null
) {
    fun isFull(): Boolean = occupiedSlots >= totalSlots

    fun hasAvailableSlots(): Boolean = occupiedSlots < totalSlots

    fun remainingSlots(): Int = totalSlots - occupiedSlots
}
