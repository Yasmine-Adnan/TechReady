package com.example.quizapp_adnan.data.remote;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    // IP locale du PC sur le réseau de partage de connexion (hotspot) - Carte Wi-Fi 172.20.10.3
    // Attention : on garde le slash final ("/") sans ajouter "api/" car les endpoints dans TechReadyApiService incluent déjà "api/..."
    private static final String BASE_URL = "http://172.20.10.3:8080/";
    
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configuration de l'intercepteur de logs pour voir les requêtes/réponses dans le Logcat
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

            // Configuration de Gson pour sérialiser les Dates au format ISO-8601 (compatible avec Spring Boot Jackson)
            com.google.gson.Gson gson = new com.google.gson.GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
                    .create();

            // Création de l'instance Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson)) // Utilisation du Gson configuré
                    .build();
        }
        return retrofit;
    }

    public static TechReadyApiService getApiService() {
        return getClient().create(TechReadyApiService.class);
    }
}
