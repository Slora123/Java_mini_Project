package com.recyclingquest.db;

import java.sql.*;

public class Migrations {
    public static void run(Database db) {
        try (Connection conn = db.getConnection(); Statement st = conn.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "nickname TEXT NOT NULL UNIQUE," +
                    "avatar TEXT," +
                    "city TEXT," +
                    "points INTEGER NOT NULL DEFAULT 0" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS recycle_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "category TEXT NOT NULL," +
                    "weight_kg REAL NOT NULL," +
                    "photo_path TEXT," +
                    "created_at INTEGER NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS trade_entries (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "item TEXT NOT NULL," +
                    "amount REAL NOT NULL," +
                    "created_at INTEGER NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS spots (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "address TEXT NOT NULL," +
                    "city TEXT NOT NULL" +
                    ")");

            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_recycle_user ON recycle_entries(user_id)");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_trade_user ON trade_entries(user_id)");

            // Coupons table for collaboration rewards
            st.executeUpdate("CREATE TABLE IF NOT EXISTS coupons (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "level_required TEXT NOT NULL," +
                    "company_name TEXT NOT NULL," +
                    "description TEXT," +
                    "discount TEXT," +
                    "coupon_code TEXT," +
                    "expiry_date TEXT," +
                    "is_claimed INTEGER NOT NULL DEFAULT 0," +
                    "logo_url TEXT" +
                    ")");

            // Seed dummy coupons if empty
            try (ResultSet rs = st.executeQuery("SELECT COUNT(1) AS n FROM coupons")) {
                if (rs.next() && rs.getInt("n") == 0) {
                    st.executeUpdate("INSERT INTO coupons(level_required, company_name, description, discount, coupon_code, expiry_date, is_claimed, logo_url) VALUES" +
                            "('Bronze','Eco Cafe','10% off on any drink','10% OFF','BRONZE10','2030-12-31',0,'https://example.com/cafe.png')," +
                            "('Silver','Green Threads','15% off on apparel','15% OFF','SILVER15','2030-12-31',0,'https://example.com/threads.png')," +
                            "('Gold','Eco Mart','₹500 voucher on orders over ₹2000','₹500 Voucher','GOLD500','2030-12-31',0,'https://example.com/ecomart.png')," +
                            "('Green Hero','Earth NGO','VIP pass to annual eco event','Event Pass','HERO-EVENT','2030-12-31',0,'https://example.com/ngo.png')");
                }
            }
            ensureColumn(conn, "users", "email", "ALTER TABLE users ADD COLUMN email TEXT");
            ensureColumn(conn, "users", "password", "ALTER TABLE users ADD COLUMN password TEXT");

            // User coupons table to track redeemed coupons
            st.executeUpdate("CREATE TABLE IF NOT EXISTS user_coupons (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "user_id INTEGER NOT NULL," +
                    "coupon_id INTEGER NOT NULL," +
                    "redeemed_at INTEGER NOT NULL," +
                    "FOREIGN KEY(user_id) REFERENCES users(id)," +
                    "FOREIGN KEY(coupon_id) REFERENCES coupons(id)" +
                    ")");
            st.executeUpdate("CREATE INDEX IF NOT EXISTS idx_user_coupons_user ON user_coupons(user_id)");

            // Ensure unique index on email (ignore NULL emails)
            try (Statement s = conn.createStatement()) {
                s.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email ON users(email)");
            } catch (SQLException ignored) {
                // Some SQLite builds allow partial indexes; if needed, fallback without WHERE clause already above
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void ensureColumn(Connection conn, String table, String column, String alterSql) throws SQLException {
        String check = "PRAGMA table_info(" + table + ")";
        boolean exists = false;
        try (Statement s = conn.createStatement(); ResultSet rs = s.executeQuery(check)) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (column.equalsIgnoreCase(name)) {
                    exists = true;
                    break;
                }
            }
        }
        if (!exists) {
            try (Statement s = conn.createStatement()) {
                s.executeUpdate(alterSql);
            }
        }
    }
}
