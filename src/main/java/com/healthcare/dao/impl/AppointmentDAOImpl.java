package com.healthcare.dao.impl;

import com.healthcare.config.DatabaseConnection;
import com.healthcare.dao.IAppointmentDAO;
import com.healthcare.model.Appointment;
import com.healthcare.model.AppointmentStatus;
import com.healthcare.util.DataAccessException;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppointmentDAOImpl implements IAppointmentDAO {

    private static final String BASE_JOIN_QUERY =
            "SELECT a.appointment_id, a.patient_id, a.doctor_id, a.appointment_date, a.appointment_time, " +
            "       a.status, a.reason, a.notes, a.created_at, a.updated_at, " +
            "       p.full_name AS patient_name, p.phone AS patient_phone, " +
            "       d.full_name AS doctor_name, d.specialization AS doctor_specialization, d.consultation_fee " +
            "FROM appointments a " +
            "JOIN patients p ON a.patient_id = p.patient_id " +
            "JOIN doctors d ON a.doctor_id = d.doctor_id ";

    @Override
    public int create(Appointment appointment) throws DataAccessException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, appointment_date, appointment_time, status, reason, notes) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, appointment.getPatientId());
                pstmt.setInt(2, appointment.getDoctorId());
                pstmt.setDate(3, Date.valueOf(appointment.getAppointmentDate()));
                pstmt.setTime(4, Time.valueOf(appointment.getAppointmentTime()));
                pstmt.setString(5, appointment.getStatus().name());
                pstmt.setString(6, appointment.getReason());
                pstmt.setString(7, appointment.getNotes());

                int affected = pstmt.executeUpdate();
                if (affected == 0) {
                    throw new DataAccessException("Booking failed, no rows inserted.");
                }

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        appointment.setAppointmentId(id);
                        return id;
                    } else {
                        throw new DataAccessException("Booking failed, no ID returned.");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error while creating appointment: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Appointment> findById(int appointmentId) throws DataAccessException {
        String sql = BASE_JOIN_QUERY + "WHERE a.appointment_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, appointmentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToAppointment(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error finding appointment by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public List<Appointment> findByPatientId(int patientId) throws DataAccessException {
        String sql = BASE_JOIN_QUERY + "WHERE a.patient_id = ? ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, patientId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapRowToAppointment(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error retrieving patient appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public List<Appointment> findAll() throws DataAccessException {
        String sql = BASE_JOIN_QUERY + "ORDER BY a.appointment_date DESC, a.appointment_time DESC";
        List<Appointment> list = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToAppointment(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error retrieving all appointments: " + e.getMessage(), e);
        }
        return list;
    }

    @Override
    public boolean isSlotBooked(int doctorId, LocalDate date, LocalTime time) throws DataAccessException {
        String sql = "SELECT 1 FROM appointments " +
                     "WHERE doctor_id = ? AND appointment_date = ? AND appointment_time = ? AND status != 'CANCELLED' " +
                     "LIMIT 1";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, doctorId);
                pstmt.setDate(2, Date.valueOf(date));
                pstmt.setTime(3, Time.valueOf(time));
                try (ResultSet rs = pstmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error checking slot conflict: " + e.getMessage(), e);
        }
    }

    @Override
    public List<LocalTime> getBookedTimeslots(int doctorId, LocalDate date) throws DataAccessException {
        String sql = "SELECT appointment_time FROM appointments " +
                     "WHERE doctor_id = ? AND appointment_date = ? AND status != 'CANCELLED'";
        List<LocalTime> bookedSlots = new ArrayList<>();
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, doctorId);
                pstmt.setDate(2, Date.valueOf(date));
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        Time sqlTime = rs.getTime("appointment_time");
                        if (sqlTime != null) {
                            bookedSlots.add(sqlTime.toLocalTime());
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error retrieving booked slots: " + e.getMessage(), e);
        }
        return bookedSlots;
    }

    @Override
    public boolean updateStatus(int appointmentId, AppointmentStatus status, String notes) throws DataAccessException {
        String sql = "UPDATE appointments SET status = ?, notes = COALESCE(?, notes) WHERE appointment_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, status.name());
                pstmt.setString(2, notes);
                pstmt.setInt(3, appointmentId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error updating appointment status: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean cancel(int appointmentId) throws DataAccessException {
        String sql = "UPDATE appointments SET status = 'CANCELLED' WHERE appointment_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, appointmentId);
                return pstmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error cancelling appointment: " + e.getMessage(), e);
        }
    }

    private Appointment mapRowToAppointment(ResultSet rs) throws SQLException {
        int id = rs.getInt("appointment_id");
        int patientId = rs.getInt("patient_id");
        int doctorId = rs.getInt("doctor_id");
        Date dateSql = rs.getDate("appointment_date");
        LocalDate date = dateSql != null ? dateSql.toLocalDate() : null;
        Time timeSql = rs.getTime("appointment_time");
        LocalTime time = timeSql != null ? timeSql.toLocalTime() : null;
        AppointmentStatus status = AppointmentStatus.fromString(rs.getString("status"));
        String reason = rs.getString("reason");
        String notes = rs.getString("notes");
        Timestamp createdAtSql = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtSql != null ? createdAtSql.toLocalDateTime() : LocalDateTime.now();
        Timestamp updatedAtSql = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = updatedAtSql != null ? updatedAtSql.toLocalDateTime() : LocalDateTime.now();

        Appointment appt = new Appointment(id, patientId, doctorId, date, time, status, reason, notes, createdAt, updatedAt);

        // Joined fields
        appt.setPatientName(rs.getString("patient_name"));
        appt.setPatientPhone(rs.getString("patient_phone"));
        appt.setDoctorName(rs.getString("doctor_name"));
        appt.setDoctorSpecialization(rs.getString("doctor_specialization"));
        appt.setConsultationFee(rs.getBigDecimal("consultation_fee"));

        return appt;
    }
}
