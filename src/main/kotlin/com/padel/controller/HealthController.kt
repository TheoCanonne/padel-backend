package com.padel.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Endpoint de santé de l'application")
class HealthController {

    @GetMapping
    @Operation(
        summary = "Health check",
        description = "Vérifie que l'API est opérationnelle et retourne le timestamp actuel"
    )
    fun health(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "UP",
                "timestamp" to Instant.now().toString()
            )
        )
    }
}
