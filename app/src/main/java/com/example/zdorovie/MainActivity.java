package com.example.zdorovie;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.example.zdorovie.databinding.ActivityMainBinding;
import com.example.zdorovie.model.User;
import com.example.zdorovie.viewmodel.AuthViewModel;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {
    
    private ActivityMainBinding binding;
    private NavController navController;
    private AuthViewModel authViewModel;
    private User.UserRole currentUserRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Инициализация Firebase
        FirebaseApp.initializeApp(this);

        //Привязка layout'а через View Binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Инициализация ViewModel для отслеживания состояния авторизации
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Установка начальной навигации
        setupInitialNavigation();
        observeUserState();
    }
    
    private void setupInitialNavigation() {
        // Получение NavController через NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
            .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Загрузка основного навигационного графа (экранов авторизации)
            navController.setGraph(R.navigation.nav_graph);

            // Скрытие нижнего меню до входа пользователя
            binding.bottomNavigation.setVisibility(View.GONE);
        }
    }
    
    private void observeUserState() {
        // Подписка на состояние авторизации
        authViewModel.getAuthState().observe(this, authState -> {
            if (authState instanceof AuthViewModel.AuthState.Authenticated) {
                // When user is authenticated, check their role
                User user = authViewModel.getCurrentUser().getValue();
                if (user != null && (currentUserRole == null || currentUserRole != user.getRole())) {
                    currentUserRole = user.getRole();
                    setupUserSpecificNavigation(user);
                }
            } else if (authState instanceof AuthViewModel.AuthState.Unauthenticated) {
                if (currentUserRole != null) {
                    // Пользователь вышел — сброс роли и переход к экранам авторизации
                    currentUserRole = null;
                    setupAuthNavigation();
                }
            }
            // Игнорировать другие состояния
        });
        
        // Дополнительная подписка на объект пользователя
        authViewModel.getCurrentUser().observe(this, user -> {
            if (user != null && (currentUserRole == null || currentUserRole != user.getRole())) {
                //  Новый пользователь или изменилась роль
                currentUserRole = user.getRole();
                setupUserSpecificNavigation(user);
            } else if (user == null && currentUserRole != null) {
                // UПользователь вышел — сброс
                currentUserRole = null;
                setupAuthNavigation();
            }
        });
    }
    
    private void setupUserSpecificNavigation(User user) {
        //Настройка навигации в зависимости от роли пользователя
        if (user.getRole() == User.UserRole.PATIENT) {
            setupPatientNavigation();
        } else if (user.getRole() == User.UserRole.CONTROLLER) {
            setupControllerNavigation();
        }
    }
    
    private void setupPatientNavigation() {
        // Загрузка графа для пациента
        navController.setGraph(R.navigation.patient_nav_graph);
        
        // Настройка нижнего меню для пациента
        binding.bottomNavigation.getMenu().clear();
        getMenuInflater().inflate(R.menu.patient_bottom_navigation, binding.bottomNavigation.getMenu());
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        
        // Отображение нижнего меню
        binding.bottomNavigation.setVisibility(View.VISIBLE);

        setupDestinationListener();
    }
    
    private void setupControllerNavigation() {
        // Загрузка графа для контролера
        navController.setGraph(R.navigation.controller_nav_graph);
        
        // Настройка нижнего меню для контролера
        binding.bottomNavigation.getMenu().clear();
        getMenuInflater().inflate(R.menu.controller_bottom_navigation, binding.bottomNavigation.getMenu());
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController);
        
        // Отображение нижнего меню
        binding.bottomNavigation.setVisibility(View.VISIBLE);
        
        setupDestinationListener();
    }
    
    private void setupAuthNavigation() {
        // Навигация к экрану входа, если пользователь вышел
        try {
            if (navController.getCurrentDestination() != null && 
                navController.getCurrentDestination().getId() != R.id.loginFragment) {
                navController.navigate(R.id.loginFragment);
            }
        } catch (Exception e) {
            // В случае ошибки — загрузка графа авторизации
            navController.setGraph(R.navigation.nav_graph);
        }
        
        // Скрытие нижнего меню
        binding.bottomNavigation.setVisibility(View.GONE);
    }
    
    private void setupDestinationListener() {
        // Удаление старых Listener (если были)
        navController.removeOnDestinationChangedListener(destinationChangedListener);
        
        // Добавление нового Listener
        navController.addOnDestinationChangedListener(destinationChangedListener);
    }

    // изменения текущего экрана
    private final NavController.OnDestinationChangedListener destinationChangedListener = 
        new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller,
                                           @NonNull NavDestination destination,
                                           @Nullable Bundle arguments) {
                int destinationId = destination.getId();
                // Скрытие нижнего меню на определённых экранах
                if (destinationId == R.id.loginFragment || 
                    destinationId == R.id.registerFragment ||
                    destinationId == R.id.addParameterFragment ||
                    destinationId == R.id.patientDetailsFragment ||
                    destinationId == R.id.createRecommendationFragment) {
                    binding.bottomNavigation.setVisibility(View.GONE);
                } else {
                    // Отображение нижнего меню, если пользователь авторизован
                    if (currentUserRole != null) {
                        binding.bottomNavigation.setVisibility(View.VISIBLE);
                    }
                }
            }
        };
} 