package com.example.zdorovie.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.zdorovie.model.HealthParameter;
import com.example.zdorovie.model.Recommendation;
import com.example.zdorovie.model.Reminder;
import com.example.zdorovie.model.User;
import com.example.zdorovie.repository.BaseRepository;
import com.example.zdorovie.repository.HealthParameterRepository;
import com.example.zdorovie.repository.RecommendationRepository;
import com.example.zdorovie.repository.ReminderRepository;
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

public class PatientViewModel extends ViewModel {
    private final UserRepository userRepository = new UserRepository();
    private final HealthParameterRepository healthParameterRepository = new HealthParameterRepository();
    private final ReminderRepository reminderRepository = new ReminderRepository();
    private final RecommendationRepository recommendationRepository = new RecommendationRepository();
    private final ThresholdRepository thresholdRepository = new ThresholdRepository();
    private final Executor executor = Executors.newSingleThreadExecutor();
    
    private final MutableLiveData<List<HealthParameter>> healthParameters = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Map<HealthParameter.ParameterType, HealthParameter>> latestParameters = 
        new MutableLiveData<>(new HashMap<>());
    private final MutableLiveData<List<Reminder>> reminders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Recommendation>> recommendations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<User> controller = new MutableLiveData<>();
    private final MutableLiveData<List<User>> availableControllers = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<UiState> uiState = new MutableLiveData<>();
    
    private String currentUserId;
    
    public PatientViewModel() {
        loadUserData();
    }
    
    private void loadUserData() {
        CompletableFuture.runAsync(() -> {
            try {
                currentUserId = userRepository.getCurrentUserId();
                if (currentUserId != null) {
                    loadHealthParameters(currentUserId);
                    loadReminders(currentUserId);
                    loadRecommendations(currentUserId);
                    loadController();
                }
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(e.getMessage()));
            }
        }, executor);
    }
    
    private void loadHealthParameters(String userId) {
        CompletableFuture.runAsync(() -> {
            try {
                // Загрузить все параметры здоровья
                CompletableFuture<BaseRepository.Result<List<HealthParameter>>> parametersFuture =
                    healthParameterRepository.getParametersByUser(userId);
                parametersFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        healthParameters.postValue(result.getData());
                    }
                });
                
                // Загрузить последние параметры по типу
                healthParameterRepository.observeLatestParametersByType(
                    userId, 
                    latestMap -> latestParameters.postValue(latestMap),
                    e -> uiState.postValue(new UiState.Error("Ошибка при получении параметров: " + e.getMessage()))
                );
            } catch (Exception e) {
                uiState.postValue(new UiState.Error("Ошибка загрузки параметров здоровья: " + e.getMessage()));
            }
        }, executor);
    }
    
    private void loadReminders(String userId) {
        CompletableFuture.runAsync(() -> {
            try {
                CompletableFuture<BaseRepository.Result<List<Reminder>>> remindersFuture =
                    reminderRepository.getRemindersByUser(userId);
                remindersFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        reminders.postValue(result.getData());
                    }
                });
            } catch (Exception e) {
                uiState.postValue(new UiState.Error("Ошибка загрузки напоминаний: " + e.getMessage()));
            }
        }, executor);
    }
    
    private void loadRecommendations(String userId) {
        CompletableFuture.runAsync(() -> {
            try {
                CompletableFuture<BaseRepository.Result<List<Recommendation>>> recommendationsFuture = 
                    recommendationRepository.getRecommendationsByPatient(userId);
                recommendationsFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        // Записать в лог количество полученных рекомендаций
                        List<Recommendation> fetchedRecommendations = result.getData();
                        System.out.println("PatientViewModel: Loaded " + fetchedRecommendations.size() + " recommendations");
                        
                        // Опубликовать все рекомендации в LiveData
                        recommendations.postValue(fetchedRecommendations);
                    } else {
                        // Установить пустой список, если произошла ошибка
                        recommendations.postValue(new ArrayList<>());
                        uiState.postValue(new UiState.Error("Не удалось загрузить рекомендации"));
                    }
                });
            } catch (Exception e) {
                recommendations.postValue(new ArrayList<>());
                uiState.postValue(new UiState.Error("Ошибка загрузки рекомендаций: " + e.getMessage()));
            }
        }, executor);
    }
    
    private void loadController() {
        CompletableFuture.runAsync(() -> {
            try {
                CompletableFuture<BaseRepository.Result<User>> userFuture = userRepository.getCurrentUser();
                userFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        User user = result.getData();
                        if (user.getControllerId() != null) {
                            loadControllerById(user.getControllerId());
                        }
                    }
                });
            } catch (Exception e) {
                uiState.postValue(new UiState.Error("Ошибка загрузки данных пользователя: " + e.getMessage()));
            }
        }, executor);
    }
    
    public void addHealthParameter(HealthParameter.ParameterType type, double value, String notes) {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            if (currentUserId != null) {
                try {
                    // Проверить, находится ли значение в нормальном диапазоне
                    boolean isNormal = value >= type.getNormalMin() && value <= type.getNormalMax();
                    
                    HealthParameter parameter = new HealthParameter(
                        null, // ID будет сгенерирован
                        currentUserId,
                        type,
                        value,
                        type.getUnit(),
                        System.currentTimeMillis(), // текущее время
                        isNormal,
                        notes
                    );
                    
                    healthParameterRepository.addParameter(parameter);
                    
                    // Проверить превышение порога
                    CompletableFuture<BaseRepository.Result<Boolean>> violationFuture =
                        thresholdRepository.checkThresholdViolation(currentUserId, parameter);
                    violationFuture.thenAccept(result -> {
                        boolean isViolation = result.isSuccess() && result.getData() != null && result.getData();
                        
                        if (isViolation) {
                            uiState.postValue(new UiState.Success(
                                "Параметр сохранен. Внимание: значение вышло за установленные пределы!"));
                        } else {
                            uiState.postValue(new UiState.Success("Параметр успешно сохранен"));
                        }
                    });
                } catch (Exception e) {
                    uiState.postValue(new UiState.Error(
                        e.getMessage() != null ? e.getMessage() : "Ошибка сохранения параметра"));
                }
            }
        }, executor);
    }
    
    public void addReminder(HealthParameter.ParameterType parameterType, String time, List<Integer> repeatDays) {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            if (currentUserId != null) {
                try {
                    // Использовать правильный конструктор со всеми необходимыми параметрами
                    Reminder reminder = new Reminder(
                        null, // id will be generated
                        currentUserId,
                        "", // title
                        "", // content
                        System.currentTimeMillis(), // createdAt
                        false, // not a text reminder
                        parameterType,
                        time,
                        true, // active by default
                        repeatDays
                    );
                    
                    reminderRepository.addReminder(reminder)
                        .thenAccept(result -> {
                            // Обновить список напоминаний после добавления
                            loadReminders(currentUserId);
                            uiState.postValue(new UiState.Success("Напоминание создано"));
                        });
                } catch (Exception e) {
                    uiState.postValue(new UiState.Error(
                        e.getMessage() != null ? e.getMessage() : "Ошибка создания напоминания"));
                }
            }
        }, executor);
    }
    
    public void toggleReminder(String reminderId, boolean isActive) {
        CompletableFuture.runAsync(() -> {
            try {
                reminderRepository.toggleReminder(reminderId, isActive);
                uiState.postValue(new UiState.Success("Напоминание обновлено"));
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(
                    e.getMessage() != null ? e.getMessage() : "Ошибка обновления напоминания"));
            }
        }, executor);
    }
    
    public void deleteReminder(String reminderId) {
        CompletableFuture.runAsync(() -> {
            try {
                reminderRepository.deleteReminder(reminderId);
                uiState.postValue(new UiState.Success("Напоминание удалено"));
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(
                    e.getMessage() != null ? e.getMessage() : "Ошибка удаления напоминания"));
            }
        }, executor);
    }
    
    public void markRecommendationAsRead(String recommendationId) {
        CompletableFuture.runAsync(() -> {
            try {
                recommendationRepository.markAsRead(recommendationId);
                // Нет необходимости показывать уведомление пользователю
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(
                    e.getMessage() != null ? e.getMessage() : "Ошибка обновления рекомендации"));
            }
        }, executor);
    }
    
    public void loadAvailableControllers() {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            try {
                CompletableFuture<BaseRepository.Result<List<User>>> controllersFuture = userRepository.getAllControllers();
                controllersFuture.thenAccept(result -> {
                    if (result.isSuccess() && result.getData() != null) {
                        availableControllers.postValue(result.getData());
                        uiState.postValue(new UiState.Success("Список контролеров загружен"));
                    } else {
                        uiState.postValue(new UiState.Error("Не удалось загрузить список контролеров"));
                    }
                });
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(
                    e.getMessage() != null ? e.getMessage() : "Ошибка загрузки контролеров"));
            }
        }, executor);
    }
    
    public void assignController(String controllerId) {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            try {
                if (currentUserId != null) {
                    CompletableFuture<BaseRepository.Result<Void>> assignFuture =
                        userRepository.assignController(currentUserId, controllerId);
                    
                    assignFuture.thenAccept(result -> {
                        if (result.isSuccess()) {
                            loadControllerById(controllerId);
                            uiState.postValue(new UiState.Success("Контролер назначен"));
                        } else {
                            uiState.postValue(new UiState.Error("Не удалось назначить контролера"));
                        }
                    });
                }
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(
                    e.getMessage() != null ? e.getMessage() : "Ошибка назначения контролера"));
            }
        }, executor);
    }
    
    private void loadControllerById(String controllerId) {
        CompletableFuture.runAsync(() -> {
            try {
                CompletableFuture<BaseRepository.Result<User>> controllerFuture = userRepository.getUserById(controllerId);
                controllerFuture.thenAccept(result -> {
                    if (result.isSuccess()) {
                        controller.postValue(result.getData());
                    }
                });
            } catch (Exception e) {
                uiState.postValue(new UiState.Error("Ошибка загрузки данных контролера: " + e.getMessage()));
            }
        }, executor);
    }
    
    public void addTextReminder(String title, String content, String time, List<Integer> repeatDays) {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            if (currentUserId != null) {
                try {
                    // Создать текстовое напоминание с помощью полного конструктора
                    Reminder reminder = new Reminder(
                        null, // id will be generated
                        currentUserId,
                        title,
                        content,
                        System.currentTimeMillis(), // createdAt
                        true, // isTextReminder
                        null, // parameterType (null for text reminders)
                        time,
                        true, // active by default
                        repeatDays != null ? repeatDays : new ArrayList<>()
                    );
                    
                    reminderRepository.addReminder(reminder)
                        .thenAccept(result -> {
                            // Перезагрузить напоминания после добавления нового
                            loadReminders(currentUserId);
                            uiState.postValue(new UiState.Success("Напоминание создано"));
                        });
                } catch (Exception e) {
                    uiState.postValue(new UiState.Error(
                        e.getMessage() != null ? e.getMessage() : "Ошибка создания напоминания"));
                }
            }
        }, executor);
    }
    
    public void addSimpleReminder(String title, String content) {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            if (currentUserId != null) {
                try {
                    //Создать простое напоминание с полным конструктором и значениями по умолчанию
                    Reminder reminder = new Reminder(
                        null, // id will be generated
                        currentUserId,
                        title,
                        content,
                        System.currentTimeMillis(), // createdAt
                        true, // isTextReminder
                        null, // parameterType (null for text reminders)
                        "", // empty time for simple reminders
                        true, // active by default
                        new ArrayList<>() // empty repeat days for simple reminders
                    );
                    
                    reminderRepository.addReminder(reminder)
                        .thenAccept(result -> {
                            // Перезагрузить напоминания после добавления нового
                            loadReminders(currentUserId);
                            uiState.postValue(new UiState.Success("Напоминание создано"));
                        });
                } catch (Exception e) {
                    uiState.postValue(new UiState.Error(
                        e.getMessage() != null ? e.getMessage() : "Ошибка создания напоминания"));
                }
            }
        }, executor);
    }
    
    public void updateReminder(String reminderId, String title, String content) {
        CompletableFuture.runAsync(() -> {
            uiState.postValue(new UiState.Loading());
            
            try {
                reminderRepository.updateTextReminder(reminderId, title, content);
                uiState.postValue(new UiState.Success("Напоминание обновлено"));
            } catch (Exception e) {
                uiState.postValue(new UiState.Error(
                    e.getMessage() != null ? e.getMessage() : "Ошибка обновления напоминания"));
            }
        }, executor);
    }
    
    // Getters for LiveData
    public LiveData<List<HealthParameter>> getHealthParameters() {
        return healthParameters;
    }
    
    public LiveData<Map<HealthParameter.ParameterType, HealthParameter>> getLatestParameters() {
        return latestParameters;
    }
    
    public LiveData<List<Reminder>> getReminders() {
        return reminders;
    }
    
    public LiveData<List<Recommendation>> getRecommendations() {
        return recommendations;
    }
    
    public LiveData<User> getController() {
        return controller;
    }
    
    public LiveData<List<User>> getAvailableControllers() {
        return availableControllers;
    }
    
    public LiveData<UiState> getUiState() {
        return uiState;
    }
    
    // Настроить автоматическое периодическое обновление рекомендаций
    public void setupAutomaticRecommendationRefresh() {
        if (currentUserId != null) {
            // Настроить таймер для периодической проверки новых рекомендаций
            CompletableFuture.runAsync(() -> {
                try {
                    // Начальная загрузка
                    loadRecommendations(currentUserId);
                    
                    // Настроить периодическую проверку
                    Thread.sleep(10000); // 10 seconds
                    setupAutomaticRecommendationRefresh(); // Рекурсивно вызвать для создания цикла
                } catch (InterruptedException e) {
                    // Поток был прерван, ничего делать не нужно
                }
            }, executor);
        }
    }
    
    // Переопределить onCleared для очистки ресурсов
    @Override
    protected void onCleared() {
        super.onCleared();
        // Дополнительная очистка при необходимости
    }
    
    public void refreshRecommendations() {
        if (currentUserId != null) {
            loadRecommendations(currentUserId);
        }
    }
    
    // Состояние UI для обработки состояний загрузки, успеха и ошибки
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