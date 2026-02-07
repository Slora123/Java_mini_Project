package com.recyclingquest.dao;

import com.recyclingquest.db.Database;
import com.recyclingquest.model.TradeEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TradeDao {
    private final Database db;

    public TradeDao(Database db) {
        this.db = db;
    }

    public void insert(TradeEntry e) {
        String sql = "INSERT INTO trade_entries(user_id, item, amount, created_at) VALUES(?,?,?,?)";
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, e.getUserId());
            ps.setString(2, e.getItem());
            ps.setDouble(3, e.getAmount());
            ps.setLong(4, e.getCreatedAt());
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<TradeEntry> listByUser(long userId) {
        String sql = "SELECT id, user_id, item, amount, created_at FROM trade_entries WHERE user_id = ? ORDER BY created_at DESC";
        List<TradeEntry> list = new ArrayList<>();
        try (Connection c = db.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TradeEntry e = new TradeEntry();
                    e.setId(rs.getLong("id"));
                    e.setUserId(rs.getLong("user_id"));
                    e.setItem(rs.getString("item"));
                    e.setAmount(rs.getDouble("amount"));
                    e.setCreatedAt(rs.getLong("created_at"));
                    list.add(e);
                }
            }
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return list;
    }
}
