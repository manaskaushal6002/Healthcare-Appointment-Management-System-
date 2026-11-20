package com.healthcare.dao.impl;

import com.healthcare.config.DatabaseConnection;
import com.healthcare.dao.IPatientDAO;
import com.healthcare.model.Gender;
import com.healthcare.model.Patient;
import com.healthcare.util.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PatientDAOImpl implements IPatientDAO {

    @Override
    public int create(Patient patient) throws DataAccessException {
        String sql = "INSERT INTO patients (full_name, email, password_hash, salt, phone, date_of_birth, gender, blood_group, address) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, patient.getFullName());
                pstmt.setString(2, patient.getEmail().trim().toLowerCase());
                pstmt.setString(3, patient.getPasswordHash());
                pstmt.setString(4, patient.getSalt());
                pstmt.setString(5, patient.getPhone());
                pstmt.setDate(6, Date.valueOf(patient.getDateOfBirth()));
                pstmt.setString(7, patient.getGender().name());
                pstmt.setString(8, patient.getBloodGroup());
                pstmt.setString(9, patient.getAddress());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new DataAccessException("Failed to insert patient record, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        patient.setId(id);
                        return id;
                    } else {
                        throw new DataAccessException("Failed to insert patient record, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while creating patient: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Patient> findById(int patientId) throws DataAccessException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToPatient(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while finding patient by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Patient> findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM patients WHERE LOWER(email) = LOWER(?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToPatient(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while finding patient by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public boolean update(Patient patient) throws DataAccessException {
        String sql = "UPDATE patients SET full_name = ?, phone = ?, date_of_birth = ?, gender = ?, blood_group = ?, address = ? " +
                     "WHERE patient_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, patient.getFullName());
                pstmt.setString(2, patient.getPhone());
                pstmt.setDate(3, Date.valueOf(patient.getDateOfBirth()));
                pstmt.setString(4, patient.getGender().name());
                pstmt.setString(5, patient.getBloodGroup());
                pstmt.setString(6, patient.getAddress());
                pstmt.setInt(7, patient.getId());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while updating patient: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Patient> findAll() throws DataAccessException {
        String sql = "SELECT * FROM patients ORDER BY full_name ASC";
        List<Patient> patients = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    patients.add(mapRowToPatient(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while fetching all patients: " + e.getMessage(), e);
        }
        return patients;
    }

    @Override
    public boolean existsByEmail(String email) throws DataAccessException {
        String sql = "SELECT 1 FROM patients WHERE LOWER(email) = LOWER(?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while checking patient email existence: " + e.getMessage(), e);
        }
    }

    private Patient mapRowToPatient(ResultSet rs) throws SQLException {
        int id = rs.getInt("patient_id");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        String phone = rs.getString("phone");
        Date dobSql = rs.getDate("date_of_birth");
        LocalDate dob = dobSql != null ? dobSql.toLocalDate() : null;
        Gender gender = Gender.fromString(rs.getString("gender"));
        String bloodGroup = rs.getString("blood_group");
        String address = rs.getString("address");
        Timestamp createdAtSql = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtSql != null ? createdAtSql.toLocalDateTime() : LocalDateTime.now();

        return new Patient(id, fullName, email, passwordHash, salt, phone, dob, gender, bloodGroup, address, createdAt);
    }
}
