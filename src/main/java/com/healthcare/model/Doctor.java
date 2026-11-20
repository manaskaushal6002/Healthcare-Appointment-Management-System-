package com.healthcare.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Entity representing a medical doctor in the hospital directory.
 * Encapsulates practitioner profile, qualifications, pricing, and scheduling shifts.
 */
public class Doctor {

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private String specialization;
    private String qualification;
    private int experienceYears;
    private BigDecimal consultationFee;
    private String availableDays; // e.g. "Monday,Wednesday,Friday"
    private LocalTime shiftStartTime;
    private LocalTime shiftEndTime;
    private boolean active;
    private LocalDateTime createdAt;

    public Doctor() {
        this.active = true;
        this.shiftStartTime = LocalTime.of(9, 0);
        this.shiftEndTime = LocalTime.of(17, 0);
        this.consultationFee = BigDecimal.ZERO;
        this.availableDays = "Monday,Tuesday,Wednesday,Thursday,Friday";
    }

    public Doctor(int id, String fullName, String email, String phone, String specialization,
                  String qualification, int experienceYears, BigDecimal consultationFee,
                  String availableDays, LocalTime shiftStartTime, LocalTime shiftEndTime,
                  boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.specialization = specialization;
        this.qualification = qualification;
        this.experienceYears = experienceYears;
        this.consultationFee = consultationFee;
        this.availableDays = availableDays;
        this.shiftStartTime = shiftStartTime;
        this.shiftEndTime = shiftEndTime;
        this.active = active;
        this.createdAt = createdAt;
    }

    public Doctor(String fullName, String email, String phone, String specialization,
                  String qualification, int experienceYears, BigDecimal consultationFee,
                  String availableDays, LocalTime shiftStartTime, LocalTime shiftEndTime) {
        this(0, fullName, email, phone, specialization, qualification, experienceYears,
                consultationFee, availableDays, shiftStartTime, shiftEndTime, true, LocalDateTime.now());
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public BigDecimal getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(BigDecimal consultationFee) {
        this.consultationFee = consultationFee;
    }

    public String getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(String availableDays) {
        this.availableDays = availableDays;
    }

    public LocalTime getShiftStartTime() {
        return shiftStartTime;
    }

    public void setShiftStartTime(LocalTime shiftStartTime) {
        this.shiftStartTime = shiftStartTime;
    }

    public LocalTime getShiftEndTime() {
        return shiftEndTime;
    }

    public void setShiftEndTime(LocalTime shiftEndTime) {
        this.shiftEndTime = shiftEndTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + specialization + " - " + qualification + ")";
    }
}
