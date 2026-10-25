package com.trainingmanagement.controller;

import com.trainingmanagement.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** SQL demonstration reports (JOIN, GROUP BY, HAVING, subqueries, procedures...). */
@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return reportService.list();
    }

    /** e.g. GET /api/reports/popular-sessions -> { title, technique, sql, rows } */
    @GetMapping("/{key}")
    public Map<String, Object> run(@PathVariable("key") String key) {
        return reportService.run(key);
    }
}
