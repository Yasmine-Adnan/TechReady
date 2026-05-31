package com.techready.backend.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.techready.backend.model.InterviewQuestion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class QuestionService {

    private static final String COLLECTION_NAME = "questions_entretien";

    @Autowired
    private Firestore firestore;

    public List<InterviewQuestion> getQuestionsByCriteria(String filiere, String specialite, String niveau) throws ExecutionException, InterruptedException {
        CollectionReference questions = firestore.collection(COLLECTION_NAME);
        
        Query query = questions;
        if (filiere != null && !filiere.isEmpty()) {
            query = query.whereEqualTo("filiere", filiere);
        }
        if (specialite != null && !specialite.isEmpty()) {
            query = query.whereEqualTo("specialite", specialite);
        }
        if (niveau != null && !niveau.isEmpty()) {
            query = query.whereEqualTo("niveau", niveau);
        }

        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<InterviewQuestion> resultList = new ArrayList<>();
        
        for (DocumentSnapshot document : querySnapshot.get().getDocuments()) {
            InterviewQuestion question = document.toObject(InterviewQuestion.class);
            if (question != null) {
                question.setId(document.getId());
                resultList.add(question);
            }
        }
        return resultList;
    }

    public String addQuestion(InterviewQuestion question) throws ExecutionException, InterruptedException {
        ApiFuture<DocumentReference> addedDocRef = firestore.collection(COLLECTION_NAME).add(question);
        return addedDocRef.get().getId();
    }

    public InterviewQuestion getQuestionById(String id) throws ExecutionException, InterruptedException {
        DocumentReference docRef = firestore.collection(COLLECTION_NAME).document(id);
        DocumentSnapshot document = docRef.get().get();
        if (document.exists()) {
            InterviewQuestion question = document.toObject(InterviewQuestion.class);
            if (question != null) {
                question.setId(document.getId());
                return question;
            }
        }
        return null;
    }
}
