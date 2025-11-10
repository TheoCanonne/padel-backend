package com.padel.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"

        return OpenAPI()
            .info(
                Info()
                    .title("Padel API")
                    .version("1.0.0")
                    .description(
                        """
                        API pour l'application Padel - Plateforme de mise en relation de joueurs.

                        ## Authentification
                        L'API utilise Clerk pour l'authentification JWT.
                        Pour accéder aux endpoints protégés, ajoutez votre token JWT dans le header Authorization:
                        ```
                        Authorization: Bearer YOUR_JWT_TOKEN
                        ```

                        ## Endpoints
                        - **/api/v1/health** - Health check (public)
                        - **/api/v1/auth** - Authentification et gestion utilisateur
                        - **/api/v1/webhooks** - Webhooks pour intégrations externes (public)
                        - Tous les autres endpoints nécessitent une authentification
                        """.trimIndent()
                    )
                    .contact(
                        Contact()
                            .name("Padel Team")
                            .email("contact@padel.app")
                    )
                    .license(
                        License()
                            .name("Propriétaire")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:8080")
                        .description("Développement local"),
                    Server()
                        .url("https://api.padel.app")
                        .description("Production")
                )
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Token JWT fourni par Clerk")
                    )
            )
    }
}
