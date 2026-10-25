-- =====================================================================
--  Training Management System  -  sample_data.sql   (OPTIONAL)
-- ---------------------------------------------------------------------
--  Only needed for a fresh, empty database.
--
--  Every statement is INSERT IGNORE:
--    * if a row with the same primary key (or unique email) already
--      exists, MySQL silently skips it, so your existing data is never
--      overwritten or duplicated.
--
--  Session dates are relative to CURDATE() so the dashboard always has
--  a mix of past sessions (with attendance) and upcoming sessions.
-- =====================================================================

USE training_management_system;

-- Departments ----------------------------------------------------------
INSERT IGNORE INTO department (Dept_ID, Dept_Name, HOD_Name) VALUES
    (101, 'CSE',   'Dr. Ramesh'),
    (102, 'ECE',   'Dr. Suresh'),
    (103, 'EEE',   'Dr. Lakshmi'),
    (104, 'Mech',  'Dr. Prakash'),
    (105, 'Civil', 'Dr. Anitha');

-- Students -------------------------------------------------------------
INSERT IGNORE INTO student (Student_ID, Name, Gender, DOB, Email, Mobile_Number, CGPA, Dept_ID) VALUES
    (1,  'Rahul',   'Male',   '2004-03-12', 'rahul@college.edu',   '9876500001', 8.45, 101),
    (2,  'Anjali',  'Female', '2004-07-22', 'anjali@college.edu',  '9876500002', 9.10, 101),
    (3,  'Kiran',   'Male',   '2003-11-05', 'kiran@college.edu',   '9876500003', 7.60, 102),
    (4,  'Sneha',   'Female', '2004-01-30', 'sneha@college.edu',   '9876500004', 8.90, 103),
    (5,  'Arjun',   'Male',   '2003-09-18', 'arjun@college.edu',   '9876500005', 6.95, 104),
    (6,  'Divya',   'Female', '2004-05-09', 'divya@college.edu',   '9876500006', 8.20, 102),
    (7,  'Vikram',  'Male',   '2003-12-25', 'vikram@college.edu',  '9876500007', 7.35, 105),
    (8,  'Meera',   'Female', '2004-02-14', 'meera@college.edu',   '9876500008', 9.40, 101);

-- Trainers -------------------------------------------------------------
INSERT IGNORE INTO trainer (Trainer_ID, Trainer_Name, Email, Expertise) VALUES
    (301, 'Amit Kumar',   'amit.kumar@training.org',   'Python'),
    (302, 'Priya Sharma', 'priya.sharma@training.org', 'Java'),
    (303, 'Ravi Teja',    'ravi.teja@training.org',    'Databases'),
    (304, 'Neha Singh',   'neha.singh@training.org',   'Soft Skills'),
    (305, 'Suresh Rao',   'suresh.rao@training.org',   'Machine Learning');

-- Training programmes --------------------------------------------------
INSERT IGNORE INTO training (Training_ID, Tr_Name, Tr_Type, Description, Duration_Hrs) VALUES
    (201, 'Python',           'Technical', 'Python programming fundamentals',          30),
    (202, 'Java',             'Technical', 'Core Java and object oriented programming', 40),
    (203, 'SQL',              'Technical', 'Relational databases and SQL queries',      20),
    (204, 'Communication',    'Soft Skill','Presentation and workplace communication',  12),
    (205, 'Machine Learning', 'Technical', 'Introduction to supervised learning',       36);

-- Training sessions (dates relative to today) ---------------------------
INSERT IGNORE INTO training_session (Session_ID, Session_Date, Start_Time, End_Time, Training_ID, Trainer_ID) VALUES
    (401, DATE_SUB(CURDATE(), INTERVAL 21 DAY), '09:30:00', '12:30:00', 201, 301),
    (402, DATE_SUB(CURDATE(), INTERVAL 14 DAY), '10:00:00', '13:00:00', 202, 302),
    (403, DATE_SUB(CURDATE(), INTERVAL 10 DAY), '14:00:00', '16:00:00', 203, 303),
    (404, DATE_SUB(CURDATE(), INTERVAL 7 DAY),  '11:00:00', '12:30:00', 204, 304),
    (405, DATE_SUB(CURDATE(), INTERVAL 3 DAY),  '09:30:00', '12:30:00', 201, 301),
    (406, DATE_ADD(CURDATE(), INTERVAL 2 DAY),  '10:00:00', '13:00:00', 205, 305),
    (407, DATE_ADD(CURDATE(), INTERVAL 5 DAY),  '14:00:00', '17:00:00', 202, 302),
    (408, DATE_ADD(CURDATE(), INTERVAL 9 DAY),  '09:00:00', '11:00:00', 203, 303);

-- Enrollments with attendance -------------------------------------------
INSERT IGNORE INTO session_enrollment (Session_ID, Student_ID, Attendance_Status, Enrolled_At) VALUES
    (401, 1, 'Present', DATE_SUB(NOW(), INTERVAL 25 DAY)),
    (401, 2, 'Present', DATE_SUB(NOW(), INTERVAL 25 DAY)),
    (401, 3, 'Absent',  DATE_SUB(NOW(), INTERVAL 24 DAY)),
    (401, 8, 'Present', DATE_SUB(NOW(), INTERVAL 24 DAY)),
    (402, 1, 'Present', DATE_SUB(NOW(), INTERVAL 18 DAY)),
    (402, 4, 'Absent',  DATE_SUB(NOW(), INTERVAL 18 DAY)),
    (402, 5, 'Present', DATE_SUB(NOW(), INTERVAL 17 DAY)),
    (403, 2, 'Present', DATE_SUB(NOW(), INTERVAL 13 DAY)),
    (403, 3, 'Present', DATE_SUB(NOW(), INTERVAL 13 DAY)),
    (403, 6, 'Absent',  DATE_SUB(NOW(), INTERVAL 12 DAY)),
    (404, 4, 'Present', DATE_SUB(NOW(), INTERVAL 9 DAY)),
    (404, 7, 'Absent',  DATE_SUB(NOW(), INTERVAL 9 DAY)),
    (405, 1, 'Absent',  DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (405, 8, 'Present', DATE_SUB(NOW(), INTERVAL 5 DAY)),
    (406, 2, 'Pending', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (406, 8, 'Pending', DATE_SUB(NOW(), INTERVAL 2 DAY)),
    (406, 6, 'Pending', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (407, 5, 'Pending', DATE_SUB(NOW(), INTERVAL 1 DAY)),
    (408, 3, 'Pending', NOW());
