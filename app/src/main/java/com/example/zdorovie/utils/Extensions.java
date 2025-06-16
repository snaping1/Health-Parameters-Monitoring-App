package com.example.zdorovie.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Extensions {

    // Приватный конструктор для предотвращения создания экземпляров
    private Extensions() {
        // Empty private constructor
    }

    public static void showToast(Fragment fragment, String message) {
        Toast.makeText(fragment.requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View view, String message) {
        showSnackbar(view, message, Snackbar.LENGTH_SHORT);
    }

    public static void showSnackbar(View view, String message, int duration) {
        Snackbar.make(view, message, duration).show();
    }

    public static String toDateString(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    public static String toTimeString(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    public static String toDateTimeString(long timestamp) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return formatter.format(new Date(timestamp));
    }

    public static void showDatePicker(Context context, TriConsumer<Integer, Integer, Integer> onDateSelected) {
        showDatePicker(context, Calendar.getInstance(), onDateSelected);
    }

    public static void showDatePicker(Context context, Calendar initialDate,
                                    TriConsumer<Integer, Integer, Integer> onDateSelected) {
        DatePickerDialog dialog = new DatePickerDialog(
            context,
            (view, year, month, dayOfMonth) -> onDateSelected.accept(year, month, dayOfMonth),
            initialDate.get(Calendar.YEAR),
            initialDate.get(Calendar.MONTH),
            initialDate.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    public static void showTimePicker(Context context, BiConsumer<Integer, Integer> onTimeSelected) {
        showTimePicker(context, Calendar.getInstance(), onTimeSelected);
    }

    public static void showTimePicker(Context context, Calendar initialTime,
                                   BiConsumer<Integer, Integer> onTimeSelected) {
        TimePickerDialog dialog = new TimePickerDialog(
            context,
            (view, hourOfDay, minute) -> onTimeSelected.accept(hourOfDay, minute),
            initialTime.get(Calendar.HOUR_OF_DAY),
            initialTime.get(Calendar.MINUTE),
            true
        );
        dialog.show();
    }

    public static boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
} 