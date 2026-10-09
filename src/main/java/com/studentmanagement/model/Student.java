package com.studentmanagement.model;

/**
 * POJO / Model class representing a Student entity.
 */
public class Student {

    // Private fields corresponding to columns in the 'students' table
    private int studentId;
    private String name;
    private String email;
    private int age;
    private int departmentId;

    // 1. No-argument constructor
    public Student() {
    }

    // 2. Parameterized constructor for all fields
    public Student(int studentId, String name, String email, int age, int departmentId) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.age = age;
        this.departmentId = departmentId;
    }

    // Constructor without studentId (useful when inserting new records with auto-generated ID)
    public Student(String name, String email, int age, int departmentId) {
        this.name = name;
        this.email = email;
        this.age = age;
        this.departmentId = departmentId;
    }

    // Getters and Setters
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    // toString() representation for easy display
    @Override
    public String toString() {
        return "Student{" +
                "studentId=" + studentId +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                ", departmentId=" + departmentId +
                '}';
    }
}
