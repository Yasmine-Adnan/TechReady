package com.techready.backend.service;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Autowired
    private QuestionService questionService;

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";
    private final OkHttpClient client = new OkHttpClient();

    public String analyzeSession(int score, int total, String filiere, String specialite, String niveau, java.util.List<java.util.Map<String, Object>> questionsAnswered) throws IOException {
        if (apiKey == null || apiKey.equals("YOUR_API_KEY_HERE") || apiKey.trim().isEmpty()) {
            return "Veuillez configurer votre clé API Gemini dans le fichier application.properties du backend pour obtenir une analyse personnalisée.";
        }

        StringBuilder contextBuilder = new StringBuilder();
        if (questionsAnswered != null && !questionsAnswered.isEmpty()) {
            contextBuilder.append("\nVoici le détail des questions posées et de ses réponses :\n");
            for (java.util.Map<String, Object> qa : questionsAnswered) {
                String qId = (String) qa.get("questionId");
                Boolean isCorrect = qa.get("isCorrect") != null && (Boolean) qa.get("isCorrect");
                
                try {
                    com.techready.backend.model.InterviewQuestion q = questionService.getQuestionById(qId);
                    if (q != null) {
                        contextBuilder.append("- Question : ").append(q.getQuestion()).append("\n");
                        contextBuilder.append("  Statut : ").append(isCorrect ? "Correcte (Acquis)" : "Incorrecte (À réviser)").append("\n");
                        if (!isCorrect && q.getExplanation() != null) {
                            contextBuilder.append("  Explication attendue : ").append(q.getExplanation()).append("\n");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        String prompt = String.format(
            "Tu es un coach technique expert en recrutement IT. L'utilisateur vient de passer un entretien technique blanc.\n" +
            "Son profil : Filière %s, Spécialité %s, Niveau %s.\n" +
            "Il a obtenu un score de %d sur %d.\n%s\n" +
            "Fais-lui un retour très constructif et motivant (environ 4 à 6 phrases). " +
            "Analyse spécifiquement ses erreurs d'après les questions fournies, explique brièvement pourquoi il a eu faux sur les concepts clés, " +
            "recommande-lui les notions exactes sur lesquelles il doit se concentrer en priorité, et félicite-le pour les concepts qu'il maîtrise déjà. " +
            "Sois précis, pédagogique et bienveillant.",
            filiere != null ? filiere : "Générale", 
            specialite != null ? specialite : "Générale", 
            niveau != null ? niveau : "Non précisé", 
            score, total, contextBuilder.toString()
        );

        // Nettoyer le prompt pour éviter les erreurs JSON
        String safePrompt = prompt.replace("\\", "\\\\")
                                  .replace("\"", "\\\"")
                                  .replace("\n", "\\n")
                                  .replace("\r", "");

        String jsonBody = "{"
            + "\"contents\": [{"
            + "\"parts\":[{\"text\": \"" + safePrompt + "\"}]"
            + "}]"
            + "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                if (response.code() == 400) {
                    return "La clé API Gemini configurée semble invalide ou expirée (Erreur 400). Veuillez la mettre à jour dans application.properties.";
                } else if (response.code() == 401 || response.code() == 403) {
                    return "Accès refusé par l'API Gemini (Erreur " + response.code() + "). Vérifiez votre clé API.";
                } else if (response.code() == 429) {
                    return "Le quota gratuit de l'API Gemini est temporairement dépassé (Erreur 429). Veuillez patienter une minute.";
                }
                return "L'IA Gemini est temporairement indisponible (Erreur " + response.code() + ").";
            }

            String responseBody = response.body().string();
            return extractTextFromJson(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            return "Impossible de se connecter à l'IA Gemini. Veuillez vérifier votre connexion ou la clé API.";
        }
    }

    private String extractTextFromJson(String json) {
        try {
            // Extraction basique sans Jackson pour rester léger et rapide
            String searchString = "\"text\": \"";
            int startIndex = json.indexOf(searchString);
            if (startIndex == -1) return "Erreur lors de l'analyse du retour de l'IA.";
            
            startIndex += searchString.length();
            int endIndex = json.indexOf("\"", startIndex);
            
            String text = json.substring(startIndex, endIndex);
            // Gérer les retours à la ligne échappés
            return text.replace("\\n", "\n").replace("\\\"", "\"");
        } catch (Exception e) {
            e.printStackTrace();
            return "Impossible de lire la réponse de l'IA.";
        }
    }

    public com.techready.backend.model.VocalSessionResponse evaluateVocalResponse(String question, String reponseAttendue, String reponseCandidat) {
        if (apiKey == null || apiKey.equals("YOUR_API_KEY_HERE") || apiKey.trim().isEmpty()) {
            return new com.techready.backend.model.VocalSessionResponse(0.0, "Clé API non configurée.", "", "");
        }

        String prompt = String.format(
            "Tu es un évaluateur technique senior.\n" +
            "Question posée : %s\n" +
            "Réponse attendue : %s\n" +
            "Réponse du candidat : %s\n" +
            "Évalue la réponse sur 10 et fournis un feedback constructif de 3-4 phrases en français.\n" +
            "Réponds UNIQUEMENT en JSON strict, sans markdown, sans backticks :\n" +
            "{\"score\": X, \"feedback\": \"...\", \"pointsForts\": \"...\", \"aAmeliorer\": \"...\"}",
            question != null ? question.replace("\"", "\\\"") : "", 
            reponseAttendue != null ? reponseAttendue.replace("\"", "\\\"") : "", 
            reponseCandidat != null ? reponseCandidat.replace("\"", "\\\"") : ""
        );

        String safePrompt = prompt.replace("\\", "\\\\")
                                  .replace("\"", "\\\"")
                                  .replace("\n", "\\n")
                                  .replace("\r", "");

        String jsonBody = "{"
            + "\"contents\": [{"
            + "\"parts\":[{\"text\": \"" + safePrompt + "\"}]"
            + "}]"
            + "}";

        RequestBody body = RequestBody.create(jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(GEMINI_API_URL + "?key=" + apiKey)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return new com.techready.backend.model.VocalSessionResponse(0.0, "Erreur API Gemini: " + response.code(), "", "");
            }

            String responseBody = response.body().string();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode rootNode = mapper.readTree(responseBody);
            
            String rawJsonText = "";
            try {
                rawJsonText = rootNode.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            } catch (Exception ex) {
                return new com.techready.backend.model.VocalSessionResponse(0.0, "Format de réponse IA inattendu.", "", "");
            }
            
            // Clean markdown if Gemini still adds it
            if(rawJsonText.startsWith("```json")) {
                rawJsonText = rawJsonText.substring(7);
            }
            if(rawJsonText.startsWith("```")) {
                rawJsonText = rawJsonText.substring(3);
            }
            if(rawJsonText.endsWith("```")) {
                rawJsonText = rawJsonText.substring(0, rawJsonText.length() - 3);
            }
            rawJsonText = rawJsonText.trim();

            return mapper.readValue(rawJsonText, com.techready.backend.model.VocalSessionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new com.techready.backend.model.VocalSessionResponse(0.0, "Erreur lors de l'analyse IA: " + e.getMessage(), "", "");
        }
    }
}
