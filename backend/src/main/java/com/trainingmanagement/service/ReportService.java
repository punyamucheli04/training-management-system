package com.trainingmanagement.service;

import com.trainingmanagement.exception.ResourceNotFoundException;
import com.trainingmanagement.repository.ReportRepository;
import com.trainingmanagement.repository.ReportRepository.ReportDefinition;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lists and runs the SQL demonstration reports. */
@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public List<Map<String, Object>> list() {
        return reportRepository.definitions().stream().map(this::describe).toList();
    }

    public Map<String, Object> run(String key) {
        ReportDefinition report = reportRepository.definition(key);
        if (report == null) {
            throw new ResourceNotFoundException("Report '" + key + "' does not exist.");
        }
        Map<String, Object> result = describe(report);
        result.put("sql", report.sql());
        result.put("rows", reportRepository.run(report));
        return result;
    }

    private Map<String, Object> describe(ReportDefinition r) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("key", r.key());
        m.put("title", r.title());
        m.put("technique", r.technique());
        m.put("description", r.description());
        return m;
    }
}
