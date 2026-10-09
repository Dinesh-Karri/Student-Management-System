package com.studentmanagement.callable;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.studentmanagement.util.DBConnection;

/**
 * Demonstrates JDBC CallableStatement for executing MySQL Stored Procedures:
 * 1. Procedure with IN parameter returning a ResultSet (get_student_by_id)
 * 2. Procedure with OUT parameter returning an aggregated scalar value (count_students)
 * 3. Procedure with multiple IN parameters executing DML INSERT (insert_student)
 */
public class CallableStatementDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                    JDBC CALLABLESTATEMENT DEMONSTRATION                        ");
        System.out.println("================================================================================");

        // 1. Resolve an existing student ID and valid department ID
        int existingStudentId = getExistingStudentId();
        int validDepartmentId = getValidDepartmentId();

        // -------------------------------------------------------------------------
        // SCENARIO 1: Procedure with OUT Parameter (count_students)
        // -------------------------------------------------------------------------
        System.out.println("\n--- SCENARIO 1: Calling Procedure with OUT Parameter (count_students) ---");
        int initialCount = countStudents();
        System.out.println("Initial student count in database: " + initialCount);

        // -------------------------------------------------------------------------
        // SCENARIO 2: Procedure with IN Parameter returning ResultSet (get_student_by_id)
        // -------------------------------------------------------------------------
        System.out.println("\n--- SCENARIO 2: Calling Procedure with IN Parameter (get_student_by_id) ---");
        getStudentById(existingStudentId);

        // Test non-existent student ID
        System.out.println("\nTesting non-existent student ID (-999):");
        getStudentById(-999);

        // -------------------------------------------------------------------------
        // SCENARIO 3: Procedure with IN Parameters executing DML (insert_student)
        // -------------------------------------------------------------------------
        System.out.println("\n--- SCENARIO 3: Calling DML Procedure (insert_student) ---");
        long timestamp = System.currentTimeMillis();
        String testName = "Callable Tester";
        String testEmail = "callable_" + timestamp + "@example.com";
        int testAge = 24;

        boolean inserted = insertStudent(testName, testEmail, testAge, validDepartmentId);
        System.out.println("Insertion result: " + (inserted ? "SUCCESS" : "FAILED"));

        // -------------------------------------------------------------------------
        // SCENARIO 4: Re-verify Student Count
        // -------------------------------------------------------------------------
        System.out.println("\n--- SCENARIO 4: Verifying Student Count After Insert ---");
        int updatedCount = countStudents();
        System.out.println("Updated student count in database: " + updatedCount);
        if (updatedCount == initialCount + 1) {
            System.out.println("Verification passed: Student count incremented by 1.");
        }

        System.out.println("\n================================================================================");
        System.out.println("                 CALLABLESTATEMENT DEMONSTRATION COMPLETED                      ");
        System.out.println("================================================================================");
    }

    /**
     * Calls stored procedure 'get_student_by_id(?p_student_id)' which returns a ResultSet.
     *
     * @param studentId The ID of the student to fetch
     */
    public static void getStudentById(int studentId) {
        String callSql = "{call get_student_by_id(?)}";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database connection failed.");
                return;
            }

            try (CallableStatement callableStatement = connection.prepareCall(callSql)) {
                // 1. Bind IN parameter (1-based index)
                callableStatement.setInt(1, studentId);

                // 2. Execute the procedure and obtain the returned ResultSet
                try (ResultSet resultSet = callableStatement.executeQuery()) {
                    if (resultSet.next()) {
                        int id = resultSet.getInt("student_id");
                        String name = resultSet.getString("name");
                        String email = resultSet.getString("email");
                        int age = resultSet.getInt("age");
                        int deptId = resultSet.getInt("department_id");
                        String biography = resultSet.getString("biography");

                        System.out.println("Student record found via stored procedure:");
                        System.out.printf("  ID            : %d%n", id);
                        System.out.printf("  Name          : %s%n", name);
                        System.out.printf("  Email         : %s%n", email);
                        System.out.printf("  Age           : %d%n", age);
                        System.out.printf("  Department ID : %d%n", deptId);
                        System.out.printf("  Biography     : %s%n", (biography == null ? "(None)" : biography));
                    } else {
                        System.out.println("No student found with ID: " + studentId);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL error executing get_student_by_id: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Calls stored procedure 'count_students(?p_total)' which outputs the total count via OUT parameter.
     *
     * @return Total student count, or -1 on error
     */
    public static int countStudents() {
        String callSql = "{call count_students(?)}";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database connection failed.");
                return -1;
            }

            try (CallableStatement callableStatement = connection.prepareCall(callSql)) {
                // 1. Register the OUT parameter and its SQL data type
                callableStatement.registerOutParameter(1, Types.INTEGER);

                // 2. Execute the stored procedure
                callableStatement.execute();

                // 3. Retrieve the output parameter value using getter
                int totalStudents = callableStatement.getInt(1);
                return totalStudents;
            }

        } catch (SQLException e) {
            System.err.println("SQL error executing count_students: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * Calls stored procedure 'insert_student(?, ?, ?, ?)' to insert a student row.
     *
     * @param name         Student full name
     * @param email        Unique email address
     * @param age          Student age
     * @param departmentId Associated department foreign key
     * @return true if insertion succeeded, false otherwise
     */
    public static boolean insertStudent(String name, String email, int age, int departmentId) {
        String callSql = "{call insert_student(?, ?, ?, ?)}";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Database connection failed.");
                return false;
            }

            try (CallableStatement callableStatement = connection.prepareCall(callSql)) {
                // Bind all IN parameters
                callableStatement.setString(1, name);
                callableStatement.setString(2, email);
                callableStatement.setInt(3, age);
                callableStatement.setInt(4, departmentId);

                // Execute the DML stored procedure
                int rowsAffected = callableStatement.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("Stored procedure insert_student executed successfully.");
                    return true;
                } else {
                    System.out.println("Procedure executed, but no rows reported affected.");
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("SQL error executing insert_student: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Resolves an existing student ID in MySQL for testing.
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

    /**
     * Resolves a valid department ID from the students or departments table.
     */
    private static int getValidDepartmentId() {
        String query = "SELECT department_id FROM students WHERE department_id IS NOT NULL LIMIT 1";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt("department_id");
            }
        } catch (SQLException e) {
            System.err.println("Notice: Could not fetch valid department ID: " + e.getMessage());
        }
        return 1;
    }
}
