package com.recyclingquest.dao;

import com.recyclingquest.db.Database;
import com.recyclingquest.model.RecycleEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RecycleDao {
    private final Database db;

    public RecycleDao(Database db) {
        this.db = db;
    }

    public void insert(RecycleEntry e) {
        String sql = "INSERT INTO recycle_entries(user_id, category, weight_kg, photo_path, created_at) VALUES(?,?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, e.getUserId());
            ps.setString(2, e.getCategory());
            ps.setDouble(3, e.getWeightKg());
            ps.setString(4, e.getPhotoPath());
            ps.setLong(5, e.getCreatedAt());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<RecycleEntry> listByUser(long userId) {
        String sql = "SELECT id, user_id, category, weight_kg, photo_path, created_at FROM recycle_entries WHERE user_id = ? ORDER BY created_at DESC";
        List<RecycleEntry> list = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    RecycleEntry e = new RecycleEntry();
                    e.setId(rs.getLong("id"));
                    e.setUserId(rs.getLong("user_id"));
                    e.setCategory(rs.getString("category"));
                    e.setWeightKg(rs.getDouble("weight_kg"));
                    e.setPhotoPath(rs.getString("photo_path"));
                    e.setCreatedAt(rs.getLong("created_at"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }

    public void update(RecycleEntry e) {
        String sql = "UPDATE recycle_entries SET category = ?, weight_kg = ?, photo_path = ? WHERE id = ? AND user_id = ?";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getCategory());
            ps.setDouble(2, e.getWeightKg());
            ps.setString(3, e.getPhotoPath());
            ps.setLong(4, e.getId());
            ps.setLong(5, e.getUserId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }
}
