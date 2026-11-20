package com.healthcare.dao;

import com.healthcare.model.Admin;
import com.healthcare.util.DataAccessException;

import java.util.Optional;

/**
 * Data Access Object interface for Administrator authentication and profile retrieval.
 */
public interface IAdminDAO {

    /**
     * Finds an administrator by username.
     */
    Optional<Admin> findByUsername(String username) throws DataAccessException;

    /**
     * Finds an administrator by email address.
     */
    Optional<Admin> findByEmail(String email) throws DataAccessException;

    /**
     * Finds an administrator by primary key ID.
     */
    Optional<Admin> findById(int adminId) throws DataAccessException;
}
