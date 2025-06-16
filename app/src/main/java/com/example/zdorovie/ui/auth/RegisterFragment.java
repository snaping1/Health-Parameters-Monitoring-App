package com.example.zdorovie.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentRegisterBinding;
import com.example.zdorovie.model.User;
import com.example.zdorovie.utils.Extensions;
import com.example.zdorovie.viewmodel.AuthViewModel;

public class RegisterFragment extends Fragment {
    
    private FragmentRegisterBinding binding;
    private AuthViewModel authViewModel;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        setupRoleSpinner();
        setupClickListeners();
        observeAuthState();
    }
    
    private void setupRoleSpinner() {
        String[] roles = {
            getString(R.string.patient),
            getString(R.string.controller)
        };
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            requireContext(), 
            android.R.layout.simple_spinner_item, 
            roles
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerRole.setAdapter(adapter);
    }
    
    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> {
            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
            
            User.UserRole role = binding.spinnerRole.getSelectedItemPosition() == 0 
                ? User.UserRole.PATIENT 
                : User.UserRole.CONTROLLER;
            
            if (validateInput(name, email, password, confirmPassword)) {
                authViewModel.signUp(email, password, name, role);
            }
        });
        
        binding.tvLogin.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.popBackStack();
        });
    }
    
    private boolean validateInput(
        String name,
        String email,
        String password,
        String confirmPassword
    ) {
        boolean isValid = true;
        
        if (name.isEmpty()) {
            binding.tilName.setError(getString(R.string.field_required));
            isValid = false;
        } else {
            binding.tilName.setError(null);
        }
        
        if (email.isEmpty()) {
            binding.tilEmail.setError(getString(R.string.field_required));
            isValid = false;
        } else if (!Extensions.isValidEmail(email)) {
            binding.tilEmail.setError(getString(R.string.invalid_email));
            isValid = false;
        } else {
            binding.tilEmail.setError(null);
        }
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(getString(R.string.field_required));
            isValid = false;
        } else if (password.length() < 6) {
            binding.tilPassword.setError(getString(R.string.password_too_short));
            isValid = false;
        } else {
            binding.tilPassword.setError(null);
        }
        
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(getString(R.string.field_required));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError(getString(R.string.passwords_not_match));
            isValid = false;
        } else {
            binding.tilConfirmPassword.setError(null);
        }
        
        return isValid;
    }
    
    private void observeAuthState() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthViewModel.AuthState.Loading) {
                binding.btnRegister.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof AuthViewModel.AuthState.Authenticated) {
                binding.btnRegister.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Extensions.showToast(this, "Регистрация выполнена успешно");
            } else if (state instanceof AuthViewModel.AuthState.Error) {
                binding.btnRegister.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Extensions.showToast(this, ((AuthViewModel.AuthState.Error) state).getMessage());
            } else if (state instanceof AuthViewModel.AuthState.Unauthenticated) {
                binding.btnRegister.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 