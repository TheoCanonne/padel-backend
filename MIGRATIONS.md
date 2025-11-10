# Guide des Migrations Flyway

## 📋 Vue d'ensemble

Ce projet utilise Flyway pour gérer les migrations de base de données. Les migrations sont versionnées et appliquées automatiquement au démarrage de l'application.

## 📁 Structure

Les migrations sont situées dans : `src/main/resources/db/migration/`

### Migrations existantes

1. **V1__initial_schema.sql** - Schéma initial
   - Extension PostGIS
   - Table `users` (avec support Clerk via `external_auth_id`)
   - Table `sports`
   - Table `player_profiles`
   - Table `player_sport_levels`
   - Table `venues`
   - Table `venue_sports`
   - Table `games`
   - Table `game_participations`
   - Table `game_reviews`

2. **V2__seed_sports.sql** - Données initiales
   - Padel
   - Tennis
   - Futsal

## 🔧 Configuration

La configuration Flyway est dans `application.yml` :

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
```

## 🚀 Utilisation

### Avec Supabase

Ce projet utilise Supabase comme base de données. Consultez `SUPABASE_SETUP.md` pour la configuration complète.

Les migrations s'exécutent automatiquement au démarrage de l'application :

```bash
# Assurez-vous que .env est configuré avec votre URL Supabase
./gradlew bootRun
```

### Vérifier l'état des migrations

```bash
# Via Gradle
./gradlew flywayInfo

# Voir l'historique en base (via Supabase SQL Editor ou psql)
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

### Créer une nouvelle migration

Les migrations doivent suivre la convention de nommage : `V{version}__{description}.sql`

Exemple : `V3__add_notifications_table.sql`

```bash
# Créer un nouveau fichier
touch src/main/resources/db/migration/V3__add_notifications_table.sql
```

```sql
-- V3__add_notifications_table.sql
CREATE TABLE notifications (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(read_at);
```

## 🔄 Commandes utiles

### Via Gradle

```bash
# Afficher l'état des migrations
./gradlew flywayInfo

# Appliquer les migrations
./gradlew flywayMigrate

# Nettoyer la base (ATTENTION: supprime tout !)
./gradlew flywayClean

# Valider les migrations
./gradlew flywayValidate

# Réparer la table de métadonnées
./gradlew flywayRepair
```

### Via Supabase SQL Editor

Dans le dashboard Supabase, utilisez le SQL Editor :

```sql
-- Voir les migrations appliquées
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- Voir les tables créées
SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;

-- Voir la structure d'une table
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'users';
```

## 🐛 Résolution de problèmes

### Erreur : "Validate failed: Detected applied migration not resolved locally"

Cela signifie que la base contient des migrations qui n'existent plus dans le code.

```bash
# Option 1 : Réparer les métadonnées
./gradlew flywayRepair

# Option 2 : Réinitialiser (ATTENTION: perte de données)
./gradlew flywayClean
./gradlew flywayMigrate
```

### Erreur : "Migration checksum mismatch"

Une migration déjà appliquée a été modifiée.

```bash
# NE JAMAIS modifier une migration déjà appliquée !
# Si nécessaire, créer une nouvelle migration pour corriger

# En développement seulement : réinitialiser
./gradlew flywayClean
./gradlew flywayMigrate
```

### Réinitialiser complètement la base (développement)

**Avec Supabase** : utilisez le SQL Editor pour supprimer toutes les tables

```sql
-- ATTENTION : cela supprime TOUTES vos données !
DROP SCHEMA public CASCADE;
CREATE SCHEMA public;
GRANT ALL ON SCHEMA public TO postgres;
GRANT ALL ON SCHEMA public TO public;
```

Puis redémarrez l'application pour réappliquer les migrations :
```bash
./gradlew bootRun
```

## 📝 Bonnes pratiques

### 1. Ne jamais modifier une migration déjà appliquée

❌ **Mauvais** :
```sql
-- Modifier V1__initial_schema.sql après l'avoir appliqué
ALTER TABLE users ADD COLUMN new_field VARCHAR(255);
```

✅ **Bon** :
```sql
-- Créer V3__add_user_new_field.sql
ALTER TABLE users ADD COLUMN new_field VARCHAR(255);
```

### 2. Nommer clairement les migrations

❌ **Mauvais** :
- `V3__update.sql`
- `V4__fix.sql`

✅ **Bon** :
- `V3__add_notifications_table.sql`
- `V4__add_user_preferences_column.sql`

### 3. Tester les migrations

```bash
# Toujours tester sur une base vide
docker-compose down -v
docker-compose up -d
./gradlew bootRun
```

### 4. Migrations réversibles (rollback)

Flyway ne supporte pas nativement le rollback. Pour annuler une migration :

```sql
-- V3__add_notifications_table.sql (migration)
CREATE TABLE notifications (...);

-- V4__rollback_notifications_table.sql (si besoin d'annuler)
DROP TABLE IF EXISTS notifications;
```

### 5. Données de test vs production

Pour les données de test, utilisez des noms différents :

- `V2__seed_sports.sql` - Données de référence (prod)
- `V99__test_data.sql` - Données de test (dev uniquement)

Ou gérez les données de test séparément :
```bash
# Script séparé pour les tests
src/test/resources/data.sql
```

## 🔒 En production

### Vérifications avant déploiement

1. ✅ Tester les migrations sur une copie de la prod
2. ✅ Sauvegarder la base avant déploiement
3. ✅ Vérifier que les migrations sont idempotentes
4. ✅ Prévoir un plan de rollback

### Stratégie de déploiement

```bash
# 1. Backup
pg_dump -U padel padel > backup_$(date +%Y%m%d_%H%M%S).sql

# 2. Appliquer les migrations
./gradlew flywayMigrate

# 3. Vérifier
./gradlew flywayInfo

# 4. Démarrer l'application
./gradlew bootRun
```

## 📚 Ressources

- [Documentation Flyway](https://flywaydb.org/documentation/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [PostGIS Documentation](https://postgis.net/documentation/)
