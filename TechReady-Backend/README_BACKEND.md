# TechReady Backend API

Ce projet est le backend REST Spring Boot pour l'application Android TechReady. Il sert d'interface entre l'application mobile et la base de données Firebase Firestore.

## 🛠️ Prérequis

Avant de lancer le projet, assurez-vous d'avoir :
1. **Java 17** installé sur votre machine.
2. **Maven** installé (ou utilisez l'extension Maven de votre IDE).
3. Le fichier **`serviceAccountKey.json`** généré depuis votre console Firebase et placé dans le dossier `src/main/resources/`.

---

## 🚀 Instructions de Lancement

1. Ouvrez un terminal.
2. Placez-vous à la racine du projet backend :
   ```powershell
   cd d:\YASMINE\TechReady-Backend
   ```
3. Lancez la commande suivante pour compiler et démarrer le serveur :
   ```powershell
   mvn spring-boot:run
   ```
   *L'application démarrera par défaut sur le port **8080**.*

---

## 📚 Documentation des Endpoints

### 1. Questions
**`GET /api/questions`**
- **Description :** Récupère les questions d'entretien filtrées. Si la filière est absente, retourne une erreur `400`. Si moins de 5 questions sont trouvées, relance la recherche sans le critère `niveau` (et ajoute le header `X-Filter-Relaxed: true`).
- **Paramètres :**
  - `filiere` (Requis)
  - `specialite` (Optionnel)
  - `niveau` (Optionnel)
- **Exemple de réponse (200 OK) :**
  ```json
  [
    {
      "id": "docId123",
      "filiere": "Informatique",
      "specialite": "Développement",
      "niveau": "Junior",
      "question": "Qu'est-ce que le polymorphisme ?",
      "options": ["A", "B", "C"],
      "correctIndex": 1,
      "explanation": "Explication détaillée ici.",
      "tags": ["OOP", "Java"],
      "difficulty": "Moyen"
    }
  ]
  ```

### 2. Profils Utilisateurs
**`POST /api/profiles`**
- **Description :** Crée ou met à jour un profil utilisateur.
- **Body JSON Requis :**
  ```json
  {
    "userId": "user123",
    "filiere": "Informatique",
    "specialite": "DevOps",
    "technos": ["Docker", "Kubernetes"],
    "niveau": "Senior",
    "typeContrat": "CDI"
  }
  ```
- **Réponse :** `201 Created` avec l'ID du profil en corps de réponse.

**`GET /api/profiles/{userId}/last`**
- **Description :** Récupère le dernier profil de l'utilisateur.
- **Réponse :** `200 OK` avec le profil (identique au JSON ci-dessus) ou `404 Not Found`.

### 3. Sessions
**`POST /api/sessions`**
- **Description :** Sauvegarde une session terminée. Calcule automatiquement le pourcentage et met à jour le `bestScore` et `totalSessions` du profil de l'utilisateur.
- **Body JSON Requis :**
  ```json
  {
    "userId": "user123",
    "profileId": "profile456",
    "score": 8,
    "total": 10,
    "timeTakenSeconds": 120
  }
  ```
- **Réponse :** `201 Created` avec l'ID de la session généré.

**`GET /api/sessions/{userId}`**
- **Description :** Récupère l'historique complet des sessions de l'utilisateur, trié par date décroissante.
- **Réponse :** `200 OK` avec une liste de sessions ou `404 Not Found`.

---

## 🧪 Commandes de Test (CURL)

Utilisez ces commandes dans un autre terminal (PowerShell, Bash ou Git Bash) pendant que le serveur tourne pour tester l'API en direct :

**Tester la recherche de questions :**
```bash
curl -X GET "http://localhost:8080/api/questions?filiere=Informatique&specialite=DevOps&niveau=Junior"
```

**Créer un profil (Attention aux quotes sous Windows, utilisez PowerShell ou Postman si erreur) :**
```bash
curl -X POST "http://localhost:8080/api/profiles" -H "Content-Type: application/json" -d "{\"userId\":\"user123\", \"filiere\":\"Informatique\", \"specialite\":\"DevOps\", \"technos\":[\"Docker\", \"Kubernetes\"], \"niveau\":\"Senior\", \"typeContrat\":\"CDI\"}"
```

**Récupérer le profil créé :**
```bash
curl -X GET "http://localhost:8080/api/profiles/user123/last"
```

**Créer une session de Quiz :**
```bash
curl -X POST "http://localhost:8080/api/sessions" -H "Content-Type: application/json" -d "{\"userId\":\"user123\", \"profileId\":\"profile456\", \"score\":8, \"total\":10, \"timeTakenSeconds\":120}"
```

**Récupérer l'historique des sessions :**
```bash
curl -X GET "http://localhost:8080/api/sessions/user123"
```
