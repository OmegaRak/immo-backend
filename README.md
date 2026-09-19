# Backend Immobilier — SOAP + Socket TCP brut + Temps réel (SSE) + Frontend React

Projet Java/Maven qui expose un jeu de données immobilières par **trois canaux** différents,
avec deux pages de test technique et deux applications "métier" React en temps réel.

| Page | Rôle | Technologie sous-jacente |
|---|---|---|
| `soap.html` | Page de test 1 | Service SOAP (JAX-WS) |
| `socket.html` | Page de test 2 | Socket TCP brute |
| `client.html` | Espace client | React + TypeScript + Tailwind CSS, temps réel (SSE) |
| `agence.html` | Espace agence | React + TypeScript + Tailwind CSS, temps réel (SSE) |

---

## 1. Architecture générale
                ┌───────────────────────────────────────────────────────┐

Navigateur │ JVM unique (mg.immo.Main) │
┌──────────┐ │ │
│soap.html │──POST SOAP──▶ /ws/immobilier (JAX-WS Endpoint) │
└──────────┘ │ │ │
│ ▼ │
┌──────────┐ │ BienRepository (mémoire, partagé) │
│socket. │ │ ▲ │
│html │──POST──▶ /api/socket ──── │ (ouvre une socket TCP cliente) │
└──────────┘ │ │ │ │
│ └──▶ ServerSocket :9090 ─────────────────────┘ │
│ │
┌──────────┐ │ │
│agence. │──POST──▶ /api/annonces ──▶ BienRepository.ajouter() ─┐ │
│html │──POST──▶ /api/visites/valider ──▶ DemandeVisiteRepo ─┤ │
└──────────┘ │ │ │
│ ▼ │
┌──────────┐ │ EventBus │
│client. │──POST──▶ /api/visites (demande) ──────────────▶ (pub/sub) │
│html │◀──SSE─── /api/events (flux ouvert en continu) ◀────┘ │
└──────────┘ └───────────────────────────────────────────────────────┘


**Le temps réel repose sur les *Server-Sent Events* (SSE)** : chaque page `client.html` et
`agence.html` ouvre une connexion HTTP persistante vers `/api/events` grâce à l'API
JavaScript native `EventSource`. Dès qu'un évènement se produit côté serveur (nouvelle
annonce, nouvelle demande, validation, refus), il est **poussé instantanément** vers toutes
les pages connectées, sans qu'elles aient besoin d'interroger le serveur en boucle.

**Pourquoi un pont HTTP pour la socket brute (`socket.html`) ?**
Un navigateur ne peut pas ouvrir de socket TCP brute (sécurité du bac à sable JS). Le
handler `SocketBridgeHandler` reçoit la commande en HTTP puis ouvre lui-même une **vraie
`java.net.Socket`** vers le port 9090. Vérifiable sans navigateur avec `nc localhost 9090`.

---

## 2. Prérequis sur Ubuntu

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven git curl netcat-openbsd
```

Vérification :

```bash
java -version     # 17 ou plus
mvn -v
git --version
```

---

## 3. Structure du projet

immo-backend/
├── pom.xml
├── run.sh
├── build-frontend.sh ← compile le frontend React et le copie dans le backend
├── .gitignore
├── README.md
├── frontend/ ← CODE SOURCE React + TypeScript + Tailwind CSS
│ ├── package.json
│ ├── vite.config.ts ← build multi-pages (client.html + agence.html)
│ ├── client.html ← point d'entree Vite de la page client
│ ├── agence.html ← point d'entree Vite de la page agence
│ └── src/
│ ├── client/main.tsx ← montage React de l'espace client
│ ├── agence/main.tsx ← montage React de l'espace agence
│ ├── pages/
│ │ ├── PageClient.tsx ← composant principal cote client
│ │ └── PageAgence.tsx ← composant principal cote agence
│ ├── components/ ← CarteBien, ModaleDemandeVisite, Toasts, BadgeStatut...
│ ├── hooks/
│ │ ├── useFluxTempsReel.ts ← encapsule EventSource (SSE) pour React
│ │ └── useClientId.ts ← identifiant anonyme persistant (localStorage)
│ ├── api/ ← fonctions typees qui appellent le backend Java
│ ├── types/index.ts ← types TypeScript alignes sur les modeles Java
│ └── index.css ← styles Tailwind
└── src/main/
├── java/mg/immo/
│ ├── Main.java ← point d'entrée (HTTP + SOAP + Socket + SSE)
│ ├── model/
│ │ ├── Bien.java
│ │ ├── Statistiques.java
│ │ └── DemandeVisite.java ← demande de visite (temps réel)
│ ├── repo/
│ │ ├── BienRepository.java ← données partagées (singleton thread-safe)
│ │ └── DemandeVisiteRepository.java
│ ├── soap/
│ │ ├── ImmobilierService.java ← contrat SOAP (SEI)
│ │ └── ImmobilierServiceImpl.java
│ ├── socket/
│ │ ├── SocketServer.java ← ServerSocket brut
│ │ └── ClientHandler.java ← protocole texte
│ ├── http/
│ │ ├── SocketBridgeHandler.java ← pont HTTP → socket
│ │ ├── StaticFileHandler.java ← sert les pages HTML et les assets React compiles
│ │ ├── CorsFilter.java
│ │ ├── AnnonceApiHandler.java ← API REST annonces
│ │ ├── VisiteApiHandler.java ← API REST demandes de visite
│ │ ├── EventBus.java ← bus pub/sub en mémoire
│ │ ├── SseHandler.java ← flux temps réel (Server-Sent Events)
│ │ └── Forms.java ← parseur de formulaires HTTP
│ └── util/Json.java
└── resources/web/
├── index.html
├── soap.html ← PAGE DE TEST 1 (SOAP)
├── socket.html ← PAGE DE TEST 2 (Socket brute)
├── client.html ← ESPACE CLIENT — build React compile (genere)
├── agence.html ← ESPACE AGENCE — build React compile (genere)
└── assets/ ← JS/CSS generes par Vite (genere)


**Important** : `client.html`, `agence.html` et `assets/` dans `src/main/resources/web/`
sont des fichiers **générés** par la compilation du dossier `frontend/`. Pour les modifier,
on édite toujours le code dans `frontend/src/`, jamais directement ces fichiers compilés.

---

## 4. Compilation et lancement

### 4.1 Backend seul (si le frontend compilé n'a pas changé)

```bash
cd immo-backend
mvn clean package
java -jar target/immo-backend.jar
```

Ou plus court : `./run.sh`

### 4.2 Après une modification du frontend React

Le dossier `frontend/` est le code source ; il doit être recompilé et copié dans
`src/main/resources/web/` avant que le backend ne le serve. Le script `build-frontend.sh`
automatise ces deux étapes :

```bash
# Prerequis (une seule fois) : Node.js et npm
sudo apt install -y nodejs npm

# A chaque modification du frontend :
./build-frontend.sh
mvn clean package
java -jar target/immo-backend.jar
```

`build-frontend.sh` fait : `npm install` → `npm run build` (Vite compile `frontend/src/`
vers `frontend/dist/`) → copie `dist/*` dans `src/main/resources/web/`.

Pour développer avec rechargement instantané (sans recompiler le Jar a chaque fois) :

```bash
cd frontend
npm install
npm run dev
```

Vite démarre alors un serveur de développement sur `http://localhost:5173`, avec un
proxy automatique qui relaie les appels `/api/*` vers le backend Java sur le port 8081
(le backend doit tourner en parallèle). Voir `frontend/vite.config.ts`.

Sortie attendue au lancement du jar :
[SOCKET] Serveur socket brut demarre sur le port 9090
BACKEND IMMOBILIER DEMARRE
Page SOAP : http://localhost:8081/soap.html
Page SOCKET : http://localhost:8081/socket.html
Page CLIENT : http://localhost:8081/client.html
Page AGENCE : http://localhost:8081/agence.html
WSDL : http://localhost:8081/ws/immobilier?wsdl
Socket brute : localhost:9090 (nc localhost 9090)

Arrêt : `Ctrl+C`.

**Un seul serveur à la fois** : si tu vois l'erreur `Adresse déjà utilisée`, un ancien
processus tourne encore. Trouve-le et arrête-le :

```bash
ps aux | grep immo-backend
kill <PID>
```

---

## 5. Tests

### 5.1 Page 1 — SOAP (`http://localhost:8081/soap.html`)

- **Lister tous les biens** : opération `listerBiens`
- **Recherche multi-critères** : `rechercherBiens(ville, type, prixMax, surfaceMin)`
- **Ajouter** : `ajouterBien(...)`
- **Supprimer** (bouton X) : `supprimerBien(id)`
- **Statistiques** : `obtenirStatistiques()`

Le panneau « Journal SOAP » affiche l'enveloppe XML envoyée et la réponse brute.

```bash
curl http://localhost:8081/ws/immobilier?wsdl
```

### 5.2 Page 2 — Socket (`http://localhost:8081/socket.html`)

Boutons de commandes rapides + champ de saisie libre + console TCP.

Sans navigateur :

```bash
nc localhost 9090
LIST
STATS
QUIT
```

Protocole : `HELP`, `LIST`, `GET <id>`, `SEARCH ville|type|prixMax|surfaceMin`,
`ADD titre|type|ville|quartier|surface|nbPieces|prix`, `DELETE <id>`, `STATS`, `QUIT`.

### 5.3 Pages 3 et 4 — Client / Agence en temps réel

Ouvre **deux fenêtres de navigateur côte à côte** :

- `http://localhost:8081/agence.html`
- `http://localhost:8081/client.html`

Le point en haut à droite de chaque page doit passer au **vert** ("En direct") après
connexion au flux SSE.

**Scénario à tester :**

1. **Agence publie une annonce** → remplir le formulaire "Publier une annonce" et valider.
   → Côté client, la nouvelle annonce apparaît **instantanément**, avec une animation et
   une notification, sans recharger la page.

2. **Client demande une visite** → cliquer sur "Demander une visite" sur un bien, remplir
   nom / téléphone / date, envoyer.
   → Côté agence, une notification apparaît immédiatement et la demande s'ajoute à la
   liste "Demandes de visite reçues".

3. **Agence valide (ou refuse)** → cliquer sur "✓ Valider" ou "✗ Refuser" sur une demande.
   → Côté client, une notification confirme la décision et le statut de "Mes demandes de
   visite" se met à jour en direct.

### 5.4 API REST utilisée par ces deux pages

| Méthode | Route | Description |
|---|---|---|
| GET | `/api/annonces` | Liste des biens |
| POST | `/api/annonces` | Création d'une annonce (agence) — diffusée en temps réel |
| GET | `/api/visites` | Toutes les demandes de visite (agence) |
| GET | `/api/visites?clientId=xxx` | Demandes d'un client précis |
| POST | `/api/visites` | Création d'une demande (client) — diffusée en temps réel |
| POST | `/api/visites/valider` | Validation d'une demande (`id` en paramètre) |
| POST | `/api/visites/refuser` | Refus d'une demande (`id` en paramètre) |
| GET | `/api/events` | Flux Server-Sent Events (temps réel) |

Les requêtes POST utilisent le format `application/x-www-form-urlencoded`.

Le flux `/api/events` envoie des trames au format :

event: annonce
data: {"id":7,"titre":"Villa neuve",...}

event: demande
data: {"id":3,"nomClient":"Rakoto Jean",...}

event: validation
data: {"id":3,"statut":"VALIDEE",...}


Chaque page filtre côté JavaScript les évènements qui la concernent (`EventSource.
addEventListener('annonce', ...)`, etc.).

### 5.5 Architecture du frontend React

Le frontend est un projet **Vite** en mode multi-pages : deux applications React
indépendantes (`client` et `agence`), chacune avec son propre point d'entrée HTML, pas de
routeur nécessaire.

| Aspect | Détail |
|---|---|
| Framework | React 19 + TypeScript |
| Style | Tailwind CSS 4 (plugin `@tailwindcss/vite`, pas de fichier `tailwind.config.js`) |
| Bundler | Vite (build multi-pages, voir `vite.config.ts`) |
| Temps réel | Hook `useFluxTempsReel` encapsulant `EventSource` (SSE) |
| État client | `useState`/`useEffect` React natifs, pas de librairie externe |
| Identité visiteur | Hook `useClientId`, identifiant anonyme stocké en `localStorage` |
| Appels API | Fonctions typées dans `src/api/`, alignées sur les modèles Java (`src/types/`) |

Les types TypeScript de `frontend/src/types/index.ts` (`Bien`, `DemandeVisite`,
`ReponseApi<T>`) reflètent exactement la structure JSON produite par
`mg.immo.model.Bien.toJson()` et `mg.immo.model.DemandeVisite.toJson()` côté Java — toute
évolution d'un modèle Java doit être répercutée dans ce fichier.

---

## 6. Dépannage

| Problème | Solution |
|---|---|
| `Address already in use` / `Adresse déjà utilisée` | Un ancien processus tourne : `ps aux \| grep immo-backend` puis `kill <PID>` |
| `release version 17 not supported` | Installer le JDK 17 ou baisser `<maven.compiler.release>` |
| Pages HTML vides | Ouvrir via `http://localhost:8081/...`, jamais en `file://` |
| Point de connexion reste rouge sur client/agence | Vérifier que le serveur tourne et que le port 8081 est bien celui utilisé |
| `Connexion socket impossible` | Le serveur socket n'a pas démarré : vérifier le log `[SOCKET]` |
| Modif du frontend invisible après rechargement | Il faut relancer `./build-frontend.sh` puis `mvn clean package` — le navigateur affiche l'ancien build tant que `src/main/resources/web/` n'est pas régénéré |
| `npm: command not found` | Installer Node.js : `sudo apt install -y nodejs npm` |

Ouvrir les ports si UFW est actif :

```bash
sudo ufw allow 8081/tcp
sudo ufw allow 9090/tcp
```

---

## 7. Publier sur Git (GitHub / GitLab)

### 7.1 Configuration initiale (une seule fois)

```bash
git config --global user.name "Ton Nom"
git config --global user.email "ton.email@example.com"
```

### 7.2 Routine habituelle après une modification

```bash
git add .
git commit -m "description du changement"
git push
```

### 7.3 Première initialisation (si le dépôt n'existe pas encore)

```bash
git init
git add .
git commit -m "feat: backend immobilier SOAP + socket TCP brut + temps reel"
git branch -M main
git remote add origin https://github.com/TON-PSEUDO/immo-backend.git
git push -u origin main
```

Authentification HTTPS : GitHub demande un **Personal Access Token** à la place du mot de
passe (`Settings → Developer settings → Personal access tokens → Generate new token
(classic)`, cocher `repo`).

---

## 8. Pistes d'amélioration

- Persistance PostgreSQL/MySQL via JDBC ou JPA à la place des dépôts en mémoire
- Authentification (comptes agence / clients, WS-Security côté SOAP)
- Historique des évènements SSE (actuellement, un évènement manqué pendant une
  déconnexion n'est pas rejoué)
- Notifications ciblées par identifiant d'agence si plusieurs agences utilisent le système
- Tests unitaires JUnit 5 sur les dépôts et le parseur de commandes socket
- Interface d'administration pour supprimer une annonce ou consulter les statistiques
