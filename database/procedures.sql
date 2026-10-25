-- =====================================================================
--  Training Management System  -  procedures.sql   (REQUIRED)
-- ---------------------------------------------------------------------
--  Stored procedures are named SQL programs stored inside MySQL.
--  The Spring Boot backend calls them through JDBC CallableStatement:
--
--    GetStudentAttendance(student_id)        -> GET /api/students/{id}/attendance
--    GetTrainingEnrollmentCount()            -> GET /api/reports/training-enrollment
--    GetSessionAttendanceSummary(id, OUT...) -> GET /api/sessions/{id}/attendance-summary
--
--  "DROP PROCEDURE IF EXISTS" only removes the old procedure definition,
--  never table data, so the script can be re-run safely.
-- =====================================================================

USE training_management_system;

DROP PROCEDURE IF EXISTS GetStudentAttendance;
DROP PROCEDURE IF EXISTS GetTrainingEnrollmentCount;
DROP PROCEDURE IF EXISTS GetSessionAttendanceSummary;

DELIMITER $$

-- ---------------------------------------------------------------------
-- 1. All sessions a student is enrolled in, with attendance status.
--    IN parameter + multi-table JOIN.
-- ---------------------------------------------------------------------
CREATE PROCEDURE GetStudentAttendance(IN p_student_id INT)
BEGIN
    SELECT e.Session_ID,
           ts.Session_Date,
           ts.Start_Time,
           ts.End_Time,
           t.Tr_Name,
           tr.Trainer_Name,
           e.Attendance_Status
      FROM session_enrollment e
      JOIN training_session ts ON e.Session_ID   = ts.Session_ID
      JOIN training         t  ON ts.Training_ID = t.Training_ID
      JOIN trainer          tr ON ts.Trainer_ID  = tr.Trainer_ID
     WHERE e.Student_ID = p_student_id
     ORDER BY ts.Session_Date DESC, ts.Start_Time DESC;
END$$

-- ---------------------------------------------------------------------
-- 2. Enrollment and attendance totals for every training programme.
--    LEFT JOIN + GROUP BY + COUNT / SUM + attendance percentage.
-- ---------------------------------------------------------------------
CREATE PROCEDURE GetTrainingEnrollmentCount()
BEGIN
    SELECT t.Training_ID,
           t.Tr_Name,
           t.Tr_Type,
           COUNT(DISTINCT ts.Session_ID)                               AS Total_Sessions,
           COUNT(e.Student_ID)                                         AS Total_Enrollments,
           COALESCE(SUM(e.Attendance_Status = 'Present'), 0)           AS Present_Count,
           COALESCE(SUM(e.Attendance_Status = 'Absent'), 0)            AS Absent_Count,
           ROUND(100 * SUM(e.Attendance_Status = 'Present')
                 / NULLIF(SUM(e.Attendance_Status IN ('Present', 'Absent')), 0), 2)
                                                                       AS Attendance_Pct
      FROM training t
      LEFT JOIN training_session   ts ON ts.Training_ID = t.Training_ID
      LEFT JOIN session_enrollment e  ON e.Session_ID   = ts.Session_ID
     GROUP BY t.Training_ID, t.Tr_Name, t.Tr_Type
     ORDER BY Total_Enrollments DESC, t.Tr_Name;
END$$

-- ---------------------------------------------------------------------
-- 3. Attendance counts for one session, returned through OUT parameters.
-- ---------------------------------------------------------------------
CREATE PROCEDURE GetSessionAttendanceSummary(
    IN  p_session_id INT,
    OUT p_present    INT,
    OUT p_absent     INT,
    OUT p_pending    INT
)
BEGIN
    SELECT COALESCE(SUM(Attendance_Status = 'Present'), 0),
           COALESCE(SUM(Attendance_Status = 'Absent'), 0),
           COALESCE(SUM(Attendance_Status = 'Pending'), 0)
      INTO p_present, p_absent, p_pending
      FROM session_enrollment
     WHERE Session_ID = p_session_id;
END$$

DELIMITER ;

-- Quick test (optional):
--   CALL GetStudentAttendance(1);
--   CALL GetTrainingEnrollmentCount();
--   CALL GetSessionAttendanceSummary(401, @p, @a, @n);  SELECT @p, @a, @n;
