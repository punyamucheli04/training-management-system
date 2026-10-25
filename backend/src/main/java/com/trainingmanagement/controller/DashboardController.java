package com.trainingmanagement.controller;

import com.trainingmanagement.service.DashboardService;
import com.trainingmanagement.service.HealthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Dashboard data and a simple connection check. */
@RestController
@RequestMapping("/api")
public class DashboardController {

    private final DashboardService dashboardService;
    private final HealthService healthService;

    public DashboardController(DashboardService dashboardService, HealthService healthService) {
        this.dashboardService = dashboardService;
        this.healthService = healthService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard() {
        return dashboardService.getDashboard();
    }

    /** GET /api/health - is the JDBC connection to MySQL working? */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return healthService.check();
    }
}
