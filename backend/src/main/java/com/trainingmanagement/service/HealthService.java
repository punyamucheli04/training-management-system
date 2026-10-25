package com.trainingmanagement.service;

import com.trainingmanagement.repository.HealthRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Reports whether the JDBC connection works and which DB objects are missing. */
@Service
public class HealthService {

    private final HealthRepository healthRepository;

    /** false in production: do not reveal database name / version / objects */
    @Value("${app.health.show-details:true}")
    private boolean showDetails;

    public HealthService(HealthRepository healthRepository) {
        this.healthRepository = healthRepository;
    }

    public Map<String, Object> check() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            java.util.Map<String, Object> info = healthRepository.connectionInfo();
            result.put("connected", true);
            if (showDetails) {
                result.putAll(info);
                result.put("missingObjects", healthRepository.missingDatabaseObjects());
            }
        } catch (Exception ex) {
            result.put("connected", false);
            result.put("message", showDetails
                    ? "Cannot connect to MySQL. Check MySQL is running and the credentials in "
                            + "backend/src/main/resources/application.properties."
                    : "The database is not reachable at the moment.");
        }
        return result;
    }
}
