package com.example.zdorovie.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Objects;

// Класс, представляющий рекомендацию для пациента.
// Содержит информацию о рекомендации, включая отправителя, получателя, содержание и статус прочтения.
public class Recommendation {
    @DocumentId // Аннотация для Firestore - указывает, что это поле содержит ID документа
    private String id; // Уникальный идентификатор рекомендации
    private String controllerId; // ID контроллера/врача, создавшего рекомендацию
    private String patientId; // ID пациента, для которого предназначена рекомендация
    private String title; // Заголовок рекомендации
    private String content; // Содержание рекомендации
    private boolean isRead;  // Флаг, указывающий, прочитана ли рекомендация
    private long createdAt; // Временная метка создания (в миллисекундах)

    // Пустой конструктор, необходимый для Firestore
    public Recommendation() {
        this.id = "";
        this.controllerId = "";
        this.patientId = "";
        this.title = "";
        this.content = "";
        this.isRead = false;
        this.createdAt = System.currentTimeMillis();
    }

    public Recommendation(String id, String controllerId, String patientId, String title, 
                         String content, boolean isRead, long createdAt) {
        this.id = id;
        this.controllerId = controllerId;
        this.patientId = patientId;
        this.title = title;
        this.content = content;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
    
    // Конструктор копирования
    public Recommendation(Recommendation recommendation) {
        this.id = recommendation.id;
        this.controllerId = recommendation.controllerId;
        this.patientId = recommendation.patientId;
        this.title = recommendation.title;
        this.content = recommendation.content;
        this.isRead = recommendation.isRead;
        this.createdAt = recommendation.createdAt;
    }
    
    // Вложенный класс Builder для создания объектов Recommendation
    public static class Builder {
        private String id = "";
        private String controllerId = "";
        private String patientId = "";
        private String title = "";
        private String content = "";
        private boolean isRead = false;
        private long createdAt = System.currentTimeMillis();

        // Методы для установки значений полей с возвращением this (для цепочки вызовов)
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder controllerId(String controllerId) {
            this.controllerId = controllerId;
            return this;
        }
        
        public Builder patientId(String patientId) {
            this.patientId = patientId;
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
        
        public Builder isRead(boolean isRead) {
            this.isRead = isRead;
            return this;
        }
        
        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        // Создает объект Recommendation с текущими значениями полей Builder.
        public Recommendation build() {
            return new Recommendation(id, controllerId, patientId, title, content, isRead, createdAt);
        }
    }
    
    // Стандартные геттеры и сеттеры для всех полей класса
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Переопределение метода equals для сравнения объектов Recommendation
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Recommendation that = (Recommendation) o;
        return isRead == that.isRead &&
                createdAt == that.createdAt &&
                Objects.equals(id, that.id) &&
                Objects.equals(controllerId, that.controllerId) &&
                Objects.equals(patientId, that.patientId) &&
                Objects.equals(title, that.title) &&
                Objects.equals(content, that.content);
    }

    // Переопределение метода hashCode для корректной работы с коллекциями
    @Override
    public int hashCode() {
        return Objects.hash(id, controllerId, patientId, title, content, isRead, createdAt);
    }

    // Переопределение метода toString для удобного вывода информации об объекте.
    @Override
    public String toString() {
        return "Recommendation{" +
                "id='" + id + '\'' +
                ", controllerId='" + controllerId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", isRead=" + isRead +
                ", createdAt=" + createdAt +
                '}';
    }
} 