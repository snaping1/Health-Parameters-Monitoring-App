package com.example.zdorovie.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Objects;



//Класс, представляющий параметр здоровья пользователя.
//Используется для хранения и обработки данных о различных медицинских показателях.

public class HealthParameter {
    
    public enum ParameterType {
        BLOOD_PRESSURE_SYSTOLIC("Систолическое давление", "мм рт.ст.", 110.0, 130.0),
        BLOOD_PRESSURE_DIASTOLIC("Диастолическое давление", "мм рт.ст.", 70.0, 85.0),
        HEART_RATE("Пульс", "уд/мин", 60.0, 100.0),
        TEMPERATURE("Температура", "°C", 36.3, 37.1),
        BLOOD_SUGAR("Сахар в крови", "ммоль/л", 3.9, 5.6),
        WEIGHT("Вес", "кг", 0.0, 300.0),
        HEIGHT("Рост", "см", 0.0, 300.0),
        OXYGEN_SATURATION("Насыщение кислородом", "%", 95.0, 100.0),
        CHOLESTEROL("Холестерин", "ммоль/л", 0.0, 5.2);
        
        private final String displayName; // Отображаемое название параметра
        private final String unit; // Единица измерения
        private final double normalMin; // Минимальное нормальное значение
        private final double normalMax; // Максимальное нормальное значение

        // Конструктор для enum значений
        ParameterType(String displayName, String unit, double normalMin, double normalMax) {
            this.displayName = displayName;
            this.unit = unit;
            this.normalMin = normalMin;
            this.normalMax = normalMax;
        }

        // Геттеры для свойств enum
        public String getDisplayName() {
            return displayName;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public double getNormalMin() {
            return normalMin;
        }
        
        public double getNormalMax() {
            return normalMax;
        }
    }
    
    @DocumentId // Аннотация для Firestore - указывает, что это поле содержит ID документа
    private String id; // Уникальный идентификатор записи
    private String userId; // ID пользователя, к которому относится параметр
    private ParameterType type; // Тип параметра здоровья
    private double value; // Значение параметра
    private String unit; // Единица измерения (может дублироваться из type)
    private long timestamp; // Временная метка измерения (в миллисекундах)
    private boolean isNormal; // Флаг, указывающий, находится ли значение в норме
    private String notes;  // Дополнительные заметки к измерению
    
    // Пустой конструктор, необходимый для Firestore.
    // Инициализирует поля значениями по умолчанию.
    public HealthParameter() {
        this.id = "";
        this.userId = "";
        this.type = ParameterType.BLOOD_PRESSURE_SYSTOLIC;
        this.value = 0.0;
        this.unit = "";
        this.timestamp = System.currentTimeMillis();
        this.isNormal = true;
        this.notes = "";
    }

    // Основной конструктор для создания объекта HealthParameter
    public HealthParameter(String id, String userId, ParameterType type, double value, 
                          String unit, long timestamp, boolean isNormal, String notes) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.value = value;
        this.unit = unit;
        this.timestamp = timestamp;
        this.isNormal = isNormal;
        this.notes = notes;
    }

    // Конструктор копирования
    // Создает новый объект на основе существующего
    // @param parameter Объект для копирования
    public HealthParameter(HealthParameter parameter) {
        this.id = parameter.id;
        this.userId = parameter.userId;
        this.type = parameter.type;
        this.value = parameter.value;
        this.unit = parameter.unit;
        this.timestamp = parameter.timestamp;
        this.isNormal = parameter.isNormal;
        this.notes = parameter.notes;
    }
    
    // Вложенный класс Builder для создания объектов HealthParameter
    // с использованием паттерна "Строитель".
    public static class Builder {
        private String id = "";
        private String userId = "";
        private ParameterType type = ParameterType.BLOOD_PRESSURE_SYSTOLIC;
        private double value = 0.0;
        private String unit = "";
        private long timestamp = System.currentTimeMillis();
        private boolean isNormal = true;
        private String notes = "";
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        // Методы для установки значений полей с возвращением this (для цепочки вызовов)
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder type(ParameterType type) {
            this.type = type;
            this.unit = type.getUnit();
            return this;
        }
        
        public Builder value(double value) {
            this.value = value;
            return this;
        }
        
        public Builder unit(String unit) {
            this.unit = unit;
            return this;
        }
        
        public Builder timestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder isNormal(boolean isNormal) {
            this.isNormal = isNormal;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        // Создает объект HealthParameter с текущими значениями полей Builder.
        // @return новый объект HealthParameter
        public HealthParameter build() {
            return new HealthParameter(id, userId, type, value, unit, timestamp, isNormal, notes);
        }
    }
    
    // Cтандартные геттеры и сеттеры для всех полей класса
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
    
    public ParameterType getType() {
        return type;
    }
    
    public void setType(ParameterType type) {
        this.type = type;
    }
    
    public double getValue() {
        return value;
    }
    
    public void setValue(double value) {
        this.value = value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public void setUnit(String unit) {
        this.unit = unit;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    public boolean isNormal() {
        return isNormal;
    }
    
    public void setNormal(boolean normal) {
        isNormal = normal;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }

    //Переопределение метода equals для сравнения объектов HealthParameter. Сравниваются все поля объектов.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HealthParameter that = (HealthParameter) o;
        return Double.compare(that.value, value) == 0 &&
                timestamp == that.timestamp &&
                isNormal == that.isNormal &&
                Objects.equals(id, that.id) &&
                Objects.equals(userId, that.userId) &&
                type == that.type &&
                Objects.equals(unit, that.unit) &&
                Objects.equals(notes, that.notes);
    }

    // Переопределение метода hashCode для корректной работы с коллекциями
    @Override
    public int hashCode() {
        return Objects.hash(id, userId, type, value, unit, timestamp, isNormal, notes);
    }

    // Переопределение метода toString для удобного вывода информации об объекте
    @Override
    public String toString() {
        return "HealthParameter{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", type=" + type +
                ", value=" + value +
                ", unit='" + unit + '\'' +
                ", timestamp=" + timestamp +
                ", isNormal=" + isNormal +
                ", notes='" + notes + '\'' +
                '}';
    }
} 