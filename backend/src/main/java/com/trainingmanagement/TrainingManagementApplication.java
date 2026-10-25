package com.trainingmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the Training Management System backend.
 *
 * Architecture:
 *   React (Vite)  --HTTP/JSON-->  Controller  ->  Service  ->  Repository
 *                                                              |
 *                                                   JdbcTemplate / JDBC
 *                                                              |
 *                                                            MySQL
 *
 * Only spring-boot-starter-jdbc is used for database access (no JPA/Hibernate).
 */
@SpringBootApplication
public class TrainingManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainingManagementApplication.class, args);
    }
}
