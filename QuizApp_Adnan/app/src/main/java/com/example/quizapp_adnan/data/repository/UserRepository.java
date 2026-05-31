package com.example.quizapp_adnan.data.repository;

import com.example.quizapp_adnan.data.model.User;
import com.example.quizapp_adnan.data.remote.FirestoreDataSource;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

public class UserRepository {
    private final FirestoreDataSource dataSource;

    public UserRepository() {
        dataSource = FirestoreDataSource.getInstance();
    }

    public Task<Void> saveUser(User user) {
        return dataSource.getDb().collection("users").document(user.getUserId()).set(user);
    }

    public Task<DocumentSnapshot> getUser(String userId) {
        return dataSource.getDb().collection("users").document(userId).get();
    }
}
