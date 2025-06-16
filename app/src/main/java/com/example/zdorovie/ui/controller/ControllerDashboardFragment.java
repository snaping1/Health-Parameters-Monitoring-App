package com.example.zdorovie.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentControllerDashboardBinding;
import com.example.zdorovie.model.Recommendation;
import com.example.zdorovie.ui.common.RecommendationsAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.ControllerViewModel;

import java.util.ArrayList;

public class ControllerDashboardFragment extends Fragment {
    
    private FragmentControllerDashboardBinding binding;
    private ControllerViewModel controllerViewModel;
    private RecommendationsAdapter recommendationsAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentControllerDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Используйте ViewModel в области действия для обмена данными между фрагментами
        controllerViewModel = new ViewModelProvider(requireActivity()).get(ControllerViewModel.class);
        
        setupRecyclerViews();
        setupClickListeners();
        observeData();
        
        // Загрузка данных панели мониторинга
        controllerViewModel.loadDashboardData();
        // загружать рекомендации
        controllerViewModel.loadRecommendations();
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновите данные при возвращении к этому фрагменту
        controllerViewModel.loadRecommendations();
    }
    
    private void setupRecyclerViews() {
        // Setup recommendations adapter
        recommendationsAdapter = new RecommendationsAdapter(recommendation -> 
            ViewUtils.showToast(requireContext(), "Рекомендация: " + recommendation.getTitle())
        );
        
        binding.rvRecommendations.setAdapter(recommendationsAdapter);
        binding.rvRecommendations.setLayoutManager(
            new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        );
    }
    
    private void setupClickListeners() {
        binding.btnPatientsAll.setOnClickListener(v -> 
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_controllerDashboardFragment_to_patientsListFragment)
        );
        
        binding.btnRecommendationsAll.setOnClickListener(v -> 
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_controllerDashboardFragment_to_controllerRecommendationsFragment)
        );
    }
    
    private void observeData() {
        controllerViewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            // Создайте новый ArrayList, чтобы адаптер мог обнаруживать изменения
            recommendationsAdapter.submitList(new ArrayList<>(recommendations));
            
            // Журнал для отладки
            System.out.println("ControllerDashboardFragment: Received " + recommendations.size() + " recommendations");
            
            if (recommendations.isEmpty()) {
                binding.tvEmptyRecommendations.setVisibility(View.VISIBLE);
                binding.rvRecommendations.setVisibility(View.GONE);
            } else {
                binding.tvEmptyRecommendations.setVisibility(View.GONE);
                binding.rvRecommendations.setVisibility(View.VISIBLE);
            }
        });
        
        controllerViewModel.getPatients().observe(getViewLifecycleOwner(), patients -> 
            binding.tvPatientCount.setText(String.valueOf(patients.size()))
        );
        
        controllerViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> 
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE)
        );
        
        controllerViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                ViewUtils.showToast(requireContext(), errorMessage);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 