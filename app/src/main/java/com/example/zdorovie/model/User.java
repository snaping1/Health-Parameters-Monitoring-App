package com.example.zdorovie.model;

import com.google.firebase.firestore.DocumentId;
import java.util.Date;
import java.util.Objects;

// Класс, представляющий пользователя системы.
public class User {
    // Перечисление ролей пользователей в системе.
    public enum UserRole {
        PATIENT,
        CONTROLLER
    }

    @DocumentId
    private String id;
    private String email;
    private String name;
    private String fullName; // Полное имя пользователя
    private UserRole role;
    private String controllerId; // ID контролера для пациента
    private long createdAt;
    private Date registrationDate; // Дата регистрации

    public User() {
        // Empty constructor needed for Firestore
        this.id = "";
        this.email = "";
        this.name = "";
        this.fullName = "";
        this.role = UserRole.PATIENT;
        this.controllerId = null;
        this.createdAt = System.currentTimeMillis();
        this.registrationDate = new Date(createdAt);
    }

    public User(String id, String email, String name, String fullName, UserRole role, 
                String controllerId, long createdAt, Date registrationDate) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.fullName = fullName;
        this.role = role;
        this.controllerId = controllerId;
        this.createdAt = createdAt;
        this.registrationDate = registrationDate;
    }

    // Copy constructor
    public User(User user) {
        this.id = user.id;
        this.email = user.email;
        this.name = user.name;
        this.fullName = user.fullName;
        this.role = user.role;
        this.controllerId = user.controllerId;
        this.createdAt = user.createdAt;
        this.registrationDate = user.registrationDate;
    }

    // Вложенный класс Builder для создания объектов User
    public static class Builder {
        private String id = "";
        private String email = "";
        private String name = "";
        private String fullName = "";
        private UserRole role = UserRole.PATIENT;
        private String controllerId = null;
        private long createdAt = System.currentTimeMillis();
        private Date registrationDate = new Date(System.currentTimeMillis());

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder controllerId(String controllerId) {
            this.controllerId = controllerId;
            return this;
        }

        public Builder createdAt(long createdAt) {
            this.createdAt = createdAt;
            this.registrationDate = new Date(createdAt);
            return this;
        }

        public User build() {
            return new User(id, email, name, fullName, role, controllerId, 
                          createdAt, registrationDate);
        }
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    // Переопределение метода equals для сравнения объектов User
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return createdAt == user.createdAt &&
                Objects.equals(id, user.id) &&
                Objects.equals(email, user.email) &&
                Objects.equals(name, user.name) &&
                Objects.equals(fullName, user.fullName) &&
                role == user.role &&
                Objects.equals(controllerId, user.controllerId) &&
                Objects.equals(registrationDate, user.registrationDate);
    }

    // Переопределение метода hashCode для корректной работы с коллекциями
    @Override
    public int hashCode() {
        return Objects.hash(id, email, name, fullName, role, controllerId, createdAt, registrationDate);
    }

    // Переопределение метода toString для удобного вывода информации об объекте
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role=" + role +
                ", controllerId='" + controllerId + '\'' +
                ", createdAt=" + createdAt +
                ", registrationDate=" + registrationDate +
                '}';
    }
} 