package com.example.quizapp_adnan.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {FlashcardEntity.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    public abstract FlashcardDao flashcardDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "techready_database")
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            databaseWriteExecutor.execute(() -> {
                FlashcardDao dao = INSTANCE.flashcardDao();
                
                // --- Données de Base pour les Flashcards ---
                List<FlashcardEntity> seeds = Arrays.asList(
                    new FlashcardEntity("Java", "Qu'est-ce que le polymorphisme ?", "La capacité d'un objet à prendre plusieurs formes. Typiquement via l'héritage et les interfaces."),
                    new FlashcardEntity("Java", "Différence entre List et Set ?", "List permet les doublons et préserve l'ordre d'insertion. Set n'autorise aucun doublon."),
                    new FlashcardEntity("Java", "Différence entre int et Integer ?", "'int' est un type primitif, 'Integer' est une classe wrapper qui permet d'utiliser 'int' comme un objet (ex: dans les collections)."),
                    new FlashcardEntity("Java", "Qu'est-ce qu'une NullPointerException ?", "Une erreur qui survient lorsqu'on essaie d'appeler une méthode ou d'accéder à une variable sur un objet qui vaut 'null'."),
                    new FlashcardEntity("Spring Boot", "À quoi sert @RestController ?", "C'est une combinaison de @Controller et @ResponseBody, utilisée pour créer des endpoints d'API REST."),
                    new FlashcardEntity("Spring Boot", "Qu'est-ce que l'injection de dépendances (IoC) ?", "Un design pattern où le framework (Spring) se charge de créer et d'injecter les objets nécessaires au lieu de le faire manuellement avec 'new'."),
                    new FlashcardEntity("Spring Boot", "Différence entre @Component et @Service ?", "@Service est une spécialisation de @Component utilisée pour marquer les classes contenant la logique métier. Techniquement, elles font la même chose."),
                    new FlashcardEntity("Base de données", "Que signifie ACID ?", "Atomicité, Cohérence, Isolation, Durabilité. Ce sont les 4 propriétés d'une transaction SQL fiable."),
                    new FlashcardEntity("Base de données", "Différence entre INNER JOIN et LEFT JOIN ?", "INNER JOIN retourne les lignes ayant une correspondance dans les deux tables. LEFT JOIN retourne toutes les lignes de la table de gauche, même sans correspondance."),
                    new FlashcardEntity("Base de données", "Qu'est-ce qu'un index SQL ?", "Une structure de données qui améliore la vitesse des opérations de recherche (SELECT) au détriment de la vitesse des écritures (INSERT/UPDATE)."),
                    new FlashcardEntity("Cybersécurité", "Qu'est-ce qu'une attaque XSS ?", "Cross-Site Scripting. Une faille permettant d'injecter du code JavaScript malveillant dans une page web vue par d'autres utilisateurs."),
                    new FlashcardEntity("Cybersécurité", "Qu'est-ce que l'injection SQL ?", "Une faille où un attaquant insère des requêtes SQL malveillantes via les champs de saisie pour manipuler la base de données."),
                    new FlashcardEntity("Architecture", "Qu'est-ce que le modèle MVC ?", "Model-View-Controller. Un modèle d'architecture séparant les données (Model), l'interface (View) et la logique de contrôle (Controller)."),
                    new FlashcardEntity("Architecture", "Qu'est-ce qu'une API RESTful ?", "Une architecture logicielle basée sur le protocole HTTP utilisant les méthodes GET, POST, PUT, DELETE de manière stateless pour manipuler des ressources."),
                    new FlashcardEntity("Outils", "À quoi sert Git ?", "C'est un système de contrôle de version distribué permettant de suivre les modifications des fichiers et de collaborer sur un projet."),
                    new FlashcardEntity("IA", "Qu'est-ce qu'un LLM ?", "Large Language Model (Grand Modèle de Langage). C'est un modèle d'IA entraîné sur d'immenses quantités de texte pour comprendre et générer du langage humain.")
                );
                
                dao.insertAll(seeds);
            });
        }
    };
}
