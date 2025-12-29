package com.smartmoldguard.report.application.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.smartmoldguard.report.domain.model.DailyRiskReport;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class ReportExportService {

    public byte[] exportToPdf(DailyRiskReport report) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(bos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Daily Risk Report"));
            document.add(new Paragraph("Date: " + report.getReportDate()));
            document.add(new Paragraph("Total Risks Detected: " + report.getTotalRisksDetected()));
            document.add(new Paragraph("Critical Risks: " + report.getCriticalRisks()));
            document.add(new Paragraph("Warning Risks: " + report.getWarningRisks()));

            document.close();
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }
}
