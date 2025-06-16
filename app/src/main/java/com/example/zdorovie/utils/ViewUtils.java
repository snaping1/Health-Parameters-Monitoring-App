package com.example.zdorovie.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class ViewUtils {
    
    // Приватный конструктор для предотвращения создания экземпляров
    private ViewUtils() {
        // Empty private constructor
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    public static void showToast(Fragment fragment, String message) {
        Context context = fragment.getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showLongToast(Fragment fragment, String message) {
        Context context = fragment.getContext();
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void show(View view) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static void hide(View view) {
        if (view != null) {
            view.setVisibility(View.GONE);
        }
    }

    public static void invisible(View view) {
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public static void toggleVisibility(View view) {
        if (view != null) {
            view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        }
    }
} 