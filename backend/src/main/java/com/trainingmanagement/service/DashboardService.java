package com.trainingmanagement.service;

import com.trainingmanagement.repository.DashboardRepository;
import com.trainingmanagement.repository.EnrollmentRepository;
import com.trainingmanagement.repository.ReportRepository;
import com.trainingmanagement.repository.SessionRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/** Collects everything the dashboard page shows in one response. */
@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ReportRepository reportRepository;

    public DashboardService(DashboardRepository dashboardRepository,
                            SessionRepository sessionRepository,
                            EnrollmentRepository enrollmentRepository,
                            ReportRepository reportRepository) {
        this.dashboardRepository = dashboardRepository;
        this.sessionRepository = sessionRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.reportRepository = reportRepository;
    }

    public Map<String, Object> getDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totals", dashboardRepository.totals());
        data.put("upcomingSessions", sessionRepository.findUpcoming(6));
        data.put("recentEnrollments", enrollmentRepository.findRecent(6));
        data.put("trainingPrograms", reportRepository.trainingEnrollmentCount()); // stored procedure
        data.put("attendanceByDepartment", dashboardRepository.attendanceByDepartment());
        return data;
    }
}
