package com.recyclingquest.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private final String url;

    public Database() {
        String baseDir = System.getProperty("user.dir") + File.separator + "data";
        File dir = new File(baseDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.url = "jdbc:sqlite:" + baseDir + File.separator + "recycling.db";
    }

    public Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(url);
        // Configure connection to reduce locking issues and enforce constraints
        try (Statement st = conn.createStatement()) {
            // Enable foreign key constraints
            st.execute("PRAGMA foreign_keys = ON");
            // Use WAL journal mode to allow concurrent readers during writes
            st.execute("PRAGMA journal_mode = WAL");
            // Set a busy timeout so SQLite waits for the lock instead of failing immediately
            st.execute("PRAGMA busy_timeout = 5000");
            // Reasonable sync level for desktop apps to reduce fsync overhead
            st.execute("PRAGMA synchronous = NORMAL");
        } catch (SQLException ignored) {
            // If pragmas are not supported, continue with the connection anyway
        }
        return conn;
    }
}
