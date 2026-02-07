package com.recyclingquest.dao;

import com.recyclingquest.db.Database;
import com.recyclingquest.model.Spot;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SpotDao {
    private final Database db;

    public SpotDao(Database db) {
        this.db = db;
    }

    public void seedIfEmpty() {
        String countSql = "SELECT COUNT(*) FROM spots";
        try (Connection c = db.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery(countSql)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String ins = "INSERT INTO spots(name, address, city) VALUES(?,?,?)";
                    try (PreparedStatement ps = c.prepareStatement(ins)) {
                        insertSpot(ps, "Green Cycle Center", "123 Park St", "Metro City");
                        insertSpot(ps, "Eco DropPoint", "45 River Ave", "Metro City");
                        insertSpot(ps, "Reclaim Hub", "9 Sunset Blvd", "Metro City");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void insertSpot(PreparedStatement ps, String name, String address, String city) throws SQLException {
        ps.setString(1, name);
        ps.setString(2, address);
        ps.setString(3, city);
        ps.addBatch();
        ps.executeBatch();
    }

    public List<Spot> listAll() {
        String sql = "SELECT id, name, address, city FROM spots ORDER BY city, name";
        List<Spot> list = new ArrayList<>();
        try (Connection c = db.getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Spot(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("address"),
                        rs.getString("city")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
