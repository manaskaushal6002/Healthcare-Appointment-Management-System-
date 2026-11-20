package com.healthcare.dao;

import com.healthcare.model.Patient;
import com.healthcare.util.DataAccessException;

import java.util.List;
import java.util.Optional;

/**
 * Data Access Object interface for Patient entity persistence operations.
 */
public interface IPatientDAO {

    /**
     * Inserts a new patient record into the database.
     * @param patient patient entity
     * @return generated patient_id
     */
    int create(Patient patient) throws DataAccessException;

    /**
     * Finds a patient by primary key ID.
     */
    Optional<Patient> findById(int patientId) throws DataAccessException;

    /**
     * Finds a patient by email address (used for login and uniqueness checks).
     */
    Optional<Patient> findByEmail(String email) throws DataAccessException;

    /**
     * Updates an existing patient profile.
     */
    boolean update(Patient patient) throws DataAccessException;

    /**
     * Retrieves all registered patients (for admin dashboard).
     */
    List<Patient> findAll() throws DataAccessException;

    /**
     * Checks if a patient already exists with the given email.
     */
    boolean existsByEmail(String email) throws DataAccessException;
}
