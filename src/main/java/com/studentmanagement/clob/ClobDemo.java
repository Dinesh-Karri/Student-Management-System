package com.studentmanagement.clob;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.studentmanagement.util.DBConnection;

/**
 * Demonstrates JDBC CLOB (Character Large Object) handling using character streams:
 * - Storing large textual data into MySQL TEXT column using setCharacterStream().
 * - Retrieving large text using getCharacterStream() and BufferedReader.
 * - Resource management with try-with-resources and proper error handling.
 */
public class ClobDemo {

    private static final String UPDATE_BIOGRAPHY_SQL = 
            "UPDATE students SET biography = ? WHERE student_id = ?";

    private static final String SELECT_BIOGRAPHY_SQL = 
            "SELECT name, biography FROM students WHERE student_id = ?";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("              JDBC CLOB DEMONSTRATION             ");
        System.out.println("==================================================");

        // Resolve an existing student ID from the database
        int studentId = getExistingStudentId();
        System.out.println("Target Student ID for tests: " + studentId);

        // ---------------------------------------------------------------
        // TEST CASE 1: Store Multi-Line Biography for an Existing Student
        // ---------------------------------------------------------------
        System.out.println("\n--- TEST CASE 1: Storing Biography (setCharacterStream) ---");
        String sampleBiography = """
                Dinesh is an enthusiastic computer science undergraduate specializing in backend systems.
                Academic Highlights:
                  - Top 5% in Relational Database Management Systems (RDBMS).
                  - Completed hands-on training in Core Java, JDBC, and MySQL.
                  - Built an end-to-end Student Management System console application.
                Career Objective:
                  Aspires to work as a Software Engineer designing high-throughput, transactional data systems.
                """.trim();

        boolean updated = updateStudentBiography(studentId, sampleBiography);
        System.out.println("Update result: " + (updated ? "SUCCESS" : "FAILED"));

        // ---------------------------------------------------------------
        // TEST CASE 2: Retrieve and Display Biography (getCharacterStream)
        // ---------------------------------------------------------------
        System.out.println("\n--- TEST CASE 2: Retrieving Biography (getCharacterStream) ---");
        String retrievedBio = getStudentBiography(studentId);
        if (retrievedBio != null) {
            System.out.println("----------------- Retrieved Text -----------------");
            System.out.println(retrievedBio);
            System.out.println("--------------------------------------------------");
        }

        // ---------------------------------------------------------------
        // TEST CASE 3: Handling Non-Existent Student ID (-999)
        // ---------------------------------------------------------------
        System.out.println("\n--- TEST CASE 3: Handling Non-Existent Student ID ---");
        int nonExistentId = -999;
        boolean invalidUpdate = updateStudentBiography(nonExistentId, "Ghost biography text.");
        System.out.println("Update for invalid ID result: " + (invalidUpdate ? "SUCCESS" : "FAILED (Handled gracefully)"));

        String invalidRetrieval = getStudentBiography(nonExistentId);
        System.out.println("Retrieval for invalid ID result: " + (invalidRetrieval == null ? "NULL (Handled gracefully)" : "FOUND"));

        // ---------------------------------------------------------------
        // TEST CASE 4: Validation on Empty / Null Biography
        // ---------------------------------------------------------------
        System.out.println("\n--- TEST CASE 4: Validating Null / Blank Biography ---");
        updateStudentBiography(studentId, null);
        updateStudentBiography(studentId, "   ");

        System.out.println("\n==================================================");
        System.out.println("          JDBC CLOB DEMO COMPLETED                ");
        System.out.println("==================================================");
    }

    /**
     * Stores a biography string into the students table using PreparedStatement.setCharacterStream().
     *
     * @param studentId The primary key ID of the student
     * @param biography The text content to store
     * @return true if the student was found and updated, false otherwise
     */
    public static boolean updateStudentBiography(int studentId, String biography) {
        // Input validation
        if (studentId <= 0) {
            System.err.println("Validation Error: Invalid student ID: " + studentId);
            return false;
        }
        if (biography == null || biography.trim().isEmpty()) {
            System.err.println("Validation Error: Biography cannot be null or empty.");
            return false;
        }

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database Error: Connection could not be established.");
                return false;
            }

            // Using StringReader to stream the characters into the JDBC driver
            try (PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_BIOGRAPHY_SQL);
                 Reader characterReader = new StringReader(biography)) {

                // Stream character data into parameter 1 (biography column)
                preparedStatement.setCharacterStream(1, characterReader, biography.length());
                preparedStatement.setInt(2, studentId);

                int rowsAffected = preparedStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Biography saved successfully for student ID: " + studentId);
                    return true;
                } else {
                    System.out.println("Student ID " + studentId + " not found. No records updated.");
                    return false;
                }
            }

        } catch (SQLException e) {
            System.err.println("Database Error updating biography: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            System.err.println("I/O Error closing character stream: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Retrieves the biography text for a student using ResultSet.getCharacterStream() and BufferedReader.
     *
     * @param studentId The primary key ID of the student
     * @return The retrieved biography text, or null if not found
     */
    public static String getStudentBiography(int studentId) {
        if (studentId <= 0) {
            System.err.println("Validation Error: Invalid student ID: " + studentId);
            return null;
        }

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database Error: Connection could not be established.");
                return null;
            }

            try (PreparedStatement preparedStatement = connection.prepareStatement(SELECT_BIOGRAPHY_SQL)) {
                preparedStatement.setInt(1, studentId);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        String name = resultSet.getString("name");

                        // Obtain character stream from the CLOB / TEXT column
                        try (Reader reader = resultSet.getCharacterStream("biography")) {
                            if (reader == null) {
                                System.out.println("Student '" + name + "' (ID: " + studentId + ") exists, but has no biography.");
                                return null;
                            }

                            // Read characters incrementally using a BufferedReader
                            StringBuilder content = new StringBuilder();
                            try (BufferedReader bufferedReader = new BufferedReader(reader)) {
                                String line;
                                while ((line = bufferedReader.readLine()) != null) {
                                    content.append(line).append(System.lineSeparator());
                                }
                            }

                            System.out.println("Found biography for: " + name + " (ID: " + studentId + ")");
                            return content.toString().trim();
                        }

                    } else {
                        System.out.println("Student with ID " + studentId + " does not exist.");
                        return null;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Database Error reading biography: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("I/O Error streaming character data: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Dynamically finds an existing student ID in MySQL for demo execution.
     * Defaults to 1 if the table is currently empty.
     */
    private static int getExistingStudentId() {
        String query = "SELECT student_id FROM students LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("student_id");
            }
        } catch (SQLException e) {
            System.err.println("Notice: Could not fetch existing student ID: " + e.getMessage());
        }
        return 1;
    }
}
