package com.padel.service.club.usecase

import com.padel.controller.dto.ListClubsRequest
import com.padel.controller.dto.ListClubsResponse
import com.padel.repository.ClubRepository
import com.padel.service.club.mapper.ClubMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListClubsUseCase(
    private val clubRepository: ClubRepository,
    private val clubMapper: ClubMapper
) {

    operator fun invoke(request: ListClubsRequest): ListClubsResponse {
        val clubs = when {
            // Search with location and radius
            request.latitude != null && request.longitude != null && request.radiusInKm != null -> {
                val radiusInMeters = request.radiusInKm * 1000
                if (request.type != null) {
                    clubRepository.findClubsWithinRadiusByType(
                        latitude = request.latitude,
                        longitude = request.longitude,
                        radiusInMeters = radiusInMeters,
                        type = request.type.name
                    )
                } else {
                    clubRepository.findClubsWithinRadius(
                        latitude = request.latitude,
                        longitude = request.longitude,
                        radiusInMeters = radiusInMeters
                    )
                }
            }
            // Filter by type only
            request.type != null -> {
                clubRepository.findByIsActiveTrueAndTypeOrderByNameAsc(request.type)
            }
            // List all clubs
            else -> {
                clubRepository.findByIsActiveTrueOrderByNameAsc()
            }
        }

        val clubResponses = clubs.map { club ->
            val distance = if (request.latitude != null && request.longitude != null) {
                clubMapper.calculateDistance(
                    request.latitude,
                    request.longitude,
                    club.location.y,
                    club.location.x
                )
            } else null

            clubMapper.toClubResponse(club, distance = distance)
        }

        return ListClubsResponse(
            clubs = clubResponses,
            total = clubResponses.size
        )
    }
}
