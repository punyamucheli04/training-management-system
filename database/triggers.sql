-- =====================================================================
--  Training Management System  -  triggers.sql   (REQUIRED)
-- ---------------------------------------------------------------------
--  Triggers run automatically inside MySQL when rows are inserted or
--  updated. They enforce rules even if data is changed outside the app
--  (for example directly from MySQL Workbench).
--
--   session_enrollment
--     trg_enrollment_before_insert : only Present / Absent / Pending allowed
--     trg_enrollment_before_update : same check + attendance cannot be
--                                    marked for a session in the future
--     trg_enrollment_after_update  : writes every attendance change to
--                                    the attendance_audit log table
--   training_session
--     trg_session_before_insert    : end time after start time, and the
--     trg_session_before_update      trainer must not have an overlapping
--                                    session on the same date
--
--  This file creates ONE new, additional table (attendance_audit) with
--  CREATE TABLE IF NOT EXISTS. No existing table is modified.
--  "DROP TRIGGER IF EXISTS" removes only the old trigger definition.
-- =====================================================================

USE training_management_system;

-- Log table filled by trg_enrollment_after_update.
-- No foreign keys on purpose: the history must stay even if an
-- enrollment is later removed.
CREATE TABLE IF NOT EXISTS attendance_audit (
    Audit_ID    INT          NOT NULL AUTO_INCREMENT,
    Session_ID  INT          NOT NULL,
    Student_ID  INT          NOT NULL,
    Old_Status  VARCHAR(20),
    New_Status  VARCHAR(20)  NOT NULL,
    Changed_By  VARCHAR(100),
    Changed_At  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (Audit_ID)
);

DROP TRIGGER IF EXISTS trg_enrollment_before_insert;
DROP TRIGGER IF EXISTS trg_enrollment_before_update;
DROP TRIGGER IF EXISTS trg_enrollment_after_update;
DROP TRIGGER IF EXISTS trg_session_before_insert;
DROP TRIGGER IF EXISTS trg_session_before_update;

DELIMITER $$

-- ---------------------------------------------------------------------
-- 1. Validate (and tidy) the attendance status of a new enrollment.
--    'present' or ' PRESENT ' becomes 'Present'; anything else is rejected.
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_enrollment_before_insert
BEFORE INSERT ON session_enrollment
FOR EACH ROW
BEGIN
    SET NEW.Attendance_Status = CONCAT(UPPER(LEFT(TRIM(NEW.Attendance_Status), 1)),
                                       LOWER(SUBSTRING(TRIM(NEW.Attendance_Status), 2)));

    IF NEW.Attendance_Status NOT IN ('Present', 'Absent', 'Pending') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid attendance status. Allowed values: Present, Absent, Pending';
    END IF;
END$$

-- ---------------------------------------------------------------------
-- 2. Validate attendance updates, and block marking Present/Absent
--    for a session whose date is still in the future.
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_enrollment_before_update
BEFORE UPDATE ON session_enrollment
FOR EACH ROW
BEGIN
    DECLARE v_session_date DATE;

    SET NEW.Attendance_Status = CONCAT(UPPER(LEFT(TRIM(NEW.Attendance_Status), 1)),
                                       LOWER(SUBSTRING(TRIM(NEW.Attendance_Status), 2)));

    IF NEW.Attendance_Status NOT IN ('Present', 'Absent', 'Pending') THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Invalid attendance status. Allowed values: Present, Absent, Pending';
    END IF;

    SELECT Session_Date INTO v_session_date
      FROM training_session
     WHERE Session_ID = NEW.Session_ID;

    IF NEW.Attendance_Status IN ('Present', 'Absent') AND v_session_date > CURDATE() THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Attendance cannot be marked for a session that has not taken place yet';
    END IF;
END$$

-- ---------------------------------------------------------------------
-- 3. Keep a history of every attendance change (audit log).
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_enrollment_after_update
AFTER UPDATE ON session_enrollment
FOR EACH ROW
BEGIN
    IF NOT (OLD.Attendance_Status <=> NEW.Attendance_Status) THEN
        INSERT INTO attendance_audit (Session_ID, Student_ID, Old_Status, New_Status, Changed_By)
        VALUES (NEW.Session_ID, NEW.Student_ID, OLD.Attendance_Status, NEW.Attendance_Status, CURRENT_USER());
    END IF;
END$$

-- ---------------------------------------------------------------------
-- 4. A new session must end after it starts, and its trainer must be
--    free (no overlapping session on the same date).
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_session_before_insert
BEFORE INSERT ON training_session
FOR EACH ROW
BEGIN
    IF NEW.End_Time <= NEW.Start_Time THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'End time must be later than start time';
    END IF;

    IF EXISTS (SELECT 1
                 FROM training_session ts
                WHERE ts.Trainer_ID   = NEW.Trainer_ID
                  AND ts.Session_Date = NEW.Session_Date
                  AND NEW.Start_Time  < ts.End_Time
                  AND NEW.End_Time    > ts.Start_Time) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This trainer already has a session that overlaps this date and time';
    END IF;
END$$

-- ---------------------------------------------------------------------
-- 5. Same rules when an existing session is edited
--    (the session being edited is excluded from the overlap check).
-- ---------------------------------------------------------------------
CREATE TRIGGER trg_session_before_update
BEFORE UPDATE ON training_session
FOR EACH ROW
BEGIN
    IF NEW.End_Time <= NEW.Start_Time THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'End time must be later than start time';
    END IF;

    IF EXISTS (SELECT 1
                 FROM training_session ts
                WHERE ts.Trainer_ID   = NEW.Trainer_ID
                  AND ts.Session_Date = NEW.Session_Date
                  AND ts.Session_ID  <> OLD.Session_ID
                  AND NEW.Start_Time  < ts.End_Time
                  AND NEW.End_Time    > ts.Start_Time) THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'This trainer already has a session that overlaps this date and time';
    END IF;
END$$

DELIMITER ;

-- Quick test (optional) - this should fail with the trigger's message:
--   INSERT INTO session_enrollment (Session_ID, Student_ID, Attendance_Status)
--   VALUES (401, 5, 'Late');
