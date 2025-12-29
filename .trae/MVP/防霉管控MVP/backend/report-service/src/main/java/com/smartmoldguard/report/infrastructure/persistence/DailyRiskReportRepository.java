package com.smartmoldguard.report.infrastructure.persistence;

import com.smartmoldguard.report.domain.model.DailyRiskReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyRiskReportRepository extends JpaRepository<DailyRiskReport, Long> {
    Optional<DailyRiskReport> findByReportDate(LocalDate reportDate);
}
