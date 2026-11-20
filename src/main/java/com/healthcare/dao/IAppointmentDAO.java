package com.healthcare.dao;

import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.util.DataAccessException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Appointment scheduling and lifecycle management.
 */
public interface IAppointmentDAO {

    /**
     * Books a new appointment.
     * @return generated appointment_id
     */
    int create(Appointment appointment) throws DataAccessException;

    /**
     * Retrieves an appointment by ID with joined patient and doctor details.
     */
    Optional<Appointment> findById(int appointmentId) throws DataAccessException;

    /**
     * Retrieves appointment history for a specific patient.
     */
    List<Appointment> findByPatientId(int patientId) throws DataAccessException;

    /**
     * Retrieves all appointments in the system for administrative tracking.
     */
    List<Appointment> findAll() throws DataAccessException;

    /**
     * Checks if a doctor is already booked for a specific date and time slot.
     * Only considers active appointments (excludes CANCELLED).
     */
    boolean isSlotBooked(int doctorId, LocalDate date, LocalTime time) throws DataAccessException;

    /**
     * Retrieves all booked timeslots for a doctor on a specific date.
     */
    List<LocalTime> getBookedTimeslots(int doctorId, LocalDate date) throws DataAccessException;

    /**
     * Updates the lifecycle status and optional notes of an appointment.
     */
    boolean updateStatus(int appointmentId, AppointmentStatus status, String notes) throws DataAccessException;

    /**
     * Cancels an appointment.
     */
    boolean cancel(int appointmentId) throws DataAccessException;
}
