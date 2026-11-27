package com.alpay.remittance.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Single shared connection to the MySQL database running in Docker.
 * Uses the singleton pattern so the whole application shares one connection.
 */
public class DBConnection {

    private static DBConnection instance;
    private Connection connection;

    // Docker MySQL container: host port 3307 maps to MySQL's port 3306.
    // This password is for the local Docker database only.
    private static final String URL = "jdbc:mysql://localhost:3307/alpay_remittance";
    private static final String USER = "root";
    private static final String PASSWORD = "AlpayRoot2026";

    private DBConnection() {
    }

    public static synchronized DBConnection getInstance() {
        if (instance == null) {
            instance = new DBConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Could not close the database connection: " + e.getMessage());
        }
    }
}
