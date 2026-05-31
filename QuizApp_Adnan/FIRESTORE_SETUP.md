# Configuration Firestore - TechReady (ex QuizApp_Adnan)

Ce document décrit l'architecture de la base de données Firestore mise en place pour le projet.

## 1. Règles de Sécurité Firestore

Pour que l'application fonctionne correctement, Firestore doit autoriser les opérations en lecture/écriture pour les utilisateurs authentifiés.
Dans la console Firebase (onglet **Firestore Database** > **Règles**), utilisez la configuration suivante :

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      // Autorise l'accès uniquement aux utilisateurs connectés via Firebase Auth
      allow read, write: if request.auth != null;
    }
  }
}
```

## 2. Structure des Collections

L'application utilise 5 collections principales :

### `users`
Stocke les statistiques globales de chaque utilisateur.
- **ID Document** : UID (généré par Firebase Auth)
- **Champs** :
  - `userId` (String)
  - `displayName` (String)
  - `email` (String)
  - `createdAt` (Timestamp)
  - `totalSessions` (Number)
  - `lastScore` (Number)
  - `bestScore` (Number)

### `profiles`
Stocke les réponses au profilage initial (l'arbre de décision).
- **ID Document** : UID de l'utilisateur
- **Champs** :
  - `userId` (String)
  - `filiere` (String)
  - `specialite` (String)
  - `niveau` (String)
  - `typeContrat` (String)
  - `technos` (Array de Strings)
  - `createdAt` (Timestamp)

### `sessions`
Historique complet des entretiens passés.
- **ID Document** : Auto-généré
- **Champs** :
  - `userId` (String)
  - `profileId` (String)
  - `score` (Number)
  - `total` (Number)
  - `percentage` (Number)
  - `timeTakenSeconds` (Number)
  - `date` (Timestamp)
  - `questionsAnswered` (Array d'objets) : Contient l'index choisi, si c'est correct, et l'ID de la question.

### `questions_profiling`
L'arbre adaptatif pour cerner le profil.
- **ID Document** : Identifiant manuel (ex: `q1`, `q2a`)
- **Champs** :
  - `id` (String)
  - `order` (Number)
  - `parentQuestionId` (String, nullable) : ID de la question précédente
  - `parentAnswer` (String, nullable) : Réponse déclencheuse
  - `fieldKey` (String) : Champ dans `UserProfile` (ex: `filiere`)
  - `text` (String)
  - `options` (Array de Strings)
  - `multiSelect` (Boolean)

### `questions_entretien`
Le dictionnaire de toutes les questions d'entretien technique.
- **ID Document** : Auto-généré ou ID métier (ex: `dev_back_java_1`)
- **Champs** :
  - `id` (String)
  - `filiere`, `specialite`, `niveau` (Strings) : Tags de ciblage
  - `question` (String)
  - `options` (Array de Strings)
  - `correctIndex` (Number)
  - `explanation` (String)

## 3. Indexation

L'affichage de l'historique dans `ResultActivity` et `HistoryActivity` nécessite un **Index Composite** :
- **Collection** : `sessions`
- **Champ 1** : `userId` (Croissant)
- **Champ 2** : `date` (Décroissant)

## 4. Initialisation Automatique (Seed)

Au premier lancement de `LoginActivity`, la classe `SeedDataManager` vérifie si la collection `questions_entretien` est vide.
Si c'est le cas, elle injecte automatiquement une quarantaine de questions pour le profilage et les entretiens techniques (ex: Java, Cybersécurité, IA).
