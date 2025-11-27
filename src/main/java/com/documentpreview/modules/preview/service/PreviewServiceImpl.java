package com.documentpreview.modules.preview.service;
import com.documentpreview.modules.document.domain.PreviewContent;import com.documentpreview.modules.document.domain.FileType;import com.documentpreview.shared.ddd.Result;import com.vladsch.flexmark.html.HtmlRenderer;import com.vladsch.flexmark.parser.Parser;import com.vladsch.flexmark.util.ast.Node;import com.vladsch.flexmark.util.data.MutableDataSet;import org.apache.commons.io.FileUtils;import org.apache.commons.io.FilenameUtils;import org.jsoup.Jsoup;import org.jsoup.safety.Safelist;import com.opencsv.CSVReader;import com.opencsv.exceptions.CsvValidationException;import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.beans.factory.annotation.Value;import org.springframework.cache.annotation.Cacheable;import org.springframework.stereotype.Service;import java.io.*;import java.nio.charset.StandardCharsets;import java.nio.file.Files;import java.nio.file.Paths;import java.util.*;
@Service
public class PreviewServiceImpl implements PreviewService {
    private static final Logger logger = LoggerFactory.getLogger(PreviewServiceImpl.class);
    private static final Parser MARKDOWN_PARSER; private static final HtmlRenderer HTML_RENDERER;
    static { MutableDataSet options = new MutableDataSet(); MARKDOWN_PARSER = Parser.builder(options).build(); HTML_RENDERER = HtmlRenderer.builder(options).build(); }
    @Value("${app.preview.max-file-size}") private long maxFileSizeMB;
    @Value("${app.preview.pdf.max-pages-per-request}") private int maxPdfPagesPerRequest;
    @Value("${app.scan.root-dirs}") private String rootDir;
    @Override @Cacheable(value = "preview-content", key = "#filePath + '-' + #root.targetClass.simpleName")
    public Result<PreviewContent> generatePreview(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null"); File file = new File(filePath); if (!file.isAbsolute()) { file = new File(rootDir, filePath); } return generatePreview(file);
    }
    @Override @Cacheable(value = "preview-content", key = "#file.absolutePath + '-' + #root.targetClass.simpleName")
    public Result<PreviewContent> generatePreview(File file) {
        Objects.requireNonNull(file, "File cannot be null"); try {
            logger.info("Generating preview for file: {}", file.getAbsolutePath()); if (!file.exists() || !file.isFile()) { return Result.failure("File does not exist or is not a file"); }
            long fileSizeBytes = file.length(); long maxFileSizeBytes = maxFileSizeMB * 1024 * 1024; if (fileSizeBytes > maxFileSizeBytes) { return Result.failure(String.format("File size exceeds maximum limit of %d MB", maxFileSizeMB)); }
            String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase(); if (!isPreviewSupported(fileExtension)) { return Result.failure(String.format("Preview not supported for file type: %s", fileExtension)); }
            FileType fileType = FileType.fromExtension(fileExtension); switch (fileType) {
                case MD: return generateMarkdownPreview(file);
                case DOCX: return generateDocxPreview(file);
                case PDF: return generatePdfPreview(file.getAbsolutePath(), 1, maxPdfPagesPerRequest);
                case CSV: return generateCsvPreview(file);
                case SVG: return generateSvgPreview(file);
                case HTML: case HTM: return generateHtmlPreview(file);
                case XLSX: return generateXlsxPreview(file);
                default: return Result.failure(String.format("Preview not supported for file type: %s", fileExtension)); }
        } catch (Exception e) { String errorMessage = String.format("Failed to generate preview for file: %s", e.getMessage()); logger.error(errorMessage, e); return Result.failure(errorMessage); }
    }
    @Override @Cacheable(value = "preview-content", key = "#filePath + '-' + #startPage + '-' + #endPage + '-' + #root.targetClass.simpleName")
    public Result<PreviewContent> generatePdfPreview(String filePath, int startPage, int endPage) {
        Objects.requireNonNull(filePath, "File path cannot be null"); try {
            logger.info("Generating PDF preview for file: {}, pages {}-{}", filePath, startPage, endPage); if (startPage < 1) { startPage = 1; } int actualEndPage = Math.min(endPage, startPage + maxPdfPagesPerRequest - 1);
            byte[] pdfBytes = Files.readAllBytes(Paths.get(filePath)); PreviewContent previewContent = new PreviewContent("application/pdf", pdfBytes, true); return Result.success(previewContent);
        } catch (Exception e) { String errorMessage = String.format("Failed to generate PDF preview: %s", e.getMessage()); logger.error(errorMessage, e); return Result.failure(errorMessage); }
    }
    @Override public boolean isPreviewSupported(String fileType) { if (fileType == null) { return false; } FileType type = FileType.fromExtension(fileType); return type != null; }
    @Override public long getMaxSupportedFileSize() { return maxFileSizeMB * 1024 * 1024; }
    private Result<PreviewContent> generateMarkdownPreview(File file) throws IOException { String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8); Node document = MARKDOWN_PARSER.parse(markdown); String html = HTML_RENDERER.render(document); return Result.success(new PreviewContent("text/html", html, true)); }
    private Result<PreviewContent> generateDocxPreview(File file) throws IOException { return Result.failure("DOCX preview not implemented in Git push stub"); }
    private Result<PreviewContent> generateCsvPreview(File file) throws IOException, CsvValidationException { List<String[]> csvData = new ArrayList<>(); try (CSVReader reader = new CSVReader(new FileReader(file, StandardCharsets.UTF_8))) { String[] nextLine; while ((nextLine = reader.readNext()) != null) { csvData.add(nextLine); } } if (csvData.isEmpty()) { return Result.success(new PreviewContent("text/html", "<p>Empty CSV file</p>", true)); } StringBuilder htmlBuilder = new StringBuilder(); htmlBuilder.append("<html><body><table border='1' style='border-collapse: collapse;'>"); htmlBuilder.append("<thead><tr>"); for (String header : csvData.get(0)) { htmlBuilder.append("<th style='padding: 5px; background-color: #f2f2f2;'>").append(header).append("</th>"); } htmlBuilder.append("</tr></thead><tbody>"); for (int i = 1; i < csvData.size(); i++) { String[] row = csvData.get(i); htmlBuilder.append("<tr>"); for (String cell : row) { htmlBuilder.append("<td style='padding: 5px;'>").append(cell).append("</td>"); } htmlBuilder.append("</tr>"); } htmlBuilder.append("</tbody></table></body></html>"); return Result.success(new PreviewContent("text/html", htmlBuilder.toString(), true)); }
    private Result<PreviewContent> generateSvgPreview(File file) throws IOException { String svgContent = Files.readString(file.toPath(), StandardCharsets.UTF_8); String sanitizedSvg = Jsoup.clean(svgContent, Safelist.relaxed().addTags("svg","path","circle","rect","line","polyline","polygon","g","defs","style")); return Result.success(new PreviewContent("image/svg+xml", sanitizedSvg, true)); }
    private Result<PreviewContent> generateHtmlPreview(File file) throws IOException { String html = Files.readString(file.toPath(), StandardCharsets.UTF_8); String sanitizedHtml = Jsoup.clean(html, Safelist.relaxed().addTags("pre","code","iframe").addAttributes("iframe","src","width","height","allow","allowfullscreen").addAttributes("code","class")); return Result.success(new PreviewContent("text/html", sanitizedHtml, true)); }
    private Result<PreviewContent> generateXlsxPreview(File file) throws IOException { return Result.failure("XLSX preview not implemented in Git push stub"); }
}
