package com.studentmanagement.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // Database configuration constants
    private static final String URL = "jdbc:mysql://localhost:3306/student_management_project";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "Dinesh.K_29"; // Replace with your MySQL password if different

    // Private constructor to prevent instantiation of this utility class
    private DBConnection() {
    }

    /**
     * Establishes and returns a Connection object to the MySQL database.
     *
     * @return java.sql.Connection object if successful, or null if connection fails
     */
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            e.printStackTrace();
        }
        return connection;
    }
}
