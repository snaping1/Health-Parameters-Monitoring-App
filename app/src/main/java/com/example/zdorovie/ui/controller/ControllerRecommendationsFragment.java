package com.example.zdorovie.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentControllerRecommendationsBinding;
import com.example.zdorovie.ui.common.RecommendationsAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.ControllerViewModel;

import java.util.ArrayList;

public class ControllerRecommendationsFragment extends Fragment {
    
    private FragmentControllerRecommendationsBinding binding;
    private ControllerViewModel controllerViewModel;
    private RecommendationsAdapter recommendationsAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentControllerRecommendationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        controllerViewModel = new ViewModelProvider(requireActivity()).get(ControllerViewModel.class);
        
        setupRecyclerView();
        setupFab();
        setupSwipeRefresh();
        observeData();
        
        // Рекомендации по нагрузке на начальном этапе
        refreshRecommendations();
    }
    
    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshRecommendations());
    }
    
    private void refreshRecommendations() {
        // Явно принудительно перезагрузить рекомендации
        controllerViewModel.loadRecommendations();
        
        // Остановить обновление индикатора после некоторой задержки
        handler.postDelayed(() -> {
            if (binding != null && binding.swipeRefreshLayout != null && isAdded()) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновить рекомендации, когда фрагмент станет видимым
        refreshRecommendations();
    }
    
    private void setupRecyclerView() {
        recommendationsAdapter = new RecommendationsAdapter(recommendation -> {
            // Обработать рекомендацию, нажав при необходимости
            ViewUtils.showToast(requireContext(), "Рекомендация: " + recommendation.getContent());
        });
        
        binding.rvRecommendations.setAdapter(recommendationsAdapter);
        binding.rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void setupFab() {
        binding.fabAddRecommendation.setOnClickListener(v -> {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_controllerRecommendationsFragment_to_createRecommendationFragment);
        });
    }
    
    private void observeData() {
        controllerViewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            // Создать новый ArrayList, чтобы адаптер распознал изменение
            recommendationsAdapter.submitList(new ArrayList<>(recommendations));
            
            if (recommendations.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvRecommendations.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvRecommendations.setVisibility(View.VISIBLE);
            }
            
            // Log recommendations count for debugging
            System.out.println("ControllerRecommendationsFragment: Received " + recommendations.size() + " recommendations");
        });
        
        controllerViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> 
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );
        
        controllerViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                ViewUtils.showToast(requireContext(), errorMessage);
            }
        });
        
        // Наблюдайте за созданием рекомендаций
        controllerViewModel.getRecommendationCreated().observe(getViewLifecycleOwner(), created -> {
            if (created) {
                // Обновить список, когда будет создана рекомендация
                refreshRecommendations();
                // Сбросить флаг
                controllerViewModel.resetRecommendationCreated();
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Отменить все отложенные операции
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
} 