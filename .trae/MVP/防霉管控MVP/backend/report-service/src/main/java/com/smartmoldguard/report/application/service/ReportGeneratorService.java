package com.smartmoldguard.report.application.service;

import com.smartmoldguard.report.domain.model.DailyRiskReport;
import com.smartmoldguard.report.infrastructure.persistence.DailyRiskReportRepository;
import com.smartmoldguard.report.infrastructure.persistence.RiskEventLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGeneratorService {

    private final RiskEventLogRepository riskEventLogRepository;
    private final DailyRiskReportRepository dailyRiskReportRepository;

    // Run every day at 08:00 AM (Mocking every minute for demo/testing if needed, but let's stick to cron or manual trigger)
    // For E2E test, we might want a manual trigger endpoint.
    @Scheduled(cron = "0 0 8 * * ?") 
    @Transactional
    public void generateDailyReport() {
        generateReportForDate(LocalDate.now().minusDays(1)); // Generate for yesterday
    }

    @Transactional
    public DailyRiskReport generateReportForDate(LocalDate date) {
        log.info("Generating Daily Risk Report for {}", date);
        
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        
        Integer total = riskEventLogRepository.countByDateRange(start, end);
        Integer critical = riskEventLogRepository.countByRiskLevelAndDateRange("CRITICAL", start, end);
        Integer warning = riskEventLogRepository.countByRiskLevelAndDateRange("WARNING", start, end);
        
        DailyRiskReport report = dailyRiskReportRepository.findByReportDate(date)
                .orElse(DailyRiskReport.builder()
                        .reportDate(date)
                        .build());
        
        report.setTotalRisksDetected(total);
        report.setCriticalRisks(critical);
        report.setWarningRisks(warning);
        report.setGeneratedAt(LocalDateTime.now());
        
        return dailyRiskReportRepository.save(report);
    }
}
