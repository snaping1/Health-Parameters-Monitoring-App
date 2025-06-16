package com.example.zdorovie.repository;

import com.example.zdorovie.model.HealthParameter;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// Репозиторий для работы с параметрами здоровья в Firestore
public class HealthParameterRepository extends BaseRepository {
    private final CollectionReference parametersCollection;
    
    public HealthParameterRepository() {
        super();
        this.parametersCollection = firestore.collection("health_parameters");
    }

    // Добавляет новый параметр здоровья в базу данных
    public CompletableFuture<Result<String>> addParameter(HealthParameter parameter) {
        CompletableFuture<Result<String>> resultFuture = new CompletableFuture<>();
        
        parametersCollection.add(parameter)
            .addOnSuccessListener(documentReference -> 
                resultFuture.complete(Result.success(documentReference.getId())))
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<HealthParameter>>> getParametersByUser(String userId, int limit) {
        CompletableFuture<Result<List<HealthParameter>>> resultFuture = new CompletableFuture<>();
        
        parametersCollection
            .whereEqualTo("userId", userId)
            .limit(limit)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<HealthParameter> parameters = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    HealthParameter parameter = doc.toObject(HealthParameter.class);
                    if (parameter != null) {
                        parameters.add(parameter);
                    }
                }
                
                // Сортировка по временной метке в порядке убывания
                Collections.sort(parameters, (p1, p2) -> 
                    Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                
                resultFuture.complete(Result.success(parameters));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    // Перегруженный метод с ограничением по умолчанию
    public CompletableFuture<Result<List<HealthParameter>>> getParametersByUser(String userId) {
        return getParametersByUser(userId, 100);
    }
    
    public CompletableFuture<Result<List<HealthParameter>>> getParametersByUserAndType(
            String userId, HealthParameter.ParameterType type, int limit) {
        CompletableFuture<Result<List<HealthParameter>>> resultFuture = new CompletableFuture<>();
        
        parametersCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", type.name())
            .limit(limit)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<HealthParameter> parameters = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    HealthParameter parameter = doc.toObject(HealthParameter.class);
                    if (parameter != null) {
                        parameters.add(parameter);
                    }
                }
                
                // Сортировка по временной метке в порядке убывания
                Collections.sort(parameters, (p1, p2) -> 
                    Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                
                resultFuture.complete(Result.success(parameters));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    // Перегруженный метод с ограничением по умолчанию
    public CompletableFuture<Result<List<HealthParameter>>> getParametersByUserAndType(
            String userId, HealthParameter.ParameterType type) {
        return getParametersByUserAndType(userId, type, 50);
    }
    
    public ListenerRegistration observeUserParameters(
            String userId, Consumer<List<HealthParameter>> onParametersChanged, Consumer<Exception> onError) {
        return parametersCollection
            .whereEqualTo("userId", userId)
            .limit(100)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    onError.accept(error);
                    return;
                }
                
                List<HealthParameter> parameters = new ArrayList<>();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        HealthParameter parameter = doc.toObject(HealthParameter.class);
                        if (parameter != null) {
                            parameters.add(parameter);
                        }
                    }
                    
                    // Сортировка по временной метке в порядке убывания
                    Collections.sort(parameters, (p1, p2) -> 
                        Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                }
                
                onParametersChanged.accept(parameters);
            });
    }
    
    public ListenerRegistration observeLatestParametersByType(
            String userId, 
            Consumer<Map<HealthParameter.ParameterType, HealthParameter>> onParametersChanged, 
            Consumer<Exception> onError) {
        return parametersCollection
            .whereEqualTo("userId", userId)
            .limit(50)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    onError.accept(error);
                    return;
                }
                
                List<HealthParameter> parameters = new ArrayList<>();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        HealthParameter parameter = doc.toObject(HealthParameter.class);
                        if (parameter != null) {
                            parameters.add(parameter);
                        }
                    }
                }
                
                // Сортировка всех параметры по временной метке в порядке убывания
                Collections.sort(parameters, (p1, p2) -> 
                    Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                
                // Группировка по типу и выбор последнего параметра для каждого типа
                Map<HealthParameter.ParameterType, HealthParameter> latestByType = new HashMap<>();
                for (HealthParameter parameter : parameters) {
                    if (!latestByType.containsKey(parameter.getType())) {
                        latestByType.put(parameter.getType(), parameter);
                    }
                }
                
                onParametersChanged.accept(latestByType);
            });
    }

    // Удаляет параметр здоровья по ID
    public CompletableFuture<Result<Void>> deleteParameter(String parameterId) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        parametersCollection.document(parameterId).delete()
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }

    // Получает параметры здоровья за указанный временной период
    public CompletableFuture<Result<List<HealthParameter>>> getParametersInTimeRange(
            String userId, long startTime, long endTime) {
        CompletableFuture<Result<List<HealthParameter>>> resultFuture = new CompletableFuture<>();
        
        parametersCollection
            .whereEqualTo("userId", userId)
            .whereGreaterThanOrEqualTo("timestamp", startTime)
            .whereLessThanOrEqualTo("timestamp", endTime)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<HealthParameter> parameters = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    HealthParameter parameter = doc.toObject(HealthParameter.class);
                    if (parameter != null) {
                        parameters.add(parameter);
                    }
                }
                
                // Сортировка по временной метке (новые сначала)
                Collections.sort(parameters, (p1, p2) -> 
                    Long.compare(p2.getTimestamp(), p1.getTimestamp()));
                
                resultFuture.complete(Result.success(parameters));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
} 