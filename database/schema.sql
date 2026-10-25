-- =====================================================================
--  Training Management System  -  schema.sql
-- ---------------------------------------------------------------------
--  SAFE TO RUN ON AN EXISTING DATABASE.
--
--  * Uses CREATE DATABASE IF NOT EXISTS and CREATE TABLE IF NOT EXISTS,
--    so existing tables and rows are left untouched.
--  * Contains NO "DROP DATABASE", "DROP TABLE" or "TRUNCATE" statements.
--
--  If you already have the training_management_system database with the
--  7 tables below, you do NOT need to run this file at all.
--  It exists only for a fresh installation on a new machine.
-- =====================================================================


-- ---------------------------------------------------------------------
-- PART 1 : Database creation (skipped if it already exists)
-- ---------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS training_management_system;

USE training_management_system;


-- ---------------------------------------------------------------------
-- PART 2 : Table creation (each table is skipped if it already exists)
-- ---------------------------------------------------------------------

-- Departments of the college. Dept_ID is entered manually (101, 102, ...).
CREATE TABLE IF NOT EXISTS department (
    Dept_ID    INT          NOT NULL,
    Dept_Name  VARCHAR(50)  NOT NULL,
    HOD_Name   VARCHAR(50),
    PRIMARY KEY (Dept_ID)
);

-- Students; every student may belong to one department.
CREATE TABLE IF NOT EXISTS student (
    Student_ID     INT           NOT NULL AUTO_INCREMENT,
    Name           VARCHAR(50)   NOT NULL,
    Gender         VARCHAR(10),
    DOB            DATE,
    Email          VARCHAR(100)  UNIQUE,
    Mobile_Number  VARCHAR(50),
    CGPA           DECIMAL(4,2),
    Dept_ID        INT,
    PRIMARY KEY (Student_ID),
    CONSTRAINT fk_student_department
        FOREIGN KEY (Dept_ID) REFERENCES department (Dept_ID)
);

-- Trainers who conduct sessions.
CREATE TABLE IF NOT EXISTS trainer (
    Trainer_ID    INT           NOT NULL AUTO_INCREMENT,
    Trainer_Name  VARCHAR(50)   NOT NULL,
    Email         VARCHAR(100)  UNIQUE,
    Expertise     VARCHAR(50),
    PRIMARY KEY (Trainer_ID)
);

-- Training programmes (Python, Java, SQL ...).
CREATE TABLE IF NOT EXISTS training (
    Training_ID   INT           NOT NULL AUTO_INCREMENT,
    Tr_Name       VARCHAR(50)   NOT NULL,
    Tr_Type       VARCHAR(30),
    Description   VARCHAR(100),
    Duration_Hrs  INT,
    PRIMARY KEY (Training_ID)
);

-- A scheduled class of one training, taken by one trainer.
CREATE TABLE IF NOT EXISTS training_session (
    Session_ID    INT   NOT NULL AUTO_INCREMENT,
    Session_Date  DATE  NOT NULL,
    Start_Time    TIME  NOT NULL,
    End_Time      TIME  NOT NULL,
    Training_ID   INT   NOT NULL,
    Trainer_ID    INT   NOT NULL,
    PRIMARY KEY (Session_ID),
    CONSTRAINT fk_session_training
        FOREIGN KEY (Training_ID) REFERENCES training (Training_ID),
    CONSTRAINT fk_session_trainer
        FOREIGN KEY (Trainer_ID)  REFERENCES trainer (Trainer_ID)
);

-- Many-to-many link between students and sessions, plus attendance.
-- Composite primary key: one student can enrol in a session only once.
CREATE TABLE IF NOT EXISTS session_enrollment (
    Session_ID         INT          NOT NULL,
    Student_ID         INT          NOT NULL,
    Attendance_Status  VARCHAR(20)  NOT NULL DEFAULT 'Pending',
    Enrolled_At        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (Session_ID, Student_ID),
    CONSTRAINT fk_enrollment_session
        FOREIGN KEY (Session_ID) REFERENCES training_session (Session_ID),
    CONSTRAINT fk_enrollment_student
        FOREIGN KEY (Student_ID) REFERENCES student (Student_ID)
);

-- Application users (kept as in the existing design; the app does not
-- implement login, as authentication is outside the project scope).
CREATE TABLE IF NOT EXISTS users (
    User_ID        INT           NOT NULL AUTO_INCREMENT,
    Username       VARCHAR(50)   NOT NULL UNIQUE,
    Email          VARCHAR(100)  UNIQUE,
    Password_Hash  VARCHAR(255)  NOT NULL,
    Role           VARCHAR(20),
    Created_At     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (User_ID)
);

-- PART 3 (optional sample data) lives in sample_data.sql.
