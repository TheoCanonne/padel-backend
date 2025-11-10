# Padel Backend - Spring Boot (Kotlin)

Backend service pour l'application Padel, gérant les parties, les joueurs, les lieux et les participations.

## Stack technique

- **Framework**: Spring Boot 3.2.2
- **Langage**: Kotlin 1.9.22
- **Base de données**: PostgreSQL + PostGIS
- **Migration**: Flyway
- **Build**: Gradle (Kotlin DSL)

## Prérequis

- Java 17+
- PostgreSQL 15+ avec extension PostGIS
- Gradle 8.5+ (ou utiliser le wrapper fourni)

## Installation

### 1. Base de données

Créer une base de données PostgreSQL :

```sql
CREATE DATABASE padel;
\c padel
CREATE EXTENSION IF NOT EXISTS postgis;
```

### 2. Configuration

Créer un fichier `src/main/resources/application-local.yml` pour le développement local :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/padel
    username: votre_user
    password: votre_password

external:
  clerk:
    issuer-url: https://your-clerk-domain.clerk.accounts.dev
    jwks-url: https://your-clerk-domain.clerk.accounts.dev/.well-known/jwks.json

  stream-chat:
    api-key: your_stream_api_key
    api-secret: your_stream_api_secret

  onesignal:
    app-id: your_onesignal_app_id
    api-key: your_onesignal_api_key

  resend:
    api-key: your_resend_api_key

  google-maps:
    api-key: your_google_maps_api_key

  scaleway:
    access-key: your_scaleway_access_key
    secret-key: your_scaleway_secret_key
    bucket: padel-uploads
    region: fr-par
    endpoint: https://s3.fr-par.scw.cloud
```

### 3. Build & Run

```bash
# Build
./gradlew build

# Run
./gradlew bootRun

# Run avec profil local
./gradlew bootRun --args='--spring.profiles.active=local'
```

L'application démarre sur `http://localhost:8080`

## Endpoints

### Health check

```bash
curl http://localhost:8080/api/v1/health
```

## Structure du projet

Architecture 3 couches : **Controller → Service/Model → Repository**

```
backend/
├── src/main/kotlin/com/padel/
│   ├── PadelApplication.kt
│   ├── controller/         # Couche présentation - REST Controllers
│   ├── service/            # Couche métier - Business logic
│   │   ├── dto/            # Data Transfer Objects
│   │   └── exception/      # Exception handlers
│   ├── model/              # Couche données - Entités JPA
│   ├── repository/         # Couche données - Repositories Spring Data
│   └── config/             # Configuration Spring
└── src/main/resources/
    ├── application.yml
    └── db/migration/       # Scripts Flyway
```

## Modèle de données

### Entités principales

- **User**: Compte utilisateur
- **PlayerProfile**: Profil joueur avec préférences
- **Sport**: Sports disponibles (Padel, Tennis, Futsal)
- **PlayerSportLevel**: Niveau par sport
- **Venue**: Lieux/clubs
- **Game**: Parties/créneaux
- **GameParticipation**: Participations aux parties
- **GameReview**: Évaluations post-match

## TODO

- [ ] Implémenter l'authentification avec Clerk
- [ ] Ajouter les services métier (GameService, etc.)
- [ ] Implémenter les controllers REST
- [ ] Intégrer Stream Chat pour la messagerie
- [ ] Intégrer OneSignal pour les notifications push
- [ ] Intégrer Resend pour les emails
- [ ] Implémenter la recherche géographique avec PostGIS
- [ ] Ajouter les tests unitaires et d'intégration
- [ ] Configurer le monitoring (Sentry)
- [ ] Dockeriser l'application

## Développement

### Tests

```bash
./gradlew test
```

### Format du code

Le projet utilise les conventions Kotlin standard. Assurez-vous de configurer votre IDE en conséquence.

## Licence

Propriétaire
