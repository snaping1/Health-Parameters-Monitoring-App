package com.example.zdorovie.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.zdorovie.R;
import com.example.zdorovie.databinding.FragmentRemindersBinding;
import com.example.zdorovie.model.Reminder;
import com.example.zdorovie.ui.common.RemindersAdapter;
import com.example.zdorovie.ui.common.RemindersAdapter.OnReminderActionListener;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.PatientViewModel;

public class RemindersFragment extends Fragment implements OnReminderActionListener {
    
    private FragmentRemindersBinding binding;
    private PatientViewModel patientViewModel;
    private RemindersAdapter remindersAdapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRemindersBinding.inflate(inflater, container, false);
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
        remindersAdapter = new RemindersAdapter(this);
        
        binding.rvReminders.setAdapter(remindersAdapter);
        binding.rvReminders.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void setupClickListeners() {
        binding.fabAddReminder.setOnClickListener(v -> navigateToAddReminder());
    }
    
    private void navigateToAddReminder() {
        try {
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_remindersFragment_to_addReminderFragment);
        } catch (Exception e) {
            try {
                NavHostFragment.findNavController(this).navigate(R.id.addReminderFragment);
            } catch (Exception ex) {
                ViewUtils.showToast(requireContext(), "Ошибка навигации: " + ex.getMessage());
            }
        }
    }
    
    @Override
    public void onEditClick(Reminder reminder) {
        // Создаем Bundle с данными для передачи в AddReminderFragment
        Bundle bundle = new Bundle();
        bundle.putString("reminder_id", reminder.getId());
        bundle.putString("reminder_title", reminder.getTitle());
        bundle.putString("reminder_content", reminder.getContent());
        
        // Переходим на экран добавления/редактирования напоминания с передачей данных
        try {
            NavHostFragment.findNavController(this).navigate(R.id.addReminderFragment, bundle);
        } catch (Exception ex) {
            ViewUtils.showToast(requireContext(), "Ошибка навигации: " + ex.getMessage());
        }
    }
    
    @Override
    public void onDeleteClick(Reminder reminder) {
        // Показываем диалог подтверждения перед удалением
        new AlertDialog.Builder(requireContext())
            .setTitle("Удаление напоминания")
            .setMessage("Вы уверены, что хотите удалить это напоминание?")
            .setPositiveButton("Удалить", (dialog, which) -> 
                patientViewModel.deleteReminder(reminder.getId())
            )
            .setNegativeButton("Отмена", null)
            .show();
    }
    
    private void observeData() {
        patientViewModel.getReminders().observe(getViewLifecycleOwner(), allReminders -> {
            // Выводим все напоминания без фильтрации
            remindersAdapter.submitList(allReminders);
            
            if (allReminders.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvReminders.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvReminders.setVisibility(View.VISIBLE);
            }
        });
        
        patientViewModel.getUiState().observe(getViewLifecycleOwner(), state -> {
            if (state instanceof PatientViewModel.UiState.Loading) {
                binding.progressBar.setVisibility(View.VISIBLE);
            } else if (state instanceof PatientViewModel.UiState.Success) {
                binding.progressBar.setVisibility(View.GONE);
                // Показываем сообщение об успехе, только если оно связано с напоминаниями
                String message = ((PatientViewModel.UiState.Success) state).getMessage();
                if (message.contains("Напоминание")) {
                    ViewUtils.showToast(requireContext(), message);
                }
            } else if (state instanceof PatientViewModel.UiState.Error) {
                binding.progressBar.setVisibility(View.GONE);
                ViewUtils.showToast(requireContext(), 
                    ((PatientViewModel.UiState.Error) state).getMessage());
            }
        });
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 