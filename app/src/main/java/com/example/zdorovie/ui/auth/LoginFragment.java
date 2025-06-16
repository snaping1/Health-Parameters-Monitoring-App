package com.example.zdorovie.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentLoginBinding;
import com.example.zdorovie.utils.Extensions;
import com.example.zdorovie.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {
    
    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        
        setupClickListeners();
        observeAuthState();
    }
    
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            
            if (validateInput(email, password)) {
                authViewModel.signIn(email, password);
            }
        });
        
        binding.tvRegister.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireView());
            navController.navigate(R.id.action_loginFragment_to_registerFragment);
        });
    }
    
    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        
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
        
        return isValid;
    }
    
    private void observeAuthState() {
        authViewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof AuthViewModel.AuthState.Loading) {
                binding.btnLogin.setEnabled(false);
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof AuthViewModel.AuthState.Authenticated) {
                binding.btnLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                
                // Force update state in parent activity
                if (getActivity() != null) {
                    AuthViewModel sharedViewModel = new ViewModelProvider(getActivity()).get(AuthViewModel.class);
                    if (authViewModel.getCurrentUser().getValue() != null) {
                        // Update currentUser in shared ViewModel so MainActivity can react
                        sharedViewModel.updateCurrentUser(authViewModel.getCurrentUser().getValue());
                    }
                }
                
                Extensions.showToast(this, "Вход выполнен успешно");
            } else if (state instanceof AuthViewModel.AuthState.Error) {
                binding.btnLogin.setEnabled(true);
                binding.progressBar.setVisibility(View.GONE);
                Extensions.showToast(this, ((AuthViewModel.AuthState.Error) state).getMessage());
            } else if (state instanceof AuthViewModel.AuthState.Unauthenticated) {
                binding.btnLogin.setEnabled(true);
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