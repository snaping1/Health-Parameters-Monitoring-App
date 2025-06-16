package com.example.zdorovie.ui.common;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentProfileBinding;
import com.example.zdorovie.model.User;
import com.example.zdorovie.utils.Extensions;
import com.example.zdorovie.viewmodel.AuthViewModel;
import com.example.zdorovie.viewmodel.PatientViewModel;

import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {
    
    private FragmentProfileBinding binding;
    private AuthViewModel authViewModel;
    private PatientViewModel patientViewModel;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModels
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        
        setupClickListeners();
        observeData();
    }
    
    private void setupClickListeners() {
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());
        
        binding.btnAssignController.setOnClickListener(v -> showControllerSelectionDialog());
    }
    
    private void observeData() {
        authViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                updateUserInfo(user);
            }
        });
        
        patientViewModel.getController().observe(getViewLifecycleOwner(), controller -> {
            if (controller != null) {
                binding.tvControllerName.setText(controller.getName());
                binding.tvControllerEmail.setText(controller.getEmail());
                binding.layoutControllerInfo.setVisibility(View.VISIBLE);
                binding.btnAssignController.setText("Изменить контролера");
            } else {
                binding.layoutControllerInfo.setVisibility(View.GONE);
                binding.btnAssignController.setText(getString(R.string.assign_controller));
            }
        });
        
        patientViewModel.getAvailableControllers().observe(getViewLifecycleOwner(), controllers -> {
            if (controllers != null && !controllers.isEmpty()) {
                showControllerSelectionDialog(controllers);
            }
        });
        
        patientViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof PatientViewModel.UiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof PatientViewModel.UiState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                String message = ((PatientViewModel.UiState.Success) state).getMessage();
                if (!message.equals("Список контроллеров загружен")) {
                    Extensions.showToast(this, message);
                }
            } else if (state instanceof PatientViewModel.UiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                Extensions.showToast(this, ((PatientViewModel.UiState.Error) state).getMessage());
            }
        });
    }
    
    private void updateUserInfo(User user) {
        binding.tvUserName.setText(user.getName());
        binding.tvUserEmail.setText(user.getEmail());
        
        String roleText;
        if (user.getRole() == User.UserRole.PATIENT) {
            roleText = getString(R.string.patient);
            binding.layoutControllerSection.setVisibility(View.VISIBLE);
        } else {
            roleText = getString(R.string.controller);
            binding.layoutControllerSection.setVisibility(View.GONE);
        }
        binding.tvUserRole.setText(roleText);
    }
    
    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
            .setTitle("Выход")
            .setMessage("Вы уверены, что хотите выйти из аккаунта?")
            .setPositiveButton("Выйти", (dialog, which) -> {
                authViewModel.signOut();
                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_profileFragment_to_loginFragment);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void showControllerSelectionDialog() {
        // Загрузка доступных контролеров
        patientViewModel.loadAvailableControllers();
    }
    
    private void showControllerSelectionDialog(List<User> controllers) {
        if (controllers.isEmpty()) {
            Extensions.showToast(this, "Нет доступных контроллеров");
            return;
        }
        
        String[] controllerNames = new String[controllers.size()];
        for (int i = 0; i < controllers.size(); i++) {
            User controller = controllers.get(i);
            controllerNames[i] = controller.getName() + " (" + controller.getEmail() + ")";
        }
        
        new AlertDialog.Builder(requireContext())
            .setTitle("Выберите контролера")
            .setItems(controllerNames, (dialog, which) -> {
                User selectedController = controllers.get(which);
                patientViewModel.assignController(selectedController.getId());
            })
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 