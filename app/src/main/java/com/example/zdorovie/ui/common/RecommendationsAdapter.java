package com.example.zdorovie.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zdorovie.R;
import com.example.zdorovie.databinding.ItemRecommendationBinding;
import com.example.zdorovie.model.Recommendation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RecommendationsAdapter extends ListAdapter<Recommendation, RecommendationsAdapter.RecommendationViewHolder> {

    public interface OnRecommendationClickListener {
        void onRecommendationClick(Recommendation recommendation);
    }

    private final OnRecommendationClickListener listener;

    public RecommendationsAdapter(OnRecommendationClickListener listener) {
        super(new RecommendationDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRecommendationBinding binding = ItemRecommendationBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new RecommendationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class RecommendationViewHolder extends RecyclerView.ViewHolder {
        private final ItemRecommendationBinding binding;

        public RecommendationViewHolder(@NonNull ItemRecommendationBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Recommendation recommendation) {
            binding.tvRecommendationTitle.setText(recommendation.getTitle());
            binding.tvRecommendationContent.setText(recommendation.getContent());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            binding.tvRecommendationDate.setText(dateFormat.format(new Date(recommendation.getCreatedAt())));
            
            if (!recommendation.isRead()) {
                binding.cardRecommendation.setCardBackgroundColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), R.color.unread_background));
                
                if (binding.ivUnreadIndicator != null) {
                    binding.ivUnreadIndicator.setVisibility(View.VISIBLE);
                }
            } else {
                binding.cardRecommendation.setCardBackgroundColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
                
                if (binding.ivUnreadIndicator != null) {
                    binding.ivUnreadIndicator.setVisibility(View.GONE);
                }
            }
            
            binding.getRoot().setOnClickListener(v -> {
                listener.onRecommendationClick(recommendation);
                
                binding.cardRecommendation.setCardBackgroundColor(
                    ContextCompat.getColor(binding.getRoot().getContext(), R.color.white));
                
                if (binding.ivUnreadIndicator != null) {
                    binding.ivUnreadIndicator.setVisibility(View.GONE);
                }
            });
        }
    }

    private static class RecommendationDiffCallback extends DiffUtil.ItemCallback<Recommendation> {
        @Override
        public boolean areItemsTheSame(@NonNull Recommendation oldItem, @NonNull Recommendation newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Recommendation oldItem, @NonNull Recommendation newItem) {
            return oldItem.equals(newItem);
        }
    }
} 