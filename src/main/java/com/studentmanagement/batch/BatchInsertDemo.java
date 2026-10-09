package com.studentmanagement.batch;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.studentmanagement.model.Student;
import com.studentmanagement.util.DBConnection;

/**
 * Demonstrates JDBC Batch Processing with Transaction Management:
 * - PreparedStatement.addBatch()
 * - PreparedStatement.executeBatch()
 * - Handling int[] batch results
 * - setAutoCommit(false), commit(), and rollback()
 * - try-with-resources and SQLException handling
 */
public class BatchInsertDemo {

    private static final String INSERT_SQL = 
            "INSERT INTO students (name, email, age, department_id) VALUES (?, ?, ?, ?)";

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       JDBC BATCH PROCESSING DEMO       ");
        System.out.println("========================================");

        // ==========================================================
        // SCENARIO 1: SUCCESSFUL BATCH INSERTION
        // ==========================================================
        System.out.println("\nSCENARIO 1: SUCCESSFUL BATCH\n");
        long timestamp1 = System.currentTimeMillis();

        List<Student> successfulBatch = new ArrayList<>();
        successfulBatch.add(new Student("Rahul Sharma", "rahul_" + timestamp1 + "@example.com", 21, 1));
        successfulBatch.add(new Student("Sneha Patel", "sneha_" + timestamp1 + "@example.com", 22, 1));
        successfulBatch.add(new Student("Amit Verma", "amit_" + timestamp1 + "@example.com", 20, 1));
        successfulBatch.add(new Student("Pooja Nair", "pooja_" + timestamp1 + "@example.com", 23, 1));
        successfulBatch.add(new Student("Kiran Rao", "kiran_" + timestamp1 + "@example.com", 21, 1));

        executeBatchInsert(successfulBatch);

        // Verification for Scenario 1
        boolean firstStudentInserted = doesStudentExist("rahul_" + timestamp1 + "@example.com");
        System.out.println("Verification: Is Rahul in DB? " + firstStudentInserted + " (Expected: true)");

        // ==========================================================
        // SCENARIO 2: INTENTIONAL BATCH FAILURE (TRIGGERING ROLLBACK)
        // ==========================================================
        System.out.println("\n----------------------------------------");
        System.out.println("SCENARIO 2: FAILED BATCH\n");
        long timestamp2 = System.currentTimeMillis() + 5000;

        List<Student> failingBatch = new ArrayList<>();
        // Item 1: Valid student
        failingBatch.add(new Student("Vikram Singh", "vikram_" + timestamp2 + "@example.com", 22, 1));
        // Item 2: Deliberate duplicate email (violates UNIQUE constraint on students.email)
        failingBatch.add(new Student("Duplicate User", "vikram_" + timestamp2 + "@example.com", 23, 1));
        // Item 3: Valid student
        failingBatch.add(new Student("Deepa Gupta", "deepa_" + timestamp2 + "@example.com", 21, 1));

        executeBatchInsert(failingBatch);

        // Verification for Scenario 2: Verify that Item 1 was NOT committed due to rollback
        boolean vikramExists = doesStudentExist("vikram_" + timestamp2 + "@example.com");
        System.out.println("Verification: Is Vikram in DB? " + vikramExists + " (Expected: false due to rollback)");
        if (!vikramExists) {
            System.out.println("Confirmation: Transaction rollback ensured entire batch was undone cleanly!");
        }

        System.out.println("\n========================================");
    }

    /**
     * Executes a batch insert operation wrapped inside a single JDBC transaction.
     *
     * @param students List of students to insert in bulk
     */
    public static void executeBatchInsert(List<Student> students) {
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database connection failed. Cannot execute batch.");
                return;
            }

            // Step 1: Disable auto-commit to treat the entire batch as ONE transaction
            connection.setAutoCommit(false);
            System.out.println("Transaction started.");

            try (PreparedStatement preparedStatement = connection.prepareStatement(INSERT_SQL)) {
                System.out.println("Adding students to batch...");

                // Step 2: Add each student's parameters to the batch buffer
                for (Student student : students) {
                    preparedStatement.setString(1, student.getName());
                    preparedStatement.setString(2, student.getEmail());
                    preparedStatement.setInt(3, student.getAge());
                    preparedStatement.setInt(4, student.getDepartmentId());

                    preparedStatement.addBatch();
                }

                System.out.println(students.size() + " operations added to batch.");
                System.out.println("Executing batch...");

                // Step 3: Send all buffered commands to the database in a single network round-trip
                int[] results = preparedStatement.executeBatch();
                System.out.println("Batch executed successfully.");
                System.out.println("Batch result count: " + results.length);

                // Step 4: Inspect individual result codes in the int[] array
                for (int i = 0; i < results.length; i++) {
                    int status = results[i];
                    String statusDesc;
                    if (status >= 0) {
                        statusDesc = "SUCCESS (" + status + " row(s) affected)";
                    } else if (status == Statement.SUCCESS_NO_INFO) {
                        statusDesc = "SUCCESS_NO_INFO (Executed successfully, affected row count unknown)";
                    } else if (status == Statement.EXECUTE_FAILED) {
                        statusDesc = "EXECUTE_FAILED";
                    } else {
                        statusDesc = "STATUS_CODE: " + status;
                    }
                    System.out.println("  Operation " + (i + 1) + " (" + students.get(i).getName() + "): " + statusDesc);
                }

                // Step 5: Commit the entire batch transaction
                connection.commit();
                System.out.println("Transaction committed successfully.");

            } catch (SQLException e) {
                // Step 6: If any item in the batch fails, rollback all changes
                System.out.println("Batch execution failed.");
                System.out.println("Rolling back batch transaction.");
                try {
                    connection.rollback();
                    System.out.println("Transaction rolled back successfully.");
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback execution failed: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace();
                }

                System.err.println("Error details: " + e.getMessage());
                if (e instanceof BatchUpdateException bue) {
                    int[] updateCounts = bue.getUpdateCounts();
                    System.err.println("Operations attempted prior to failure: " + updateCounts.length);
                }
            } finally {
                // Step 7: Restore auto-commit state to default (true)
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    System.err.println("Error restoring auto-commit: " + e.getMessage());
                }
            }

        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Helper method to verify whether a student with the given email exists in the database.
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
