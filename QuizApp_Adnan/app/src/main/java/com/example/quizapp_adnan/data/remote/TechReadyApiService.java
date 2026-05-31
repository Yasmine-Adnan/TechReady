package com.example.quizapp_adnan.data.remote;

import com.example.quizapp_adnan.data.model.InterviewQuestion;
import com.example.quizapp_adnan.data.model.Session;
import com.example.quizapp_adnan.data.model.UserProfile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TechReadyApiService {

    @GET("api/questions")
    Call<List<InterviewQuestion>> getQuestions(
            @Query("filiere") String filiere,
            @Query("specialite") String specialite,
            @Query("niveau") String niveau,
            @Query("isVocalAccessible") Boolean isVocalAccessible,
            @Query("entrepriseTag") String entrepriseTag
    );

    @POST("api/profiles")
    Call<Void> createProfile(@Body UserProfile profile);

    @GET("api/profiles/{userId}/last")
    Call<UserProfile> getLastProfile(@Path("userId") String userId);

    @POST("api/sessions")
    Call<Void> createSession(@Body Session session);

    @GET("api/sessions/{userId}")
    Call<List<Session>> getUserSessions(@Path("userId") String userId);

    @GET("api/profiles/skills")
    Call<List<com.example.quizapp_adnan.data.model.SkillDTO>> getSkills(@Query("userId") String userId);

    @POST("api/ai/analyze")
    Call<java.util.Map<String, String>> analyzeSession(@Body java.util.Map<String, Object> payload);

    @POST("api/ai/vocal")
    Call<com.example.quizapp_adnan.data.model.VocalSessionResponse> evaluateVocalSession(@Body com.example.quizapp_adnan.data.model.VocalSessionRequest request);

    @GET("api/profiles/leaderboard")
    Call<List<UserProfile>> getLeaderboard(@Query("filiere") String filiere);

    @GET("api/challenges/current")
    Call<com.example.quizapp_adnan.data.model.Challenge> getCurrentChallenge();

    @POST("api/leaderboard/campus")
    Call<com.example.quizapp_adnan.data.model.CampusRankResponse> getCampusRank(
            @Body java.util.Map<String, Object> body
    );
}
