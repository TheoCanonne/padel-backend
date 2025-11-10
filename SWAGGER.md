# Documentation Swagger / OpenAPI

## 📋 Vue d'ensemble

Swagger UI est maintenant activé sur votre API Padel. Il fournit une documentation interactive de tous vos endpoints.

## 🚀 Accès à Swagger UI

### En développement local

Une fois l'application démarrée :

```bash
./gradlew bootRun
```

Ouvrez votre navigateur à l'une de ces URLs :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **Documentation JSON** : http://localhost:8080/api-docs
- **Documentation YAML** : http://localhost:8080/api-docs.yaml

## 🔐 Tester les endpoints avec authentification

### 1. Obtenir un token JWT depuis Clerk

Vous devez d'abord obtenir un token JWT de Clerk. Plusieurs options :

**Option A : Via votre frontend React Native**
```typescript
import { useAuth } from '@clerk/clerk-expo';

const { getToken } = useAuth();
const token = await getToken();
console.log(token);
```

**Option B : Via Clerk Dashboard (pour tests)**
1. Allez sur le Clerk Dashboard
2. Users → Sélectionnez un utilisateur
3. Copiez le "User ID"
4. Utilisez le "Generate JWT" dans les outils de développement

### 2. Utiliser le token dans Swagger UI

1. Cliquez sur le bouton **"Authorize"** en haut à droite
2. Dans le champ "Value", entrez : `Bearer VOTRE_TOKEN_JWT`
3. Cliquez sur **"Authorize"**
4. Fermez le modal

Maintenant tous les endpoints protégés incluront automatiquement le header Authorization !

### 3. Tester un endpoint

1. Cliquez sur un endpoint (ex: `GET /api/v1/auth/me`)
2. Cliquez sur **"Try it out"**
3. Cliquez sur **"Execute"**
4. Voyez la réponse en dessous

## 📚 Endpoints disponibles

### Health
- `GET /api/v1/health` - Health check (public)

### Authentication
- `GET /api/v1/auth/me` - Obtenir l'utilisateur courant (authentifié)
- `POST /api/v1/auth/sync` - Synchroniser l'utilisateur (authentifié)

### Webhooks
- `POST /api/v1/webhooks/clerk` - Webhook Clerk (public avec signature)

## 🎨 Personnalisation de la documentation

### Ajouter des annotations à vos contrôleurs

```kotlin
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement

@RestController
@RequestMapping("/api/v1/games")
@Tag(name = "Games", description = "Gestion des parties de Padel")
class GameController {

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtenir une partie par ID",
        description = "Récupère les détails d'une partie spécifique",
        security = [SecurityRequirement(name = "Bearer Authentication")]
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Partie trouvée"),
            ApiResponse(responseCode = "404", description = "Partie non trouvée"),
            ApiResponse(responseCode = "401", description = "Non authentifié")
        ]
    )
    fun getGame(
        @Parameter(description = "ID de la partie", required = true)
        @PathVariable id: UUID
    ): ResponseEntity<GameResponse> {
        // ...
    }
}
```

### Annoter les DTOs

```kotlin
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Représentation d'une partie de Padel")
data class GameResponse(
    @Schema(description = "Identifiant unique de la partie", example = "123e4567-e89b-12d3-a456-426614174000")
    val id: String,

    @Schema(description = "Sport de la partie", example = "PADEL")
    val sport: String,

    @Schema(description = "Date et heure de début", example = "2025-11-10T14:00:00Z")
    val startTime: String,

    @Schema(description = "Nombre de participants actuels", example = "2", minimum = "0")
    val currentParticipants: Int
)
```

## 🔧 Configuration

### application.yml

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method      # Trier par méthode HTTP
    tagsSorter: alpha             # Trier les tags alphabétiquement
  show-actuator: false
```

### OpenApiConfig.kt

Le fichier `OpenApiConfig.kt` contient la configuration principale :
- Informations générales de l'API
- Serveurs (local, production)
- Schéma d'authentification JWT
- Description générale

## 🚀 En production

### Sécuriser Swagger en production

**Option 1 : Désactiver complètement**

```yaml
# application-prod.yml
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

**Option 2 : Protéger avec authentification**

Modifiez `SecurityConfig.kt` :

```kotlin
.authorizeHttpRequests { authorize ->
    authorize
        // Retirer swagger-ui des endpoints publics
        .requestMatchers(
            "/api/v1/health",
            "/api/v1/public/**",
            "/api/v1/webhooks/**"
        ).permitAll()
        // Exiger authentification pour Swagger
        .requestMatchers("/swagger-ui/**", "/api-docs/**").authenticated()
        .requestMatchers("/api/v1/**").authenticated()
        .anyRequest().permitAll()
}
```

**Option 3 : IP Whitelist (via firewall/load balancer)**

Restreindre l'accès à Swagger à certaines IPs uniquement (équipe interne).

## 📝 Bonnes pratiques

### 1. Documenter tous les endpoints

- Ajoutez `@Operation` à chaque endpoint
- Décrivez clairement le comportement
- Listez les codes de réponse possibles

### 2. Documenter les paramètres

```kotlin
@GetMapping
fun searchGames(
    @Parameter(description = "Sport à rechercher", example = "PADEL")
    @RequestParam sport: String,

    @Parameter(description = "Numéro de page (commence à 0)", example = "0")
    @RequestParam(defaultValue = "0") page: Int
): ResponseEntity<Page<GameResponse>>
```

### 3. Documenter les DTOs

Ajoutez `@Schema` sur les classes et propriétés pour enrichir la documentation.

### 4. Grouper par tags

Utilisez `@Tag` pour organiser vos endpoints :
- Authentication
- Games
- Venues
- Players
- Reviews

### 5. Exemples de requêtes/réponses

Swagger génère automatiquement des exemples basés sur vos DTOs.

## 🔗 Ressources

- [SpringDoc Documentation](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger Annotations](https://github.com/swagger-api/swagger-core/wiki/Swagger-2.X---Annotations)

## 🐛 Dépannage

### Swagger UI ne s'affiche pas

1. Vérifiez que l'application démarre sans erreur
2. Vérifiez l'URL : `http://localhost:8080/swagger-ui.html`
3. Vérifiez les logs pour des erreurs SpringDoc

### Les endpoints n'apparaissent pas

1. Vérifiez que vos contrôleurs ont `@RestController`
2. Vérifiez les `@RequestMapping`
3. Redémarrez l'application

### "Unauthorized" lors des tests

1. Cliquez sur "Authorize"
2. Entrez votre token JWT avec le préfixe `Bearer `
3. Assurez-vous que le token n'est pas expiré

### Documentation JSON vide

Vérifiez `application.yml` :
```yaml
springdoc:
  api-docs:
    enabled: true
```
