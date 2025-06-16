package com.example.zdorovie.repository;

import com.example.zdorovie.model.Recommendation;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RecommendationRepository extends BaseRepository {
    private final CollectionReference recommendationsCollection;
    
    public RecommendationRepository() {
        super();
        this.recommendationsCollection = firestore.collection("recommendations");
    }
    
    public CompletableFuture<Result<String>> addRecommendation(Recommendation recommendation) {
        CompletableFuture<Result<String>> resultFuture = new CompletableFuture<>();
        
        recommendationsCollection.add(recommendation)
            .addOnSuccessListener(documentReference -> 
                resultFuture.complete(Result.success(documentReference.getId())))
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<Recommendation>>> getRecommendationsByPatient(String patientId) {
        CompletableFuture<Result<List<Recommendation>>> resultFuture = new CompletableFuture<>();
        
        System.out.println("RecommendationRepository: Getting recommendations for patient " + patientId);
        
        recommendationsCollection
            .whereEqualTo("patientId", patientId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Recommendation> recommendations = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Recommendation recommendation = doc.toObject(Recommendation.class);
                    if (recommendation != null) {
                        // Set the ID from the document
                        recommendation.setId(doc.getId());
                        recommendations.add(recommendation);
                    }
                }
                
                // Сортировать по убыванию
                Collections.sort(recommendations, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                
                System.out.println("RecommendationRepository: Found " + recommendations.size() + 
                                   " recommendations for patient " + patientId);
                
                resultFuture.complete(Result.success(recommendations));
            })
            .addOnFailureListener(e -> {
                System.out.println("RecommendationRepository: Error getting recommendations for patient " + 
                                  patientId + ": " + e.getMessage());
                resultFuture.complete(Result.failure(e));
            });
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<Recommendation>>> getRecommendationsByController(String controllerId) {
        CompletableFuture<Result<List<Recommendation>>> resultFuture = new CompletableFuture<>();
        
        System.out.println("RecommendationRepository: Getting recommendations for controller " + controllerId);
        
        recommendationsCollection
            .whereEqualTo("controllerId", controllerId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Recommendation> recommendations = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Recommendation recommendation = doc.toObject(Recommendation.class);
                    if (recommendation != null) {
                        // Set the ID from the document
                        recommendation.setId(doc.getId());
                        recommendations.add(recommendation);
                    }
                }
                
                // Сортировать по убыванию
                Collections.sort(recommendations, (r1, r2) -> 
                    Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                
                System.out.println("RecommendationRepository: Found " + recommendations.size() + 
                                   " recommendations for controller " + controllerId);
                
                resultFuture.complete(Result.success(recommendations));
            })
            .addOnFailureListener(e -> {
                System.out.println("RecommendationRepository: Error getting recommendations for controller " + 
                                  controllerId + ": " + e.getMessage());
                resultFuture.complete(Result.failure(e));
            });
        
        return resultFuture;
    }
    
    public ListenerRegistration observePatientRecommendations(
            String patientId, 
            Consumer<List<Recommendation>> onRecommendationsChanged, 
            Consumer<Exception> onError) {
        return recommendationsCollection
            .whereEqualTo("patientId", patientId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    onError.accept(error);
                    return;
                }
                
                List<Recommendation> recommendations = new ArrayList<>();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Recommendation recommendation = doc.toObject(Recommendation.class);
                        if (recommendation != null) {
                            recommendations.add(recommendation);
                        }
                    }
                    
                    // Сортировать по убыванию
                    Collections.sort(recommendations, (r1, r2) -> 
                        Long.compare(r2.getCreatedAt(), r1.getCreatedAt()));
                }
                
                onRecommendationsChanged.accept(recommendations);
            });
    }
    
    public CompletableFuture<Result<Void>> markAsRead(String recommendationId) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        recommendationsCollection.document(recommendationId).update("isRead", true)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Integer>> getUnreadCount(String patientId) {
        CompletableFuture<Result<Integer>> resultFuture = new CompletableFuture<>();
        
        recommendationsCollection
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener(querySnapshot -> 
                resultFuture.complete(Result.success(querySnapshot.size())))
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Void>> deleteRecommendation(String recommendationId) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        recommendationsCollection.document(recommendationId).delete()
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
} 