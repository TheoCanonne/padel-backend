package com.padel.service.club.usecase

import com.fasterxml.jackson.databind.ObjectMapper
import com.padel.controller.dto.ClubResponse
import com.padel.controller.dto.CreateClubRequest
import com.padel.model.Club
import com.padel.model.User
import com.padel.repository.ClubRepository
import com.padel.service.GooglePlacesService
import com.padel.service.club.mapper.ClubMapper
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.Instant

@Service
@Transactional
class CreateClubUseCase(
    private val clubRepository: ClubRepository,
    private val googlePlacesService: GooglePlacesService,
    private val clubMapper: ClubMapper,
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(CreateClubUseCase::class.java)
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    operator fun invoke(request: CreateClubRequest, user: User): ClubResponse {
        // Check if club already exists
        val existingClub = clubRepository.findByGooglePlaceId(request.googlePlaceId)
        if (existingClub != null) {
            logger.info("Club already exists: ${existingClub.name}")
            return clubMapper.toClubResponse(existingClub)
        }

        // Fetch details from Google Places
        val placeDetails = googlePlacesService.getPlaceDetails(request.googlePlaceId)
            ?: throw IllegalArgumentException("Place not found in Google Places API")

        // Extract address components
        val addressComponents = clubMapper.extractAddressComponents(placeDetails)

        // Create geometry point (longitude, latitude)
        val point = geometryFactory.createPoint(
            Coordinate(
                placeDetails.geometry.location.lng,
                placeDetails.geometry.location.lat
            )
        )

        // Get photo URL
        val photoUrl = placeDetails.photos?.firstOrNull()?.let { photo ->
            googlePlacesService.getPhotoUrl(photo.photoReference)
        }

        // Serialize opening hours to JSON
        val openingHoursJson = placeDetails.openingHours?.let {
            objectMapper.writeValueAsString(it)
        }

        // Create club entity
        val club = Club(
            googlePlaceId = placeDetails.placeId,
            name = placeDetails.name,
            formattedAddress = placeDetails.formattedAddress,
            type = request.type,
            location = point,
            streetNumber = addressComponents["streetNumber"],
            route = addressComponents["route"],
            locality = addressComponents["locality"],
            postalCode = addressComponents["postalCode"],
            country = addressComponents["country"],
            rating = placeDetails.rating?.let { BigDecimal.valueOf(it) },
            totalRatings = placeDetails.userRatingsTotal,
            phoneNumber = placeDetails.formattedPhoneNumber,
            website = placeDetails.website,
            googleMapsUrl = placeDetails.url,
            photoUrl = photoUrl,
            openingHours = openingHoursJson,
            isVerified = false,
            isActive = true,
            createdBy = user,
            lastSyncedAt = Instant.now()
        )

        val savedClub = clubRepository.save(club)
        logger.info("Club created: ${savedClub.name} (${savedClub.id})")

        return clubMapper.toClubResponse(savedClub)
    }
}
