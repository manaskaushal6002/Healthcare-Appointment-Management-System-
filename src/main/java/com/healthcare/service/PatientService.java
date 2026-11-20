package com.healthcare.service;

import com.healthcare.dao.IPatientDAO;
import com.healthcare.dao.impl.PatientDAOImpl;
import com.healthcare.model.Patient;
import com.healthcare.util.UserSession;
import com.healthcare.util.ValidationException;
import com.healthcare.util.ValidationUtils;

import java.util.List;
import java.util.Optional;

/**
 * Service managing patient profile updates and directory lookups.
 */
public class PatientService {

    private final IPatientDAO patientDAO;

    public PatientService() {
        this(new PatientDAOImpl());
    }

    public PatientService(IPatientDAO patientDAO) {
        this.patientDAO = patientDAO;
    }

    public Optional<Patient> getPatientById(int patientId) {
        return patientDAO.findById(patientId);
    }

    public List<Patient> getAllPatients() {
        return patientDAO.findAll();
    }

    /**
     * Validates and updates a patient's personal and medical demographics.
     */
    public boolean updateProfile(Patient patient) throws ValidationException {
        if (patient == null || patient.getId() <= 0) {
            throw new ValidationException("Invalid patient record.");
        }
        if (!ValidationUtils.isNonEmpty(patient.getFullName())) {
            throw new ValidationException("Full name is required.");
        }
        if (!ValidationUtils.isValidPhone(patient.getPhone())) {
            throw new ValidationException("Valid phone number is required.");
        }
        if (!ValidationUtils.isValidBirthDate(patient.getDateOfBirth())) {
            throw new ValidationException("Please provide a valid date of birth.");
        }

        boolean updated = patientDAO.update(patient);
        if (updated && UserSession.isPatient() && UserSession.getCurrentPatient().getId() == patient.getId()) {
            UserSession.setCurrentUser(patient);
        }
        return updated;
    }
}
