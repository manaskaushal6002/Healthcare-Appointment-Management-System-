package com.healthcare.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Concrete entity representing a registered patient in the healthcare system.
 * Inherits authentication and core personal details from User.
 */
public class Patient extends User {

    private LocalDate dateOfBirth;
    private Gender gender;
    private String bloodGroup;
    private String address;

    public Patient() {
        super();
        this.role = Role.PATIENT;
    }

    public Patient(int id, String fullName, String email, String passwordHash, String salt, String phone,
                   LocalDate dateOfBirth, Gender gender, String bloodGroup, String address, LocalDateTime createdAt) {
        super(id, fullName, email, passwordHash, salt, phone, Role.PATIENT, createdAt);
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
    }

    public Patient(String fullName, String email, String passwordHash, String salt, String phone,
                   LocalDate dateOfBirth, Gender gender, String bloodGroup, String address) {
        super(fullName, email, passwordHash, salt, phone, Role.PATIENT);
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.bloodGroup = bloodGroup;
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Calculates patient's age in years based on date of birth.
     */
    public int getAge() {
        if (dateOfBirth == null) {
            return 0;
        }
        return Period.between(dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", age=" + getAge() +
                ", gender=" + gender +
                ", bloodGroup='" + bloodGroup + '\'' +
                '}';
    }
}
