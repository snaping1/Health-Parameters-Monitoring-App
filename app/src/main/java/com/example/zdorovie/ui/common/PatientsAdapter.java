package com.example.zdorovie.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zdorovie.databinding.ItemPatientBinding;
import com.example.zdorovie.model.User;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class PatientsAdapter extends ListAdapter<User, PatientsAdapter.PatientViewHolder> {

    public interface OnPatientClickListener {
        void onPatientClick(User patient);
    }

    private final OnPatientClickListener listener;

    public PatientsAdapter(OnPatientClickListener listener) {
        super(new PatientDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPatientBinding binding = ItemPatientBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new PatientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PatientViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class PatientViewHolder extends RecyclerView.ViewHolder {
        private final ItemPatientBinding binding;

        public PatientViewHolder(@NonNull ItemPatientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User patient) {
            binding.tvPatientName.setText(patient.getName());
            binding.tvPatientEmail.setText(patient.getEmail());
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
            binding.tvRegistrationDate.setText("Регистрация: " + 
                dateFormat.format(patient.getRegistrationDate()));
            
            binding.getRoot().setOnClickListener(v -> listener.onPatientClick(patient));
        }
    }

    private static class PatientDiffCallback extends DiffUtil.ItemCallback<User> {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.equals(newItem);
        }
    }
} 