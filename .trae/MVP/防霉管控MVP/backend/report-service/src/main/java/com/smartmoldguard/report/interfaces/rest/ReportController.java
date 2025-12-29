package com.smartmoldguard.report.interfaces.rest;

import com.smartmoldguard.report.application.service.ReportExportService;
import com.smartmoldguard.report.application.service.ReportGeneratorService;
import com.smartmoldguard.report.domain.model.DailyRiskReport;
import com.smartmoldguard.report.infrastructure.persistence.DailyRiskReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGeneratorService reportGeneratorService;
    private final DailyRiskReportRepository dailyRiskReportRepository;
    private final ReportExportService reportExportService;

    @PostMapping("/generate/daily")
    public ResponseEntity<DailyRiskReport> triggerDailyReport(@RequestParam(required = false) String date) {
        LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(reportGeneratorService.generateReportForDate(reportDate));
    }

    @GetMapping("/daily")
    public ResponseEntity<List<DailyRiskReport>> getDailyReports() {
        return ResponseEntity.ok(dailyRiskReportRepository.findAll());
    }
    
    @GetMapping("/dashboard/overview")
    public ResponseEntity<DailyRiskReport> getDashboardOverview() {
        // For MVP, return today's live report (calculated on the fly or last generated)
        return ResponseEntity.ok(reportGeneratorService.generateReportForDate(LocalDate.now()));
    }

    @GetMapping("/daily/{date}/export/pdf")
    @SuppressWarnings("null")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String date) {
        DailyRiskReport report = reportGeneratorService.generateReportForDate(LocalDate.parse(date));
        byte[] pdfBytes = reportExportService.exportToPdf(report);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=report-" + date + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
