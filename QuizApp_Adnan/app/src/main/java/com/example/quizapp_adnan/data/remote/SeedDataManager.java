package com.example.quizapp_adnan.data.remote;

import android.util.Log;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.model.ProfilingQuestion;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SeedDataManager {
    private static final String TAG = "SeedDataManager";
    private final FirebaseFirestore db;

    public SeedDataManager() {
        db = FirestoreDataSource.getInstance().getDb();
    }

    public void seedIfNeeded() {
        Log.d(TAG, "Mise à jour forcée des questions de profilage (ajout du Panic Mode)...");
        seedProfilingQuestions();
        
        db.collection("questions_entretien").limit(1).get()
                .addOnCompleteListener(task -> {
                    // On force le re-seed pour ajouter les nouvelles questions demandées par l'utilisateur (12 au lieu de 5)
                    Log.d(TAG, "Mise à jour de la base de données... Insertion de 12 questions par catégorie.");
                    seedInterviewQuestions();
                });
    }

    private void seedProfilingQuestions() {
        // Q1 (Racine commune)
        ProfilingQuestion q1 = new ProfilingQuestion("q1", 1, null, null,
                "Quelle est ta filière actuelle ?",
                Arrays.asList("Intelligence Artificielle & Data", "Développement & Systèmes d'Information", "Cybersécurité", "Je n'ai pas de filière précise / Informatique générale"),
                "filiere", false);

        // --- Branche A : IA & Data ---
        ProfilingQuestion q2a = new ProfilingQuestion("q2a", 2, "q1", "Intelligence Artificielle & Data",
                "Quel domaine t'intéresse le plus ?",
                Arrays.asList("Chatbots & IA générative (LLM, RAG, Prompt Engineering)", "Data Science & Machine Learning", "Data Engineering & Analyse", "Computer Vision & Deep Learning"),
                "specialite", false);
        ProfilingQuestion q3a = new ProfilingQuestion("q3a", 3, "q2a", null,
                "Quel est ton niveau actuel ?",
                Arrays.asList("Débutant (je connais Python, je commence le ML)", "Intermédiaire (j'ai fait des projets, je connais les bases du ML)", "Avancé (j'ai des projets complexes, je connais les architectures modernes)"),
                "niveau", false);

        // --- Branche B : Dev & SI ---
        ProfilingQuestion q2b = new ProfilingQuestion("q2b", 2, "q1", "Développement & Systèmes d'Information",
                "Quel type de développement te passionne ?",
                Arrays.asList("Backend (API REST, microservices, bases de données)", "Frontend (interfaces, UX, frameworks JS)", "Fullstack (les deux)", "Mobile (Android, iOS, Flutter)"),
                "specialite", false);
        ProfilingQuestion q3b = new ProfilingQuestion("q3b", 3, "q2b", null,
                "Quelles technos maîtrises-tu le mieux ?",
                Arrays.asList("Spring Boot / Java EE", ".NET / C#", "Node.js / Express", "React / Vue / Angular", "Flutter / React Native", "Django / FastAPI"),
                "technos", true);

        // --- Branche C : Cybersécurité ---
        ProfilingQuestion q2c = new ProfilingQuestion("q2c", 2, "q1", "Cybersécurité",
                "Quel domaine de la cybersécurité t'attire ?",
                Arrays.asList("Pentesting & Red Team (tests d'intrusion, exploitation)", "Blue Team & SOC (détection, réponse aux incidents)", "Sécurité des données & conformité (RGPD, ISO 27001)", "Sécurité réseau & infrastructure (firewalls, VPN, IDS)"),
                "specialite", false);
        ProfilingQuestion q3c = new ProfilingQuestion("q3c", 3, "q2c", null,
                "Quel est ton niveau pratique ?",
                Arrays.asList("Débutant (je connais les concepts, pas encore de pratique)", "Intermédiaire (j'utilise Kali Linux, j'ai fait des CTF)", "Avancé (j'ai des certifications ou des projets réels)"),
                "niveau", false);

        // --- Branche D : Informatique générale ---
        ProfilingQuestion q2d = new ProfilingQuestion("q2d", 2, "q1", "Je n'ai pas de filière précise / Informatique générale",
                "Quel type de poste vises-tu ?",
                Arrays.asList("Développeur généraliste", "Analyste / Chef de projet IT", "Support technique / Administrateur système", "Je cherche encore ma voie"),
                "specialite", false);

        // --- Question Finale Commune (Clonée pour gérer le retour de chaque branche) ---
        List<String> optionsContrat = Arrays.asList("Stage (1 à 6 mois)", "Alternance", "Premier emploi (CDI/CDD)");
        ProfilingQuestion qfA = new ProfilingQuestion("qfA", 98, "q3a", null, "Quel type de contrat recherches-tu ?", optionsContrat, "typeContrat", false);
        ProfilingQuestion qfB = new ProfilingQuestion("qfB", 98, "q3b", null, "Quel type de contrat recherches-tu ?", optionsContrat, "typeContrat", false);
        ProfilingQuestion qfC = new ProfilingQuestion("qfC", 98, "q3c", null, "Quel type de contrat recherches-tu ?", optionsContrat, "typeContrat", false);
        ProfilingQuestion qfD = new ProfilingQuestion("qfD", 98, "q2d", null, "Quel type de contrat recherches-tu ?", optionsContrat, "typeContrat", false);

        // --- Question Finale Objectif (Panic Mode) ---
        List<String> optionsObjectif = Arrays.asList("Advance (Je me prépare à l'avance)", "Soon (Mon entretien est bientôt)", "Panic (Mon entretien est demain !!)");
        ProfilingQuestion qObjA = new ProfilingQuestion("qObjA", 99, "qfA", null, "Quel est ton objectif de préparation ?", optionsObjectif, "objectif", false);
        ProfilingQuestion qObjB = new ProfilingQuestion("qObjB", 99, "qfB", null, "Quel est ton objectif de préparation ?", optionsObjectif, "objectif", false);
        ProfilingQuestion qObjC = new ProfilingQuestion("qObjC", 99, "qfC", null, "Quel est ton objectif de préparation ?", optionsObjectif, "objectif", false);
        ProfilingQuestion qObjD = new ProfilingQuestion("qObjD", 99, "qfD", null, "Quel est ton objectif de préparation ?", optionsObjectif, "objectif", false);

        List<ProfilingQuestion> allProfiling = Arrays.asList(q1, q2a, q3a, q2b, q3b, q2c, q3c, q2d, qfA, qfB, qfC, qfD, qObjA, qObjB, qObjC, qObjD);
        for (ProfilingQuestion q : allProfiling) {
            db.collection("questions_profiling").document(q.getId()).set(q);
        }
    }

    private void seedInterviewQuestions() {
        // --- Exemples : IA & Data ---
        String fIA = "Intelligence Artificielle & Data";
        String sChatbot = "Chatbots & IA générative (LLM, RAG, Prompt Engineering)";
        String nIA = "Intermédiaire (j'ai fait des projets, je connais les bases du ML)";

        InterviewQuestion ia1 = new InterviewQuestion("ia1", fIA, sChatbot, nIA,
                "Qu'est-ce que le RAG ?",
                Arrays.asList("Un algorithme de classification d'images", "Une méthode combinant recherche documentaire et génération LLM", "Un framework Frontend", "Une base de données SQL"),
                1, "Le Retrieval-Augmented Generation (RAG) améliore les réponses des LLM en allant chercher des informations dans une base de connaissances.", Arrays.asList("RAG", "LLM"), "medium");

        InterviewQuestion ia2 = new InterviewQuestion("ia2", fIA, sChatbot, nIA,
                "Quelle librairie Python est connue pour orchestrer des pipelines LLM ?",
                Arrays.asList("Pandas", "LangChain", "TensorFlow", "FastAPI"),
                1, "LangChain est un framework très populaire pour développer des applications basées sur les modèles de langage.", Arrays.asList("Python", "LangChain"), "easy");

        InterviewQuestion ia3 = new InterviewQuestion("ia3", fIA, sChatbot, nIA,
                "Qu'est-ce qu'un Embedding ?",
                Arrays.asList("Une représentation vectorielle d'un texte", "Un type de base de données", "Un langage de programmation", "Un modèle de diffusion"),
                0, "Un embedding convertit du texte en vecteurs numériques comparables mathématiquement.", Arrays.asList("Embedding", "NLP"), "medium");

        InterviewQuestion ia4 = new InterviewQuestion("ia4", fIA, sChatbot, nIA,
                "Lequel est un modèle Open Source ?",
                Arrays.asList("GPT-4", "Claude 3", "Llama 3", "Gemini Ultra"),
                2, "Llama 3 est développé par Meta et ses poids sont disponibles publiquement.", Arrays.asList("LLM", "OpenSource"), "easy");

        InterviewQuestion ia5 = new InterviewQuestion("ia5", fIA, sChatbot, nIA,
                "Quelle base de données est optimale pour le RAG ?",
                Arrays.asList("MongoDB", "Redis", "Pinecone", "PostgreSQL classique"),
                2, "Les bases vectorielles (ex: Pinecone) sont optimisées pour la recherche de similarité.", Arrays.asList("VectorDB", "RAG"), "medium");

        InterviewQuestion ia6 = new InterviewQuestion("ia6", fIA, sChatbot, nIA,
                "Lequel n'est pas un LLM ?",
                Arrays.asList("BERT", "GPT-3", "Claude", "React"),
                3, "React est une bibliothèque Frontend, pas un modèle de langage.", Arrays.asList("LLM"), "easy");
                
        InterviewQuestion ia7 = new InterviewQuestion("ia7", fIA, sChatbot, nIA,
                "Qu'est-ce qu'un Token ?",
                Arrays.asList("Une monnaie crypto", "Une unité de base de texte lue par le modèle", "Un mot de passe", "Une fonction Python"),
                1, "Un token représente un mot, un sous-mot ou un caractère traité par le LLM.", Arrays.asList("Token", "LLM"), "easy");

        InterviewQuestion ia8 = new InterviewQuestion("ia8", fIA, sChatbot, nIA,
                "Quelle est la principale limite d'un LLM ?",
                Arrays.asList("Les hallucinations", "La vitesse", "Le prix", "L'interface"),
                0, "Les modèles peuvent inventer des faits de manière convaincante (hallucination).", Arrays.asList("Hallucination"), "medium");

        InterviewQuestion ia9 = new InterviewQuestion("ia9", fIA, sChatbot, nIA,
                "Que permet le Fine-tuning ?",
                Arrays.asList("Accélérer le réseau", "Adapter le modèle à un domaine précis", "Réduire la taille de l'écran", "Remplacer la BDD"),
                1, "Le fine-tuning réentraîne un modèle existant sur des données spécifiques.", Arrays.asList("Fine-tuning"), "medium");

        InterviewQuestion ia10 = new InterviewQuestion("ia10", fIA, sChatbot, nIA,
                "Qu'est-ce que la 'Temperature' dans un LLM ?",
                Arrays.asList("La chaleur du serveur", "Un paramètre contrôlant la créativité/aléatoire", "La taille du modèle", "Le temps de réponse"),
                1, "Une température élevée donne des réponses plus variées, une faible donne des réponses déterministes.", Arrays.asList("Paramètres"), "medium");

        InterviewQuestion ia11 = new InterviewQuestion("ia11", fIA, sChatbot, nIA,
                "Qu'est-ce que LangSmith ?",
                Arrays.asList("Un outil de monitoring pour LLM", "Un nouveau langage", "Une BDD relationnelle", "Un type de serveur"),
                0, "Créé par les auteurs de LangChain pour monitorer, débugger et évaluer les applications LLM.", Arrays.asList("LangSmith", "Monitoring"), "hard");

        InterviewQuestion ia12 = new InterviewQuestion("ia12", fIA, sChatbot, nIA,
                "Qu'est-ce que le Chunking dans un système RAG ?",
                Arrays.asList("Supprimer les données", "Découper de grands textes en petits blocs", "Encoder la vidéo", "Un algorithme de tri"),
                1, "Le chunking permet de stocker et retrouver des passages précis du texte.", Arrays.asList("Chunking", "RAG"), "medium");

        // --- Exemples : Dev & SI (Backend) ---
        String fDev = "Développement & Systèmes d'Information";
        String sBack = "Backend (API REST, microservices, bases de données)";

        InterviewQuestion dev1 = new InterviewQuestion("dev1", fDev, sBack, "",
                "Annotation Spring Boot pour endpoint REST ?",
                Arrays.asList("@RestController", "@Controller", "@Service", "@Api"),
                0, "@RestController combine @Controller et @ResponseBody.", Arrays.asList("Spring Boot", "REST"), "easy");

        InterviewQuestion dev2 = new InterviewQuestion("dev2", fDev, sBack, "",
                "Qu'est-ce que l'injection de dépendances ?",
                Arrays.asList("Fournir les dépendances depuis l'extérieur", "Une attaque de sécurité", "Compiler dynamiquement", "Un pattern UI"),
                0, "L'IoC permet de déléguer la création des objets au framework.", Arrays.asList("Architecture", "Pattern"), "medium");

        InterviewQuestion dev3 = new InterviewQuestion("dev3", fDev, sBack, "",
                "Laquelle n'est pas une méthode HTTP ?",
                Arrays.asList("GET", "FETCH", "POST", "PATCH"),
                1, "FETCH est une API JS, pas un verbe HTTP standard.", Arrays.asList("HTTP", "API"), "easy");

        InterviewQuestion dev4 = new InterviewQuestion("dev4", fDev, sBack, "",
                "Que signifie ACID en base de données ?",
                Arrays.asList("Atomic, Consistent, Isolated, Durable", "Array, Class, Integer, Double", "Async, Concurrent, Indexed, Distributed", "Advanced Cache Interface Daemon"),
                0, "C'est un ensemble de propriétés garantissant la fiabilité des transactions.", Arrays.asList("SQL", "Database"), "medium");

        InterviewQuestion dev5 = new InterviewQuestion("dev5", fDev, sBack, "",
                "Qu'est-ce qu'un index en base de données ?",
                Arrays.asList("Une clé étrangère", "Une structure de données améliorant la vitesse de lecture", "Un trigger", "Une vue"),
                1, "L'index accélère les requêtes SELECT au détriment des écritures.", Arrays.asList("SQL", "Performance"), "medium");

        InterviewQuestion dev6 = new InterviewQuestion("dev6", fDev, sBack, "",
                "Que fait l'annotation @Transactional dans Spring ?",
                Arrays.asList("Démarre un serveur", "Annule toutes les requêtes si une exception survient", "Envoie un email", "Crée une table SQL"),
                1, "Elle garantit l'intégrité ACID en appliquant un rollback en cas d'erreur.", Arrays.asList("Spring", "SQL"), "medium");

        InterviewQuestion dev7 = new InterviewQuestion("dev7", fDev, sBack, "",
                "Qu'est-ce qu'un ORM ?",
                Arrays.asList("Un Object Relational Mapper", "Un système de cache", "Un proxy réseau", "Une librairie UI"),
                0, "L'ORM fait le pont entre le modèle orienté objet et la base de données relationnelle.", Arrays.asList("ORM", "Hibernate"), "easy");

        InterviewQuestion dev8 = new InterviewQuestion("dev8", fDev, sBack, "",
                "Laquelle est une base NoSQL ?",
                Arrays.asList("PostgreSQL", "MySQL", "MongoDB", "Oracle"),
                2, "MongoDB est une base de données NoSQL orientée documents.", Arrays.asList("NoSQL", "DB"), "easy");

        InterviewQuestion dev9 = new InterviewQuestion("dev9", fDev, sBack, "",
                "Quelle erreur HTTP correspond à 'Non Autorisé' ?",
                Arrays.asList("400", "401", "404", "500"),
                1, "401 Unauthorized indique que l'authentification est requise.", Arrays.asList("HTTP", "API"), "easy");

        InterviewQuestion dev10 = new InterviewQuestion("dev10", fDev, sBack, "",
                "Qu'est-ce qu'un JWT ?",
                Arrays.asList("Java Web Toolkit", "JSON Web Token", "JavaScript Window Target", "Job Worker Thread"),
                1, "Le JWT est un token utilisé pour sécuriser les échanges et l'authentification.", Arrays.asList("Sécurité", "Auth"), "medium");

        InterviewQuestion dev11 = new InterviewQuestion("dev11", fDev, sBack, "",
                "Qu'est-ce que le pattern MVC ?",
                Arrays.asList("Model View Controller", "Main Virtual Core", "Memory Value Cache", "Multi Variable Context"),
                0, "MVC sépare la logique (Model), l'interface (View) et le contrôle (Controller).", Arrays.asList("Architecture"), "easy");

        InterviewQuestion dev12 = new InterviewQuestion("dev12", fDev, sBack, "",
                "Lequel sert de conteneur d'application ?",
                Arrays.asList("Docker", "React", "Postman", "Git"),
                0, "Docker permet de créer, déployer et exécuter des conteneurs.", Arrays.asList("Docker", "DevOps"), "medium");

        // --- Exemples : Cybersécurité ---
        String fCyber = "Cybersécurité";
        String sPentest = "Pentesting & Red Team (tests d'intrusion, exploitation)";
        String nCyber = "Intermédiaire (j'utilise Kali Linux, j'ai fait des CTF)";

        InterviewQuestion cy1 = new InterviewQuestion("cy1", fCyber, sPentest, nCyber,
                "Outil pour scanner les ports ouverts ?",
                Arrays.asList("Wireshark", "Nmap", "Burp Suite", "Metasploit"),
                1, "Nmap est l'outil standard pour la découverte de réseau et scan de ports.", Arrays.asList("Nmap", "Réseau"), "easy");

        InterviewQuestion cy2 = new InterviewQuestion("cy2", fCyber, sPentest, nCyber,
                "Qu'est-ce qu'une SQL Injection ?",
                Arrays.asList("Injecter du code JS", "Injecter du SQL malveillant dans un formulaire", "Un déni de service", "Une élévation de privilèges"),
                1, "Elle permet d'interagir avec la BDD en contournant l'authentification.", Arrays.asList("Web", "SQLi"), "easy");

        InterviewQuestion cy3 = new InterviewQuestion("cy3", fCyber, sPentest, nCyber,
                "A quoi sert Burp Suite ?",
                Arrays.asList("Casser des mots de passe", "Proxy HTTP pour intercepter les requêtes web", "Scanner les vulnérabilités réseau", "Créer des malwares"),
                1, "Burp Suite se place entre le navigateur et le serveur pour analyser les flux.", Arrays.asList("Web", "Burp"), "medium");

        InterviewQuestion cy4 = new InterviewQuestion("cy4", fCyber, sPentest, nCyber,
                "Que cible une attaque XSS ?",
                Arrays.asList("Le serveur BDD", "Le pare-feu", "Le navigateur de l'utilisateur", "Le routeur WiFi"),
                2, "Le XSS exécute du code malveillant côté client (navigateur).", Arrays.asList("Web", "XSS"), "medium");

        // ... (remaining code remains the same as read before, I will just output the full file with the fix)
        InterviewQuestion cy5 = new InterviewQuestion("cy5", fCyber, sPentest, nCyber,
                "Dans un shell inversé (Reverse Shell)...",
                Arrays.asList("La cible se connecte à l'attaquant", "L'attaquant se connecte à la cible", "Les deux échangent des clés", "Le shell est chiffré"),
                0, "La cible initie la connexion vers l'attaquant pour contourner les pare-feu entrants.", Arrays.asList("Exploitation", "Shell"), "hard");

        InterviewQuestion cy6 = new InterviewQuestion("cy6", fCyber, sPentest, nCyber,
                "Quel protocole est utilisé pour chiffrer la navigation web ?",
                Arrays.asList("FTP", "HTTPS", "Telnet", "SMTP"),
                1, "HTTPS (HTTP sur TLS/SSL) chiffre les données entre le navigateur et le serveur.", Arrays.asList("Web", "Crypto"), "easy");

        InterviewQuestion cy7 = new InterviewQuestion("cy7", fCyber, sPentest, nCyber,
                "Qu'est-ce qu'une attaque DDoS ?",
                Arrays.asList("Distributed Denial of Service", "Direct Data Over System", "Digital Domain of Security", "Dynamic Document Object Shell"),
                0, "Une attaque DDoS sature un serveur de requêtes pour le rendre indisponible.", Arrays.asList("Réseau", "DDoS"), "medium");

        InterviewQuestion cy8 = new InterviewQuestion("cy8", fCyber, sPentest, nCyber,
                "Qu'est-ce que le Phishing ?",
                Arrays.asList("Un virus", "Une attaque réseau", "Une technique d'ingénierie sociale", "Un système de cryptage"),
                2, "Le phishing (hameçonnage) trompe l'utilisateur pour voler ses informations.", Arrays.asList("Social Engineering"), "easy");

        InterviewQuestion cy9 = new InterviewQuestion("cy9", fCyber, sPentest, nCyber,
                "Que fait un Ransomware ?",
                Arrays.asList("Il efface le disque dur", "Il chiffre les fichiers et demande une rançon", "Il espionne le clavier", "Il affiche des pubs"),
                1, "Le ransomware prend en otage les données de l'utilisateur contre paiement.", Arrays.asList("Malware"), "easy");

        InterviewQuestion cy10 = new InterviewQuestion("cy10", fCyber, sPentest, nCyber,
                "Qu'est-ce qu'un Hash (ex: SHA-256) ?",
                Arrays.asList("Une fonction à sens unique", "Un algorithme de chiffrement asymétrique", "Un protocole VPN", "Un type de pare-feu"),
                0, "Contrairement au chiffrement, le hash ne peut théoriquement pas être inversé.", Arrays.asList("Crypto", "Hash"), "medium");

        InterviewQuestion cy11 = new InterviewQuestion("cy11", fCyber, sPentest, nCyber,
                "Qu'est-ce que le principe du Moindre Privilège ?",
                Arrays.asList("Tout bloquer par défaut", "Donner uniquement les droits nécessaires", "Ne pas utiliser d'admin", "Mettre des mots de passe courts"),
                1, "Un utilisateur ou programme ne doit avoir que les accès strictement nécessaires à sa fonction.", Arrays.asList("Concept", "Droits"), "medium");

        InterviewQuestion cy12 = new InterviewQuestion("cy12", fCyber, sPentest, nCyber,
                "Quelle est la principale fonction d'un IDS ?",
                Arrays.asList("Bloquer les attaques", "Détecter les intrusions", "Chiffrer le réseau", "Stocker les logs"),
                1, "Un Intrusion Detection System détecte les menaces et lève des alertes (contrairement à l'IPS qui bloque).", Arrays.asList("Réseau", "IDS"), "medium");

        List<InterviewQuestion> allInterview = new ArrayList<>(Arrays.asList(ia1, ia2, ia3, ia4, ia5, ia6, ia7, ia8, ia9, ia10, ia11, ia12, 
                                                            dev1, dev2, dev3, dev4, dev5, dev6, dev7, dev8, dev9, dev10, dev11, dev12, 
                                                            cy1, cy2, cy3, cy4, cy5, cy6, cy7, cy8, cy9, cy10, cy11, cy12));

        // Distribution des tags sur les questions existantes
        for (int i = 0; i < allInterview.size(); i++) {
            InterviewQuestion q = allInterview.get(i);
            List<String> tags = new ArrayList<>(q.getTags() != null ? q.getTags() : new ArrayList<>());
            if (i % 3 == 0) tags.add("mode_quick");
            else if (i % 3 == 1) tags.add("mode_mock");
            else tags.add("mode_survival");
            q.setTags(tags);
        }

        // Ajout de questions supplémentaires réalistes pour étoffer la base
        InterviewQuestion ia13 = new InterviewQuestion("ia13", fIA, sChatbot, nIA,
                "Qu'est-ce que le Machine Learning ?",
                Arrays.asList("Une branche de l'IA basée sur l'apprentissage par les données", "Un langage de programmation", "Un framework frontend", "Une base de données"),
                0, "Le Machine Learning permet aux systèmes d'apprendre à partir des données sans être explicitement programmés.", Arrays.asList("mode_quick", "ML"), "easy");
        InterviewQuestion ia14 = new InterviewQuestion("ia14", fIA, sChatbot, nIA,
                "Quelle est la différence entre IA forte et IA faible ?",
                Arrays.asList("L'IA forte a une conscience, l'IA faible résout une tâche précise", "L'IA forte utilise plus de RAM", "L'IA faible ne marche que sur CPU", "Il n'y a aucune différence"),
                0, "L'IA faible est spécialisée (ex: échecs, chatbot), l'IA forte aurait une intelligence générale comparable à l'humain.", Arrays.asList("mode_mock", "Concept"), "medium");
        InterviewQuestion ia15 = new InterviewQuestion("ia15", fIA, sChatbot, nIA,
                "A quoi sert une fonction d'activation dans un réseau de neurones ?",
                Arrays.asList("A démarrer le serveur", "A introduire de la non-linéarité dans le modèle", "A stocker les poids", "A afficher les résultats"),
                1, "Sans fonction d'activation non-linéaire, un réseau de neurones ne serait qu'une simple régression linéaire.", Arrays.asList("mode_survival", "NeuralNet"), "hard");
        InterviewQuestion ia16 = new InterviewQuestion("ia16", fIA, sChatbot, nIA,
                "Qu'est-ce que l'Overfitting ?",
                Arrays.asList("Quand le modèle apprend par cœur les données d'entraînement", "Quand le modèle est trop rapide", "Un manque de données", "Un algorithme de tri"),
                0, "L'overfitting (surapprentissage) rend le modèle incapable de généraliser sur de nouvelles données.", Arrays.asList("mode_quick", "ML"), "medium");
        InterviewQuestion ia17 = new InterviewQuestion("ia17", fIA, sChatbot, nIA,
                "Quel est le rôle de la descente de gradient (Gradient Descent) ?",
                Arrays.asList("Générer des images", "Minimiser la fonction de perte en ajustant les poids", "Colorer l'interface", "Trier les données"),
                1, "C'est l'algorithme d'optimisation principal pour entraîner les réseaux de neurones.", Arrays.asList("mode_mock", "Maths"), "hard");

        InterviewQuestion dev13 = new InterviewQuestion("dev13", fDev, sBack, "",
                "Quelle est la différence entre une API REST et SOAP ?",
                Arrays.asList("SOAP utilise XML, REST utilise souvent JSON", "SOAP est plus récent", "REST est limité au C++", "Aucune différence"),
                0, "REST est un style architectural léger (souvent JSON) tandis que SOAP est un protocole strict basé sur XML.", Arrays.asList("mode_survival", "API"), "medium");
        InterviewQuestion dev14 = new InterviewQuestion("dev14", fDev, sBack, "",
                "Qu'est-ce que le CI/CD ?",
                Arrays.asList("Un langage de script", "L'intégration et le déploiement continus", "Une base de données cloud", "Un framework CSS"),
                1, "Le CI/CD automatise les tests et le déploiement du code pour une livraison plus rapide et fiable.", Arrays.asList("mode_quick", "DevOps"), "easy");
        InterviewQuestion dev15 = new InterviewQuestion("dev15", fDev, sBack, "",
                "Expliquez le principe de l'inversion de contrôle (IoC).",
                Arrays.asList("Le framework contrôle le flux du programme", "L'utilisateur gère la mémoire", "Le CPU est inversé", "Une faille de sécurité"),
                0, "Au lieu que le code appelle les bibliothèques, c'est le framework qui appelle le code (Hollywood Principle).", Arrays.asList("mode_mock", "Architecture"), "hard");
        InterviewQuestion dev16 = new InterviewQuestion("dev16", fDev, sBack, "",
                "Quel est l'avantage principal de l'architecture Microservices ?",
                Arrays.asList("Tous les composants sont dans le même fichier", "Le déploiement et l'évolution se font indépendamment", "Moins de requêtes réseau", "Plus facile à tracer"),
                1, "Les microservices permettent de scaler et de mettre à jour chaque partie du système séparément.", Arrays.asList("mode_survival", "Architecture"), "medium");
        InterviewQuestion dev17 = new InterviewQuestion("dev17", fDev, sBack, "",
                "Pourquoi utiliser un gestionnaire de version comme Git ?",
                Arrays.asList("Pour compiler le code", "Pour exécuter des tests", "Pour suivre l'historique et collaborer", "Pour chiffrer les mots de passe"),
                2, "Git permet de garder l'historique des modifications, de travailler à plusieurs et de gérer des branches.", Arrays.asList("mode_quick", "Outils"), "easy");

        InterviewQuestion cy13 = new InterviewQuestion("cy13", fCyber, sPentest, nCyber,
                "Qu'est-ce qu'une attaque Man-in-the-Middle (MitM) ?",
                Arrays.asList("Une attaque physique sur un serveur", "L'interception des communications entre deux parties", "Un virus par email", "Une attaque par force brute"),
                1, "L'attaquant s'insère entre l'utilisateur et le serveur pour espionner ou altérer les échanges.", Arrays.asList("mode_mock", "Réseau"), "medium");
        InterviewQuestion cy14 = new InterviewQuestion("cy14", fCyber, sPentest, nCyber,
                "A quoi sert un WAF (Web Application Firewall) ?",
                Arrays.asList("A accélérer le réseau", "A protéger les applications web contre les attaques", "A compiler le code web", "A gérer les bases de données"),
                1, "Le WAF filtre et bloque le trafic HTTP malveillant (comme les injections SQL ou XSS).", Arrays.asList("mode_survival", "Web"), "easy");
        InterviewQuestion cy15 = new InterviewQuestion("cy15", fCyber, sPentest, nCyber,
                "Quelle est la différence entre un chiffrement symétrique et asymétrique ?",
                Arrays.asList("Le symétrique utilise une seule clé, l'asymétrique deux", "L'asymétrique est plus rapide", "Le symétrique est pour le texte clair", "Il n'y a pas de différence"),
                0, "Le chiffrement asymétrique utilise une clé publique pour chiffrer et une clé privée pour déchiffrer.", Arrays.asList("mode_quick", "Crypto"), "medium");
        InterviewQuestion cy16 = new InterviewQuestion("cy16", fCyber, sPentest, nCyber,
                "Que signifie le concept de Zero Trust en sécurité ?",
                Arrays.asList("Faire confiance à tout le réseau interne", "Ne jamais faire confiance, toujours vérifier", "Désactiver les mots de passe", "Utiliser uniquement HTTP"),
                1, "Zero Trust part du principe qu'aucune entité (interne ou externe) n'est fiable par défaut.", Arrays.asList("mode_mock", "Concept"), "medium");
        InterviewQuestion cy17 = new InterviewQuestion("cy17", fCyber, sPentest, nCyber,
                "Qu'est-ce qu'une vulnérabilité Zero-Day ?",
                Arrays.asList("Une faille connue et corrigée", "Une faille inconnue du développeur et sans correctif", "Un virus du jour de l'an", "Un problème de serveur hors ligne"),
                1, "Une vulnérabilité Zero-Day est exploitée par les pirates avant que l'éditeur n'ait pu créer un patch.", Arrays.asList("mode_survival", "Vulnérabilité"), "hard");

        allInterview.addAll(Arrays.asList(
                ia13, ia14, ia15, ia16, ia17,
                dev13, dev14, dev15, dev16, dev17,
                cy13, cy14, cy15, cy16, cy17
        ));

        for (InterviewQuestion q : allInterview) {
            db.collection("questions_entretien").document(q.getId()).set(q);
        }
    }

    /**
     * Enrichit les questions existantes avec les nouveaux champs de l'Étape 12.
     * À appeler UNE SEULE FOIS manuellement depuis un écran de debug ou un bouton admin.
     * N'écrase PAS les champs existants (utilise update()).
     */
    public void updateExistingQuestions() {
        Log.d(TAG, "=== updateExistingQuestions() démarré ===");

        // Carte des enrichissements : questionId -> Map des champs à ajouter/mettre à jour
        java.util.Map<String, java.util.Map<String, Object>> enrichments = new java.util.HashMap<>();

        // --- IA & Data : open-ended (iVocalAccessible = true) ---
        enrichments.put("ia1", buildEnrichment(true, "Capgemini", "Casablanca",
                "Le RAG (Retrieval-Augmented Generation) est une technique qui améliore les LLMs en les combinant avec une base de connaissances externe. Au lieu de tout mémoriser lors de l'entraînement, le modèle interroge un index vectoriel pour trouver des passages pertinents, puis les intègre dans sa réponse. Cela réduit les hallucinations et permet d'utiliser des données récentes ou privées."));

        enrichments.put("ia8", buildEnrichment(true, "OCP", "Casablanca",
                "La principale limite d'un LLM est l'hallucination : le modèle génère du texte très convaincant mais factuellement incorrect. Cela vient du fait qu'il prédit statistiquement les mots suivants sans raisonner. Pour mitiger ce problème, on peut utiliser le RAG, le grounding avec des sources vérifiées, ou des techniques de self-consistency."));

        enrichments.put("ia9", buildEnrichment(true, "Maroc Telecom", "Rabat",
                "Le fine-tuning consiste à réentraîner un modèle pré-entraîné sur un dataset spécifique à un domaine, afin de spécialiser ses réponses. Contrairement au prompting, il modifie réellement les poids du modèle. Il est particulièrement utile pour adapter le ton, le vocabulaire ou les connaissances métier d'un LLM généraliste."));

        enrichments.put("ia14", buildEnrichment(true, null, null,
                "L'IA faible désigne des systèmes spécialisés dans une tâche précise (comme jouer aux échecs ou reconnaître des images), sans compréhension générale. L'IA forte, encore théorique, aurait une intelligence générale comparable à celle de l'humain, capable de raisonner sur n'importe quel sujet. Aujourd'hui, tous les systèmes IA existants sont des IA faibles."));

        enrichments.put("ia15", buildEnrichment(true, null, null,
                "La fonction d'activation introduit de la non-linéarité dans les réseaux de neurones, ce qui leur permet d'apprendre des relations complexes dans les données. Sans elle, empiler plusieurs couches ne ferait qu'une transformation linéaire. Les plus courantes sont ReLU (max(0,x)), Sigmoid (sortie entre 0 et 1) et Tanh (sortie entre -1 et 1)."));

        // --- Dev Backend : questions ouvertes ---
        enrichments.put("dev2", buildEnrichment(true, "Capgemini", "Casablanca",
                "L'injection de dépendances (DI) est un pattern où les composants reçoivent leurs dépendances de l'extérieur plutôt que de les créer eux-mêmes. Dans Spring Boot, le conteneur IoC gère le cycle de vie des Beans et les injecte via @Autowired. Cela favorise la testabilité, le découplage et la maintenabilité du code."));

        enrichments.put("dev15", buildEnrichment(true, "Orange Maroc", "Casablanca",
                "L'inversion de contrôle (IoC) signifie que le contrôle du flux d'exécution est délégué au framework plutôt qu'au code applicatif. C'est ce qu'on appelle le 'Principe de Hollywood' : ne nous appelez pas, nous vous appellerons. Dans Spring, c'est le conteneur qui instancie les objets et orchestre les dépendances."));

        enrichments.put("dev16", buildEnrichment(true, "Capgemini", "Rabat",
                "L'architecture microservices décompose une application en petits services indépendants, chacun responsable d'une fonctionnalité métier. Cela permet un déploiement indépendant, une meilleure résilience (si un service tombe, les autres continuent) et un scaling ciblé. Le principal défi est la complexité opérationnelle et la communication inter-services via des APIs ou des files de messages."));

        // --- Cybersécurité : questions ouvertes ---
        enrichments.put("cy2", buildEnrichment(true, "CNSS", "Rabat",
                "Une injection SQL consiste à insérer du code SQL malveillant dans un champ de formulaire pour manipuler la base de données. Par exemple, saisir ' OR '1'='1 dans un champ login peut contourner l'authentification. Les contre-mesures incluent les requêtes préparées (parameterized queries) et l'ORM, qui échappent automatiquement les entrées utilisateur."));

        enrichments.put("cy16", buildEnrichment(true, "CBI", "Casablanca",
                "Zero Trust est un modèle de sécurité basé sur le principe 'ne jamais faire confiance, toujours vérifier'. Contrairement au modèle périmétrique traditionnel, il n'y a pas de zone de confiance implicite : chaque accès, même interne, doit être authentifié et autorisé. Il repose sur trois piliers : vérification de l'identité, validation des devices, et accès au moindre privilège."));

        enrichments.put("cy17", buildEnrichment(true, "DGSSI", "Rabat",
                "Une vulnérabilité Zero-Day est une faille de sécurité inconnue du fabricant et sans correctif disponible. Elle est particulièrement dangereuse car il n'existe aucune défense officielle au moment de son exploitation. Les exploits Zero-Day sont très prisés sur les marchés noirs et par les agences de renseignement. La meilleure protection reste la défense en profondeur et la détection comportementale."));

        // Appliquer les enrichissements via update()
        for (java.util.Map.Entry<String, java.util.Map<String, Object>> entry : enrichments.entrySet()) {
            String docId = entry.getKey();
            java.util.Map<String, Object> fields = entry.getValue();
            db.collection("questions_entretien").document(docId)
                    .update(fields)
                    .addOnSuccessListener(v -> Log.d(TAG, "✅ Enrichie : " + docId))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Erreur enrichissement " + docId + " : " + e.getMessage()));
        }

        Log.d(TAG, "=== updateExistingQuestions() : " + enrichments.size() + " questions en cours de mise à jour ===");
    }

    private java.util.Map<String, Object> buildEnrichment(boolean isVocalAccessible,
                                                           String entrepriseTag,
                                                           String localisationTag,
                                                           String bonneReponseComplete) {
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("isVocalAccessible", isVocalAccessible);
        if (entrepriseTag != null) map.put("entrepriseTag", entrepriseTag);
        if (localisationTag != null) map.put("localisationTag", localisationTag);
        if (bonneReponseComplete != null) map.put("bonneReponseComplete", bonneReponseComplete);
        return map;
    }
}
