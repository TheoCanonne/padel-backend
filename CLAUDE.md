# CLAUDE.md — Backend Padel (Spring Boot Kotlin)

## 🎯 Objectif
API REST en Spring Boot (Kotlin) gérant le domaine métier : utilisateurs, profils, parties, participations, invitations. Les briques non-différenciantes (auth, chat, push, email) sont déléguées à des SaaS.

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

## 📦 Use Cases à implémenter

### 🔐 Auth & User Management
- [ ] **UC-AUTH-01** : Vérifier JWT (middleware Spring Security)
- [ ] **UC-AUTH-02** : Webhook Clerk (sync user: created/updated/deleted)
- [ ] **UC-USER-01** : Récupérer mon profil (GET /api/v1/users/me)
- [ ] **UC-USER-02** : Supprimer mon compte - RGPD (DELETE /api/v1/users/me)

### 👤 Player Profile
- [ ] **UC-PROFILE-01** : Créer/Compléter mon profil
- [ ] **UC-PROFILE-02** : Modifier mon profil
- [ ] **UC-PROFILE-03** : Voir un profil joueur (avec visibilité)

### 🏟️ Club (club/lieux)
- [ ] **UC-VENUE-01** : Rechercher des lieux (DB + Google Places API)
- [ ] **UC-VENUE-02** : Récupérer un lieu
- [ ] **UC-VENUE-03** : Ajouter un lieu manquant
- [ ] **UC-VENUE-04** : Lister mes lieux favoris
- [ ] **UC-VENUE-05** : Ajouter/Retirer un favori

### 🎾 Game (Partie/Créneau)
- [ ] **UC-GAME-01** : Créer une partie (+ channel Stream Chat auto)
- [ ] **UC-GAME-02** : Rechercher des parties (PostGIS distance + filtres niveau/date)
- [ ] **UC-GAME-03** : Récupérer une partie (détails + participants)
- [ ] **UC-GAME-04** : Modifier une partie (organizer only)
- [ ] **UC-GAME-05** : Annuler une partie (notif participants)
- [ ] **UC-GAME-06** : Lister mes parties (créées + participées)

### 🙋 Participation & Candidature
- [ ] **UC-PART-01** : Candidater à une partie (éligibilité + waitlist si complet)
- [ ] **UC-PART-02** : Retirer ma candidature / me désister (promo waitlist)
- [ ] **UC-PART-03** : Accepter/Refuser une candidature (organizer)
- [ ] **UC-PART-04** : Lister les participants d'une partie
- [ ] **UC-PART-05** : Marquer un no-show (impact réputation)

### 💌 Invitation
- [ ] **UC-INVIT-01** : Inviter un joueur (userId ou email)
- [ ] **UC-INVIT-02** : Générer un lien d'invitation privé (token)
- [ ] **UC-INVIT-03** : Accepter une invitation (via token)
- [ ] **UC-INVIT-04** : Refuser une invitation
- [ ] **UC-INVIT-05** : Lister mes invitations

### 💬 Messagerie (Stream Chat)
- [ ] **UC-CHAT-01** : Récupérer le token Stream Chat (JWT signé)
- [ ] **UC-CHAT-02** : Créer un channel pour une partie (interne, appelé par UC-GAME-01)
- [ ] **UC-CHAT-03** : Ajouter un membre au channel (interne, participation acceptée)
- [ ] **UC-CHAT-04** : Modération - signaler un message

### 🔔 Notifications
- [ ] **UC-NOTIF-01** : Envoyer une notification push (service interne OneSignal)
- [ ] **UC-NOTIF-02** : Enregistrer un device token
- [ ] **UC-NOTIF-03** : Préférences de notifications (GET/PATCH)
- [ ] **UC-NOTIF-04** : Centre de notifications in-app (liste + read status)

### 📅 Calendrier & Rappels
- [ ] **UC-CAL-01** : Mon agenda (parties à venir)
- [ ] **UC-CAL-02** : Export iCal
- [ ] **UC-CAL-03** : Envoyer rappels automatiques (CRON J-1, H-2)
- [ ] **UC-CAL-04** : Confirmation de présence (check-in)

### ⭐ Réputation & Évaluations
- [ ] **UC-REP-01** : Évaluer un joueur après une partie
- [ ] **UC-REP-02** : Voir les évaluations d'un joueur
- [ ] **UC-REP-03** : Recalculer le score de réputation (interne, auto après éval)

### 🚨 Signalement & Modération
- [ ] **UC-MOD-01** : Signaler un joueur
- [ ] **UC-MOD-02** : Signaler une partie
- [ ] **UC-MOD-03** : Bloquer un joueur
- [ ] **UC-MOD-04** : Lister les signalements (admin)
- [ ] **UC-MOD-05** : Résoudre un signalement (admin: warn/suspend/dismiss)

### 📊 Admin / Dashboard
- [ ] **UC-ADMIN-01** : Stats globales (users, games, fill rate, no-show rate)
- [ ] **UC-ADMIN-02** : Gérer les lieux (validation, fusion doublons)
- [ ] **UC-ADMIN-03** : Logs d'activité

---

## 🚀 Roadmap de développement (phases)

### Phase 1 : Fondations
- Setup projet (Gradle, Spring Boot, Supabase, Flyway)
- Config Spring Security + JWT (Clerk)
- Entités JPA : User, PlayerProfile, Venue, Game, Participation
- UC-AUTH-01, UC-AUTH-02, UC-USER-01, UC-PROFILE-01, UC-PROFILE-02

### Phase 2 : Lieux & Parties
- UC-VENUE (recherche, ajout, favoris)
- Intégration Google Places API
- UC-GAME (CRUD, recherche géo PostGIS)

### Phase 3 : Participations & Invitations
- UC-PART (candidature, accept/decline, waitlist, no-show)
- UC-INVIT (token, email, deep links)
- Intégration Resend (emails)

### Phase 4 : Messagerie & Notifications
- Intégration Stream Chat (SDK serveur, channels)
- Intégration OneSignal (push)
- UC-NOTIF (préférences, centre notifs)

### Phase 5 : Calendrier & Réputation
- UC-CAL (agenda, iCal, CRON rappels)
- UC-REP (évaluations, scoring)

### Phase 6 : Modération & Admin
- UC-MOD (signalements, blocage)
- UC-ADMIN (stats, gestion lieux)

### Phase 7 : Finalisation MVP
- Tests end-to-end
- Sentry (error tracking)
- CI/CD (GitHub Actions → Docker → Cloud Run/Railway)
- RGPD (UC-USER-02)

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
