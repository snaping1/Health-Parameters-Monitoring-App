package com.example.zdorovie.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.zdorovie.databinding.FragmentThresholdsBinding;
import com.example.zdorovie.model.Threshold;
import com.example.zdorovie.ui.common.ThresholdsAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.ControllerViewModel;

public class ThresholdsFragment extends Fragment {
    
    private FragmentThresholdsBinding binding;
    private ControllerViewModel controllerViewModel;
    private ThresholdsAdapter thresholdsAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentThresholdsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        controllerViewModel = new ViewModelProvider(this).get(ControllerViewModel.class);
        
        setupRecyclerView();
        observeData();

        controllerViewModel.loadThresholds();
    }
    
    private void setupRecyclerView() {
        thresholdsAdapter = new ThresholdsAdapter(threshold -> {
            // Диалоговое окно редактирования порога
            showEditThresholdDialog(threshold);
        });
        
        binding.rvThresholds.setAdapter(thresholdsAdapter);
        binding.rvThresholds.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void showEditThresholdDialog(Threshold threshold) {
        // Here you would show a dialog to edit min/max values
        // For example:
        /*
        ThresholdEditDialog dialog = new ThresholdEditDialog(requireContext(), threshold, 
            updatedThreshold -> controllerViewModel.updateThreshold(updatedThreshold));
        dialog.show();
        */
        
        // For now, just show a toast
        ViewUtils.showToast(requireContext(), 
            "Редактирование порогового значения: " + threshold.getParameterType().getDisplayName());
    }
    
    private void observeData() {
        controllerViewModel.getThresholds().observe(getViewLifecycleOwner(), thresholds -> {
            thresholdsAdapter.submitList(thresholds);
            
            if (thresholds.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvThresholds.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvThresholds.setVisibility(View.VISIBLE);
            }
        });
        
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