package com.expensetracker.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Loads DB config from src/main/resources/application.properties and
 * hands out plain JDBC connections. No connection pool / ORM by design —
 * keeps the project dependency-light and easy to explain in a viva.
 */
public class DatabaseManager {

    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);

    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager() {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in == null) {
                throw new IllegalStateException("application.properties not found on classpath");
            }
            props.load(in);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }

        this.url = props.getProperty("db.url");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL JDBC driver not found on classpath", e);
        }
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Runs schema.sql against the target database. Safe to call on every
     * startup — the DDL uses CREATE TABLE IF NOT EXISTS.
     */
    public void initSchema() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            InputStream in = getClass().getClassLoader().getResourceAsStream("schema.sql");
            if (in == null) {
                throw new IllegalStateException("schema.sql not found on classpath");
            }
            String sql = new String(in.readAllBytes());
            for (String statement : sql.split(";")) {
                String trimmed = statement.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
            log.info("Database schema initialized successfully");
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
}
