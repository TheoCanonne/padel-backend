# CLAUDE.md — Backend Padel (Spring Boot Kotlin)

## 🎯 Objectif

API REST en Spring Boot (Kotlin) gérant le domaine métier : utilisateurs, profils, parties, participations, invitations.
Les briques non-différenciantes (auth, chat, push, email) sont déléguées à des SaaS.

---

## 🛠️ Stack technique

- **Framework** : Spring Boot 3.x + Kotlin
- **DB** : Supabase (PostgreSQL + PostGIS)
- **Auth externe** : Clerk (JWT)
- **Chat** : Stream Chat Server SDK
- **Push** : OneSignal REST API
- **Email** : Resend SDK
- **Géo** : Google Places API
- **Storage** : S3-compatible (Scaleway)
- **Tests** : JUnit 5 + Testcontainers + MockK

---

## 🚀 Roadmap de développement (phases)

### Phase 1 : Fondations

- Setup projet (Gradle, Spring Boot, Supabase, Flyway)
- Config Spring Security + JWT (Clerk)
- Entités JPA : User, PlayerProfile, Venue, Game, Participation
- UC-AUTH-01, UC-AUTH-02, UC-USER-01, UC-PROFILE-01, UC-PROFILE-02

### Phase 3 — Lieux (fait)

- API recherche lieux (autocomplete + carte). Sélection d’un lieu.
- Fiche lieu simple. Favoris (stockage local, API plus tard).

### Phase 4 — Parties (créneau)

- Liste de parties avec filtres basiques (sport, date, distance).
- Détail partie: infos, participants, candidats.
- Création partie: form avec capacité, règles, visibilité.

### Phase 5 — Participation & Invitations

- Candidater / se désister. Validation auto (MVP) ou manuelle.
- Lien d’invitation (deep link), recherche joueur existant.

### Phase 6 — Chat par partie

- Intégration Stream Chat sur `partie/[id]/chat`.
- Mentions & uploads légers (photos, plan d’accès).

### Phase 7 — Notifications & Agenda

- OneSignal: réception basique + deep links.
- Rappels J-1 / H-2 (push). Export iCal simple.

---

## Fonctionnalité complètes (front & back)

## 📦 Fonctionnalités cœur (par entité)

### Utilisateur / Compte

- Créer un compte, se connecter, se déconnecter
- Vérification d’email
- Mot de passe oublié / réinitialisation
- Suppression du compte (RGPD)

### Profil Joueur

- Informations générales (nom, photo, bio courte)
- Sports pratiqués (Padel, puis Tennis, Futsal, …)
- Niveau par sport (échelle, description)
- Spécifiques padel : position (gauche/droite/indifférent), main (droitier/gaucher)
- Zone géographique & rayon (km)
- Disponibilités préférées (jours/heures)
- Visibilité du profil (public restreint / participants uniquement)

### Lieu / Club

- Recherche d’un lieu/club (nom, ville)
- Sélection d’un lieu existant
- Référencement d’un nouveau lieu absent
- Fiche lieu : adresse, infos pratiques, sports supportés
- Favoris (suivre un lieu)

### Partie / Créneau

- Créer une partie : sport, lieu, date, heure début/fin
- Définir la capacité : solo, binôme, équipes
- Règles d’éligibilité : niveau min/max, rayon km
- Visibilité : public / privé (sur invitation)
- Gestion de l’état : ouvert, complet, annulé, joué, reporté
- Liste d’attente (promotion automatique quand une place se libère)
- Historique des parties créées/jouées

### Candidature / Participation

- Candidater à une partie
- Retirer sa candidature / se désister
- Validation par l’organisateur (auto ou manuelle)
- Voir la liste des participants et des candidats
- Rappels avant la partie

### Invitation

- Inviter via lien privé
- Inviter un joueur existant (recherche par nom/email)
- Inviter par email (création de compte à la volée)
- Gestion des invitations (envoyées, acceptées, expirées)

### Messagerie (par Partie)

- Fil de discussion par partie
- Mentions de joueurs (@) et notifications associées
- Pièces jointes légères (photo, plan d’accès)
- Modération basique (signaler un message)

### Recherche / Filtres / Matching

- Rechercher des parties par : sport, date/heure, distance, niveau, capacité
- Trier par pertinence (proximité, compatibilité de niveau, heure)
- Sauvegarder des recherches
- Recevoir des alertes quand un nouveau créneau correspond

### Notifications

- Centre de notifications (in-app)
- Notifications push / email : invitations, candidatures, acceptations, rappels, messages
- Préférences (activer/désactiver par type)

### Calendrier & Rappels

- Vue “Mon agenda”
- Ajouter une partie à son calendrier (export iCal)
- Rappels automatiques (J-1, H-2)
- Confirmation de présence (check-in)

### Réputation & Sécurité

- Évaluations post-match (ponctualité, fair-play, niveau perçu)
- Gestion des no-shows (impact sur réputation)
- Signaler un joueur / bloquer un joueur
- Historique des évaluations

### Administration / Modération

- Gestion des signalements (messages, profils, parties)
- Suspension/avertissement d’utilisateurs
- Fusion/édition de lieux en doublon
- Tableaux de bord (activité, remplissage, no-shows)

---

## ✅ Definition of Done Backend

### Fonctionnel

- Tous les use cases core implémentés et testés
- Intégrations SaaS fonctionnelles (Clerk, Stream, OneSignal, Resend, Places)
- Webhooks Clerk validés
- CRON rappels testés

### Technique

- Tests unitaires (>70% coverage services)
- Tests intégration (Testcontainers) passants
- Tests API pour happy paths + erreurs
- Logs structurés JSON
- Sentry configuré

### Sécurité

- JWT vérifié sur tous les endpoints privés
- CORS configuré
- Validation input (Bean Validation)
- Rate limiting basique
- Secrets externalisés

### Ops

- Dockerfile multi-stage
- Health checks (Spring Actuator)
- CI/CD pipeline
- Monitoring basique (logs + Sentry)

---

## 🧩 Principes

- **Mobile-first** : Time-to-market > perfection
- **Domaine métier** dans le service Kotlin (création partie, candidater, états, invitations)
- **SaaS** pour le non-différenciant (auth, chat, push, email, carto)
- **RGPD** : export/suppression compte, consentements

---

**Dernière mise à jour** : 2025-01-10
