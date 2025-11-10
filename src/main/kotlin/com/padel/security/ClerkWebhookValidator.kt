package com.padel.security

import com.padel.config.ClerkProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class ClerkWebhookValidator(
    private val properties: ClerkProperties
) {
    private val logger = LoggerFactory.getLogger(ClerkWebhookValidator::class.java)

    fun validateSignature(
        payload: String,
        signature: String?,
        timestamp: String?,
        id: String?
    ): Boolean {
        if (signature == null || timestamp == null || id == null) {
            logger.warn("Missing required headers")
            return false
        }

        try {
            // 1. Vérifier la tolérance du timestamp (5 minutes)
            val currentTime = System.currentTimeMillis() / 1000
            val webhookTimestamp = timestamp.toLong()
            if (Math.abs(currentTime - webhookTimestamp) > 300) {
                logger.warn("Webhook timestamp too old or in future. Current: $currentTime, Webhook: $webhookTimestamp")
                return false
            }

            // 2. Construire le message signé: "id.timestamp.payload"
            val signedContent = "$id.$timestamp.$payload"

            // 3. Retirer le préfixe "whsec_" et décoder le secret en Base64
            // Selon la doc Svix: le secret est encodé en Base64
            val secretBase64 = properties.webhookSecret.removePrefix("whsec_")
            val secretBytes = Base64.getDecoder().decode(secretBase64)

            // 4. Calculer la signature HMAC SHA256
            val mac = Mac.getInstance("HmacSHA256")
            val secretKeySpec = SecretKeySpec(secretBytes, "HmacSHA256")
            mac.init(secretKeySpec)
            val hash = mac.doFinal(signedContent.toByteArray(Charsets.UTF_8))
            val expectedSignature = Base64.getEncoder().encodeToString(hash)

            // 5. Extraire les signatures de l'en-tête (format: "v1,sig1 v1,sig2")
            val signatures = signature.split(" ").mapNotNull {
                if (it.startsWith("v1,")) it.substringAfter("v1,") else null
            }

            // 6. Comparer de manière sécurisée (éviter timing attacks)
            val isValid = signatures.any { sig ->
                MessageDigest.isEqual(
                    sig.toByteArray(Charsets.UTF_8),
                    expectedSignature.toByteArray(Charsets.UTF_8)
                )
            }

            if (!isValid) {
                logger.warn("Signature validation failed")
            }

            return isValid

        } catch (e: Exception) {
            logger.error("Error validating webhook signature", e)
            return false
        }
    }
}