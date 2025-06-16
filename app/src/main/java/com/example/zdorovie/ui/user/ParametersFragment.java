package com.example.zdorovie.ui.user;

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
import com.example.zdorovie.databinding.FragmentParametersBinding;
import com.example.zdorovie.ui.common.LatestParametersAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.PatientViewModel;

import java.util.ArrayList;

public class ParametersFragment extends Fragment {
    
    private FragmentParametersBinding binding;
    private PatientViewModel patientViewModel;
    private LatestParametersAdapter parametersAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentParametersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        
        setupRecyclerView();
        setupClickListeners();
        observeData();
    }
    
    private void setupRecyclerView() {
        parametersAdapter = new LatestParametersAdapter(parameterType -> {
            // Перейти к добавлению параметра с заранее выбранным типом
            navigateToAddParameter();
        });
        
        binding.rvParameters.setAdapter(parametersAdapter);
        binding.rvParameters.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void setupClickListeners() {
        binding.fabAddParameter.setOnClickListener(v -> navigateToAddParameter());
    }
    
    private void navigateToAddParameter() {
        try {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_parametersFragment_to_addParameterFragment);
        } catch (Exception e) {
            try {
                NavHostFragment.findNavController(this).navigate(R.id.addParameterFragment);
            } catch (Exception ex) {
                ViewUtils.showToast(requireContext(), "Ошибка навигации: " + ex.getMessage());
            }
        }
    }
    
    private void observeData() {
        patientViewModel.getLatestParameters().observe(getViewLifecycleOwner(), parametersMap -> {
            parametersAdapter.submitList(new ArrayList<>(parametersMap.values()));
            
            if (parametersMap.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvParameters.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvParameters.setVisibility(View.VISIBLE);
            }
        });
        
        patientViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof PatientViewModel.UiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof PatientViewModel.UiState.Success) {
                binding.progressBar.setVisibility(View.GONE);
            } else if (state instanceof PatientViewModel.UiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                ViewUtils.showToast(requireContext(), ((PatientViewModel.UiState.Error) state).getMessage());
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 