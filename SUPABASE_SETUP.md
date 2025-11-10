# Configuration Supabase

## 📋 Vue d'ensemble

Ce projet utilise Supabase comme base de données PostgreSQL managée avec l'extension PostGIS activée. Supabase fournit :
- PostgreSQL 15+ avec PostGIS
- Interface d'administration web
- Backups automatiques
- APIs REST et Realtime (optionnelles)

## 🚀 Configuration initiale

### 1. Créer un projet Supabase

1. Allez sur [supabase.com](https://supabase.com)
2. Créez un compte (gratuit pour commencer)
3. Créez un nouveau projet
4. Notez votre mot de passe (vous ne le reverrez plus !)

### 2. Récupérer les informations de connexion

Dans le dashboard Supabase :
1. Allez dans **Project Settings** → **Database**
2. Trouvez la section **Connection string**
3. Sélectionnez **URI** et copiez l'URL

Format de l'URL :
```
postgresql://postgres:[YOUR-PASSWORD]@db.[PROJECT-REF].supabase.co:5432/postgres
```

### 3. Configurer les variables d'environnement

Créez un fichier `.env` à la racine du backend :

```bash
# Supabase Database
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_supabase_password

# Clerk Authentication
CLERK_ISSUER_URL=https://your-domain.clerk.accounts.dev
CLERK_JWKS_URL=https://your-domain.clerk.accounts.dev/.well-known/jwks.json
CLERK_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx

# Autres services...
```

**Note** : Le format JDBC nécessite le préfixe `jdbc:postgresql://` au lieu de `postgresql://`

### 4. Activer PostGIS

Supabase a PostGIS disponible, mais il faut l'activer :

1. Dans le dashboard Supabase, allez dans **SQL Editor**
2. Créez une nouvelle requête
3. Exécutez :

```sql
CREATE EXTENSION IF NOT EXISTS postgis;
```

Ou laissez Flyway le faire automatiquement (c'est dans `V1__initial_schema.sql`)

## 🔄 Appliquer les migrations Flyway

Les migrations Flyway s'appliquent automatiquement au démarrage de l'application.

### Première fois

```bash
# 1. Assurez-vous que .env est configuré
cat .env

# 2. Démarrez l'application
./gradlew bootRun

# Les migrations V1 et V2 seront appliquées automatiquement
```

### Vérifier les migrations

Dans Supabase SQL Editor :

```sql
-- Voir les migrations appliquées
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Voir les tables créées
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- Vérifier PostGIS
SELECT PostGIS_version();
```

## 🗄️ Structure de la base

Après les migrations, vous aurez :

### Tables principales
- `users` - Utilisateurs (synchronisés avec Clerk)
- `sports` - Sports disponibles (Padel, Tennis, Futsal)
- `player_profiles` - Profils joueurs
- `player_sport_levels` - Niveaux par sport
- `venues` - Lieux/clubs
- `venue_sports` - Sports par lieu
- `games` - Parties/créneaux
- `game_participations` - Participations aux parties
- `game_reviews` - Évaluations post-match

### Extensions
- `postgis` - Fonctions géospatiales

### Métadonnées Flyway
- `flyway_schema_history` - Historique des migrations

## 🔒 Sécurité

### Row Level Security (RLS)

Supabase active RLS par défaut. Pour le développement, vous pouvez le désactiver temporairement :

```sql
-- Désactiver RLS sur toutes les tables (développement uniquement)
ALTER TABLE users DISABLE ROW LEVEL SECURITY;
ALTER TABLE player_profiles DISABLE ROW LEVEL SECURITY;
ALTER TABLE sports DISABLE ROW LEVEL SECURITY;
-- etc...
```

**Important** : En production, configurez des politiques RLS appropriées !

### Connection pooling

Supabase utilise PgBouncer. Pour éviter les problèmes :

1. Utilisez le port **5432** (connection directe) pour les migrations
2. Utilisez le port **6543** (pooler) pour l'application en production

```bash
# Développement (direct)
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres

# Production (pooled)
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:6543/postgres?prepareThreshold=0
```

## 📊 Administration via Supabase

### Table Editor

1. Allez dans **Table Editor** du dashboard
2. Visualisez et éditez les données
3. Créez des relations
4. Gérez les index

### SQL Editor

Requêtes utiles :

```sql
-- Compter les utilisateurs
SELECT COUNT(*) FROM users;

-- Voir les parties ouvertes
SELECT g.*, v.name as venue_name
FROM games g
JOIN venues v ON g.venue_id = v.id
WHERE g.status = 'OPEN';

-- Utilisateurs avec leurs profils
SELECT u.email, u.first_name, u.last_name, p.city, p.radius_km
FROM users u
LEFT JOIN player_profiles p ON u.id = p.user_id;

-- Parties par sport
SELECT s.name, COUNT(g.id) as game_count
FROM sports s
LEFT JOIN games g ON s.id = g.sport_id
GROUP BY s.id, s.name;
```

### Backups

Supabase fait des backups automatiques (plan gratuit : 7 jours de rétention).

Pour un backup manuel :
1. **Database** → **Backups**
2. Cliquez sur **Create backup**

## 🔧 Configuration JPA/Hibernate

Dans `application.yml`, gardez ces paramètres pour Supabase :

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate  # IMPORTANT : ne pas utiliser "update" avec Flyway
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          time_zone: UTC
    show-sql: false  # Mettre à true pour debug
```

## 🐛 Dépannage

### Erreur : "Connection refused"

- Vérifiez que `DATABASE_URL` est correct
- Vérifiez votre mot de passe
- Vérifiez que le projet Supabase est actif

### Erreur : "SSL required"

Supabase nécessite SSL. Ajoutez à l'URL :

```bash
DATABASE_URL=jdbc:postgresql://db.xxx.supabase.co:5432/postgres?sslmode=require
```

### Erreur : "prepared statement already exists"

Avec le pooler (port 6543), ajoutez :

```bash
DATABASE_URL=jdbc:postgresql://db.xxx.supabase.co:6543/postgres?prepareThreshold=0
```

### Migrations ne s'appliquent pas

```bash
# Vérifier l'état
./gradlew flywayInfo

# Forcer la validation
./gradlew flywayValidate

# En dernier recours (ATTENTION : perte de données)
# Dans Supabase SQL Editor :
DROP TABLE IF EXISTS flyway_schema_history CASCADE;
# Puis redémarrer l'app
```

### Performance lente

1. Vérifiez les index dans Table Editor
2. Utilisez EXPLAIN ANALYZE pour les requêtes lentes
3. Activez le pooler (port 6543) en production

## 🌍 Variables d'environnement complètes

```bash
# .env
# Supabase
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:5432/postgres
DB_USERNAME=postgres
DB_PASSWORD=your_password_here

# Clerk
CLERK_ISSUER_URL=https://your-app.clerk.accounts.dev
CLERK_JWKS_URL=https://your-app.clerk.accounts.dev/.well-known/jwks.json
CLERK_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx

# Services externes
STREAM_API_KEY=your_stream_key
STREAM_API_SECRET=your_stream_secret
ONESIGNAL_APP_ID=your_onesignal_id
ONESIGNAL_API_KEY=your_onesignal_key
RESEND_API_KEY=your_resend_key
GOOGLE_MAPS_API_KEY=your_google_key

# Scaleway S3
SCALEWAY_ACCESS_KEY=your_access_key
SCALEWAY_SECRET_KEY=your_secret_key
SCALEWAY_BUCKET=padel-uploads
SCALEWAY_REGION=fr-par
SCALEWAY_ENDPOINT=https://s3.fr-par.scw.cloud

# CORS
CORS_ORIGINS=http://localhost:19006,http://localhost:8081,https://your-app.com
```

## 📚 Ressources

- [Supabase Documentation](https://supabase.com/docs)
- [Supabase + Spring Boot](https://supabase.com/docs/guides/getting-started/tutorials)
- [PostGIS Documentation](https://postgis.net/documentation/)
- [Flyway + Supabase](https://flywaydb.org/documentation/database/postgresql)

## 🚀 Déploiement

### Développement local → Supabase

Votre setup est déjà prêt ! Supabase est accessible depuis votre machine locale.

### Production

1. Utilisez le pooler (port 6543)
2. Activez SSL mode require
3. Configurez les RLS policies
4. Configurez les backups
5. Utilisez des secrets pour les credentials

```bash
# URL de production
DATABASE_URL=jdbc:postgresql://db.xxxxxxxxxxxxx.supabase.co:6543/postgres?sslmode=require&prepareThreshold=0
```
