package com.example.zdorovie.ui.controller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import com.example.zdorovie.databinding.FragmentCreateRecommendationBinding;
import com.example.zdorovie.model.Recommendation;
import com.example.zdorovie.model.User;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.ControllerViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CreateRecommendationFragment extends Fragment {
    
    private FragmentCreateRecommendationBinding binding;
    private ControllerViewModel controllerViewModel;
    private final List<User> patientsList = new ArrayList<>();
    private String selectedPatientId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCreateRecommendationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Use the activity-scoped ViewModel
        controllerViewModel = new ViewModelProvider(requireActivity()).get(ControllerViewModel.class);
        
        setupPatientSpinner();
        setupClickListeners();
        observeData();
        
        // Загружаем список пациентов
        controllerViewModel.loadPatients();
    }
    
    private void setupPatientSpinner() {
        binding.spinnerPatient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position >= 0 && position < patientsList.size()) {
                    selectedPatientId = patientsList.get(position).getId();
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedPatientId = null;
            }
        });
    }
    
    private void setupClickListeners() {
        binding.btnCreate.setOnClickListener(v -> createRecommendation());
        
        binding.btnCancel.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).navigateUp()
        );
    }
    
    private void createRecommendation() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etMessage.getText().toString().trim();
        
        if (title.isEmpty()) {
            binding.etTitle.setError("Введите заголовок");
            return;
        }
        
        if (content.isEmpty()) {
            binding.etMessage.setError("Введите текст рекомендации");
            return;
        }
        
        if (selectedPatientId == null) {
            ViewUtils.showToast(requireContext(), "Выберите пациента");
            return;
        }
        
        // Использование шаблона построителя, чтобы убедиться, что все поля настроены правильно
        Recommendation recommendation = new Recommendation.Builder()
            .patientId(selectedPatientId)
            .title(title)
            .content(content)
            .isRead(false)
            .createdAt(System.currentTimeMillis())
            .build();
        
        // Запись рекомендации по отладке в журнал
        System.out.println("Creating recommendation: " + recommendation);
        
        controllerViewModel.createRecommendation(recommendation);
    }
    
    private void observeData() {
        controllerViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnCreate.setEnabled(!isLoading);
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });
        
        controllerViewModel.getError().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                ViewUtils.showToast(requireContext(), errorMessage);
            }
        });
        
        controllerViewModel.getRecommendationCreated().observe(getViewLifecycleOwner(), created -> {
            if (created) {
                ViewUtils.showToast(requireContext(), "Рекомендация создана успешно");
                NavHostFragment.findNavController(this).navigateUp();
            }
        });
        
        controllerViewModel.getPatients().observe(getViewLifecycleOwner(), patients -> {
            patientsList.clear();
            patientsList.addAll(patients);
            
            // Создаем адаптер для спиннера с именами пациентов
            List<String> patientNames = patients.stream()
                .map(patient -> patient.getName() + " (" + patient.getEmail() + ")")
                .collect(Collectors.toList());
                
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), 
                android.R.layout.simple_spinner_item, 
                patientNames
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerPatient.setAdapter(adapter);
            
            // Если был выбран пациент ранее, восстанавливаем выбор
            if (selectedPatientId != null) {
                for (int i = 0; i < patientsList.size(); i++) {
                    if (patientsList.get(i).getId().equals(selectedPatientId)) {
                        binding.spinnerPatient.setSelection(i);
                        break;
                    }
                }
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 