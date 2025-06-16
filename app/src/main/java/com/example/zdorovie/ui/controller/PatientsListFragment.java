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
import com.example.zdorovie.databinding.FragmentPatientsListBinding;
import com.example.zdorovie.model.User;
import com.example.zdorovie.ui.common.PatientsAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.ControllerViewModel;

public class PatientsListFragment extends Fragment {
    
    private FragmentPatientsListBinding binding;
    private ControllerViewModel controllerViewModel;
    private PatientsAdapter patientsAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPatientsListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        controllerViewModel = new ViewModelProvider(this).get(ControllerViewModel.class);
        
        setupRecyclerView();
        observeData();
        
        // Load patients on create
        controllerViewModel.loadPatients();
    }
    
    private void setupRecyclerView() {
        patientsAdapter = new PatientsAdapter(patient -> {
            // Navigate to patient details
            Bundle bundle = new Bundle();
            bundle.putString("patientId", patient.getId());
            
            NavHostFragment.findNavController(this).navigate(
                R.id.action_patientsListFragment_to_patientDetailsFragment,
                bundle
            );
        });
        
        binding.rvPatients.setAdapter(patientsAdapter);
        binding.rvPatients.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void observeData() {
        controllerViewModel.getPatients().observe(getViewLifecycleOwner(), patients -> {
            patientsAdapter.submitList(patients);
            
            if (patients.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvPatients.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvPatients.setVisibility(View.VISIBLE);
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