package com.example.zdorovie.ui.user;

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
import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentAddParameterBinding;
import com.example.zdorovie.model.HealthParameter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.PatientViewModel;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddParameterFragment extends Fragment {
    
    private FragmentAddParameterBinding binding;
    private PatientViewModel patientViewModel;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddParameterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        
        setupParameterTypeSpinner();
        setupClickListeners();
        observeViewModel();
    }
    
    private void setupParameterTypeSpinner() {
        // Get display names from all HealthParameter.ParameterType values
        List<String> parameterTypes = Arrays.stream(HealthParameter.ParameterType.values())
            .map(HealthParameter.ParameterType::getDisplayName)
            .collect(Collectors.toList());
            
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_spinner_item, 
            parameterTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerParameterType.setAdapter(adapter);
        
        // Set default selection to blood pressure
        binding.spinnerParameterType.setSelection(0);
        updateParameterInfo(HealthParameter.ParameterType.BLOOD_PRESSURE_SYSTOLIC);
        
        binding.spinnerParameterType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                HealthParameter.ParameterType selectedType = HealthParameter.ParameterType.values()[position];
                updateParameterInfo(selectedType);
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }
    
    private void updateParameterInfo(HealthParameter.ParameterType parameterType) {
        binding.tvParameterUnit.setText(parameterType.getUnit());
        binding.tvNormalRange.setText("Норма: " + parameterType.getNormalMin() + " - " 
            + parameterType.getNormalMax() + " " + parameterType.getUnit());
    }
    
    private void setupClickListeners() {
        binding.btnSave.setOnClickListener(v -> saveParameter());
        
        binding.btnCancel.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).popBackStack()
        );
    }
    
    private void saveParameter() {
        int selectedPosition = binding.spinnerParameterType.getSelectedItemPosition();
        if (selectedPosition < 0) return;
        
        HealthParameter.ParameterType parameterType = HealthParameter.ParameterType.values()[selectedPosition];
        String valueText = binding.etParameterValue.getText().toString().trim();
        String notes = binding.etNotes.getText().toString().trim();
        
        if (validateInput(valueText)) {
            double value = Double.parseDouble(valueText);
            patientViewModel.addHealthParameter(parameterType, value, notes);
        }
    }
    
    private boolean validateInput(String valueText) {
        if (valueText.isEmpty()) {
            binding.tilParameterValue.setError(getString(R.string.field_required));
            return false;
        }
        
        try {
            double value = Double.parseDouble(valueText);
            if (value <= 0) {
                binding.tilParameterValue.setError("Введите корректное значение");
                return false;
            }
        } catch (NumberFormatException e) {
            binding.tilParameterValue.setError("Введите корректное значение");
            return false;
        }
        
        binding.tilParameterValue.setError(null);
        return true;
    }
    
    private void observeViewModel() {
        patientViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof PatientViewModel.UiState.Loading) {
                binding.btnSave.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof PatientViewModel.UiState.Success) {
                binding.btnSave.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                ViewUtils.showToast(requireContext(), ((PatientViewModel.UiState.Success) state).getMessage());
                NavHostFragment.findNavController(this).popBackStack();
            } else if (state instanceof PatientViewModel.UiState.Error) {
                binding.btnSave.setEnabled(true);
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