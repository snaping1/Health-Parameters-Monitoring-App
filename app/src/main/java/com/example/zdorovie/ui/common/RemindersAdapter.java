package com.example.zdorovie.ui.common;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.zdorovie.databinding.ItemReminderBinding;
import com.example.zdorovie.model.Reminder;

public class RemindersAdapter extends ListAdapter<Reminder, RemindersAdapter.ReminderViewHolder> {

    public interface OnReminderActionListener {
        void onEditClick(Reminder reminder);
        void onDeleteClick(Reminder reminder);
    }

    private final OnReminderActionListener listener;

    public RemindersAdapter(OnReminderActionListener listener) {
        super(new ReminderDiffCallback());
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReminderBinding binding = ItemReminderBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ReminderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    public class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final ItemReminderBinding binding;

        public ReminderViewHolder(@NonNull ItemReminderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Reminder reminder) {
            // Отображаем заголовок и содержание напоминания
            binding.tvReminderTitle.setText(reminder.getTitle());
            binding.tvReminderDescription.setText(reminder.getContent());
            
            // Устанавливаем обработчики нажатий для кнопок
            binding.btnEditReminder.setOnClickListener(v -> 
                listener.onEditClick(reminder));
            
            binding.btnDeleteReminder.setOnClickListener(v -> 
                listener.onDeleteClick(reminder));
        }
    }

    private static class ReminderDiffCallback extends DiffUtil.ItemCallback<Reminder> {
        @Override
        public boolean areItemsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.equals(newItem);
        }
    }
} 