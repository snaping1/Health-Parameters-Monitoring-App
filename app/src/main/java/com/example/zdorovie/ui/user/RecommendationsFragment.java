package com.example.zdorovie.ui.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.zdorovie.databinding.FragmentRecommendationsBinding;
import com.example.zdorovie.ui.common.RecommendationsAdapter;
import com.example.zdorovie.utils.ViewUtils;
import com.example.zdorovie.viewmodel.PatientViewModel;

import java.util.ArrayList;

public class RecommendationsFragment extends Fragment {
    
    private FragmentRecommendationsBinding binding;
    private PatientViewModel patientViewModel;
    private RecommendationsAdapter recommendationsAdapter;
    private Handler handler = new Handler(Looper.getMainLooper());
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecommendationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        patientViewModel = new ViewModelProvider(requireActivity()).get(PatientViewModel.class);
        
        setupRecyclerView();
        setupSwipeRefresh();
        observeData();
        
        // Принудительная начальная загрузка
        refreshRecommendations();
    }
    
    private void setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener(() -> refreshRecommendations());
    }
    
    private void refreshRecommendations() {
        patientViewModel.refreshRecommendations();
        
        // Остановить индикатор обновления с задержкой
        handler.postDelayed(() -> {
            if (binding != null && binding.swipeRefreshLayout != null && isAdded()) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновлять рекомендации при появлении фрагмента на экране
        refreshRecommendations();
    }
    
    private void setupRecyclerView() {
        recommendationsAdapter = new RecommendationsAdapter(recommendation -> {
            // Отметить рекомендацию как прочитанную
            patientViewModel.markRecommendationAsRead(recommendation.getId());
            ViewUtils.showToast(requireContext(), "Рекомендация прочитана");
        });
        
        binding.rvRecommendations.setAdapter(recommendationsAdapter);
        binding.rvRecommendations.setLayoutManager(new LinearLayoutManager(requireContext()));
    }
    
    private void observeData() {
        patientViewModel.getRecommendations().observe(getViewLifecycleOwner(), recommendations -> {
            // Отправить полный список без ограничений
            recommendationsAdapter.submitList(new ArrayList<>(recommendations));
            
            // Записать в лог количество рекомендаций для отладки
            System.out.println("RecommendationsFragment: Received " + recommendations.size() + " recommendations");
            
            if (recommendations.isEmpty()) {
                binding.tvEmptyState.setVisibility(View.VISIBLE);
                binding.rvRecommendations.setVisibility(View.GONE);
            } else {
                binding.tvEmptyState.setVisibility(View.GONE);
                binding.rvRecommendations.setVisibility(View.VISIBLE);
                
                // Обновить количество непрочитанных
                long unreadCount = recommendations.stream().filter(r -> !r.isRead()).count();
                if (unreadCount > 0) {
                    binding.tvUnreadCount.setVisibility(View.VISIBLE);
                    binding.tvUnreadCount.setText("Новых: " + unreadCount);
                } else {
                    binding.tvUnreadCount.setVisibility(View.GONE);
                }
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
        // Отменить все ожидающие операции
        handler.removeCallbacksAndMessages(null);
        binding = null;
    }
} 