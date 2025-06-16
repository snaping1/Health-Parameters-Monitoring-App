package com.example.zdorovie.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.zdorovie.model.HealthParameter;
import com.example.zdorovie.model.Recommendation;
import com.example.zdorovie.model.Threshold;
import com.example.zdorovie.model.User;
import com.example.zdorovie.repository.BaseRepository;
import com.example.zdorovie.repository.HealthParameterRepository;
import com.example.zdorovie.repository.RecommendationRepository;
import com.example.zdorovie.repository.Result;
import com.example.zdorovie.repository.ThresholdRepository;
import com.example.zdorovie.repository.UserRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ControllerViewModel extends ViewModel {
    private final UserRepository userRepository = new UserRepository();
    private final HealthParameterRepository healthParameterRepository = new HealthParameterRepository();
    private final ThresholdRepository thresholdRepository = new ThresholdRepository();
    private final RecommendationRepository recommendationRepository = new RecommendationRepository();
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    private final MutableLiveData<List<User>> patients = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<User> selectedPatient = new MutableLiveData<>();
    private final MutableLiveData<Map<HealthParameter.ParameterType, HealthParameter>> patientParameters = 
        new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<Map<HealthParameter.ParameterType, HealthParameter>> patientLatestParameters = 
        new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<Threshold>> patientThresholds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Threshold>> thresholds = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Recommendation>> recommendations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> recommendationCreated = new MutableLiveData<>(false);
    
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    
    private String currentControllerId;
    
    public ControllerViewModel() {
        loadControllerData();
    }
    
    private void loadControllerData() {
        CompletableFuture.runAsync(() -> {
            try {
                System.out.println("ControllerViewModel: Loading controller data...");
                currentControllerId = userRepository.getCurrentUserId();
                if (currentControllerId != null) {
                    System.out.println("ControllerViewModel: Current controller ID = " + currentControllerId);
                    loadPatients(currentControllerId);
                } else {
                    System.out.println("ControllerViewModel: Current controller ID is null!");
                    error.postValue("ID контролера не определен. Попробуйте выйти и войти снова.");
                }
            } catch (Exception e) {
                System.out.println("ControllerViewModel: Error loading controller data: " + e.getMessage());
                error.postValue(e.getMessage());
            }
        }, executor);
    }
    
    public void loadDashboardData() {
        loading.setValue(true);
        CompletableFuture.runAsync(() -> {
            if (currentControllerId != null) {
                // Load patients
                loadPatients(currentControllerId);
                // Load thresholds
                loadThresholds();
                // Load recommendations
                loadRecommendations();
            }
            loading.postValue(false);
        }, executor);
    }
    
    public void loadPatients() {
        if (currentControllerId != null) {
            loadPatients(currentControllerId);
        }
    }
    
    private void loadPatients(String controllerId) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                CompletableFuture<BaseRepository.Result<List<User>>> patientsFuture =
                    userRepository.getPatientsByController(controllerId);
                
                patientsFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        patients.postValue(result.getData());
                    } else {
                        error.postValue("Не удалось загрузить список пациентов");
                    }
                });
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка загрузки пациентов";
                error.postValue(errorMessage);
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void loadPatientDetails(String patientId) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                CompletableFuture<BaseRepository.Result<User>> patientFuture = userRepository.getUserById(patientId);
                
                patientFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        selectedPatient.postValue(result.getData());
                        observePatientData(patientId);
                    } else {
                        error.postValue("Не удалось загрузить данные пациента");
                    }
                });
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка загрузки данных пациента";
                error.postValue(errorMessage);
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void selectPatient(User patient) {
        selectedPatient.setValue(patient);
        observePatientData(patient.getId());
    }
    
    private void observePatientData(String patientId) {
        // Observe health parameters
        CompletableFuture.runAsync(() -> {
            try {
                CompletableFuture<BaseRepository.Result<List<HealthParameter>>> parametersFuture =
                    healthParameterRepository.getParametersByUser(patientId);
                
                parametersFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        List<HealthParameter> parameters = result.getData();
                        Map<HealthParameter.ParameterType, HealthParameter> latestMap = new HashMap<>();
                        
                        // Group by type and find latest by timestamp
                        for (HealthParameter param : parameters) {
                            HealthParameter currentLatest = latestMap.get(param.getType());
                            if (currentLatest == null || param.getTimestamp() > currentLatest.getTimestamp()) {
                                latestMap.put(param.getType(), param);
                            }
                        }
                        
                        patientParameters.postValue(latestMap);
                    }
                });
            } catch (Exception e) {
                error.postValue("Ошибка загрузки параметров: " + e.getMessage());
            }
        }, executor);
        
        // Observe latest parameters by type
        healthParameterRepository.observeLatestParametersByType(
            patientId,
            latestMap -> patientLatestParameters.postValue(latestMap),
            e -> error.postValue("Ошибка загрузки последних параметров: " + e.getMessage())
        );
        
        // Observe thresholds
        CompletableFuture.runAsync(() -> {
            try {
                CompletableFuture<BaseRepository.Result<List<Threshold>>> thresholdsFuture =
                    thresholdRepository.getThresholdsByPatient(patientId);
                
                thresholdsFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        patientThresholds.postValue(result.getData());
                    }
                });
            } catch (Exception e) {
                error.postValue("Ошибка загрузки пороговых значений: " + e.getMessage());
            }
        }, executor);
    }
    
    public void loadThresholds() {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            if (currentControllerId != null) {
                try {
                    CompletableFuture<BaseRepository.Result<List<Threshold>>> thresholdsFuture =
                        thresholdRepository.getThresholdsByController(currentControllerId);
                    
                    thresholdsFuture.thenAccept(result -> {
                        if (result.isSuccess() && result.getData() != null) {
                            thresholds.postValue(result.getData());
                        } else {
                            error.postValue("Не удалось загрузить пороговые значения");
                        }
                    });
                } catch (Exception e) {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка загрузки пороговых значений";
                    error.postValue(errorMessage);
                }
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void updateThreshold(Threshold threshold) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                thresholdRepository.updateThreshold(threshold);
                loadThresholds();
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка обновления порогового значения";
                error.postValue(errorMessage);
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void setThreshold(
        String patientId,
        HealthParameter.ParameterType parameterType,
        double minValue,
        double maxValue
    ) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                // Check if threshold already exists
                CompletableFuture<BaseRepository.Result<Threshold>> existingThresholdFuture =
                    thresholdRepository.getThresholdForParameter(patientId, parameterType);
                
                existingThresholdFuture.thenAccept(result -> {
                    Threshold existingThreshold = result.isSuccess() ? result.getData() : null;
                    
                    if (existingThreshold != null) {
                        // Update existing threshold
                        existingThreshold.setMinValue(minValue);
                        existingThreshold.setMaxValue(maxValue);
                        thresholdRepository.updateThreshold(existingThreshold);
                    } else {
                        // Create new threshold
                        Threshold threshold = new Threshold(
                            null, // id will be generated
                            currentControllerId,
                            patientId,
                            parameterType,
                            minValue, 
                            maxValue, 
                            true, // active by default
                            System.currentTimeMillis() // current timestamp
                        );
                        
                        thresholdRepository.addThreshold(threshold);
                    }
                    
                    // Reload thresholds
                    loadThresholds();
                });
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка установки порогового значения";
                error.postValue(errorMessage);
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void deleteThreshold(String thresholdId) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                thresholdRepository.deleteThreshold(thresholdId);
                loadThresholds();
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка удаления порогового значения";
                error.postValue(errorMessage);
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void loadRecommendations() {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            if (currentControllerId != null) {
                try {
                    CompletableFuture<BaseRepository.Result<List<Recommendation>>> recommendationsFuture =
                        recommendationRepository.getRecommendationsByController(currentControllerId);
                    
                    recommendationsFuture.thenAccept(result -> {
                        if (result.isSuccess() && result.getData() != null) {
                            // Log the number of recommendations fetched
                            List<Recommendation> fetchedRecommendations = result.getData();
                            System.out.println("ControllerViewModel: Loaded " + fetchedRecommendations.size() + " recommendations");
                            
                            // Post to LiveData on the main thread
                            recommendations.postValue(fetchedRecommendations);
                        } else {
                            error.postValue("Не удалось загрузить рекомендации");
                            // Set empty list to clear any previous data
                            recommendations.postValue(new ArrayList<>());
                        }
                        loading.postValue(false);
                    });
                } catch (Exception e) {
                    String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка загрузки рекомендаций";
                    error.postValue(errorMessage);
                    // Set empty list to clear any previous data
                    recommendations.postValue(new ArrayList<>());
                    loading.postValue(false);
                }
            } else {
                error.postValue("ID контролера не определен");
                recommendations.postValue(new ArrayList<>());
                loading.postValue(false);
            }
        }, executor);
    }
    
    public void createRecommendation(Recommendation recommendation) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                // Make sure controllerId is set
                if (currentControllerId == null) {
                    error.postValue("ID контролера не определен. Попробуйте выйти и войти снова.");
                    loading.postValue(false);
                    return;
                }
                
                // Set the controllerId before saving
                recommendation.setControllerId(currentControllerId);
                
                System.out.println("ControllerViewModel: Creating recommendation with controllerId=" + 
                    currentControllerId + ", patientId=" + recommendation.getPatientId());
                
                recommendationRepository.addRecommendation(recommendation)
                    .thenAccept(result -> {
                        if (result.isSuccess()) {
                            System.out.println("ControllerViewModel: Successfully created recommendation with ID: " + 
                                result.getData());
                            // Explicitly reload recommendations to update UI
                            loadRecommendations();
                            // Also notify patients
                            notifyPatientAboutRecommendation(recommendation.getPatientId());
                            recommendationCreated.postValue(true);
                        }
                        loading.postValue(false);
                    });
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка создания рекомендации";
                System.out.println("ControllerViewModel: Exception creating recommendation: " + errorMessage);
                error.postValue(errorMessage);
                loading.postValue(false);
            }
        }, executor);
    }
    
    // Helper method to notify patient about new recommendation
    private void notifyPatientAboutRecommendation(String patientId) {
        // This method will be used to trigger patient's UI update if needed
        // For now, we'll just make sure the recommendation gets saved properly
        CompletableFuture.runAsync(() -> {
            try {
                // Optional: perform any additional actions needed for patient notification
            } catch (Exception e) {
                // Silently handle errors here, we already saved the recommendation
            }
        }, executor);
    }
    
    public void sendRecommendation(String patientId, String title, String content) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                // Check if controller ID is available
                if (currentControllerId == null) {
                    error.postValue("ID контролера не определен. Попробуйте выйти и войти снова.");
                    loading.postValue(false);
                    return;
                }
                
                System.out.println("ControllerViewModel: Sending recommendation with controllerId=" + 
                    currentControllerId + ", patientId=" + patientId);
                
                Recommendation recommendation = new Recommendation.Builder()
                    .controllerId(currentControllerId)
                    .patientId(patientId)
                    .title(title)
                    .content(content)
                    .isRead(false)
                    .createdAt(System.currentTimeMillis())
                    .build();
                
                recommendationRepository.addRecommendation(recommendation)
                    .thenAccept(result -> {
                        if (result.isSuccess()) {
                            System.out.println("ControllerViewModel: Successfully sent recommendation with ID: " + 
                                result.getData());
                            // Explicitly reload recommendations to update UI
                            loadRecommendations();
                            // Also notify patients
                            notifyPatientAboutRecommendation(patientId);
                            recommendationCreated.postValue(true);
                        }
                        loading.postValue(false);
                    });
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка отправки рекомендации";
                System.out.println("ControllerViewModel: Exception sending recommendation: " + errorMessage);
                error.postValue(errorMessage);
                loading.postValue(false);
            }
        }, executor);
    }
    
    public void getParameterHistory(String patientId, HealthParameter.ParameterType parameterType, int limit) {
        CompletableFuture.runAsync(() -> {
            loading.postValue(true);
            error.postValue(null);
            
            try {
                CompletableFuture<BaseRepository.Result<List<HealthParameter>>> parametersFuture =
                    healthParameterRepository.getParametersByUserAndType(patientId, parameterType, limit);
                
                parametersFuture.thenAccept(result -> {
                    if (result.isSuccess()) {
                        // Process the history data
                        // For now, we just load the latest parameters
                        observePatientData(patientId);
                    }
                });
            } catch (Exception e) {
                String errorMessage = e.getMessage() != null ? e.getMessage() : "Ошибка загрузки истории параметров";
                error.postValue(errorMessage);
            }
            
            loading.postValue(false);
        }, executor);
    }
    
    public void refreshPatients() {
        if (currentControllerId != null) {
            loadPatients(currentControllerId);
        }
    }
    
    // Getters for LiveData
    public LiveData<List<User>> getPatients() {
        return patients;
    }
    
    public LiveData<User> getSelectedPatient() {
        return selectedPatient;
    }
    
    public LiveData<Map<HealthParameter.ParameterType, HealthParameter>> getPatientParameters() {
        return patientParameters;
    }
    
    public LiveData<Map<HealthParameter.ParameterType, HealthParameter>> getPatientLatestParameters() {
        return patientLatestParameters;
    }
    
    public LiveData<List<Threshold>> getPatientThresholds() {
        return patientThresholds;
    }
    
    public LiveData<List<Threshold>> getThresholds() {
        return thresholds;
    }
    
    public LiveData<List<Recommendation>> getRecommendations() {
        return recommendations;
    }
    
    public LiveData<Boolean> getRecommendationCreated() {
        return recommendationCreated;
    }
    
    public void resetRecommendationCreated() {
        recommendationCreated.postValue(false);
    }
    
    public LiveData<Boolean> getLoading() {
        return loading;
    }
    
    public LiveData<String> getError() {
        return error;
    }
    
    // UI state classes for handling different states
    public static abstract class UiState {
        public static class Loading extends UiState {}
        
        public static class Success extends UiState {
            private final String message;
            
            public Success(String message) {
                this.message = message;
            }
            
            public String getMessage() {
                return message;
            }
        }
        
        public static class Error extends UiState {
            private final String message;
            
            public Error(String message) {
                this.message = message;
            }
            
            public String getMessage() {
                return message;
            }
        }
    }
} 