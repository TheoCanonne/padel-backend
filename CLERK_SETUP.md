# Configuration de l'authentification Clerk

## 📋 Vue d'ensemble

L'authentification Clerk est maintenant intégrée dans le backend. Clerk gère :
- Création de compte et connexion
- Vérification d'email
- Réinitialisation de mot de passe
- OAuth (Google, Apple, etc.)
- MFA (optionnel)

## 🔧 Configuration Backend

### 1. Créer un compte Clerk

1. Allez sur [clerk.com](https://clerk.com) et créez un compte
2. Créez une nouvelle application
3. Notez votre domaine (ex: `your-app-name.clerk.accounts.dev`)

### 2. Récupérer les clés d'API

Dans le dashboard Clerk, allez dans **Configure → API Keys** :

```bash
# Copiez ces valeurs dans votre .env
CLERK_ISSUER_URL=https://your-app-name.clerk.accounts.dev
CLERK_JWKS_URL=https://your-app-name.clerk.accounts.dev/.well-known/jwks.json
```

### 3. Configurer les Webhooks (OBLIGATOIRE)

⚠️ **IMPORTANT** : Les webhooks sont la **seule** méthode de synchronisation des utilisateurs. Ils sont obligatoires pour que votre application fonctionne correctement.

**Pourquoi c'est important ?**
- Les utilisateurs sont créés/mis à jour automatiquement dans votre base de données
- Pas besoin d'appel manuel depuis le frontend
- Garantit la cohérence entre Clerk et votre base

**Configuration :**

1. Dans le dashboard Clerk, allez dans **Webhooks**
2. Cliquez sur **Add Endpoint**
3. URL du webhook : `https://your-api-domain.com/api/v1/webhooks/clerk`
   - En local (pour tests) : Utilisez [ngrok](https://ngrok.com) pour exposer votre localhost
   - En production : Utilisez votre URL réelle
4. Sélectionnez les événements :
   - ✅ `user.created` - Crée l'utilisateur en base
   - ✅ `user.updated` - Met à jour les infos
   - ✅ `user.deleted` - Soft-delete de l'utilisateur
5. Copiez le **Signing Secret** :

```bash
CLERK_WEBHOOK_SECRET=whsec_xxxxxxxxxxxxx
```

**Tester en local avec ngrok :**
```bash
# Installez ngrok
brew install ngrok

# Exposez votre port 8080
ngrok http 8080

# Utilisez l'URL ngrok dans Clerk
# Exemple: https://abc123.ngrok.io/api/v1/webhooks/clerk
```

### 4. Configuration CORS

Ajoutez les origines autorisées dans `.env` :

```bash
CORS_ORIGINS=http://localhost:19006,http://localhost:8081,https://your-app-domain.com
```

## 🚀 Démarrer le backend

```bash
# Installer les dépendances
./gradlew build

# Démarrer la base de données
docker-compose up -d

# Démarrer l'application
./gradlew bootRun
```

## 🧪 Tester l'authentification

### 1. Tester avec curl

Vous aurez besoin d'un token JWT de Clerk. Pour l'obtenir, vous pouvez :
- Utiliser le frontend React Native
- Utiliser les outils de développement Clerk

```bash
# Obtenir l'utilisateur courant
curl -H "Authorization: Bearer YOUR_CLERK_JWT_TOKEN" \
  http://localhost:8080/api/v1/auth/me

# Synchroniser l'utilisateur
curl -X POST -H "Authorization: Bearer YOUR_CLERK_JWT_TOKEN" \
  http://localhost:8080/api/v1/auth/sync
```

### 2. Endpoints disponibles

#### Publics (pas d'authentification requise)
- `GET /api/v1/health` - Health check
- `POST /api/v1/webhooks/clerk` - Webhook Clerk (synchronisation automatique)
- `GET /api/v1/public/**` - Endpoints publics
- `/swagger-ui.html` - Documentation Swagger

#### Protégés (authentification Clerk requise)
- `GET /api/v1/auth/me` - Obtenir l'utilisateur courant
- Tous les autres endpoints `/api/v1/**`

**Note importante** : La synchronisation des utilisateurs se fait **automatiquement via le webhook Clerk**. Il n'y a pas besoin d'appel manuel.

## 📱 Configuration Frontend (React Native)

### 1. Installer Clerk pour React Native

```bash
npm install @clerk/clerk-expo
# ou
yarn add @clerk/clerk-expo
```

### 2. Configuration de base

```typescript
// app/_layout.tsx
import { ClerkProvider } from '@clerk/clerk-expo';

const publishableKey = process.env.EXPO_PUBLIC_CLERK_PUBLISHABLE_KEY!;

export default function RootLayout() {
  return (
    <ClerkProvider publishableKey={publishableKey}>
      {/* Votre app */}
    </ClerkProvider>
  );
}
```

### 3. Exemple d'utilisation

```typescript
import { useAuth, useUser } from '@clerk/clerk-expo';

function MyComponent() {
  const { getToken, isSignedIn } = useAuth();
  const { user } = useUser();

  const fetchMyProfile = async () => {
    if (!isSignedIn) {
      console.log('User not signed in');
      return;
    }

    const token = await getToken();

    // Récupérer votre profil depuis votre API
    const response = await fetch('http://localhost:8080/api/v1/auth/me', {
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    if (response.ok) {
      const data = await response.json();
      console.log('Mon profil:', data);
    } else if (response.status === 404) {
      // L'utilisateur n'existe pas encore en base
      // Il sera créé automatiquement via le webhook Clerk
      console.log('Profil en cours de création...');
    }
  };

  return (
    // Votre UI
  );
}
```

**Note** : Quand un utilisateur se crée sur Clerk, le webhook est appelé automatiquement et crée l'utilisateur en base. Il peut y avoir un léger délai (quelques secondes). Si vous obtenez un 404, réessayez après quelques secondes.

### 4. Configuration des variables d'environnement

Créez un fichier `.env` dans votre projet React Native :

```bash
EXPO_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_xxxxxxxxxxxxx
EXPO_PUBLIC_API_URL=http://localhost:8080
```

## 🔐 Sécurité

### En développement
- Le webhook peut fonctionner sans `CLERK_WEBHOOK_SECRET` (validation désactivée)
- Utilisez `http://localhost` pour tester

### En production
- ✅ Configurez toujours `CLERK_WEBHOOK_SECRET`
- ✅ Utilisez HTTPS pour tous les endpoints
- ✅ Configurez correctement les CORS
- ✅ Limitez les origines autorisées
- ✅ Activez le rate limiting sur les webhooks

## 📊 Structure de la base de données

La table `users` stocke les informations synchronisées depuis Clerk :

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    photo_url TEXT,
    bio TEXT,
    email_verified BOOLEAN DEFAULT FALSE,
    account_enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    external_auth_id VARCHAR(255) UNIQUE  -- Clerk User ID
);
```

## 🐛 Debugging

### Logs utiles

```bash
# Voir les logs de l'application
./gradlew bootRun

# Logs spécifiques à chercher :
# - "User {id} synchronized successfully" (webhook OK)
# - "Invalid webhook signature received" (problème de secret)
# - "No email found for user {id}" (problème de données Clerk)
```

### Problèmes courants

1. **401 Unauthorized**
   - Vérifiez que `CLERK_ISSUER_URL` et `CLERK_JWKS_URL` sont corrects
   - Vérifiez que le token JWT n'est pas expiré

2. **Webhook signature invalide**
   - Vérifiez `CLERK_WEBHOOK_SECRET`
   - Assurez-vous que l'endpoint webhook est accessible publiquement

3. **CORS errors**
   - Vérifiez `CORS_ORIGINS` dans `.env`
   - Assurez-vous que l'origine du frontend est incluse

## 📚 Ressources

- [Documentation Clerk](https://clerk.com/docs)
- [Clerk React Native](https://clerk.com/docs/quickstarts/expo)
- [Webhooks Clerk](https://clerk.com/docs/users/sync-data)
