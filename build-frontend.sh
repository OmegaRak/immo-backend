#!/usr/bin/env bash
# Compile le frontend React (Vite) et copie le resultat dans les ressources
# servies par le backend Java (src/main/resources/web).
#
# Usage : ./build-frontend.sh
set -e

cd "$(dirname "$0")/frontend"

echo ">> Installation des dependances npm..."
npm install

echo ">> Compilation (npm run build)..."
npm run build

DEST="../src/main/resources/web"

echo ">> Nettoyage de l'ancien build dans $DEST ..."
rm -rf "$DEST/assets" "$DEST/client.html" "$DEST/agence.html"

echo ">> Copie du nouveau build..."
cp -r dist/* "$DEST/"

echo ">> Termine. Recompile maintenant le backend avec :"
echo "   cd .. && mvn clean package"
