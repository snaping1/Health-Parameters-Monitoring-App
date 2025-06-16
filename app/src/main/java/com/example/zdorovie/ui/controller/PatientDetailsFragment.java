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
import com.example.zdorovie.databinding.FragmentPatientDetailsBinding;
import com.example.zdorovie.ui.common.LatestParametersAdapter;
import com.example.zdorovie.utils.DateUtils;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.ControllerViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PatientDetailsFragment extends Fragment {
    
    private FragmentPatientDetailsBinding binding;
    private ControllerViewModel controllerViewModel;
    private LatestParametersAdapter parametersAdapter;
    private String patientId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientDetailsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        controllerViewModel = new ViewModelProvider(this).get(ControllerViewModel.class);
        
        // Get patient ID from arguments
        Bundle args = getArguments();
        if (args != null) {
            patientId = args.getString("patientId", "");
            if (!patientId.isEmpty()) {
                setupRecyclerView();
                observeData();
                
                // Load patient details
                controllerViewModel.loadPatientDetails(patientId);
            } else {
                ViewUtils.showToast(requireContext(), "Ошибка: ID пациента не указан");
                requireActivity().onBackPressed();
            }
        } else {
            ViewUtils.showToast(requireContext(), "Ошибка: аргументы не переданы");
            requireActivity().onBackPressed();
        }
    }
    
    private void setupRecyclerView() {
        parametersAdapter = new LatestParametersAdapter(parameter -> {
            // При необходимости нажмите кнопку обработать параметр
        });
        
        binding.rvParameters.setAdapter(parametersAdapter);
        binding.rvParameters.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void observeData() {
        controllerViewModel.getSelectedPatient().observe(getViewLifecycleOwner(), patient -> {
            if (patient != null) {
                binding.tvPatientName.setText(patient.getName());
                binding.tvPatientEmail.setText(patient.getEmail());
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(new Date(String.valueOf(patient.getRegistrationDate())));
                binding.tvRegistrationDate.setText("Регистрация: " + formattedDate);
            }
        });
        
        controllerViewModel.getPatientParameters().observe(getViewLifecycleOwner(), parameters -> {
            parametersAdapter.submitList(new ArrayList<>(parameters.values()));
            
            if (parameters.isEmpty()) {
                binding.tvEmptyParameters.setVisibility(View.VISIBLE);
                binding.rvParameters.setVisibility(View.GONE);
            } else {
                binding.tvEmptyParameters.setVisibility(View.GONE);
                binding.rvParameters.setVisibility(View.VISIBLE);
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