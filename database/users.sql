-- =====================================================================
--  Training Management System  -  users.sql   (REQUIRED FOR LOGIN)
-- ---------------------------------------------------------------------
--  Adds ONE login account to your existing users table:
--
--      Username: admin
--      Password: admin123      (change it after the first login:
--                               sidebar -> "Change password")
--
--  INSERT IGNORE: if a user named 'admin' (or the email) already exists,
--  nothing is inserted and nothing is overwritten.
--
--  The password is stored as a PBKDF2 hash (salted, 120 000 iterations),
--  never as plain text. To create the hash for another password run, from
--  the backend folder:
--      java src/main/java/com/trainingmanagement/service/PasswordHasher.java yourPassword
-- =====================================================================

USE training_management_system;

INSERT IGNORE INTO users (Username, Email, Password_Hash, Role)
VALUES ('admin', 'admin@college.edu',
        'pbkdf2_sha256$120000$nyrDk0ccHo4m8HrlkRtQzg==$2rpkoTNgp/KI0MzGgsltnqllbzxWivqKhUaRyB8x4Bo=',
        'Admin');

SELECT User_ID, Username, Email, Role, Created_At FROM users;
