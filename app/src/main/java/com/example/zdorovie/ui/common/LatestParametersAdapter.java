package com.example.zdorovie.ui.common;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zdorovie.databinding.ItemLatestParameterBinding;
import com.example.zdorovie.model.HealthParameter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LatestParametersAdapter extends ListAdapter<HealthParameter, LatestParametersAdapter.ParameterViewHolder> {

    public interface OnParameterClickListener {
        void onParameterClick(HealthParameter parameter);
    }

    private final OnParameterClickListener listener;

    public LatestParametersAdapter(OnParameterClickListener listener) {
        super(new ParameterDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ParameterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLatestParameterBinding binding = ItemLatestParameterBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new ParameterViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ParameterViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ParameterViewHolder extends RecyclerView.ViewHolder {
        private final ItemLatestParameterBinding binding;

        public ParameterViewHolder(@NonNull ItemLatestParameterBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(HealthParameter parameter) {
            binding.tvParameterName.setText(parameter.getType().getDisplayName());
            binding.tvParameterValue.setText(String.valueOf(parameter.getValue()));
            binding.tvParameterUnit.setText(parameter.getType().getUnit());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
            binding.tvMeasurementTime.setText(dateFormat.format(new Date(parameter.getTimestamp())));
            
            // Set status indicator
            int statusColor = parameter.isNormal() ?
                Color.parseColor("#4CAF50") : // green
                Color.parseColor("#F44336"); // red
            
            binding.viewStatusIndicator.setBackgroundColor(statusColor);
            
            binding.getRoot().setOnClickListener(v -> listener.onParameterClick(parameter));
        }
    }

    private static class ParameterDiffCallback extends DiffUtil.ItemCallback<HealthParameter> {
        @Override
        public boolean areItemsTheSame(@NonNull HealthParameter oldItem, @NonNull HealthParameter newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull HealthParameter oldItem, @NonNull HealthParameter newItem) {
            return oldItem.equals(newItem);
        }
    }
} 