# Backend Immobilier — SOAP (JAX-WS) + Socket TCP brut

Projet Java/Maven qui expose le **même jeu de données immobilières** par deux canaux différents :

| Canal | Technologie | Adresse |
|---|---|---|
| Service SOAP | JAX-WS (RPC/literal) | `http://localhost:8081/ws/immobilier` (WSDL : `?wsdl`) |
| Socket brute | `java.net.ServerSocket` | `localhost:9090` (protocole texte) |
| Pages de test | HTML + JavaScript | `http://localhost:8081/soap.html` et `/socket.html` |

---

## 1. Architecture

```
                    ┌──────────────────────────────────────────┐
  Navigateur        │        JVM unique (mg.immo.Main)         │
  ┌──────────┐      │                                          │
  │soap.html │──POST SOAP──▶ /ws/immobilier  (JAX-WS Endpoint)  │
  └──────────┘      │                    │                     │
                    │                    ▼                     │
  ┌──────────┐      │            BienRepository  (mémoire)     │
  │socket.   │      │                    ▲                     │
  │html      │──POST──▶ /api/socket ──── │                     │
  └──────────┘      │  (pont : ouvre une │ socket TCP cliente) │
                    │        │           │                     │
                    │        └──▶ ServerSocket :9090 ──────────┘
                    └──────────────────────────────────────────┘
```

**Pourquoi un pont HTTP pour la socket ?**
Un navigateur ne peut pas ouvrir de socket TCP brute (sécurité du sandbox JS). Le handler
`SocketBridgeHandler` reçoit la commande en HTTP puis ouvre lui-même une **vraie
`java.net.Socket`** vers le port 9090 : le protocole brut est donc bien utilisé de bout en bout.
Tu peux le vérifier sans navigateur avec `nc localhost 9090`.

---

## 2. Prérequis sur Ubuntu

```bash
sudo apt update
sudo apt install -y openjdk-17-jdk maven git curl netcat-openbsd
```

Vérification :

```bash
java -version     # doit afficher 17 ou plus
mvn -v
git --version
```

Si plusieurs JDK sont installés, sélectionne le 17 :

```bash
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

---

## 3. Structure du projet

```
immo-backend/
├── pom.xml
├── run.sh
├── .gitignore
├── README.md
└── src/main/
    ├── java/mg/immo/
    │   ├── Main.java                     ← point d'entrée (HTTP + SOAP + Socket)
    │   ├── model/Bien.java
    │   ├── model/Statistiques.java
    │   ├── repo/BienRepository.java      ← données partagées (singleton thread-safe)
    │   ├── soap/ImmobilierService.java   ← contrat SOAP (SEI)
    │   ├── soap/ImmobilierServiceImpl.java
    │   ├── socket/SocketServer.java      ← ServerSocket brut
    │   ├── socket/ClientHandler.java     ← protocole texte
    │   ├── http/SocketBridgeHandler.java ← pont HTTP → socket
    │   ├── http/StaticFileHandler.java
    │   ├── http/CorsFilter.java
    │   └── util/Json.java
    └── resources/web/
        ├── index.html
        ├── soap.html                     ← PAGE DE TEST 1
        └── socket.html                   ← PAGE DE TEST 2
```

---

## 4. Compilation et lancement

```bash
cd immo-backend

# Compiler + créer le jar exécutable
mvn clean package

# Lancer
java -jar target/immo-backend.jar
```

Ou plus court :

```bash
./run.sh
```

Alternative en développement (sans packaging) :

```bash
mvn compile exec:java
```

Sortie attendue :

```
[SOCKET] Serveur socket brut demarre sur le port 9090
=====================================================
  BACKEND IMMOBILIER DEMARRE
-----------------------------------------------------
  Page SOAP    : http://localhost:8081/soap.html
  Page SOCKET  : http://localhost:8081/socket.html
  WSDL         : http://localhost:8081/ws/immobilier?wsdl
  Socket brute : localhost:9090  (nc localhost 9090)
=====================================================
```

Arrêt : `Ctrl + C`.

---

## 5. Tests

### 5.1 Page 1 — SOAP (`http://localhost:8081/soap.html`)

- **Lister tous les biens** : opération `listerBiens`
- **Recherche multi-critères** : `rechercherBiens(ville, type, prixMax, surfaceMin)`
- **Ajouter** : `ajouterBien(...)`
- **Supprimer** (bouton X) : `supprimerBien(id)`
- **Statistiques** : `obtenirStatistiques()`

Le panneau « Journal SOAP » affiche **l'enveloppe XML envoyée et la réponse brute** — pratique
pour la soutenance.

Vérifier le WSDL :

```bash
curl http://localhost:8081/ws/immobilier?wsdl
```

Tester en ligne de commande :

```bash
curl -X POST http://localhost:8081/ws/immobilier \
  -H 'Content-Type: text/xml;charset=UTF-8' \
  -H 'SOAPAction: ""' \
  -d '<?xml version="1.0" encoding="UTF-8"?>
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:imm="http://soap.immo.mg/">
  <soapenv:Body>
    <imm:rechercherBiens>
      <ville>Antananarivo</ville>
      <type></type>
      <prixMax>1000000000</prixMax>
      <surfaceMin>0</surfaceMin>
    </imm:rechercherBiens>
  </soapenv:Body>
</soapenv:Envelope>'
```

### 5.2 Page 2 — Socket (`http://localhost:8081/socket.html`)

Boutons de commandes rapides + champ de saisie libre + console TCP.

### 5.3 Socket brute sans navigateur

```bash
nc localhost 9090
```

puis tape :

```
HELP
LIST
GET 2
STATS
SEARCH Antananarivo||1000000000|
ADD Duplex vue lac|APPARTEMENT|Antsirabe|Andranobe|110|4|380000000
DELETE 3
QUIT
```

### 5.4 Protocole du serveur socket

| Commande | Format | Description |
|---|---|---|
| `HELP` | — | Liste les commandes |
| `LIST` | — | Tous les biens |
| `GET` | `GET <id>` | Un bien par id |
| `SEARCH` | `SEARCH ville\|type\|prixMax\|surfaceMin` | Champ vide = critère ignoré |
| `ADD` | `ADD titre\|type\|ville\|quartier\|surface\|nbPieces\|prix` | Création |
| `DELETE` | `DELETE <id>` | Suppression |
| `STATS` | — | Statistiques |
| `QUIT` | — | Ferme la connexion |

Réponse : **une ligne JSON**
`{"status":"OK","data":...}` ou `{"status":"ERREUR","message":"..."}`

---

## 6. Dépannage

| Problème | Solution |
|---|---|
| `Address already in use` | `sudo lsof -i :8081` puis `kill -9 <PID>` (idem pour 9090) |
| `release version 17 not supported` | Installer le JDK 17 ou baisser `<maven.compiler.release>` |
| Pages HTML vides | Ouvrir via `http://localhost:8081/...`, **jamais** en `file://` |
| Erreur `Connexion socket impossible` | Le serveur socket n'a pas démarré : vérifier le log `[SOCKET]` |
| Build lent la 1re fois | Maven télécharge les dépendances dans `~/.m2` |

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
git config --global init.defaultBranch main
```

### 7.2 Initialiser le dépôt local

```bash
cd immo-backend
git init
git add .
git status            # vérifier que target/ n'apparaît PAS
git commit -m "feat: backend immobilier SOAP + socket TCP brut avec pages de test HTML"
```

### 7.3 Créer le dépôt distant

Sur GitHub : **New repository** → nom `immo-backend` → **ne pas** cocher « Add a README »
(le projet en contient déjà un) → **Create repository**.

### 7.4 Lier et pousser

**Option A — HTTPS** (il faut un *Personal Access Token* comme mot de passe) :

```bash
git remote add origin https://github.com/TON-PSEUDO/immo-backend.git
git branch -M main
git push -u origin main
```

Pour éviter de retaper le token :

```bash
git config --global credential.helper store
```

**Option B — SSH** (recommandé) :

```bash
ssh-keygen -t ed25519 -C "ton.email@example.com"     # Entrée x3
cat ~/.ssh/id_ed25519.pub                             # copier la clé
# La coller dans GitHub → Settings → SSH and GPG keys → New SSH key
ssh -T git@github.com                                 # test

git remote add origin git@github.com:TON-PSEUDO/immo-backend.git
git branch -M main
git push -u origin main
```

### 7.5 Commits suivants

```bash
git add .
git commit -m "fix: correction de la recherche par surface"
git push
```

### 7.6 Travailler avec des branches

```bash
git checkout -b feature/authentification
# ... modifications ...
git add . && git commit -m "feat: ajout authentification"
git push -u origin feature/authentification
# puis ouvrir une Pull Request sur GitHub
```

---

## 8. Pistes d'amélioration

- Persistance PostgreSQL/MySQL via JDBC ou JPA à la place du `BienRepository` en mémoire
- Authentification (WS-Security côté SOAP, token côté socket)
- Journalisation avec SLF4J + Logback
- Tests unitaires JUnit 5 (`mvn test`) sur le repository et le parseur de commandes
- Client SOAP Java généré automatiquement avec `wsimport` / `jaxws-maven-plugin`
