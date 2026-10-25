-- =====================================================================
--  Training Management System  -  functions.sql   (REQUIRED)
-- ---------------------------------------------------------------------
--  Stored functions return a single value and can be used inside any
--  SELECT, e.g.  SELECT Name, calculate_attendance_percentage(Student_ID)
--                FROM student;
--
--  "DROP FUNCTION IF EXISTS" only removes the old version of the
--  function (code), never table data, so the script is re-runnable.
--
--  Run in the mysql command-line client or MySQL Workbench.
--  The DELIMITER lines are needed because the function body contains ';'.
-- =====================================================================

USE training_management_system;

DROP FUNCTION IF EXISTS calculate_attendance_percentage;
DROP FUNCTION IF EXISTS session_duration_hours;

DELIMITER $$

-- ---------------------------------------------------------------------
-- calculate_attendance_percentage(student_id)
--   Present sessions / sessions where attendance was taken * 100.
--   'Pending' sessions (not yet held / not yet marked) are excluded.
--   Returns NULL when no attendance has been recorded yet.
--
--   READS SQL DATA is required by MySQL when binary logging is on.
-- ---------------------------------------------------------------------
CREATE FUNCTION calculate_attendance_percentage(p_student_id INT)
RETURNS DECIMAL(5,2)
READS SQL DATA
BEGIN
    DECLARE v_present INT DEFAULT 0;
    DECLARE v_marked  INT DEFAULT 0;

    SELECT COALESCE(SUM(Attendance_Status = 'Present'), 0),
           COALESCE(SUM(Attendance_Status IN ('Present', 'Absent')), 0)
      INTO v_present, v_marked
      FROM session_enrollment
     WHERE Student_ID = p_student_id;

    IF v_marked = 0 THEN
        RETURN NULL;
    END IF;

    RETURN ROUND(v_present * 100.0 / v_marked, 2);
END$$

-- ---------------------------------------------------------------------
-- session_duration_hours(start, end)
--   Length of a session in hours, e.g. 09:30 -> 12:30 = 3.00
--   DETERMINISTIC: same input always gives the same output.
-- ---------------------------------------------------------------------
CREATE FUNCTION session_duration_hours(p_start TIME, p_end TIME)
RETURNS DECIMAL(5,2)
DETERMINISTIC
NO SQL
BEGIN
    RETURN ROUND(TIME_TO_SEC(TIMEDIFF(p_end, p_start)) / 3600, 2);
END$$

DELIMITER ;

-- Quick test (optional):
--   SELECT Student_ID, Name, calculate_attendance_percentage(Student_ID) AS Attendance_Pct
--     FROM student;
--   SELECT session_duration_hours('09:30:00', '12:30:00');   -- 3.00
