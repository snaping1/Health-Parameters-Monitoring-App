package com.example.zdorovie.repository;

public class Result<T> {
    private final T data;
    private final Exception error;
    private final boolean success;

    private Result(T data, Exception error, boolean success) {
        this.data = data;
        this.error = error;
        this.success = success;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(data, null, true);
    }

    public static <T> Result<T> failure(Exception error) {
        return new Result<>(null, error, false);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public Exception getError() {
        return error;
    }
} 