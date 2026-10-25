-- =====================================================================
--  Training Management System  -  views.sql   (REQUIRED)
-- ---------------------------------------------------------------------
--  A VIEW is a stored SELECT query that behaves like a virtual table.
--  Views hold no data of their own, so creating or replacing a view
--  never changes or deletes any rows in your tables.
--
--  "CREATE OR REPLACE VIEW" is used so the script can be run again
--  safely (MySQL has no "CREATE VIEW IF NOT EXISTS").
--
--  Used by the backend:
--    student_department_view  -> GET /api/students
--    session_details_view     -> GET /api/sessions, dashboard
--    enrollment_details_view  -> GET /api/enrollments, dashboard
-- =====================================================================

USE training_management_system;

-- 1. Student with department details (LEFT JOIN keeps students that
--    have no department assigned).
CREATE OR REPLACE VIEW student_department_view AS
SELECT
    s.Student_ID,
    s.Name,
    s.Gender,
    s.DOB,
    s.Email,
    s.Mobile_Number,
    s.CGPA,
    s.Dept_ID,
    d.Dept_Name,
    d.HOD_Name
FROM student s
LEFT JOIN department d ON s.Dept_ID = d.Dept_ID;


-- 2. Session with training, trainer and enrollment counts
--    (3-table JOIN + correlated subqueries + CASE expression).
CREATE OR REPLACE VIEW session_details_view AS
SELECT
    ts.Session_ID,
    ts.Session_Date,
    ts.Start_Time,
    ts.End_Time,
    ts.Training_ID,
    t.Tr_Name,
    t.Tr_Type,
    ts.Trainer_ID,
    tr.Trainer_Name,
    (SELECT COUNT(*)
       FROM session_enrollment e
      WHERE e.Session_ID = ts.Session_ID)                     AS Enrolled_Count,
    (SELECT COUNT(*)
       FROM session_enrollment e
      WHERE e.Session_ID = ts.Session_ID
        AND e.Attendance_Status = 'Present')                  AS Present_Count,
    CASE
        WHEN ts.Session_Date < CURDATE() THEN 'Completed'
        WHEN ts.Session_Date = CURDATE() THEN 'Today'
        ELSE 'Upcoming'
    END                                                       AS Session_Status
FROM training_session ts
JOIN training t  ON ts.Training_ID = t.Training_ID
JOIN trainer  tr ON ts.Trainer_ID  = tr.Trainer_ID;


-- 3. Enrollment with readable student / session / training names
--    (5-table JOIN).
CREATE OR REPLACE VIEW enrollment_details_view AS
SELECT
    e.Session_ID,
    e.Student_ID,
    s.Name           AS Student_Name,
    s.Email          AS Student_Email,
    d.Dept_Name,
    ts.Session_Date,
    ts.Start_Time,
    ts.End_Time,
    t.Training_ID,
    t.Tr_Name,
    tr.Trainer_Name,
    e.Attendance_Status,
    e.Enrolled_At
FROM session_enrollment e
JOIN student          s  ON e.Student_ID  = s.Student_ID
LEFT JOIN department  d  ON s.Dept_ID     = d.Dept_ID
JOIN training_session ts ON e.Session_ID  = ts.Session_ID
JOIN training         t  ON ts.Training_ID = t.Training_ID
JOIN trainer          tr ON ts.Trainer_ID  = tr.Trainer_ID;
