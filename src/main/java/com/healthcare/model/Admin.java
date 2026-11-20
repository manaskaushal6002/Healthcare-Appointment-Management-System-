package com.healthcare.model;

import java.time.LocalDateTime;

/**
 * Concrete entity representing a system administrator.
 * Inherits core identity and credentials from User.
 */
public class Admin extends User {

    private String username;

    public Admin() {
        super();
        this.role = Role.ADMIN;
    }

    public Admin(int id, String username, String fullName, String email, String passwordHash, String salt, LocalDateTime createdAt) {
        super(id, fullName, email, passwordHash, salt, "", Role.ADMIN, createdAt);
        this.username = username;
    }

    public Admin(String username, String fullName, String email, String passwordHash, String salt) {
        super(fullName, email, passwordHash, salt, "", Role.ADMIN);
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
