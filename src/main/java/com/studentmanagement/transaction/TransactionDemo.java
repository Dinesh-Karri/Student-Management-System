package com.studentmanagement.transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.studentmanagement.util.DBConnection;

/**
 * Demonstrates JDBC Transactions using:
 * - Connection.setAutoCommit(false)
 * - commit()
 * - rollback()
 * - try-with-resources
 * - PreparedStatement
 * - SQLException handling
 */
public class TransactionDemo {

    private static final String INSERT_SQL = 
            "INSERT INTO students (name, email, age, department_id) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_SQL = 
            "UPDATE students SET age = ? WHERE student_id = ?";

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("           JDBC TRANSACTION DEMONSTRATION         ");
        System.out.println("==================================================");

        // Fetch an existing student ID from the database for the update test
        int existingStudentId = getExistingStudentId();

        // SCENARIO 1: Successful Transaction
        System.out.println("\n--------------------------------------------------");
        System.out.println("SCENARIO 1: Demonstrating SUCCESSFUL Transaction");
        System.out.println("--------------------------------------------------");
        String successEmail = "success_" + System.currentTimeMillis() + "@example.com";
        executeTransaction("Alice Green", successEmail, 20, 1, existingStudentId, 23);

        // Verify that the student inserted in Scenario 1 is committed in DB
        boolean scenario1Committed = doesStudentExist(successEmail);
        System.out.println("Verification: Inserted student (" + successEmail + ") in DB? " + scenario1Committed);

        // SCENARIO 2: Failed Transaction (Rollback)
        System.out.println("\n--------------------------------------------------");
        System.out.println("SCENARIO 2: Demonstrating FAILED Transaction (Rollback)");
        System.out.println("--------------------------------------------------");
        String failEmail = "rollback_" + System.currentTimeMillis() + "@example.com";
        // Use an invalid/non-existing student ID (-999) to intentionally cause Operation 2 to fail
        int nonExistingStudentId = -999;
        executeTransaction("Bob Brown", failEmail, 21, 1, nonExistingStudentId, 25);

        // Verify that the student inserted before the failure was rolled back and is NOT in DB
        boolean scenario2Committed = doesStudentExist(failEmail);
        System.out.println("Verification: Inserted student (" + failEmail + ") in DB? " + scenario2Committed);
        if (!scenario2Committed) {
            System.out.println("Confirmation: Rollback was successful! The student was NOT persisted.");
        }
        System.out.println("==================================================");
    }

    /**
     * Executes a two-step transaction:
     * 1. INSERT a new student into the students table.
     * 2. UPDATE the age of another student in the students table.
     *
     * If both succeed, the transaction is COMMITTED.
     * If either fails, the transaction is ROLLED BACK.
     */
    public static void executeTransaction(String insertName, String insertEmail, int insertAge,
                                          int insertDeptId, int updateStudentId, int newAge) {
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database connection failed. Cannot start transaction.");
                return;
            }

            // Step 1: Disable auto-commit to begin transaction boundary
            connection.setAutoCommit(false);
            System.out.println("Transaction started.");

            try (PreparedStatement insertStatement = connection.prepareStatement(INSERT_SQL);
                 PreparedStatement updateStatement = connection.prepareStatement(UPDATE_SQL)) {

                // Operation 1: Insert student
                insertStatement.setString(1, insertName);
                insertStatement.setString(2, insertEmail);
                insertStatement.setInt(3, insertAge);
                insertStatement.setInt(4, insertDeptId);

                int insertedRows = insertStatement.executeUpdate();
                if (insertedRows > 0) {
                    System.out.println("Student insert successful.");
                } else {
                    throw new SQLException("Insert failed: Zero rows affected.");
                }

                // Operation 2: Update another student's age
                updateStatement.setInt(1, newAge);
                updateStatement.setInt(2, updateStudentId);

                int updatedRows = updateStatement.executeUpdate();
                if (updatedRows > 0) {
                    System.out.println("Student update successful.");
                } else {
                    // If no row was updated (e.g. invalid student ID), fail the transaction
                    throw new SQLException("Student update failed: No student found with ID " + updateStudentId);
                }

                // Step 2: If BOTH operations succeed, commit the transaction
                connection.commit();
                System.out.println("Transaction committed successfully.");

            } catch (SQLException e) {
                // Step 3: If ANY operation fails, rollback the transaction
                System.out.println("Transaction failed.");
                System.out.println("Rolling back transaction.");
                try {
                    connection.rollback();
                    System.out.println("Transaction rolled back successfully.");
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback execution failed: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }
                System.err.println("Error details: " + e.getMessage());
            } finally {
                // Step 4: Restore auto-commit state to default (true) before returning connection
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error resetting auto-commit: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to find an existing student ID in the database for testing updates.
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
        return 1; // Default fallback ID if database has not been populated
    }

    /**
     * Helper method to verify whether a student record with the given email exists in MySQL.
     */
    private static boolean doesStudentExist(String email) {
        String query = "SELECT student_id FROM students WHERE email = ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("Verification query failed: " + e.getMessage());
        }
        return false;
    }
}
