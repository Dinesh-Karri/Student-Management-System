package com.studentmanagement.metadata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import com.studentmanagement.util.DBConnection;

/**
 * Demonstrates JDBC Metadata inspection:
 * 1. DatabaseMetaData: Inspects database software, driver details, and server capabilities.
 * 2. ResultSetMetaData: Dynamically inspects column definitions of a query result set.
 * 3. Table Schema Metadata: Uses DatabaseMetaData.getColumns() to inspect catalog definitions.
 */
public class MetadataDemo {

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("                         JDBC METADATA DEMONSTRATION                            ");
        System.out.println("================================================================================");

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                System.err.println("Failed to obtain database connection. Please ensure MySQL is running.");
                return;
            }

            // Part 1: Inspect database engine and driver metadata
            displayDatabaseMetaData(connection);

            // Part 2: Inspect runtime query result set metadata
            displayResultSetMetaData(connection);

            // Part 3: Inspect table catalog column definitions from DatabaseMetaData
            displayTableColumnsMetaData(connection);

        } catch (SQLException e) {
            System.err.println("Database error during metadata inspection: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n================================================================================");
        System.out.println("                    METADATA DEMONSTRATION COMPLETED                            ");
        System.out.println("================================================================================");
    }

    /**
     * Inspects and displays database engine, driver specifications, and supported capabilities.
     *
     * @param connection An active JDBC database connection
     * @throws SQLException If database metadata retrieval fails
     */
    public static void displayDatabaseMetaData(Connection connection) throws SQLException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" 1. DatabaseMetaData (Server & Driver Information)");
        System.out.println("--------------------------------------------------------------------------------");

        DatabaseMetaData dbMetaData = connection.getMetaData();

        System.out.printf("%-35s : %s%n", "Database Product Name", dbMetaData.getDatabaseProductName());
        System.out.printf("%-35s : %s%n", "Database Product Version", dbMetaData.getDatabaseProductVersion());
        System.out.printf("%-35s : %s%n", "JDBC Driver Name", dbMetaData.getDriverName());
        System.out.printf("%-35s : %s%n", "JDBC Driver Version", dbMetaData.getDriverVersion());
        System.out.printf("%-35s : %s%n", "Connected Database User", dbMetaData.getUserName());
        System.out.printf("%-35s : %s%n", "Database Connection URL", dbMetaData.getURL());
        System.out.printf("%-35s : %s%n", "Transactions Supported?", dbMetaData.supportsTransactions() ? "YES" : "NO");
        System.out.printf("%-35s : %s%n", "Batch Updates Supported?", dbMetaData.supportsBatchUpdates() ? "YES" : "NO");
        System.out.printf("%-35s : %s%n", "Savepoints Supported?", dbMetaData.supportsSavepoints() ? "YES" : "NO");
        System.out.printf("%-35s : %d%n", "Max Allowed Connections", dbMetaData.getMaxConnections());
    }

    /**
     * Executes a SELECT query on the 'students' table and dynamically inspects
     * the columns returned using ResultSetMetaData.
     *
     * @param connection An active JDBC database connection
     * @throws SQLException If query execution or metadata retrieval fails
     */
    public static void displayResultSetMetaData(Connection connection) throws SQLException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" 2. ResultSetMetaData (Query Result Columns Inspection: SELECT * FROM students)");
        System.out.println("--------------------------------------------------------------------------------");

        String sql = "SELECT * FROM students";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            ResultSetMetaData rsMetaData = resultSet.getMetaData();
            int columnCount = rsMetaData.getColumnCount();

            System.out.println("Total Columns in Query Result: " + columnCount);
            System.out.println();
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
            System.out.printf("%-5s | %-16s | %-16s | %-14s | %-28s | %-12s%n",
                    "#", "Column Name", "Column Label", "SQL Type", "Java Class Name", "Nullable?");
            System.out.println("-----------------------------------------------------------------------------------------------------------------");

            for (int i = 1; i <= columnCount; i++) {
                String columnName = rsMetaData.getColumnName(i);
                String columnLabel = rsMetaData.getColumnLabel(i);
                String typeName = rsMetaData.getColumnTypeName(i);
                String className = rsMetaData.getColumnClassName(i);
                int nullableCode = rsMetaData.isNullable(i);

                String nullableDesc = switch (nullableCode) {
                    case ResultSetMetaData.columnNoNulls -> "NO";
                    case ResultSetMetaData.columnNullable -> "YES";
                    default -> "UNKNOWN";
                };

                System.out.printf("%-5d | %-16s | %-16s | %-14s | %-28s | %-12s%n",
                        i, columnName, columnLabel, typeName, className, nullableDesc);
            }
            System.out.println("-----------------------------------------------------------------------------------------------------------------");
        }
    }

    /**
     * Inspects column definitions of the 'students' table directly from the database
     * catalog using DatabaseMetaData.getColumns() filtered by the current catalog.
     *
     * @param connection An active JDBC database connection
     * @throws SQLException If catalog inspection fails
     */
    public static void displayTableColumnsMetaData(Connection connection) throws SQLException {
        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.println(" 3. Table Catalog Metadata (DatabaseMetaData.getColumns() for table: 'students')");
        System.out.println("--------------------------------------------------------------------------------");
        System.out.println("Note: This reads the persistent schema dictionary, independent of any SELECT query.");
        System.out.println();

        DatabaseMetaData dbMetaData = connection.getMetaData();
        String currentCatalog = connection.getCatalog(); // "student_management_project"

        try (ResultSet columnsRs = dbMetaData.getColumns(currentCatalog, null, "students", null)) {
            System.out.println("Catalog / Database: " + currentCatalog);
            System.out.println("-------------------------------------------------------------------------------------------------");
            System.out.printf("%-18s | %-14s | %-12s | %-12s | %-20s%n",
                    "Column Name", "Data Type", "Column Size", "Nullable?", "Default Value");
            System.out.println("-------------------------------------------------------------------------------------------------");

            boolean hasColumns = false;
            while (columnsRs.next()) {
                hasColumns = true;
                String colName = columnsRs.getString("COLUMN_NAME");
                String typeName = columnsRs.getString("TYPE_NAME");
                int colSize = columnsRs.getInt("COLUMN_SIZE");
                String isNullable = columnsRs.getString("IS_NULLABLE");
                String defaultValue = columnsRs.getString("COLUMN_DEF");

                System.out.printf("%-18s | %-14s | %-12d | %-12s | %-20s%n",
                        colName, typeName, colSize, isNullable, (defaultValue == null ? "NULL" : defaultValue));
            }

            if (!hasColumns) {
                System.out.println("No column metadata found for table 'students'. Please verify table existence.");
            }
            System.out.println("-------------------------------------------------------------------------------------------------");
        }
    }
}
