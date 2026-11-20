package com.healthcare.service;

import com.healthcare.dao.IAppointmentDAO;
import com.healthcare.dao.IDoctorDAO;
import com.healthcare.dao.impl.AppointmentDAOImpl;
import com.healthcare.dao.impl.DoctorDAOImpl;
import com.healthcare.model.Doctor;
import com.healthcare.util.ValidationException;
import com.healthcare.util.ValidationUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service managing doctor catalogs, working schedules, and availability timeslots.
 */
public class DoctorService {

    private final IDoctorDAO doctorDAO;
    private final IAppointmentDAO appointmentDAO;

    public DoctorService() {
        this(new DoctorDAOImpl(), new AppointmentDAOImpl());
    }

    public DoctorService(IDoctorDAO doctorDAO, IAppointmentDAO appointmentDAO) {
        this.doctorDAO = doctorDAO;
        this.appointmentDAO = appointmentDAO;
    }

    public List<Doctor> getActiveDoctors() {
        return doctorDAO.findAllActive();
    }

    public List<Doctor> getAllDoctors() {
        return doctorDAO.findAll();
    }

    public Optional<Doctor> getDoctorById(int id) {
        return doctorDAO.findById(id);
    }

    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        if (!ValidationUtils.isNonEmpty(specialization) || "All Specializations".equalsIgnoreCase(specialization)) {
            return getActiveDoctors();
        }
        return doctorDAO.findBySpecialization(specialization);
    }

    public List<String> getAllSpecializations() {
        return doctorDAO.getDistinctSpecializations();
    }

    /**
     * Validates and persists a doctor profile (insert or update).
     */
    public Doctor saveDoctor(Doctor doctor) throws ValidationException {
        if (doctor == null) {
            throw new ValidationException("Doctor details cannot be null.");
        }
        if (!ValidationUtils.isNonEmpty(doctor.getFullName())) {
            throw new ValidationException("Doctor full name is required.");
        }
        if (!ValidationUtils.isValidEmail(doctor.getEmail())) {
            throw new ValidationException("A valid professional email is required.");
        }
        if (doctorDAO.existsByEmail(doctor.getEmail(), doctor.getId())) {
            throw new ValidationException("Email '" + doctor.getEmail() + "' is already assigned to another doctor.");
        }
        if (!ValidationUtils.isValidPhone(doctor.getPhone())) {
            throw new ValidationException("Valid phone number is required.");
        }
        if (!ValidationUtils.isNonEmpty(doctor.getSpecialization())) {
            throw new ValidationException("Specialization is required.");
        }
        if (!ValidationUtils.isNonEmpty(doctor.getQualification())) {
            throw new ValidationException("Medical qualifications are required (e.g., MBBS, MD).");
        }
        if (doctor.getExperienceYears() < 0) {
            throw new ValidationException("Experience years cannot be negative.");
        }
        if (!ValidationUtils.isPositiveFee(doctor.getConsultationFee())) {
            throw new ValidationException("Consultation fee must be non-negative.");
        }
        if (doctor.getShiftStartTime() == null || doctor.getShiftEndTime() == null) {
            throw new ValidationException("Shift start and end times must be provided.");
        }
        if (!doctor.getShiftStartTime().isBefore(doctor.getShiftEndTime())) {
            throw new ValidationException("Shift start time must be before shift end time.");
        }
        if (!ValidationUtils.isNonEmpty(doctor.getAvailableDays())) {
            throw new ValidationException("Please specify at least one available working day.");
        }

        if (doctor.getId() == 0) {
            int newId = doctorDAO.create(doctor);
            doctor.setId(newId);
        } else {
            doctorDAO.update(doctor);
        }
        return doctor;
    }

    public boolean deactivateDoctor(int doctorId) {
        return doctorDAO.delete(doctorId);
    }

    /**
     * Checks if a doctor works on a specific day of the week.
     */
    public boolean isDoctorWorkingOnDay(Doctor doctor, LocalDate date) {
        if (doctor == null || date == null || doctor.getAvailableDays() == null) {
            return false;
        }
        DayOfWeek day = date.getDayOfWeek();
        String dayName = day.name(); // e.g. MONDAY
        String available = doctor.getAvailableDays().toUpperCase();
        return available.contains(dayName) || available.contains(dayName.substring(0, 3));
    }

    /**
     * Computes real-time available 30-minute booking slots for a doctor on a specific date,
     * filtering out booked slots and past slots for today.
     */
    public List<LocalTime> getAvailableTimeSlots(Doctor doctor, LocalDate date) {
        List<LocalTime> slots = new ArrayList<>();
        if (doctor == null || date == null || !doctor.isActive()) {
            return slots;
        }

        // Verify working day
        if (!isDoctorWorkingOnDay(doctor, date)) {
            return slots;
        }

        // Get slots already booked
        List<LocalTime> bookedSlots = appointmentDAO.getBookedTimeslots(doctor.getId(), date);

        LocalTime current = doctor.getShiftStartTime();
        LocalTime end = doctor.getShiftEndTime();
        LocalTime now = LocalTime.now();
        boolean isToday = date.isEqual(LocalDate.now());

        while (current.isBefore(end)) {
            // If date is today, skip times that have already elapsed
            if (!isToday || current.isAfter(now)) {
                // If not booked, it's open for scheduling
                if (!bookedSlots.contains(current)) {
                    slots.add(current);
                }
            }
            current = current.plusMinutes(30);
        }

        return slots;
    }
}
