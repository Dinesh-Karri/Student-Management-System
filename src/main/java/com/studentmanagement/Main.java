package com.studentmanagement;

import java.util.List;
import java.util.Scanner;

import com.studentmanagement.dao.StudentDAO;
import com.studentmanagement.dao.StudentDAOImpl;
import com.studentmanagement.model.Student;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        StudentDAO studentDAO = new StudentDAOImpl();
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt(scanner, "Enter your choice (1-7): ");

            switch (choice) {
                case 1 -> handleAddStudent(scanner, studentDAO);
                case 2 -> handleViewAllStudents(studentDAO);
                case 3 -> handleFindStudentById(scanner, studentDAO);
                case 4 -> handleUpdateStudent(scanner, studentDAO);
                case 5 -> handleDeleteStudent(scanner, studentDAO);
                case 6 -> handleSearchStudents(scanner, studentDAO);
                case 7 -> {
                    System.out.println("Thank you for using Student Management System.");
                    running = false;
                }
                default -> System.out.println("Invalid option! Please select a number between 1 and 7.");
            }
            System.out.println();
        }

        scanner.close();
    }

    private static void printMenu() {
        System.out.println("====================================");
        System.out.println("      STUDENT MANAGEMENT SYSTEM     ");
        System.out.println("====================================");
        System.out.println("1. Add Student");
        System.out.println("2. View All Students");
        System.out.println("3. Find Student By ID");
        System.out.println("4. Update Student");
        System.out.println("5. Delete Student");
        System.out.println("6. Search Student");
        System.out.println("7. Exit");
        System.out.println("====================================");
    }

    private static void handleAddStudent(Scanner scanner, StudentDAO studentDAO) {
        System.out.println("\n--- Add Student ---");
        System.out.print("Enter Student Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter Email: ");
        String email = scanner.nextLine().trim();

        int age = readInt(scanner, "Enter Age: ");
        int departmentId = readInt(scanner, "Enter Department ID: ");

        Student student = new Student(name, email, age, departmentId);
        studentDAO.addStudent(student);
    }

    private static void handleViewAllStudents(StudentDAO studentDAO) {
        System.out.println("\n--- All Students ---");
        List<Student> students = studentDAO.getAllStudents();

        if (students.isEmpty()) {
            System.out.println("No students found.");
        } else {
            printStudentTable(students);
        }
    }

    private static void handleFindStudentById(Scanner scanner, StudentDAO studentDAO) {
        System.out.println("\n--- Find Student By ID ---");
        int studentId = readInt(scanner, "Enter Student ID: ");

        Student student = studentDAO.getStudentById(studentId);
        if (student != null) {
            printStudentTable(List.of(student));
        } else {
            System.out.println("Student not found.");
        }
    }

    private static void handleUpdateStudent(Scanner scanner, StudentDAO studentDAO) {
        System.out.println("\n--- Update Student ---");
        int studentId = readInt(scanner, "Enter Student ID to update: ");

        System.out.print("Enter New Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter New Email: ");
        String email = scanner.nextLine().trim();

        int age = readInt(scanner, "Enter New Age: ");
        int departmentId = readInt(scanner, "Enter New Department ID: ");

        Student student = new Student(studentId, name, email, age, departmentId);
        boolean updated = studentDAO.updateStudent(student);

        if (updated) {
            System.out.println("Student updated successfully.");
        } else {
            System.out.println("Student not found or update failed.");
        }
    }

    private static void handleDeleteStudent(Scanner scanner, StudentDAO studentDAO) {
        System.out.println("\n--- Delete Student ---");
        int studentId = readInt(scanner, "Enter Student ID to delete: ");

        System.out.print("Are you sure you want to delete this student? (y/n): ");
        String confirmation = scanner.nextLine().trim();

        if (confirmation.equalsIgnoreCase("y")) {
            boolean deleted = studentDAO.deleteStudent(studentId);
            if (deleted) {
                System.out.println("Student deleted successfully.");
            } else {
                System.out.println("Student not found or deletion failed.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void handleSearchStudents(Scanner scanner, StudentDAO studentDAO) {
        System.out.println("\n--- Search Student ---");
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim();

        List<Student> matchingStudents = studentDAO.searchStudents(keyword);

        if (matchingStudents.isEmpty()) {
            System.out.println("No matching students found.");
        } else {
            printStudentTable(matchingStudents);
        }
    }

    /**
     * Helper method to print student list in a formatted table.
     */
    private static void printStudentTable(List<Student> students) {
        System.out.println("--------------------------------------------------------------------------------");
        System.out.printf("%-5s | %-20s | %-25s | %-5s | %-14s%n", "ID", "Name", "Email", "Age", "Department ID");
        System.out.println("--------------------------------------------------------------------------------");
        for (Student s : students) {
            System.out.printf("%-5d | %-20s | %-25s | %-5d | %-14d%n",
                    s.getStudentId(), s.getName(), s.getEmail(), s.getAge(), s.getDepartmentId());
        }
        System.out.println("--------------------------------------------------------------------------------");
    }

    /**
     * Reads an integer safely from the console to prevent application crashes on invalid input.
     */
    private static int readInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid whole number.");
            }
        }
    }
}
