package com.padel.repository

import com.padel.model.Club
import com.padel.model.ClubType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ClubRepository : JpaRepository<Club, UUID> {

    fun findByGooglePlaceId(googlePlaceId: String): Club?

    fun existsByGooglePlaceId(googlePlaceId: String): Boolean

    /**
     * Find all active clubs ordered by name
     */
    fun findByIsActiveTrueOrderByNameAsc(): List<Club>

    /**
     * Find all active clubs of a specific type
     */
    fun findByIsActiveTrueAndTypeOrderByNameAsc(type: ClubType): List<Club>

    /**
     * Find clubs within a radius (in meters) of a given point
     */
    @Query(
        value = """
        SELECT * FROM clubs c
        WHERE c.is_active = true
        AND ST_DWithin(
            c.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusInMeters
        ) = true
        ORDER BY ST_Distance(
            c.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
        ) ASC
        """,
        nativeQuery = true
    )
    fun findClubsWithinRadius(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusInMeters") radiusInMeters: Double
    ): List<Club>

    /**
     * Find clubs within a radius and filter by type
     */
    @Query(
        value = """
        SELECT * FROM clubs c
        WHERE c.is_active = true
        AND c.type = :type
        AND ST_DWithin(
            c.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
            :radiusInMeters
        ) = true
        ORDER BY ST_Distance(
            c.location,
            CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
        ) ASC
        """,
        nativeQuery = true
    )
    fun findClubsWithinRadiusByType(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusInMeters") radiusInMeters: Double,
        @Param("type") type: String
    ): List<Club>
}
