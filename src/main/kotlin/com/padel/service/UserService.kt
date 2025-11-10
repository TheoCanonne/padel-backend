package com.padel.service

import com.padel.model.User
import com.padel.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class UserService(
    private val userRepository: UserRepository
) {

    @Transactional
    fun syncUserFromClerk(
        clerkUserId: String,
        email: String,
        firstName: String,
        lastName: String,
        photoUrl: String?,
        emailVerified: Boolean
    ): User {
        val existingUser = userRepository.findByExternalAuthId(clerkUserId)

        return if (existingUser != null) {
            // Update existing user
            existingUser.email = email
            existingUser.firstName = firstName
            existingUser.lastName = lastName
            existingUser.photoUrl = photoUrl
            existingUser.emailVerified = emailVerified
            existingUser.updatedAt = Instant.now()
            userRepository.save(existingUser)
        } else {
            // Create new user
            val newUser = User(
                email = email,
                firstName = firstName,
                lastName = lastName,
                photoUrl = photoUrl,
                emailVerified = emailVerified,
                externalAuthId = clerkUserId
            )
            userRepository.save(newUser)
        }
    }

    @Transactional(readOnly = true)
    fun findByClerkId(clerkUserId: String): User? {
        return userRepository.findByExternalAuthId(clerkUserId)
    }

    @Transactional(readOnly = true)
    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    @Transactional
    fun deleteUserByClerkId(clerkUserId: String) {
        val user = userRepository.findByExternalAuthId(clerkUserId)
        if (user != null) {
            user.deletedAt = Instant.now()
            user.accountEnabled = false
            userRepository.save(user)
        }
    }

    @Transactional(readOnly = true)
    fun getUserById(userId: UUID): User? {
        return userRepository.findById(userId).orElse(null)
    }
}
