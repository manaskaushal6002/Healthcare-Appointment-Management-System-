package com.healthcare.dao.impl;

import com.healthcare.config.DatabaseConnection;
import com.healthcare.dao.IAdminDAO;
import com.healthcare.model.Admin;
import com.healthcare.util.DataAccessException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class AdminDAOImpl implements IAdminDAO {

    @Override
    public Optional<Admin> findByUsername(String username) throws DataAccessException {
        String sql = "SELECT * FROM admins WHERE LOWER(username) = LOWER(?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, username.trim().toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToAdmin(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error finding admin by username: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Admin> findByEmail(String email) throws DataAccessException {
        String sql = "SELECT * FROM admins WHERE LOWER(email) = LOWER(?)";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, email.trim().toLowerCase());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToAdmin(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error finding admin by email: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Admin> findById(int adminId) throws DataAccessException {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";
        try {
            Connection conn = DatabaseConnection.getInstance().getConnection();
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, adminId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapRowToAdmin(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error finding admin by ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    private Admin mapRowToAdmin(ResultSet rs) throws SQLException {
        int id = rs.getInt("admin_id");
        String username = rs.getString("username");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String passwordHash = rs.getString("password_hash");
        String salt = rs.getString("salt");
        Timestamp createdAtSql = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdAtSql != null ? createdAtSql.toLocalDateTime() : LocalDateTime.now();

        return new Admin(id, username, fullName, email, passwordHash, salt, createdAt);
    }
}
