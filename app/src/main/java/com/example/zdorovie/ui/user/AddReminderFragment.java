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
import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentAddReminderBinding;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.PatientViewModel;

public class AddReminderFragment extends Fragment {
    
    private FragmentAddReminderBinding binding;
    private PatientViewModel patientViewModel;
    
    // Переменная для хранения ID редактируемого напоминания
    private String editReminderId;
    private boolean isEditMode = false;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddReminderBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        patientViewModel = new ViewModelProvider(this).get(PatientViewModel.class);
        
        // Проверяем, получены ли данные для редактирования
        Bundle args = getArguments();
        if (args != null) {
            editReminderId = args.getString("reminder_id");
            String title = args.getString("reminder_title");
            String content = args.getString("reminder_content");
            
            if (editReminderId != null && title != null && !title.isEmpty() 
                    && content != null && !content.isEmpty()) {
                isEditMode = true;
                binding.tvTitle.setText("Редактировать напоминание");
                binding.etReminderTitle.setText(title);
                binding.etReminderContent.setText(content);
                binding.btnSave.setText("Обновить");
            }
        }
        
        setupClickListeners();
        observeViewModel();
    }
    
    private void setupClickListeners() {
        binding.btnSave.setOnClickListener(v -> saveReminder());
        
        binding.btnCancel.setOnClickListener(v -> 
            NavHostFragment.findNavController(this).popBackStack()
        );
    }
    
    private void saveReminder() {
        String title = binding.etReminderTitle.getText().toString().trim();
        String content = binding.etReminderContent.getText().toString().trim();
        
        if (title.isEmpty()) {
            binding.tilReminderTitle.setError("Введите заголовок напоминания");
            return;
        } else {
            binding.tilReminderTitle.setError(null);
        }
        
        if (content.isEmpty()) {
            binding.tilReminderContent.setError("Введите текст напоминания");
            return;
        } else {
            binding.tilReminderContent.setError(null);
        }
        
        if (isEditMode && editReminderId != null) {
            patientViewModel.updateReminder(editReminderId, title, content);
        } else {
            patientViewModel.addSimpleReminder(title, content);
        }
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