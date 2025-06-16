package com.example.zdorovie.repository;

import com.example.zdorovie.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class UserRepository extends BaseRepository {
    private final FirebaseAuth auth;
    private final CollectionReference usersCollection;
    
    public UserRepository() {
        super();
        this.auth = FirebaseAuth.getInstance();
        this.usersCollection = firestore.collection("users");
    }
    
    public CompletableFuture<Result<User>> signUp(String email, String password, String name, User.UserRole role) {
        CompletableFuture<Result<User>> resultFuture = new CompletableFuture<>();
        
        Task<AuthResult> authTask = auth.createUserWithEmailAndPassword(email, password);
        authTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                String userId = task.getResult().getUser().getUid();
                
                // Create user object
                User user = new User.Builder()
                    .id(userId)
                    .email(email)
                    .name(name)
                    .fullName(name)
                    .role(role)
                    .build();
                
                // Small delay to ensure auth is complete
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    resultFuture.complete(Result.failure(e));
                    return;
                }
                
                // Save user to Firestore
                usersCollection.document(userId).set(user)
                    .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(user)))
                    .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
            } else {
                resultFuture.complete(Result.failure(
                    task.getException() != null ? task.getException() : new Exception("User creation failed")));
            }
        });
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<User>> signIn(String email, String password) {
        CompletableFuture<Result<User>> resultFuture = new CompletableFuture<>();
        
        Task<AuthResult> authTask = auth.signInWithEmailAndPassword(email, password);
        authTask.addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                String userId = task.getResult().getUser().getUid();
                
                // Get user data from Firestore
                usersCollection.document(userId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            resultFuture.complete(Result.success(user));
                        } else {
                            resultFuture.complete(Result.failure(new Exception("User not found")));
                        }
                    })
                    .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
            } else {
                resultFuture.complete(Result.failure(
                    task.getException() != null ? task.getException() : new Exception("Sign in failed")));
            }
        });
        
        return resultFuture;
    }
    
    public String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();
        String userId = currentUser != null ? currentUser.getUid() : null;
        System.out.println("UserRepository: getCurrentUserId() = " + userId + 
                           ", auth = " + auth + 
                           ", currentUser = " + currentUser);
        return userId;
    }
    
    public CompletableFuture<Result<User>> getCurrentUser() {
        CompletableFuture<Result<User>> resultFuture = new CompletableFuture<>();
        
        String userId = getCurrentUserId();
        if (userId == null) {
            resultFuture.complete(Result.success(null));
            return resultFuture;
        }
        
        usersCollection.document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                resultFuture.complete(Result.success(user));
            })
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<User>> getUserById(String userId) {
        CompletableFuture<Result<User>> resultFuture = new CompletableFuture<>();
        
        usersCollection.document(userId).get()
            .addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                resultFuture.complete(Result.success(user));
            })
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public ListenerRegistration observeCurrentUser(Consumer<User> onUserChanged, Consumer<Exception> onError) {
        String userId = getCurrentUserId();
        if (userId == null) {
            onUserChanged.accept(null);
            return null;
        }
        
        return usersCollection.document(userId).addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                onError.accept(error);
                return;
            }
            
            User user = snapshot != null ? snapshot.toObject(User.class) : null;
            onUserChanged.accept(user);
        });
    }
    
    public CompletableFuture<Result<Void>> updateUser(User user) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        usersCollection.document(user.getId()).set(user)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<Void>> assignController(String patientId, String controllerId) {
        CompletableFuture<Result<Void>> resultFuture = new CompletableFuture<>();
        
        usersCollection.document(patientId).update("controllerId", controllerId)
            .addOnSuccessListener(aVoid -> resultFuture.complete(Result.success(null)))
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<User>>> getPatientsByController(String controllerId) {
        CompletableFuture<Result<List<User>>> resultFuture = new CompletableFuture<>();
        
        usersCollection
            .whereEqualTo("role", User.UserRole.PATIENT.name())
            .whereEqualTo("controllerId", controllerId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<User> patients = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    User patient = doc.toObject(User.class);
                    if (patient != null) {
                        patients.add(patient);
                    }
                }
                resultFuture.complete(Result.success(patients));
            })
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<List<User>>> getAllControllers() {
        CompletableFuture<Result<List<User>>> resultFuture = new CompletableFuture<>();
        
        usersCollection
            .whereEqualTo("role", User.UserRole.CONTROLLER.name())
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<User> controllers = new ArrayList<>();
                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                    User controller = doc.toObject(User.class);
                    if (controller != null) {
                        controllers.add(controller);
                    }
                }
                resultFuture.complete(Result.success(controllers));
            })
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public CompletableFuture<Result<User>> searchControllerByEmail(String email) {
        CompletableFuture<Result<User>> resultFuture = new CompletableFuture<>();
        
        usersCollection
            .whereEqualTo("email", email)
            .whereEqualTo("role", User.UserRole.CONTROLLER.name())
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (!querySnapshot.isEmpty()) {
                    User controller = querySnapshot.getDocuments().get(0).toObject(User.class);
                    resultFuture.complete(Result.success(controller));
                } else {
                    resultFuture.complete(Result.success(null));
                }
            })
            .addOnFailureListener(e -> resultFuture.complete(Result.failure(e)));
        
        return resultFuture;
    }
    
    public void signOut() {
        auth.signOut();
    }
} 