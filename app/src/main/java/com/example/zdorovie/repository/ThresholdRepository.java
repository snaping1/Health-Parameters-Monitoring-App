package com.example.zdorovie.repository;

import com.example.zdorovie.model.HealthParameter;
import com.example.zdorovie.model.Threshold;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ThresholdRepository extends BaseRepository {
    private final CollectionReference thresholdsCollection;
    
    public ThresholdRepository() {
        super();
        this.thresholdsCollection = firestore.collection("thresholds");
    }
    
    public CompletableFuture<Result<String>> addThreshold(Threshold threshold) {
        CompletableFuture<Result<String>> resultFuture = new CompletableFuture<>();
        
        thresholdsCollection.add(threshold)
            .addOnSuccessListener(documentReference -> 
                resultFuture.complete(Result.success(documentReference.getId())))
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Void>> updateThreshold(Threshold threshold) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        thresholdsCollection.document(threshold.getId()).set(threshold)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Void>> deleteThreshold(String thresholdId) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        thresholdsCollection.document(thresholdId).delete()
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<Threshold>>> getThresholdsByPatient(String patientId) {
        CompletableFuture<Result<List<Threshold>>> resultFuture = new CompletableFuture<>();
        
        thresholdsCollection
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("isActive", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Threshold> thresholds = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Threshold threshold = doc.toObject(Threshold.class);
                    if (threshold != null) {
                        thresholds.add(threshold);
                    }
                }
                resultFuture.complete(Result.success(thresholds));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<Threshold>>> getThresholdsByController(String controllerId) {
        CompletableFuture<Result<List<Threshold>>> resultFuture = new CompletableFuture<>();
        
        thresholdsCollection
            .whereEqualTo("controllerId", controllerId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Threshold> thresholds = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Threshold threshold = doc.toObject(Threshold.class);
                    if (threshold != null) {
                        thresholds.add(threshold);
                    }
                }
                resultFuture.complete(Result.success(thresholds));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Threshold>> getThresholdForParameter(
            String patientId, HealthParameter.ParameterType parameterType) {
        CompletableFuture<Result<Threshold>> resultFuture = new CompletableFuture<>();
        
        thresholdsCollection
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("parameterType", parameterType.name())
            .whereEqualTo("isActive", true)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    Threshold threshold = querySnapshot.getDocuments().get(0).toObject(Threshold.class);
                    resultFuture.complete(Result.success(threshold));
                } else {
                    resultFuture.complete(Result.success(null));
                }
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public ListenerRegistration observePatientThresholds(
            String patientId, 
            Consumer<List<Threshold>> onThresholdsChanged, 
            Consumer<Exception> onError) {
        return thresholdsCollection
            .whereEqualTo("patientId", patientId)
            .whereEqualTo("isActive", true)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    onError.accept(error);
                    return;
                }
                
                List<Threshold> thresholds = new ArrayList<>();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Threshold threshold = doc.toObject(Threshold.class);
                        if (threshold != null) {
                            thresholds.add(threshold);
                        }
                    }
                }
                
                onThresholdsChanged.accept(thresholds);
            });
    }
    
    public CompletableFuture<Result<Boolean>> checkThresholdViolation(
            String patientId, HealthParameter parameter) {
        CompletableFuture<Result<Boolean>> resultFuture = new CompletableFuture<>();
        
        getThresholdForParameter(patientId, parameter.getType())
            .thenAccept(result -> {
                if (result.isFailure()) {
                    resultFuture.complete(Result.failure(result.getException()));
                    return;
                }
                
                Threshold threshold = result.getData();
                if (threshold == null || !threshold.isActive()) {
                    resultFuture.complete(Result.success(false));
                    return;
                }
                
                boolean isViolated = parameter.getValue() < threshold.getMinValue() || 
                                    parameter.getValue() > threshold.getMaxValue();
                resultFuture.complete(Result.success(isViolated));
            });
        
        return resultFuture;
    }
} 