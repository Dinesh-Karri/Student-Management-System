package com.studentmanagement.dao;

import java.util.List;
import com.studentmanagement.model.Student;

/**
 * Data Access Object (DAO) interface defining operations for Student entities.
 */
public interface StudentDAO {

    void addStudent(Student student);

    List<Student> getAllStudents();

    Student getStudentById(int studentId);

    boolean updateStudent(Student student);

    boolean deleteStudent(int studentId);

    List<Student> searchStudents(String keyword);
}
