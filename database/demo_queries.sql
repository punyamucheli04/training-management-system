-- =====================================================================
--  Training Management System  -  demo_queries.sql   (OPTIONAL, READ-ONLY)
-- ---------------------------------------------------------------------
--  Handy queries to run in MySQL Workbench during a demonstration.
--  Every statement here only READS data (SELECT / CALL), except the
--  last section which shows the triggers rejecting bad data.
--  The same SQL is used by the backend (see the comments).
-- =====================================================================

USE training_management_system;

-- 1. JOIN: students with their department  (StudentRepository via student_department_view)
SELECT s.Student_ID, s.Name, d.Dept_Name
  FROM student s
  JOIN department d ON s.Dept_ID = d.Dept_ID;

-- 2. GROUP BY + COUNT / AVG / MAX / MIN  (ReportRepository "department-strength")
SELECT d.Dept_Name,
       COUNT(s.Student_ID)   AS Students,
       ROUND(AVG(s.CGPA), 2) AS Avg_CGPA,
       MAX(s.CGPA)           AS Max_CGPA,
       MIN(s.CGPA)           AS Min_CGPA
  FROM department d
  LEFT JOIN student s ON s.Dept_ID = d.Dept_ID
 GROUP BY d.Dept_ID, d.Dept_Name;

-- 3. HAVING: sessions with more than one student  (ReportRepository "popular-sessions")
SELECT Session_ID, COUNT(*) AS Enrolled_Students
  FROM session_enrollment
 GROUP BY Session_ID
HAVING COUNT(*) > 1;

-- 4. SUM: total teaching hours per trainer, using the stored function
SELECT tr.Trainer_Name,
       COALESCE(SUM(session_duration_hours(ts.Start_Time, ts.End_Time)), 0) AS Total_Hours
  FROM trainer tr
  LEFT JOIN training_session ts ON ts.Trainer_ID = tr.Trainer_ID
 GROUP BY tr.Trainer_ID, tr.Trainer_Name;

-- 5. Scalar subquery: students above the average CGPA
SELECT Name, CGPA
  FROM student
 WHERE CGPA > (SELECT AVG(CGPA) FROM student);

-- 6. Correlated subquery: topper of each department
SELECT s.Dept_ID, s.Name, s.CGPA
  FROM student s
 WHERE s.CGPA = (SELECT MAX(s2.CGPA) FROM student s2 WHERE s2.Dept_ID = s.Dept_ID);

-- 7. NOT EXISTS: students who never enrolled
SELECT s.Student_ID, s.Name
  FROM student s
 WHERE NOT EXISTS (SELECT 1 FROM session_enrollment e WHERE e.Student_ID = s.Student_ID);

-- 8. Attendance percentage in SQL  (AttendanceRepository.attendanceByStudent)
SELECT s.Student_ID,
       s.Name,
       SUM(CASE WHEN e.Attendance_Status = 'Present' THEN 1 ELSE 0 END) AS Present_Count,
       SUM(CASE WHEN e.Attendance_Status IN ('Present', 'Absent') THEN 1 ELSE 0 END) AS Marked,
       ROUND(100 * SUM(CASE WHEN e.Attendance_Status = 'Present' THEN 1 ELSE 0 END)
             / NULLIF(SUM(CASE WHEN e.Attendance_Status IN ('Present', 'Absent') THEN 1 ELSE 0 END), 0), 2) AS Attendance_Pct
  FROM student s
  LEFT JOIN session_enrollment e ON e.Student_ID = s.Student_ID
 GROUP BY s.Student_ID, s.Name;

-- 9. Stored function
SELECT Student_ID, Name, calculate_attendance_percentage(Student_ID) AS Attendance_Pct
  FROM student;

-- 10. Views
SELECT * FROM student_department_view;
SELECT * FROM session_details_view;
SELECT * FROM enrollment_details_view;

-- 11. Stored procedures
CALL GetStudentAttendance(1);
CALL GetTrainingEnrollmentCount();
CALL GetSessionAttendanceSummary(401, @present, @absent, @pending);
SELECT @present AS Present, @absent AS Absent, @pending AS Pending;

-- 12. Triggers in action (each statement below is EXPECTED to fail with
--     the trigger's message; nothing is changed). Uncomment to try:
-- UPDATE session_enrollment SET Attendance_Status = 'Late'
--  WHERE Session_ID = 401 AND Student_ID = 1;
-- INSERT INTO training_session (Session_Date, Start_Time, End_Time, Training_ID, Trainer_ID)
-- VALUES (CURDATE(), '12:00:00', '10:00:00', 201, 301);

-- Attendance change history written by trg_enrollment_after_update
SELECT * FROM attendance_audit ORDER BY Changed_At DESC LIMIT 20;
