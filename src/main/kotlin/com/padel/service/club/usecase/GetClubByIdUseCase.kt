package com.padel.service.club.usecase

import com.padel.controller.dto.ClubResponse
import com.padel.repository.ClubRepository
import com.padel.service.club.mapper.ClubMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class GetClubByIdUseCase(
    private val clubRepository: ClubRepository,
    private val clubMapper: ClubMapper
) {

    operator fun invoke(id: UUID): ClubResponse {
        val club = clubRepository.findById(id)
            .orElseThrow { IllegalArgumentException("Club not found") }
        return clubMapper.toClubResponse(club)
    }
}
