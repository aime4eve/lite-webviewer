package com.documentpreview.modules.search.service;

import com.documentpreview.modules.document.domain.FileType;
import com.documentpreview.modules.search.domain.SearchMeta;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

@Component
public class SearchMetaBuilder {

    @Value("${app.search.extract.xlsx.rows:50000}")
    private int maxExcelRows;
    
    @Value("${app.search.extract.content.max-length:200000}")
    private int maxContentLength;

    public SearchMeta build(Path rootPath, Path filePath, long modifiedAt) {
        try {
            String rel = rootPath.relativize(filePath).toString().replace("\\", "/");
            String fileName = filePath.getFileName().toString();
            String parentDir = rel.contains("/") ? rel.substring(0, rel.lastIndexOf('/')) : "";
            String ext = FilenameUtils.getExtension(fileName).toLowerCase(Locale.ROOT);
            FileType type = FileType.fromExtension(ext);

            String title = fileName;
            String contentText = null;
            Long size = Files.size(filePath);

            if (type == FileType.MD || type == FileType.CSV) {
                // simple text read
                contentText = Files.readString(filePath, StandardCharsets.UTF_8);
            } else if (type == FileType.HTML || type == FileType.HTM) {
                // extract text from HTML
                String html = Files.readString(filePath, StandardCharsets.UTF_8);
                Document doc = Jsoup.parse(html);
                title = doc.title() != null && !doc.title().isBlank() ? doc.title() : title;
                contentText = doc.text();
            } else if (type == FileType.PDF) {
                // extract text from PDF (include head and tail pages for coverage)
                try (PDDocument pdf = org.apache.pdfbox.Loader.loadPDF(new File(filePath.toString()))) {
                    PDFTextStripper stripper = new PDFTextStripper();
                    stripper.setSortByPosition(true);
                    int total = pdf.getNumberOfPages();
                    int headEnd = Math.min(5, total);
                    stripper.setStartPage(1);
                    stripper.setEndPage(headEnd);
                    String head = stripper.getText(pdf);

                    String tail = "";
                    if (total > headEnd) {
                        PDFTextStripper tailStripper = new PDFTextStripper();
                        tailStripper.setSortByPosition(true);
                        int tailStart = Math.max(1, total - 5);
                        tailStripper.setStartPage(tailStart);
                        tailStripper.setEndPage(total);
                        tail = tailStripper.getText(pdf);
                    }

                    String h = (head == null ? "" : head);
                    String t = (tail == null ? "" : tail);
                    String combined = h + t;
                    if (combined.length() > maxContentLength) {
                        String hPart = h.substring(0, Math.min(h.length(), (int)(maxContentLength * 0.75)));
                        String tPart = t;
                        if (tPart.length() > maxContentLength / 4) {
                            tPart = tPart.substring(tPart.length() - (maxContentLength / 4));
                        }
                        contentText = hPart + tPart;
                    } else {
                        contentText = combined;
                    }
                }
            } else if (type == FileType.DOCX) {
                try (XWPFDocument docx = new XWPFDocument(new FileInputStream(filePath.toFile()))) {
                    StringBuilder sb = new StringBuilder();
                    int paraCount = 0;
                    for (XWPFParagraph p : docx.getParagraphs()) {
                        if (p.getText() != null && !p.getText().isBlank()) {
                            sb.append(p.getText()).append('\n');
                        }
                        paraCount++;
                        if (paraCount >= 200) break; // cap paragraphs to avoid heavy processing
                    }
                    contentText = sb.toString();
                }
            } else if (type == FileType.XLSX) {
                try (java.io.InputStream is = new java.io.FileInputStream(filePath.toFile())) {
                    org.apache.poi.ss.usermodel.Workbook wb = org.apache.poi.ss.usermodel.WorkbookFactory.create(is);
                    StringBuilder sb = new StringBuilder();
                    org.apache.poi.ss.usermodel.DataFormatter fmt = new org.apache.poi.ss.usermodel.DataFormatter();
                    org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
                    // configure formatter to be resilient
                    fmt.setUseCachedValuesForFormulaCells(true);
                    int rows = 0;
                    for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                        org.apache.poi.ss.usermodel.Sheet sheet = wb.getSheetAt(i);
                        for (org.apache.poi.ss.usermodel.Row row : sheet) {
                            for (org.apache.poi.ss.usermodel.Cell cell : row) {
                                String val = "";
                                try {
                                    // try getting rich text first for strings
                                    if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.STRING) {
                                        val = cell.getStringCellValue();
                                    } else {
                                        val = fmt.formatCellValue(cell, evaluator);
                                    }
                                } catch (Exception ignore) {
                                    try { val = cell.toString(); } catch (Exception ignored2) {}
                                }
                                if (val != null && !val.isBlank()) {
                                    sb.append(val).append(' ');
                                }
                            }
                            sb.append('\n');
                            rows++;
                            if (rows >= maxExcelRows) break;
                        }
                        if (rows >= maxExcelRows) break;
                    }
                    contentText = sb.toString();
                    wb.close();
                }
            }

            // truncate large text to avoid huge JSON
            if (contentText != null && contentText.length() > maxContentLength) {
                contentText = contentText.substring(0, maxContentLength);
            }

            return new SearchMeta(rel, fileName, parentDir, type, title, contentText, size, modifiedAt);
        } catch (Exception e) {
            return new SearchMeta(rootPath.relativize(filePath).toString().replace("\\", "/"), filePath.getFileName().toString(), "", null, null, null, null, modifiedAt);
        }
    }
}