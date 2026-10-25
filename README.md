# Training Management System

A full-stack DBMS project for managing students, trainers, departments, training programmes,
sessions, enrollments and attendance.

```text
React + Vite (frontend, port 5173)
      |
      |  HTTP REST API (JSON)
      v
Spring Boot (backend, port 8080)
      |
      |  JDBC  (JdbcTemplate + plain JDBC transaction)
      v
MySQL  (existing database: training_management_system)
```

The frontend never talks to MySQL. All SQL is executed by the Spring Boot backend through JDBC.
No JPA / Hibernate / ORM is used.

---

## 1. Requirements

| Tool | Version | Check with |
|------|---------|------------|
| Java JDK | 17 or newer (17 / 21 recommended) | `java -version` |
| Maven | 3.8 or newer | `mvn -v` |
| Node.js | 18 or newer (20 / 22 recommended) | `node -v` |
| npm | 9 or newer (comes with Node.js) | `npm -v` |
| MySQL Server | 8.0 or newer | `mysql --version` |
| VS Code | any recent version | |

Recommended VS Code extensions (VS Code suggests them automatically when you open the folder):
*Extension Pack for Java* and *Spring Boot Extension Pack*.

---

## 2. Project structure

```text
training-management-system/
├── backend/                    Spring Boot + JDBC REST API
│   ├── pom.xml                 spring-boot-starter-jdbc + mysql-connector-j
│   ├── .env.example            optional place for the MySQL password
│   └── src/main/
│       ├── java/com/trainingmanagement/
│       │   ├── controller/     REST endpoints (no SQL here)
│       │   ├── service/        validation and business rules, @Transactional
│       │   ├── repository/     ALL SQL - JdbcTemplate / JDBC
│       │   ├── model/          Java classes for rows and request bodies
│       │   ├── config/         CORS and start-up database check
│       │   └── exception/      error handling -> clean JSON messages
│       └── resources/application.properties   <- MySQL username / password
├── frontend/                   React + Vite
│   ├── .env.example            VITE_API_URL
│   └── src/
│       ├── lib/api.js          every call to the REST API
│       ├── pages/              Dashboard, Students, Trainers, ... , SQL reports
│       └── components/         table, form, dialog, badges
├── database/
│   ├── schema.sql              (only for a NEW database) CREATE ... IF NOT EXISTS
│   ├── sample_data.sql         (optional) INSERT IGNORE sample rows
│   ├── views.sql               required - 3 views
│   ├── functions.sql           required - 2 stored functions
│   ├── procedures.sql          required - 3 stored procedures
│   ├── triggers.sql            required - 5 triggers + attendance_audit log table
│   ├── users.sql               required for login - adds user admin / admin123
│   └── demo_queries.sql        optional - queries to show in Workbench
└── README.md
```

---

## 3. Database setup (your existing database is kept as it is)

You already have the database `training_management_system` with the tables
`department, student, trainer, training, training_session, session_enrollment, users`.
**Do not recreate it.** The application uses these tables directly.

None of the scripts contain `DROP DATABASE`, `DROP TABLE` or `TRUNCATE`.
(The object scripts use `DROP PROCEDURE/FUNCTION/TRIGGER IF EXISTS`, which only replaces
the stored code, never table rows, so they can be run again safely.)

### 3a. If you already have the database (normal case)

Run only these scripts, in this order:

1. `database/views.sql`
2. `database/functions.sql`
3. `database/procedures.sql`
4. `database/triggers.sql` (also creates one new log table, `attendance_audit`)
5. `database/users.sql` (adds the login `admin` / `admin123` to your `users` table; skipped if `admin` already exists)

**Using MySQL Workbench**

1. Open MySQL Workbench and connect to your server.
2. Run `USE training_management_system;`
3. *File → Open SQL Script…* → choose `database/views.sql` → click the lightning icon (Execute).
4. Repeat for `functions.sql`, `procedures.sql`, `triggers.sql`, `users.sql`.

**Using the mysql command-line client** (works on Windows, macOS and Linux)

```text
mysql -u root -p
```
then inside the `mysql>` prompt (use your own path to the project folder, with forward slashes):
```sql
USE training_management_system;
SOURCE C:/Users/you/Desktop/training-management-system/database/views.sql;
SOURCE C:/Users/you/Desktop/training-management-system/database/functions.sql;
SOURCE C:/Users/you/Desktop/training-management-system/database/procedures.sql;
SOURCE C:/Users/you/Desktop/training-management-system/database/triggers.sql;
SOURCE C:/Users/you/Desktop/training-management-system/database/users.sql;
```

### 3b. Only for a brand-new, empty MySQL installation

Run `database/schema.sql` first (creates the database and tables only if they do not exist),
optionally `database/sample_data.sql` (uses `INSERT IGNORE`, never overwrites rows),
then the scripts from 3a.

---

## 4. MySQL username and password (backend)

Open **`backend/src/main/resources/application.properties`** and find:

```properties
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:YOUR_PASSWORD}
```

Replace `YOUR_PASSWORD` with your MySQL password (keep the `${DB_PASSWORD:` and `}` around it).
For example, if your password is `mysql@123`:

```properties
spring.datasource.password=${DB_PASSWORD:mysql@123}
```

If your MySQL user is not `root`, change `root` on the username line in the same way.
If MySQL does not run on port 3306, change `3306` in `spring.datasource.url`.

**Alternative (keeps the password out of the properties file):**
copy `backend/.env.example` to `backend/.env` and set `DB_PASSWORD=yourpassword` there.

---

## 5. Run the backend

Open a terminal in VS Code (*Terminal → New Terminal*) in the project folder:

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

When it is ready you will see lines like:

```text
JDBC connection OK -> database 'training_management_system' on MySQL 8.0.xx
All views, functions, procedures and triggers are installed.
Tomcat started on port 8080
```

Check it in the browser: <http://localhost:8080/api/health> should show `"connected": true`
and an empty `missingObjects` list. <http://localhost:8080/api/students> shows your students as JSON.

---

## 6. Run the frontend

Open a **second** terminal (leave the backend running):

```bash
cd frontend
npm install
npm run dev
```

Open <http://localhost:5173> and log in with **admin / admin123**.
Change the password right away with **Change password** at the bottom of the sidebar.

### How the login works

* Accounts come from your existing `users` table (`Username`, `Password_Hash`, `Role`).
* Passwords are stored as **PBKDF2** hashes (salted, 120 000 iterations, built into Java) -
  see `service/PasswordHasher.java`. Rows that already exist in your table with a SHA-256 hex
  hash or a plain-text password can also log in, and are not changed.
* After login the backend keeps the user in an **HTTP session** (cookie `JSESSIONID`).
  `config/AuthInterceptor.java` rejects every `/api` request without a session with HTTP 401,
  and the frontend then shows the login page again.
* To add another user, create a hash and insert it:
  ```bash
  cd backend
  java src/main/java/com/trainingmanagement/service/PasswordHasher.java theirPassword
  ```
  ```sql
  INSERT INTO users (Username, Email, Password_Hash, Role)
  VALUES ('teacher1', 'teacher1@college.edu', 'paste-the-printed-hash-here', 'Staff');
  ```

The frontend reads the API address from `VITE_API_URL`. Without a `.env` file it uses
`http://localhost:8080/api`. To change it, copy `frontend/.env.example` to `frontend/.env`.

---

## 7. URLs

| | URL |
|---|---|
| Frontend | <http://localhost:5173> |
| Backend | <http://localhost:8080> |
| API base | <http://localhost:8080/api> |
| Connection check | <http://localhost:8080/api/health> |

---

## 8. Where JDBC is used (to show your faculty)

| What | File |
|------|------|
| Dependencies `spring-boot-starter-jdbc` and `mysql-connector-j` | `backend/pom.xml` |
| JDBC URL, user, password, driver class | `backend/src/main/resources/application.properties` |
| `JdbcTemplate` queries with `?` parameters (PreparedStatement) and `RowMapper` | every class in `backend/.../repository/` (e.g. `StudentRepository.java`) |
| Generated AUTO_INCREMENT key with `KeyHolder` | `StudentRepository.insert()` |
| **Manual JDBC transaction**: `setAutoCommit(false)`, `commit()`, `rollback()` | `EnrollmentRepository.enrollStudent()` |
| Spring `@Transactional` over JDBC (two deletes / bulk update, all-or-nothing) | `StudentService.delete()`, `SessionService.delete()`, `EnrollmentService.updateSessionAttendance()` |
| Stored procedure through `CallableStatement` (IN parameter, result set) | `AttendanceRepository.studentAttendance()` |
| Stored procedure with OUT parameters | `SessionRepository.attendanceSummary()` |
| Stored function in a SELECT | `StudentRepository` (`calculate_attendance_percentage`) |
| MySQL error codes → friendly messages | `exception/GlobalExceptionHandler.java` |

Flow for one request, e.g. *GET /api/students*:
`StudentController.getAll()` → `StudentService.getAll()` → `StudentRepository.findAll()` →
`jdbcTemplate.query("SELECT … FROM student_department_view v …", ROW_MAPPER)` → MySQL.

---

## 9. DBMS concepts in this project

| Concept | Where |
|---------|-------|
| Primary / composite / foreign keys | `schema.sql` (`session_enrollment` has PK `(Session_ID, Student_ID)`) |
| JOIN (2–5 tables), LEFT JOIN | views, repositories, reports |
| GROUP BY + COUNT, AVG, SUM, MAX, MIN | Departments page, dashboard, SQL reports |
| HAVING | report *Sessions with more than one student* |
| Subqueries: scalar, correlated, EXISTS, NOT EXISTS, NOT IN | dashboard totals, trainer list, SQL reports |
| Attendance percentage in SQL | `calculate_attendance_percentage()`, `AttendanceRepository.attendanceByStudent()` |
| Views | `student_department_view`, `session_details_view`, `enrollment_details_view` |
| Stored procedures | `GetStudentAttendance`, `GetTrainingEnrollmentCount`, `GetSessionAttendanceSummary` |
| Stored functions | `calculate_attendance_percentage`, `session_duration_hours` |
| Triggers | status validation, no attendance for future sessions, attendance history log, trainer double-booking check |
| Transactions (commit / rollback) | enrolling a student, deleting with dependents, "Mark all present" |

The **SQL reports** page in the app shows each query's SQL text next to its live result.

---

## 10. REST API

| Method | URL | Purpose |
|--------|-----|---------|
| GET/POST | `/api/students` (`?search=`) | list / add |
| GET/PUT/DELETE | `/api/students/{id}` | read / update / delete (also removes enrollments) |
| GET | `/api/students/{id}/attendance` | history via stored procedure |
| GET/POST | `/api/trainers`, GET/PUT/DELETE `/api/trainers/{id}` | trainers |
| GET/POST | `/api/departments`, GET/PUT/DELETE `/api/departments/{id}` | departments |
| GET/POST | `/api/trainings`, GET/PUT/DELETE `/api/trainings/{id}` | trainings |
| GET/POST | `/api/sessions` (`?status=upcoming\|completed&search=`), GET/PUT/DELETE `/api/sessions/{id}` | sessions |
| GET | `/api/sessions/{id}/attendance-summary` | procedure with OUT parameters |
| PUT | `/api/sessions/{id}/attendance` | bulk attendance in one transaction |
| GET/POST | `/api/enrollments` (`?sessionId=&studentId=`) | list / enroll (JDBC transaction) |
| DELETE | `/api/enrollments/{sessionId}/{studentId}` | remove enrollment |
| PUT | `/api/enrollments/{sessionId}/{studentId}/attendance` | body `{"Attendance_Status":"Present"}` |
| GET | `/api/attendance/students`, `/api/attendance/history` | percentages, change log |
| POST | `/api/auth/login`, `/api/auth/logout` | log in (body `{"Username","Password"}`) / log out |
| GET / PUT | `/api/auth/me`, `/api/auth/password` | current user / change password |
| GET | `/api/dashboard`, `/api/health` | dashboard data, connection check |
| GET | `/api/reports`, `/api/reports/{key}` | SQL demonstration reports |

JSON field names are exactly the MySQL column names (`Student_ID`, `Dept_Name`, `Attendance_Status`, …),
so the database, the API and the React code use the same names.

All endpoints except login, logout, me and health require a logged-in session.

Errors are returned as `{ "status": 409, "error": "Conflict", "message": "…" }` with the proper HTTP
status (400 invalid data, 401 not logged in, 404 not found, 409 duplicate / in use, 503 database unreachable).

---

## 11. Deploying online

See **[DEPLOY.md](DEPLOY.md)**: one container (`Dockerfile`) serving the React pages and the API over HTTPS,
hosted MySQL with an encrypted connection, a least-privilege database user, secure login cookie and
login lockout. Local development is unchanged.

## 12. Troubleshooting

| Problem | Fix |
|---------|-----|
| `Access denied for user 'root'@'localhost'` in the backend log | Wrong password in `application.properties` (section 4). |
| `Communications link failure` | MySQL server is not running, or the port in the URL is wrong. |
| `Unknown database 'training_management_system'` | Check the database name, or run `schema.sql` on a new installation. |
| Yellow banner "Some database objects are not installed yet" | Run the scripts listed in the banner (section 3a). |
| `mvn` is not recognised | Install Maven and add its `bin` folder to PATH, then reopen VS Code. |
| Frontend shows "Cannot reach the backend" | Start the backend first (section 5) and keep that terminal open. |
| Port 8080 or 5173 already in use | Close the other program, or change `server.port` / `vite.config.js` and `VITE_API_URL`. |
| Login says "Incorrect username or password" for admin | Run `database/users.sql`. If your table already had a user named `admin`, use that user's existing password. |
| Error 1418 while running `functions.sql` | Run `SET GLOBAL log_bin_trust_function_creators = 1;` as root, then run the script again. |
