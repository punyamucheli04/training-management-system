# Backend - Spring Boot + JDBC

REST API for the Training Management System. Database access uses **JDBC only**
(`spring-boot-starter-jdbc`, `JdbcTemplate`, MySQL Connector/J). There is no JPA or Hibernate.

## Configure MySQL

Edit `src/main/resources/application.properties` and replace `YOUR_PASSWORD`:

```properties
spring.datasource.password=${DB_PASSWORD:YOUR_PASSWORD}
```

or copy `.env.example` to `.env` and set `DB_PASSWORD` there.

## Run

```bash
mvn clean install
mvn spring-boot:run
```

The API starts on <http://localhost:8080>. Open <http://localhost:8080/api/health> to confirm the
JDBC connection (`"connected": true`).

`mvn clean install` also runs the unit tests in `src/test` (they do not need MySQL).

## Layers

```text
controller/   receives HTTP requests, returns JSON         (no SQL)
service/      validation, business rules, @Transactional
repository/   SQL executed with JdbcTemplate / JDBC        (all SQL lives here)
model/        plain Java classes; JSON names = column names
exception/    GlobalExceptionHandler -> clean error JSON
config/       CORS for http://localhost:5173, start-up DB check
```

Key JDBC examples:

* `repository/StudentRepository.java` - `jdbcTemplate.query(...)`, `RowMapper`, `KeyHolder`
* `repository/EnrollmentRepository.java` - `enrollStudent()`: manual transaction with
  `setAutoCommit(false)`, `commit()` and `rollback()`
* `repository/AttendanceRepository.java` / `SessionRepository.java` - stored procedures via
  `CallableStatement` (IN and OUT parameters)
