#!/bin/bash

# Script pour récupérer un token JWT depuis Clerk
# Usage: ./clerk-auth.sh -u <email> -p <password>
# Note: Ce script utilise l'API Backend Clerk qui nécessite CLERK_SECRET_KEY

set -e

# Couleurs pour l'affichage
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Valeurs par défaut
EMAIL=""
PASSWORD=""
USER_ID=""
CLERK_SECRET_KEY="${CLERK_SECRET_KEY:-}"

# Fonction d'aide
show_help() {
    cat << EOF
Usage: ${0##*/} -u <email> [-i <user_id>]

Options:
    -u, --email EMAIL       Email de l'utilisateur
    -i, --user-id ID        User ID Clerk (optionnel, sinon recherche par email)
    -h, --help             Afficher ce message d'aide

Variables d'environnement:
    CLERK_SECRET_KEY       Clé secrète Clerk (obligatoire)

Exemple:
    export CLERK_SECRET_KEY="sk_test_..."
    ${0##*/} -u user@example.com
    ${0##*/} -i user_2abc123xyz
EOF
}

# Parser les arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--email)
            EMAIL="$2"
            shift 2
            ;;
        -i|--user-id)
            USER_ID="$2"
            shift 2
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}Erreur: Option inconnue: $1${NC}" >&2
            show_help
            exit 1
            ;;
    esac
done

# Vérifications
if [[ -z "$CLERK_SECRET_KEY" ]]; then
    echo -e "${RED}Erreur: CLERK_SECRET_KEY n'est pas définie${NC}" >&2
    echo "Définissez-la avec: export CLERK_SECRET_KEY='sk_test_...'" >&2
    echo ""
    echo "Vous pouvez trouver votre clé secrète dans le dashboard Clerk:"
    echo "  https://dashboard.clerk.com -> API Keys -> Secret keys"
    exit 1
fi

if [[ -z "$EMAIL" ]] && [[ -z "$USER_ID" ]]; then
    echo -e "${RED}Erreur: Email (-u) ou User ID (-i) requis${NC}" >&2
    show_help
    exit 1
fi

# URL de l'API Backend Clerk
CLERK_API_URL="https://api.clerk.com/v1"

echo -e "${YELLOW}🔐 Génération d'un token JWT via Clerk Backend API...${NC}"

# Étape 1: Trouver l'utilisateur par email si USER_ID n'est pas fourni
if [[ -z "$USER_ID" ]]; then
    echo -e "${YELLOW}🔍 Recherche de l'utilisateur par email: ${EMAIL}${NC}"

    USER_RESPONSE=$(curl -s -X GET "${CLERK_API_URL}/users?email_address=${EMAIL}" \
        -H "Authorization: Bearer ${CLERK_SECRET_KEY}" \
        -H "Content-Type: application/json")

    # Extraire le user ID
    USER_ID=$(echo "$USER_RESPONSE" | grep -o '"id":"user_[^"]*"' | head -1 | cut -d'"' -f4)

    if [[ -z "$USER_ID" ]]; then
        echo -e "${RED}❌ Utilisateur introuvable avec l'email: ${EMAIL}${NC}" >&2
        echo "Réponse: $USER_RESPONSE" >&2
        exit 1
    fi

    echo -e "${GREEN}✓${NC} Utilisateur trouvé: $USER_ID"
else
    echo "User ID: $USER_ID"
fi

echo ""

# Étape 2: Créer une session pour cet utilisateur
echo -e "${YELLOW}🔐 Création d'une session...${NC}"
SESSION_RESPONSE=$(curl -s -X POST "${CLERK_API_URL}/sessions" \
    -H "Authorization: Bearer ${CLERK_SECRET_KEY}" \
    -H "Content-Type: application/json" \
    -d "{\"user_id\": \"${USER_ID}\"}")

SESSION_ID=$(echo "$SESSION_RESPONSE" | grep -o '"id":"sess_[^"]*"' | head -1 | cut -d'"' -f4)

if [[ -z "$SESSION_ID" ]]; then
    echo -e "${RED}❌ Impossible de créer la session${NC}" >&2
    echo "Réponse: $SESSION_RESPONSE" >&2
    exit 1
fi

echo -e "${GREEN}✓${NC} Session créée: $SESSION_ID"
echo ""

# Étape 3: Récupérer le JWT depuis la session
echo -e "${YELLOW}🎫 Récupération du JWT...${NC}"
TOKEN_RESPONSE=$(curl -s -X POST "${CLERK_API_URL}/sessions/${SESSION_ID}/tokens" \
    -H "Authorization: Bearer ${CLERK_SECRET_KEY}" \
    -H "Content-Type: application/json")

JWT_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"jwt":"[^"]*"' | head -1 | cut -d'"' -f4)

if [[ -z "$JWT_TOKEN" ]]; then
    echo -e "${RED}❌ Impossible de récupérer le JWT${NC}" >&2
    echo "Réponse: $TOKEN_RESPONSE" >&2
    exit 1
fi

echo -e "${GREEN}✓${NC} JWT récupéré"

echo ""
echo -e "${GREEN}✅ Token JWT récupéré avec succès!${NC}"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "JWT ACCESS TOKEN:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "$JWT_TOKEN"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${YELLOW}💡 Utilisation:${NC}"
echo ""
echo "  export AUTH_TOKEN='$JWT_TOKEN'"
echo "  curl -H 'Authorization: Bearer \$AUTH_TOKEN' http://localhost:8080/api/..."
echo ""

# Sauvegarder dans un fichier temporaire
if [[ -n "$EMAIL" ]]; then
    TOKEN_FILE="/tmp/clerk_token_${EMAIL//[@.]/_}.txt"
else
    TOKEN_FILE="/tmp/clerk_token_${USER_ID}.txt"
fi
echo "$JWT_TOKEN" > "$TOKEN_FILE"
echo -e "${GREEN}💾 Token sauvegardé dans: ${TOKEN_FILE}${NC}"
