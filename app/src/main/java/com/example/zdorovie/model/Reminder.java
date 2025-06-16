package com.example.zdorovie.model;

import com.google.firebase.firestore.DocumentId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Класс, представляющий напоминание для пользователя.
public class Reminder {
    @DocumentId
    private String id;
    private String userId;
    private String title; // Заголовок напоминания
    private String content; // Содержание напоминания
    private long createdAt; // Временная метка создания напоминания
    
    // Поля для обратной совместимости с существующими данными:
    private boolean isTextReminder;
    private HealthParameter.ParameterType parameterType;
    private String time;
    private boolean isActive;
    private List<Integer> repeatDays;
    
    // Пустой конструктор, необходимый для Firestore.
    public Reminder() {
        this.id = "";
        this.userId = "";
        this.title = "";
        this.content = "";
        this.createdAt = System.currentTimeMillis();
        this.isTextReminder = true;
        this.parameterType = null;
        this.time = "";
        this.isActive = true;
        this.repeatDays = new ArrayList<>();
    }
    
    public Reminder(String id, String userId, String title, String content, long createdAt,
                   boolean isTextReminder, HealthParameter.ParameterType parameterType, 
                   String time, boolean isActive, List<Integer> repeatDays) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.isTextReminder = isTextReminder;
        this.parameterType = parameterType;
        this.time = time;
        this.isActive = isActive;
        this.repeatDays = repeatDays != null ? new ArrayList<>(repeatDays) : new ArrayList<>();
    }
    
    // Copy constructor
    public Reminder(Reminder reminder) {
        this.id = reminder.id;
        this.userId = reminder.userId;
        this.title = reminder.title;
        this.content = reminder.content;
        this.createdAt = reminder.createdAt;
        this.isTextReminder = reminder.isTextReminder;
        this.parameterType = reminder.parameterType;
        this.time = reminder.time;
        this.isActive = reminder.isActive;
        this.repeatDays = reminder.repeatDays != null ? new ArrayList<>(reminder.repeatDays) : new ArrayList<>();
    }
    
    // Вложенный класс Builder для создания объектов Reminder
    public static class Builder {
        private String id = "";
        private String userId = "";
        private String title = "";
        private String content = "";
        private long createdAt = System.currentTimeMillis();
        private boolean isTextReminder = true;
        private HealthParameter.ParameterType parameterType = null;
        private String time = "";
        private boolean isActive = true;
        private List<Integer> repeatDays = new ArrayList<>();

        // Методы для установки значений полей с возвращением this
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder title(String title) {
            this.title = title;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder isTextReminder(boolean isTextReminder) {
            this.isTextReminder = isTextReminder;
            return this;
        }
        
        public Builder parameterType(HealthParameter.ParameterType parameterType) {
            this.parameterType = parameterType;
            return this;
        }
        
        public Builder time(String time) {
            this.time = time;
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder repeatDays(List<Integer> repeatDays) {
            this.repeatDays = repeatDays != null ? new ArrayList<>(repeatDays) : new ArrayList<>();
            return this;
        }

        // Создает объект Reminder с текущими значениями полей Builder
        public Reminder build() {
            return new Reminder(id, userId, title, content, createdAt, isTextReminder, 
                               parameterType, time, isActive, repeatDays);
        }
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public boolean isTextReminder() {
        return isTextReminder;
    }
    
    public void setTextReminder(boolean textReminder) {
        isTextReminder = textReminder;
    }
    
    public HealthParameter.ParameterType getParameterType() {
        return parameterType;
    }
    
    public void setParameterType(HealthParameter.ParameterType parameterType) {
        this.parameterType = parameterType;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }

    // Возвращает копию списка дней повторения для защиты от внешних изменений
    public List<Integer> getRepeatDays() {
        return repeatDays;
    }

    // Устанавливает дни повторения с защитным копированием
    public void setRepeatDays(List<Integer> repeatDays) {
        this.repeatDays = repeatDays != null ? new ArrayList<>(repeatDays) : new ArrayList<>();
    }

    // Переопределение метода equals для сравнения объектов Reminder
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reminder reminder = (Reminder) o;
        return createdAt == reminder.createdAt &&
                isTextReminder == reminder.isTextReminder &&
                isActive == reminder.isActive &&
                Objects.equals(id, reminder.id) &&
                Objects.equals(userId, reminder.userId) &&
                Objects.equals(title, reminder.title) &&
                Objects.equals(content, reminder.content) &&
                parameterType == reminder.parameterType &&
                Objects.equals(time, reminder.time) &&
                Objects.equals(repeatDays, reminder.repeatDays);
    }

    // Переопределение метода hashCode для корректной работы с коллекциями
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, title, content, createdAt, isTextReminder, 
                           parameterType, time, isActive, repeatDays);
    }

    // Переопределение метода toString для удобного вывода информации об объекте
    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", isTextReminder=" + isTextReminder +
                ", parameterType=" + parameterType +
                ", time='" + time + '\'' +
                ", isActive=" + isActive +
                ", repeatDays=" + repeatDays +
                '}';
    }
} 