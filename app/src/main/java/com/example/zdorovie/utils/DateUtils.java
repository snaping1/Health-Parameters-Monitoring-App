package com.example.zdorovie.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Вспомогательные функции для форматирования и разбора дат
public class DateUtils {
    
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private static final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
    
    // Приватный конструктор для предотвращения создания экземпляров
    private DateUtils() {
        // Пустой приватный конструктор
    }

    public static String formatDate(Date date) {
        return dateFormatter.format(date);
    }

    public static String formatDateTime(Date date) {
        return dateTimeFormatter.format(date);
    }

    public static String formatDate(long timestamp) {
        return formatDate(new Date(timestamp));
    }

    public static String formatDateTime(long timestamp) {
        return formatDateTime(new Date(timestamp));
    }

    public static Date parseDate(String dateString) {
        try {
            return dateFormatter.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public static Date parseDateTime(String dateTimeString) {
        try {
            return dateTimeFormatter.parse(dateTimeString);
        } catch (ParseException e) {
            return null;
        }
    }
} 