package com.healthcare.dao;

import com.healthcare.model.Doctor;
import com.healthcare.util.DataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Doctor directory operations.
 */
public interface IDoctorDAO {

    /**
     * Inserts a new doctor record (Admin only).
     * @return generated doctor_id
     */
    int create(Doctor doctor) throws DataAccessException;

    /**
     * Updates doctor profile details, fees, or working schedule.
     */
    boolean update(Doctor doctor) throws DataAccessException;

    /**
     * Soft-deletes a doctor by setting is_active = 0.
     */
    boolean delete(int doctorId) throws DataAccessException;

    /**
     * Finds a doctor by ID.
     */
    Optional<Doctor> findById(int doctorId) throws DataAccessException;

    /**
     * Retrieves all active doctors available for patient bookings.
     */
    List<Doctor> findAllActive() throws DataAccessException;

    /**
     * Retrieves all doctors including inactive ones (Admin view).
     */
    List<Doctor> findAll() throws DataAccessException;

    /**
     * Filters active doctors by medical specialization.
     */
    List<Doctor> findBySpecialization(String specialization) throws DataAccessException;

    /**
     * Returns a list of distinct specializations present in the system.
     */
    List<String> getDistinctSpecializations() throws DataAccessException;

    /**
     * Checks if another doctor already uses the given email address.
     */
    boolean existsByEmail(String email, int excludeDoctorId) throws DataAccessException;
}
