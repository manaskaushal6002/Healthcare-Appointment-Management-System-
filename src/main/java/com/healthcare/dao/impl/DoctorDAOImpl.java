package com.healthcare.dao.impl;

import com.healthcare.config.DatabaseConnection;
import com.healthcare.dao.IDoctorDAO;
import com.healthcare.model.Doctor;
import com.healthcare.util.DataAccessException;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DoctorDAOImpl implements IDoctorDAO {

    @Override
    public int create(Doctor doctor) throws DataAccessException {
        String sql = "INSERT INTO doctors (full_name, email, phone, specialization, qualification, " +
                     "experience_years, consultation_fee, available_days, shift_start_time, shift_end_time, is_active) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, doctor.getFullName());
                pstmt.setString(2, doctor.getEmail().trim().toLowerCase());
                pstmt.setString(3, doctor.getPhone());
                pstmt.setString(4, doctor.getSpecialization());
                pstmt.setString(5, doctor.getQualification());
                pstmt.setInt(6, doctor.getExperienceYears());
                pstmt.setBigDecimal(7, doctor.getConsultationFee());
                pstmt.setString(8, doctor.getAvailableDays());
                pstmt.setTime(9, Time.valueOf(doctor.getShiftStartTime()));
                pstmt.setTime(10, Time.valueOf(doctor.getShiftEndTime()));
                pstmt.setInt(11, doctor.isActive() ? 1 : 0);

                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    throw new DataAccessException("Failed to insert doctor record, no rows affected.");
                }

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        doctor.setId(id);
                        return id;
                    } else {
                        throw new DataAccessException("Failed to insert doctor record, no ID obtained.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while creating doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean update(Doctor doctor) throws DataAccessException {
        String sql = "UPDATE doctors SET full_name = ?, email = ?, phone = ?, specialization = ?, " +
                     "qualification = ?, experience_years = ?, consultation_fee = ?, available_days = ?, " +
                     "shift_start_time = ?, shift_end_time = ?, is_active = ? WHERE doctor_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, doctor.getFullName());
                pstmt.setString(2, doctor.getEmail().trim().toLowerCase());
                pstmt.setString(3, doctor.getPhone());
                pstmt.setString(4, doctor.getSpecialization());
                pstmt.setString(5, doctor.getQualification());
                pstmt.setInt(6, doctor.getExperienceYears());
                pstmt.setBigDecimal(7, doctor.getConsultationFee());
                pstmt.setString(8, doctor.getAvailableDays());
                pstmt.setTime(9, Time.valueOf(doctor.getShiftStartTime()));
                pstmt.setTime(10, Time.valueOf(doctor.getShiftEndTime()));
                pstmt.setInt(11, doctor.isActive() ? 1 : 0);
                pstmt.setInt(12, doctor.getId());

                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while updating doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int doctorId) throws DataAccessException {
        // Soft delete: keep historical appointment associations intact
        String sql = "UPDATE doctors SET is_active = 0 WHERE doctor_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, doctorId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while deactivating doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Doctor> findById(int doctorId) throws DataAccessException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, doctorId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToDoctor(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while finding doctor by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Doctor> findAllActive() throws DataAccessException {
        String sql = "SELECT * FROM doctors WHERE is_active = 1 ORDER BY specialization ASC, full_name ASC";
        return queryDoctors(sql);
    }

    @Override
    public List<Doctor> findAll() throws DataAccessException {
        String sql = "SELECT * FROM doctors ORDER BY doctor_id DESC";
        return queryDoctors(sql);
    }

    @Override
    public List<Doctor> findBySpecialization(String specialization) throws DataAccessException {
        String sql = "SELECT * FROM doctors WHERE is_active = 1 AND LOWER(specialization) = LOWER(?) ORDER BY full_name ASC";
        List<Doctor> doctors = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, specialization);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        doctors.add(mapRowToDoctor(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while filtering doctors by specialization: " + e.getMessage(), e);
        }
        return doctors;
    }

    @Override
    public List<String> getDistinctSpecializations() throws DataAccessException {
        String sql = "SELECT DISTINCT specialization FROM doctors WHERE is_active = 1 ORDER BY specialization ASC";
        List<String> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("specialization"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while fetching specializations: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean existsByEmail(String email, int excludeDoctorId) throws DataAccessException {
        String sql = "SELECT 1 FROM doctors WHERE LOWER(email) = LOWER(?) AND doctor_id != ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                pstmt.setInt(2, excludeDoctorId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error checking doctor email: " + e.getMessage(), e);
        }
    }

    private List<Doctor> queryDoctors(String sql) {
        List<Doctor> doctors = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(mapRowToDoctor(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error fetching doctors: " + e.getMessage(), e);
        }
        return doctors;
    }

    private Doctor mapRowToDoctor(ResultSet rs) throws SQLException {
        int id = rs.getInt("doctor_id");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String phone = rs.getString("phone");
        String specialization = rs.getString("specialization");
        String qualification = rs.getString("qualification");
        int experienceYears = rs.getInt("experience_years");
        BigDecimal fee = rs.getBigDecimal("consultation_fee");
        String days = rs.getString("available_days");
        Time startTimeSql = rs.getTime("shift_start_time");
        LocalTime startTime = startTimeSql != null ? startTimeSql.toLocalTime() : LocalTime.of(9, 0);
        Time endTimeSql = rs.getTime("shift_end_time");
        LocalTime endTime = endTimeSql != null ? endTimeSql.toLocalTime() : LocalTime.of(17, 0);
        boolean active = rs.getInt("is_active") == 1;
        Timestamp createdAtSql = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtSql != null ? createdAtSql.toLocalDateTime() : LocalDateTime.now();

        return new Doctor(id, fullName, email, phone, specialization, qualification,
                experienceYears, fee, days, startTime, endTime, active, createdAt);
    }
}
