package com.example.zdorovie.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

// Абстрактный базовый класс для всех репозиториев, работающих с Firestore.
public abstract class BaseRepository {
    protected final FirebaseFirestore firestore;

    // Конструктор инициализирует подключение к Firestore.
    public BaseRepository() {
        this.firestore = FirebaseFirestore.getInstance();
    }

    protected <T> CompletableFuture<T> safeTask(Task<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        
        task.addOnSuccessListener(future::complete)
            .addOnFailureListener(future::completeExceptionally);
            
        return future;
    }

    // Безопасно выполняет действие репозитория и возвращает результат в виде Result
    protected <T> Result<T> safeCall(RepositoryAction<T> action) {
        try {
            T result = action.execute();
            return Result.success(result);
        } catch (Exception e) {
            return Result.failure(e);
        }
    }

    // Безопасно ожидает завершения CompletableFuture и возвращает результат в виде Result
    protected <T> Result<T> safeAwait(CompletableFuture<T> future) {
        try {
            T result = future.get(); // Blocking call
            return Result.success(result);
        } catch (InterruptedException | ExecutionException e) {
            return Result.failure(e);
        }
    }

    // Функциональный интерфейс для действий репозитория, которые могут выбрасывать исключения
    protected interface RepositoryAction<T> {
        T execute() throws Exception;
    }

    // Класс-обертка для результатов операций репозитория
    public static class Result<T> {
        private final T data; // Результат успешной операции
        private final Exception exception; // Исключение при ошибке
        private final boolean isSuccess; // Флаг успешности операции
        
        private Result(T data, Exception exception, boolean isSuccess) {
            this.data = data;
            this.exception = exception;
            this.isSuccess = isSuccess;
        }

        // Создает успешный результат с данными.
        public static <T> Result<T> success(T data) {
            return new Result<>(data, null, true);
        }

        // Создает результат с ошибкой.
        public static <T> Result<T> failure(Exception exception) {
            return new Result<>(null, exception, false);
        }
        
        public T getData() {
            return data;
        }
        
        public Exception getException() {
            return exception;
        }
        
        public boolean isSuccess() {
            return isSuccess;
        }
        
        public boolean isFailure() {
            return !isSuccess;
        }
    }
} 