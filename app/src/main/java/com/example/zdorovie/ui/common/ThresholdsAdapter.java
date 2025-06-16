package com.example.zdorovie.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zdorovie.databinding.ItemThresholdBinding;
import com.example.zdorovie.model.Threshold;

public class ThresholdsAdapter extends ListAdapter<Threshold, ThresholdsAdapter.ThresholdViewHolder> {

    public interface OnThresholdEditListener {
        void onThresholdEdit(Threshold threshold);
    }

    private final OnThresholdEditListener listener;

    public ThresholdsAdapter(OnThresholdEditListener listener) {
        super(new ThresholdDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ThresholdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemThresholdBinding binding = ItemThresholdBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new ThresholdViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ThresholdViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ThresholdViewHolder extends RecyclerView.ViewHolder {
        private final ItemThresholdBinding binding;

        public ThresholdViewHolder(@NonNull ItemThresholdBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Threshold threshold) {
            binding.tvParameterName.setText(threshold.getParameterType().getDisplayName());
            binding.tvThresholdRange.setText(threshold.getMinValue() + " - " + threshold.getMaxValue());
            binding.tvUnit.setText(threshold.getParameterType().getUnit());
            
            binding.getRoot().setOnClickListener(v -> listener.onThresholdEdit(threshold));
        }
    }

    private static class ThresholdDiffCallback extends DiffUtil.ItemCallback<Threshold> {
        @Override
        public boolean areItemsTheSame(@NonNull Threshold oldItem, @NonNull Threshold newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Threshold oldItem, @NonNull Threshold newItem) {
            return oldItem.equals(newItem);
        }
    }
} 