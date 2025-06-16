package com.example.zdorovie.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentDashboardBinding;
import com.example.zdorovie.ui.common.LatestParametersAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.PatientViewModel;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {
    
    private FragmentDashboardBinding binding;
    private PatientViewModel patientViewModel;
    private LatestParametersAdapter latestParametersAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        
        setupRecyclerView();
        setupClickListeners();
        observeData();
        
        // Начало периодической проверки рекомендаций
        patientViewModel.setupAutomaticRecommendationRefresh();
    }
    
    private void setupRecyclerView() {
        latestParametersAdapter = new LatestParametersAdapter(parameterType -> {
            // Перейти к добавлению параметра с заранее выбранным типом.
            navigateToAddParameter();
        });
        
        binding.rvLatestParameters.setAdapter(latestParametersAdapter);
        binding.rvLatestParameters.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void setupClickListeners() {
        binding.fabAddParameter.setOnClickListener(v -> navigateToAddParameter());
        
        binding.btnQuickMeasure.setOnClickListener(v -> {
            // Быстрое измерение для самого распространённого параметра
            navigateToAddParameter();
        });
        
        // Находим кнопку рекомендаций через родительский FrameLayout
        View recommendationsButton = binding.getRoot().findViewById(R.id.btn_recommendations);
        if (recommendationsButton != null) {
            recommendationsButton.setOnClickListener(v -> navigateToRecommendations());
        }
    }
    
    private void navigateToAddParameter() {
        try {
            NavController navController = NavHostFragment.findNavController(this);
            int currentDestination = navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId() : 0;
                
            int actionId = R.id.action_dashboardFragment_to_addParameterFragment; // Default action
            
            // Заменить оператор switch на условные операторы if-else
            if (currentDestination == R.id.dashboardFragment) {
                actionId = R.id.action_dashboardFragment_to_addParameterFragment;
            } else if (currentDestination == R.id.parametersFragment) {
                actionId = R.id.action_parametersFragment_to_addParameterFragment;
            } else if (currentDestination == R.id.remindersFragment) {
                actionId = R.id.action_remindersFragment_to_addParameterFragment;
            } else if (currentDestination == R.id.recommendationsFragment) {
                actionId = R.id.action_recommendationsFragment_to_addParameterFragment;
            }
            
            navController.navigate(actionId);
        } catch (Exception e) {
            // выполнить прямую навигацию к нужному экрану
            try {
                NavHostFragment.findNavController(this).navigate(R.id.addParameterFragment);
            } catch (Exception ex) {
                ViewUtils.showToast(requireContext(), "Ошибка навигации: " + ex.getMessage());
            }
        }
    }
    
    private void navigateToRecommendations() {
        try {
            NavHostFragment.findNavController(this).navigate(R.id.recommendationsFragment);
        } catch (Exception e) {
            ViewUtils.showToast(requireContext(), "Ошибка навигации: " + e.getMessage());
        }
    }
    
    private void observeData() {
        patientViewModel.getLatestParameters().observe(getViewLifecycleOwner(), parametersMap -> {
            latestParametersAdapter.submitList(new ArrayList<>(parametersMap.values()));
            
            if (parametersMap.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvLatestParameters.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvLatestParameters.setVisibility(View.VISIBLE);
            }
        });
        
        patientViewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            long unreadCount = recommendations.stream().filter(r -> !r.isRead()).count();
            if (unreadCount > 0) {
                binding.tvRecommendationsBadge.setVisibility(View.VISIBLE);
                binding.tvRecommendationsBadge.setText(String.valueOf(unreadCount));
            } else {
                binding.tvRecommendationsBadge.setVisibility(View.GONE);
            }
        });
        
        patientViewModel.getController().observe(getViewLifecycleOwner(), controller -> {
            if (controller != null) {
                binding.tvControllerInfo.setText("Контролер: " + controller.getName());
                binding.tvControllerInfo.setVisibility(View.VISIBLE);
            } else {
                binding.tvControllerInfo.setVisibility(View.GONE);
            }
        });
        
        patientViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof PatientViewModel.UiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof PatientViewModel.UiState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                ViewUtils.showToast(requireContext(), ((PatientViewModel.UiState.Success) state).getMessage());
            } else if (state instanceof PatientViewModel.UiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                ViewUtils.showToast(requireContext(), ((PatientViewModel.UiState.Error) state).getMessage());
            }
        });
    }
    
    // Добавить метод для ручного обновления данных
    private void refreshData() {
        // Принудительно обновить рекомендации
        patientViewModel.refreshRecommendations();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновлять данные при появлении фрагмента на экране
        refreshData();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 