package com.example.zdorovie.repository;

import com.example.zdorovie.model.Reminder;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ReminderRepository extends BaseRepository {
    private final CollectionReference remindersCollection;
    
    public ReminderRepository() {
        super();
        this.remindersCollection = firestore.collection("reminders");
    }
    
    public CompletableFuture<Result<String>> addReminder(Reminder reminder) {
        CompletableFuture<Result<String>> resultFuture = new CompletableFuture<>();
        
        remindersCollection.add(reminder)
            .addOnSuccessListener(documentReference -> 
                resultFuture.complete(Result.success(documentReference.getId())))
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<String>> addTextReminder(String userId, String title, String content, 
                                           String time, List<Integer> repeatDays) {
        CompletableFuture<Result<String>> resultFuture = new CompletableFuture<>();
        
        Reminder reminder = new Reminder.Builder()
            .userId(userId)
            .title(title)
            .content(content)
            .time(time)
            .repeatDays(repeatDays)
            .isTextReminder(true)
            .build();
        
        addReminder(reminder)
            .thenAccept(result -> {
                if (result.isSuccess()) {
                    resultFuture.complete(Result.success(result.getData()));
                } else {
                    resultFuture.complete(Result.failure(result.getException()));
                }
            });
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Void>> updateReminder(Reminder reminder) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        remindersCollection.document(reminder.getId()).set(reminder)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Void>> updateTextReminder(String reminderId, String title, String content, 
                                            String time, List<Integer> repeatDays) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("title", title);
        updates.put("content", content);
        
        // Обновляем только если значения заданы
        if (time != null && !time.isEmpty()) {
            updates.put("time", time);
        }
        
        if (repeatDays != null && !repeatDays.isEmpty()) {
            updates.put("repeatDays", repeatDays);
        }
        
        remindersCollection.document(reminderId).update(updates)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    // Перегруженный метод для обновления только заголовка и текста
    public CompletableFuture<Result<Void>> updateTextReminder(String reminderId, String title, String content) {
        return updateTextReminder(reminderId, title, content, "", new ArrayList<>());
    }
    
    public CompletableFuture<Result<Void>> deleteReminder(String reminderId) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        remindersCollection.document(reminderId).delete()
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<Reminder>>> getRemindersByUser(String userId) {
        CompletableFuture<Result<List<Reminder>>> resultFuture = new CompletableFuture<>();
        
        remindersCollection
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Reminder> reminders = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Reminder reminder = doc.toObject(Reminder.class);
                    if (reminder != null) {
                        reminders.add(reminder);
                    }
                }
                resultFuture.complete(Result.success(reminders));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<Reminder>>> getTextRemindersByUser(String userId) {
        CompletableFuture<Result<List<Reminder>>> resultFuture = new CompletableFuture<>();
        
        remindersCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isTextReminder", true)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Reminder> reminders = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    Reminder reminder = doc.toObject(Reminder.class);
                    if (reminder != null) {
                        reminders.add(reminder);
                    }
                }
                resultFuture.complete(Result.success(reminders));
            })
            .addOnFailureListener(e -> 
                resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public ListenerRegistration observeUserReminders(
            String userId, 
            Consumer<List<Reminder>> onRemindersChanged, 
            Consumer<Exception> onError) {
        return remindersCollection
            .whereEqualTo("userId", userId)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    onError.accept(error);
                    return;
                }
                
                List<Reminder> reminders = new ArrayList<>();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Reminder reminder = doc.toObject(Reminder.class);
                        if (reminder != null) {
                            reminders.add(reminder);
                        }
                    }
                }
                
                onRemindersChanged.accept(reminders);
            });
    }
    
    public ListenerRegistration observeUserTextReminders(
            String userId, 
            Consumer<List<Reminder>> onRemindersChanged, 
            Consumer<Exception> onError) {
        return remindersCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isTextReminder", true)
            .addSnapshotListener((snapshot, error) -> {
                if (error != null) {
                    onError.accept(error);
                    return;
                }
                
                List<Reminder> reminders = new ArrayList<>();
                if (snapshot != null) {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        Reminder reminder = doc.toObject(Reminder.class);
                        if (reminder != null) {
                            reminders.add(reminder);
                        }
                    }
                }
                
                onRemindersChanged.accept(reminders);
            });
    }
    
    public CompletableFuture<Result<Void>> toggleReminder(String reminderId, boolean isActive) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        remindersCollection.document(reminderId).update("isActive", isActive)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
} 