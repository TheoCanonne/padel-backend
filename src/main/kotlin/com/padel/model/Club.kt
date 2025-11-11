package com.padel.model

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import org.locationtech.jts.geom.Point
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Entity
@Table(name = "clubs")
class Club(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var googlePlaceId: String,

    @Column(nullable = false)
    var name: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var formattedAddress: String,

    // Club type
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ClubType = ClubType.PADEL,

    // PostGIS geography point (longitude, latitude)
    @Column(nullable = false, columnDefinition = "geography(Point,4326)")
    var location: Point,

    // Address components
    var streetNumber: String? = null,
    var route: String? = null,
    var locality: String? = null,
    var postalCode: String? = null,
    var country: String? = null,

    // Google Places data
    var rating: BigDecimal? = null,
    var totalRatings: Int? = null,
    var phoneNumber: String? = null,
    var website: String? = null,
    var googleMapsUrl: String? = null,
    var photoUrl: String? = null,

    // Business hours (stored as JSONB)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var openingHours: String? = null,

    // Status
    @Column(nullable = false)
    var isVerified: Boolean = false,

    @Column(nullable = false)
    var isActive: Boolean = true,

    // Metadata
    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(nullable = false)
    var updatedAt: Instant = Instant.now(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    var createdBy: User? = null,

    // Google Places sync
    var lastSyncedAt: Instant? = null
)
