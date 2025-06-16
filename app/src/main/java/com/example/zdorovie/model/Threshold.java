package com.example.zdorovie.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Objects;

// Класс, представляющий пороговые значения для параметров здоровья.
public class Threshold {
    @DocumentId
    private String id;
    private String controllerId;
    private String patientId;
    private HealthParameter.ParameterType parameterType;
    private double minValue;
    private double maxValue;
    private boolean isActive;
    private long createdAt;
    
    // Empty constructor for Firestore
    public Threshold() {
        this.id = "";
        this.controllerId = "";
        this.patientId = "";
        this.parameterType = HealthParameter.ParameterType.BLOOD_PRESSURE_SYSTOLIC;
        this.minValue = 0.0;
        this.maxValue = 0.0;
        this.isActive = true;
        this.createdAt = System.currentTimeMillis();
    }
    
    public Threshold(String id, String controllerId, String patientId, 
                    HealthParameter.ParameterType parameterType,
                    double minValue, double maxValue, boolean isActive, long createdAt) {
        this.id = id;
        this.controllerId = controllerId;
        this.patientId = patientId;
        this.parameterType = parameterType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }
    
    // Copy constructor
    public Threshold(Threshold threshold) {
        this.id = threshold.id;
        this.controllerId = threshold.controllerId;
        this.patientId = threshold.patientId;
        this.parameterType = threshold.parameterType;
        this.minValue = threshold.minValue;
        this.maxValue = threshold.maxValue;
        this.isActive = threshold.isActive;
        this.createdAt = threshold.createdAt;
    }
    
    // Вложенный класс Builder для создания объектов Threshold
    public static class Builder {
        private String id = "";
        private String controllerId = "";
        private String patientId = "";
        private HealthParameter.ParameterType parameterType = HealthParameter.ParameterType.BLOOD_PRESSURE_SYSTOLIC;
        private double minValue = 0.0;
        private double maxValue = 0.0;
        private boolean isActive = true;
        private long createdAt = System.currentTimeMillis();

        // Методы для установки значений полей с возвращением this
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
        
        public Builder parameterType(HealthParameter.ParameterType parameterType) {
            this.parameterType = parameterType;
            return this;
        }
        
        public Builder minValue(double minValue) {
            this.minValue = minValue;
            return this;
        }
        
        public Builder maxValue(double maxValue) {
            this.maxValue = maxValue;
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Threshold build() {
            return new Threshold(id, controllerId, patientId, parameterType, 
                                minValue, maxValue, isActive, createdAt);
        }
    }
    
    // Getters and setters
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
    
    public HealthParameter.ParameterType getParameterType() {
        return parameterType;
    }
    
    public void setParameterType(HealthParameter.ParameterType parameterType) {
        this.parameterType = parameterType;
    }
    
    public double getMinValue() {
        return minValue;
    }
    
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }
    
    public double getMaxValue() {
        return maxValue;
    }
    
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    // Переопределение метода equals для сравнения объектов Threshold.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Threshold threshold = (Threshold) o;
        return Double.compare(threshold.minValue, minValue) == 0 &&
                Double.compare(threshold.maxValue, maxValue) == 0 &&
                isActive == threshold.isActive &&
                createdAt == threshold.createdAt &&
                Objects.equals(id, threshold.id) &&
                Objects.equals(controllerId, threshold.controllerId) &&
                Objects.equals(patientId, threshold.patientId) &&
                parameterType == threshold.parameterType;
    }

    // Переопределение метода hashCode для корректной работы с коллекциями.
    @Override
    public int hashCode() {
        return Objects.hash(id, controllerId, patientId, parameterType, minValue, maxValue, isActive, createdAt);
    }

    // Переопределение метода toString для удобного вывода информации об объекте.
    @Override
    public String toString() {
        return "Threshold{" +
                "id='" + id + '\'' +
                ", controllerId='" + controllerId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", parameterType=" + parameterType +
                ", minValue=" + minValue +
                ", maxValue=" + maxValue +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
} 