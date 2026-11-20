package com.healthcare.model;

import java.time.LocalDateTime;

/**
 * Abstract base class modeling shared user attributes and behaviors across the system.
 * Demonstrates Abstraction and Inheritance (OOP principles).
 */
public abstract class User {

    protected int id;
    protected String fullName;
    protected String email;
    protected String passwordHash;
    protected String salt;
    protected String phone;
    protected Role role;
    protected LocalDateTime createdAt;

    public User() {
    }

    public User(int id, String fullName, String email, String passwordHash, String salt, String phone, Role role, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.phone = phone;
        this.role = role;
        this.createdAt = createdAt;
    }

    public User(String fullName, String email, String passwordHash, String salt, String phone, Role role) {
        this(0, fullName, email, passwordHash, salt, phone, role, LocalDateTime.now());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", role=" + role +
                '}';
    }
}
