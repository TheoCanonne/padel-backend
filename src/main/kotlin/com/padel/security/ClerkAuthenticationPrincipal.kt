package com.padel.security

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

/**
 * Extracts Clerk user ID from the authenticated JWT token
 */
object ClerkAuthenticationPrincipal {

    fun getClerkUserId(authentication: Authentication): String {
        if (authentication is JwtAuthenticationToken) {
            return authentication.token.subject
        }
        throw IllegalStateException("Authentication is not a JWT token")
    }

    fun getClerkUserIdOrNull(authentication: Authentication?): String? {
        return try {
            authentication?.let { getClerkUserId(it) }
        } catch (e: Exception) {
            null
        }
    }

    fun getJwt(authentication: Authentication): Jwt {
        if (authentication is JwtAuthenticationToken) {
            return authentication.token
        }
        throw IllegalStateException("Authentication is not a JWT token")
    }

    fun getEmail(authentication: Authentication): String? {
        val jwt = getJwt(authentication)
        return jwt.claims["email"] as? String
    }

    fun getEmailVerified(authentication: Authentication): Boolean {
        val jwt = getJwt(authentication)
        return jwt.claims["email_verified"] as? Boolean ?: false
    }
}
