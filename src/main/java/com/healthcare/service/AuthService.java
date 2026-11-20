package com.healthcare.service;

import com.healthcare.dao.IAdminDAO;
import com.healthcare.dao.IPatientDAO;
import com.healthcare.dao.impl.AdminDAOImpl;
import com.healthcare.dao.impl.PatientDAOImpl;
import com.healthcare.model.Admin;
import com.healthcare.model.Patient;
import com.healthcare.model.Role;
import com.healthcare.model.User;
import com.healthcare.util.*;

import java.util.Optional;

/**
 * Authentication and Registration business logic.
 * Enforces credential verification, password hashing, and user registration constraints.
 */
public class AuthService {

    private final IPatientDAO patientDAO;
    private final IAdminDAO adminDAO;

    public AuthService() {
        this(new PatientDAOImpl(), new AdminDAOImpl());
    }

    public AuthService(IPatientDAO patientDAO, IAdminDAO adminDAO) {
        this.patientDAO = patientDAO;
        this.adminDAO = adminDAO;
    }

    /**
     * Authenticates a user based on role and credentials.
     *
     * @param identifier Email for patients; Username or Email for administrators
     * @param rawPassword Plain text password entered in the UI
     * @param role Expected role (PATIENT or ADMIN)
     * @return Authenticated User entity (Patient or Admin)
     */
    public User login(String identifier, String rawPassword, Role role)
            throws ValidationException, AuthenticationException {

        if (!ValidationUtils.isNonEmpty(identifier)) {
            throw new ValidationException(role == Role.PATIENT ? "Email is required." : "Username/Email is required.");
        }
        if (!ValidationUtils.isNonEmpty(rawPassword)) {
            throw new ValidationException("Password is required.");
        }

        if (role == Role.PATIENT) {
            Optional<Patient> opt = patientDAO.findByEmail(identifier.trim());
            if (opt.isEmpty()) {
                throw new AuthenticationException("No patient account found with email: " + identifier);
            }
            Patient patient = opt.get();
            if (!PasswordUtils.verifyPassword(rawPassword, patient.getSalt(), patient.getPasswordHash())) {
                throw new AuthenticationException("Invalid password. Please try again.");
            }
            UserSession.setCurrentUser(patient);
            return patient;
        } else if (role == Role.ADMIN) {
            Optional<Admin> opt = adminDAO.findByUsername(identifier.trim());
            if (opt.isEmpty()) {
                opt = adminDAO.findByEmail(identifier.trim());
            }
            if (opt.isEmpty()) {
                throw new AuthenticationException("No administrator account found with identifier: " + identifier);
            }
            Admin admin = opt.get();
            if (!PasswordUtils.verifyPassword(rawPassword, admin.getSalt(), admin.getPasswordHash())) {
                throw new AuthenticationException("Invalid password. Please try again.");
            }
            UserSession.setCurrentUser(admin);
            return admin;
        }

        throw new AuthenticationException("Unsupported system role.");
    }

    /**
     * Registers a new patient.
     * Validates fields, checks for duplicate emails, and hashes the password before persistence.
     */
    public Patient registerPatient(Patient patient, String rawPassword) throws ValidationException {
        if (patient == null) {
            throw new ValidationException("Patient information cannot be null.");
        }
        if (!ValidationUtils.isNonEmpty(patient.getFullName())) {
            throw new ValidationException("Full name is required.");
        }
        if (!ValidationUtils.isValidEmail(patient.getEmail())) {
            throw new ValidationException("A valid email address is required (e.g., name@example.com).");
        }
        if (patientDAO.existsByEmail(patient.getEmail())) {
            throw new ValidationException("An account with email '" + patient.getEmail() + "' already exists.");
        }
        if (!ValidationUtils.isValidPhone(patient.getPhone())) {
            throw new ValidationException("A valid contact phone number is required (at least 7 digits).");
        }
        if (!ValidationUtils.isValidBirthDate(patient.getDateOfBirth())) {
            throw new ValidationException("Please provide a valid date of birth in the past.");
        }
        if (!ValidationUtils.isStrongPassword(rawPassword)) {
            throw new ValidationException("Password must be at least 6 characters in length.");
        }

        // Generate per-user salt and salted hash
        String salt = PasswordUtils.generateSalt();
        String passwordHash = PasswordUtils.hashPassword(rawPassword, salt);

        patient.setSalt(salt);
        patient.setPasswordHash(passwordHash);

        int generatedId = patientDAO.create(patient);
        patient.setId(generatedId);
        return patient;
    }
}
