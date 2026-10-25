package com.trainingmanagement.config;

import com.trainingmanagement.repository.HealthRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Runs once when the backend starts and prints whether the JDBC connection
 * to MySQL works, and whether the views / functions / procedures / triggers
 * from the database folder are installed.
 *
 * It never stops the application: if MySQL is down, the API still starts and
 * reports a clear error to the frontend.
 */
@Component
public class DatabaseStartupCheck implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupCheck.class);

    private final HealthRepository healthRepository;

    public DatabaseStartupCheck(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    @Override
    public void run(String... args) {
        try {
            Map<String, Object> info = healthRepository.connectionInfo();
            log.info("JDBC connection OK -> database '{}' on MySQL {}", info.get("database"), info.get("version"));

            List<String> missing = healthRepository.missingDatabaseObjects();
            if (missing.isEmpty()) {
                log.info("All views, functions, procedures and triggers are installed.");
            } else {
                log.warn("Missing database objects: {}", missing);
                log.warn("Run the scripts in the 'database' folder (see README, step 2).");
            }
        } catch (Exception ex) {
            log.error("Could NOT connect to MySQL: {}", ex.getMessage());
            log.error("Check spring.datasource.* in src/main/resources/application.properties (or backend/.env).");
        }
    }
}
