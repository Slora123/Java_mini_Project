package com.recyclingquest.dao;

import com.recyclingquest.db.Database;
import com.recyclingquest.model.User;

import java.sql.*;
import java.util.Optional;

public class UserDao {
    private final Database db;

    public UserDao(Database db) {
        this.db = db;
    }

    public User create(String nickname, String avatar, String city, String email, String password) {
        // Normalize email to lowercase to avoid case-sensitive mismatches
        if (email != null) {
            email = email.trim().toLowerCase();
        }
        String sql = "INSERT INTO users(nickname, avatar, city, points, email, password) VALUES(?,?,?,0,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nickname);
            ps.setString(2, avatar);
            ps.setString(3, city);
            ps.setString(4, email);
            ps.setString(5, password);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    return new User(id, nickname, avatar, city, 0, email, password);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Failed to create user");
    }

    public Optional<User> findByNickname(String nickname) {
        String sql = "SELECT id, nickname, avatar, city, points, email, password FROM users WHERE nickname = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nickname);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) return Optional.empty();
        email = email.trim().toLowerCase();
        // Case-insensitive lookup on email
        String sql = "SELECT id, nickname, avatar, city, points, email, password FROM users WHERE LOWER(email) = LOWER(?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public Optional<User> findById(long id) {
        String sql = "SELECT id, nickname, avatar, city, points, email, password FROM users WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    public void addPoints(long userId, int delta) {
        String sql = "UPDATE users SET points = points + ? WHERE id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("nickname"),
                rs.getString("avatar"),
                rs.getString("city"),
                rs.getInt("points"),
                rs.getString("email"),
                rs.getString("password")
        );
    }
}
