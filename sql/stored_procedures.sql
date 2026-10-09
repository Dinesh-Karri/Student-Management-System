-- =============================================================================
-- Database Stored Procedures for Student Management System
-- Target Database: student_management_project
-- Table: students (student_id, name, email, age, department_id, biography)
-- =============================================================================

USE student_management_project;

-- -----------------------------------------------------------------------------
-- Procedure A: get_student_by_id
-- Input:  IN p_student_id INT
-- Output: Result Set containing matched student columns
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS get_student_by_id;

DELIMITER $$
CREATE PROCEDURE get_student_by_id(
    IN p_student_id INT
)
BEGIN
    SELECT student_id, name, email, age, department_id, biography
    FROM students
    WHERE student_id = p_student_id;
END $$
DELIMITER ;

-- -----------------------------------------------------------------------------
-- Procedure B: count_students
-- Output: OUT p_total INT
-- Behavior: Counts total student rows and assigns to output parameter
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS count_students;

DELIMITER $$
CREATE PROCEDURE count_students(
    OUT p_total INT
)
BEGIN
    SELECT COUNT(*) INTO p_total
    FROM students;
END $$
DELIMITER ;

-- -----------------------------------------------------------------------------
-- Procedure C: insert_student
-- Inputs: IN p_name VARCHAR(100), IN p_email VARCHAR(150),
--         IN p_age INT, IN p_department_id INT
-- Behavior: Inserts student into students table using parameters
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS insert_student;

DELIMITER $$
CREATE PROCEDURE insert_student(
    IN p_name VARCHAR(100),
    IN p_email VARCHAR(150),
    IN p_age INT,
    IN p_department_id INT
)
BEGIN
    INSERT INTO students (name, email, age, department_id)
    VALUES (p_name, p_email, p_age, p_department_id);
END $$
DELIMITER ;
