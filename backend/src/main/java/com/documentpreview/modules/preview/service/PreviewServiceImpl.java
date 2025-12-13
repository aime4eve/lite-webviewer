package com.documentpreview.modules.preview.service;

import com.documentpreview.modules.config.service.ConfigService;
import com.documentpreview.modules.document.domain.PreviewContent;
import com.documentpreview.modules.document.domain.FileType;
import com.documentpreview.shared.ddd.Result;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.zwobble.mammoth.DocumentConverter;
import java.util.Base64;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Implementation of the PreviewService interface.
 * This service handles the conversion of documents of various types into previewable formats.
 */
@Service
public class PreviewServiceImpl implements PreviewService {
    
    private static final Logger logger = LoggerFactory.getLogger(PreviewServiceImpl.class);
    
    // Markdown parser and renderer
    private static final Parser MARKDOWN_PARSER;
    private static final HtmlRenderer HTML_RENDERER;
    
    // Maximum pages to return for PDF preview
    private static final int MAX_PAGES_PER_PDF_REQUEST = 20;
    
    // Initialize Markdown parser and renderer
    static {
        MutableDataSet options = new MutableDataSet();
        // Enable table extensions for Markdown
        options.set(Parser.EXTENSIONS, Arrays.asList(
            com.vladsch.flexmark.ext.tables.TablesExtension.create()
        ));
        MARKDOWN_PARSER = Parser.builder(options).build();
        HTML_RENDERER = HtmlRenderer.builder(options).build();
    }
    
    @Value("${app.preview.max-file-size}")
    private long maxFileSizeMB;
    
    @Value("${app.preview.pdf.max-pages-per-request}")
    private int maxPdfPagesPerRequest;
    
    // 从ConfigService获取根目录配置
    private final ConfigService configService;
    
    // 构造函数注入ConfigService
    public PreviewServiceImpl(ConfigService configService) {
        this.configService = configService;
    }
    
    @Override
    @Cacheable(value = "preview-content", key = "#filePath + '-' + #root.targetClass.simpleName")
    public Result<PreviewContent> generatePreview(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        
        // Resolve relative paths against the root directory
        File file = new File(filePath);
        if (!file.isAbsolute()) {
            // Check if filePath already contains the root directory path
            // This happens when the frontend sends a relative path that is actually part of the indexed path
            // The file scan service stores relative paths from root, but sometimes we might get partial paths
            
            // Try resolving against configured root dirs
            // ConfigService might return comma-separated paths, we should try each
            String rootDirs = configService.getRootDirs();
            if (rootDirs != null && !rootDirs.isEmpty()) {
                String[] roots = rootDirs.split(",");
                boolean found = false;
                for (String root : roots) {
                    File candidate = new File(root.trim(), filePath);
                    if (candidate.exists()) {
                        file = candidate;
                        found = true;
                        break;
                    }
                }
                
                // If not found in any root, try using the first root as default base
                if (!found && roots.length > 0) {
                     file = new File(roots[0].trim(), filePath);
                }
            }
        } else {
             // Even if it is absolute, double check if it exists. 
             // If not, it might be a concatenation error or path mismatch from DB
             if (!file.exists()) {
                 // Try to strip the root dir if it's duplicated in the path
                 String rootDirs = configService.getRootDirs();
                 if (rootDirs != null) {
                     for (String root : rootDirs.split(",")) {
                         String rootPath = root.trim();
                         if (filePath.startsWith(rootPath + "/" + rootPath)) {
                             // Fix double root path issue: /root/root/file -> /root/file
                             File fixed = new File(filePath.replace(rootPath + "/" + rootPath, rootPath));
                             if (fixed.exists()) {
                                 file = fixed;
                                 break;
                             }
                         }
                     }
                 }
             }
        }
        
        return generatePreview(file);
    }
    
    @Override
    @Cacheable(value = "preview-content", key = "#file.absolutePath + '-' + #root.targetClass.simpleName")
    public Result<PreviewContent> generatePreview(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        
        try {
            logger.info("Generating preview for file: {}", file.getAbsolutePath());
            
            // Check if file exists
            if (!file.exists() || !file.isFile()) {
                return Result.failure("File does not exist or is not a file");
            }
            
            // Check file size
            long fileSizeBytes = file.length();
            long maxFileSizeBytes = maxFileSizeMB * 1024 * 1024;
            if (fileSizeBytes > maxFileSizeBytes) {
                return Result.failure(String.format("File size exceeds maximum limit of %d MB", maxFileSizeMB));
            }
            
            // Get file extension
            String fileExtension = FilenameUtils.getExtension(file.getName()).toLowerCase();
            
            // Check if preview is supported for this file type
            if (!isPreviewSupported(fileExtension)) {
                return Result.failure(String.format("Preview not supported for file type: %s", fileExtension));
            }
            
            // Generate preview based on file extension without enum switch to avoid synthetic class issues
            if ("md".equals(fileExtension)) {
                return generateMarkdownPreview(file);
            } else if ("doc".equals(fileExtension)) {
                return generateDocPreview(file);
            } else if ("docx".equals(fileExtension)) {
                return generateDocxPreview(file);
            } else if ("pdf".equals(fileExtension)) {
                return generatePdfPreview(file.getAbsolutePath(), 1, maxPdfPagesPerRequest);
            } else if ("csv".equals(fileExtension)) {
                return generateCsvPreview(file);
            } else if ("svg".equals(fileExtension)) {
                return generateSvgPreview(file);
            } else if ("html".equals(fileExtension) || "htm".equals(fileExtension)) {
                return generateHtmlPreview(file);
            } else if ("xlsx".equals(fileExtension) || "xls".equals(fileExtension)) {
                return generateXlsxPreview(file);
            } else if ("log".equals(fileExtension) || "txt".equals(fileExtension)) {
                return generateTextPreview(file);
            } else if ("jpg".equals(fileExtension) || "jpeg".equals(fileExtension) || "png".equals(fileExtension) || "gif".equals(fileExtension) || "bmp".equals(fileExtension) || "webp".equals(fileExtension)) {
                return generateImagePreview(file);
            } else {
                return Result.failure(String.format("Preview not supported for file type: %s", fileExtension));
            }
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to generate preview for file: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    @Cacheable(value = "preview-content", key = "#filePath + '-' + #startPage + '-' + #endPage + '-' + #root.targetClass.simpleName")
    public Result<PreviewContent> generatePdfPreview(String filePath, int startPage, int endPage) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        
        try {
            logger.info("Generating PDF preview for file: {}, pages {}-{}", filePath, startPage, endPage);
            
            // Validate page range
            if (startPage < 1) {
                startPage = 1;
            }
            
            // Limit pages per request
            int actualEndPage = Math.min(endPage, startPage + maxPdfPagesPerRequest - 1);
            
            // Return the original PDF bytes for simplicity and reliability
            byte[] pdfBytes = Files.readAllBytes(Paths.get(filePath));
            PreviewContent previewContent = new PreviewContent(
                    "application/pdf",
                    pdfBytes,
                    true
            );
            return Result.success(previewContent);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to generate PDF preview: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public boolean isPreviewSupported(String fileType) {
        if (fileType == null) {
            return false;
        }
        
        FileType type = FileType.fromExtension(fileType);
        return type != null;
    }
    
    @Override
    public long getMaxSupportedFileSize() {
        return maxFileSizeMB * 1024 * 1024;
    }
    
    /**
     * Generates preview content for a DOC file using Apache POI HWPF.
     * 
     * @param file The DOC file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateDocPreview(File file) {
        logger.debug("Generating DOC preview for file: {}", file.getAbsolutePath());
        try (InputStream fis = new FileInputStream(file);
             org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(fis);
             org.apache.poi.hwpf.extractor.WordExtractor extractor = new org.apache.poi.hwpf.extractor.WordExtractor(doc)) {
            
            String text = extractor.getText();
            // Convert text to simple HTML (preserving paragraphs)
            String html = "<html><body><pre style='white-space: pre-wrap; font-family: serif;'>" + 
                         org.springframework.web.util.HtmlUtils.htmlEscape(text) + 
                         "</pre></body></html>";
            
            PreviewContent previewContent = new PreviewContent(
                    "text/html",
                    html,
                    true
            );
            return Result.success(previewContent);
        } catch (org.apache.poi.OldFileFormatException e) {
            // Handle Word 6.0/95 files which HWPF might not support well
            logger.warn("Old Word format detected for file: {}", file.getName());
            return Result.failure("Format too old: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to generate DOC preview for file: " + file.getAbsolutePath(), e);
            return Result.failure("Failed to generate DOC preview: " + e.getMessage());
        }
    }

    /**
     * Generates preview content for an image file.
     * 
     * @param file The image file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateImagePreview(File file) throws IOException {
        logger.debug("Generating image preview for file: {}", file.getAbsolutePath());
        
        byte[] imageBytes = Files.readAllBytes(file.toPath());
        String mimeType = Files.probeContentType(file.toPath());
        if (mimeType == null) {
            String ext = FilenameUtils.getExtension(file.getName()).toLowerCase();
            switch (ext) {
                case "jpg":
                case "jpeg": mimeType = "image/jpeg"; break;
                case "png": mimeType = "image/png"; break;
                case "gif": mimeType = "image/gif"; break;
                case "bmp": mimeType = "image/bmp"; break;
                case "webp": mimeType = "image/webp"; break;
                default: mimeType = "application/octet-stream";
            }
        }
        
        PreviewContent previewContent = new PreviewContent(
                mimeType,
                imageBytes,
                true
        );
        
        return Result.success(previewContent);
    }

    /**
     * Generates preview content for a Markdown file.
     * 
     * @param file The Markdown file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateMarkdownPreview(File file) throws IOException {
        logger.debug("Generating Markdown preview for file: {}", file.getAbsolutePath());
        
        // Read Markdown content
        String markdown = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        
        // Parse and render Markdown to HTML
        Node document = MARKDOWN_PARSER.parse(markdown);
        String html = HTML_RENDERER.render(document);
        
        PreviewContent previewContent = new PreviewContent(
                "text/html",
                html,
                true
        );
        
        return Result.success(previewContent);
    }
    
    /**
     * Generates preview content for a DOCX file.
     * 
     * @param file The DOCX file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateDocxPreview(File file) throws IOException {
        logger.debug("Generating DOCX preview for file: {}", file.getAbsolutePath());

        DocumentConverter converter = new DocumentConverter()
                .addStyleMap("p[style-name='Title'] => h1:fresh")
                .addStyleMap("p[style-name='Heading 1'] => h1:fresh")
                .addStyleMap("p[style-name='Heading 2'] => h2:fresh")
                .addStyleMap("p[style-name='Heading 3'] => h3:fresh")
                .addStyleMap("p[style-name='Heading 4'] => h4:fresh")
                .addStyleMap("p[style-name='Heading 5'] => h5:fresh")
                .addStyleMap("p[style-name='Heading 6'] => h6:fresh")
                .addStyleMap("p[style-name='Subtitle'] => h2:fresh")
                .imageConverter(image -> {
                    try {
                        byte[] bytes;
                        try (InputStream in = image.getInputStream(); ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                            in.transferTo(bos);
                            bytes = bos.toByteArray();
                        }
                        String base64 = Base64.getEncoder().encodeToString(bytes);
                        String contentType = image.getContentType();
                        String src = "data:" + (contentType != null ? contentType : "image/png") + ";base64," + base64;
                        Map<String, String> attrs = new HashMap<>();
                        attrs.put("src", src);
                        attrs.put("alt", image.getAltText().orElse("image"));
                        return attrs;
                    } catch (IOException e) {
                        logger.warn("Image conversion failed: {}", e.getMessage());
                        return Map.of("alt", "image");
                    }
                });

        org.zwobble.mammoth.Result<String> result = converter.convertToHtml(file);
        String rawHtml = result.getValue();
        List<String> warnings = new ArrayList<>(result.getWarnings());
        if (!warnings.isEmpty()) {
            logger.warn("Mammoth warnings: {}", warnings);
        }

        if (rawHtml != null) {
            rawHtml = rawHtml.replaceAll("<img(?![^>]*src)", "<img src=\"\" ");
        }

        String sanitizedHtml = Jsoup.clean(rawHtml, Safelist.relaxed()
                .addTags("h1", "h2", "h3", "h4", "h5", "h6", "u", "s", "sup", "sub", "thead", "tbody", "colgroup", "col")
                .addAttributes("img", "src", "alt", "width", "height")
                .addAttributes("a", "href", "title", "target")
                .addAttributes("table", "border", "cellpadding", "cellspacing")
                .addAttributes("td", "colspan", "rowspan", "style")
                .addAttributes("th", "colspan", "rowspan", "style")
                .addAttributes("p", "style")
                .addProtocols("img", "src", "data", "http", "https")
        );

        PreviewContent previewContent = new PreviewContent(
                "text/html",
                sanitizedHtml,
                true
        );

        return Result.success(previewContent);
    }
    
    /**
     * Generates preview content for a CSV file.
     * 
     * @param file The CSV file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateCsvPreview(File file) throws IOException, CsvValidationException {
        logger.debug("Generating CSV preview for file: {}", file.getAbsolutePath());
        
        // Read CSV file
        List<String[]> csvData = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                csvData.add(nextLine);
            }
        }
        
        if (csvData.isEmpty()) {
            return Result.success(new PreviewContent(
                    "text/html",
                    "<p>Empty CSV file</p>",
                    true
            ));
        }
        
        // Convert CSV to HTML table
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body><table border='1' style='border-collapse: collapse;'>");
        
        // Add header row
        htmlBuilder.append("<thead><tr>");
        for (String header : csvData.get(0)) {
            htmlBuilder.append("<th style='padding: 5px; background-color: #f2f2f2;'>")
                      .append(header)
                      .append("</th>");
        }
        htmlBuilder.append("</tr></thead><tbody>");
        
        // Add data rows
        for (int i = 1; i < csvData.size(); i++) {
            String[] row = csvData.get(i);
            htmlBuilder.append("<tr>");
            for (String cell : row) {
                htmlBuilder.append("<td style='padding: 5px;'>")
                          .append(cell)
                          .append("</td>");
            }
            htmlBuilder.append("</tr>");
        }
        
        htmlBuilder.append("</tbody></table></body></html>");
        
        PreviewContent previewContent = new PreviewContent(
                "text/html",
                htmlBuilder.toString(),
                true
        );
        
        return Result.success(previewContent);
    }
    
    /**
     * Generates preview content for an SVG file.
     * 
     * @param file The SVG file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateSvgPreview(File file) throws IOException {
        logger.debug("Generating SVG preview for file: {}", file.getAbsolutePath());
        
        // Read SVG content
        String svgContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        
        // Sanitize SVG to remove potentially malicious content
        String sanitizedSvg = Jsoup.clean(svgContent, Safelist.relaxed().addTags("svg", "path", "circle", "rect", "line", "polyline", "polygon", "g", "defs", "style"));
        
        PreviewContent previewContent = new PreviewContent(
                "image/svg+xml",
                sanitizedSvg,
                true
        );
        
        return Result.success(previewContent);
    }

    private Result<PreviewContent> generateHtmlPreview(File file) throws IOException {
        logger.debug("Generating HTML preview for file: {}", file.getAbsolutePath());
        String html = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        String sanitizedHtml = Jsoup.clean(html, Safelist.relaxed()
                .addTags("pre", "code", "iframe")
                .addAttributes("iframe", "src", "width", "height", "allow", "allowfullscreen")
                .addAttributes("code", "class")
        );
        PreviewContent previewContent = new PreviewContent(
                "text/html",
                sanitizedHtml,
                true
        );
        return Result.success(previewContent);
    }

    private Result<PreviewContent> generateXlsxPreview(File file) throws IOException {
        logger.debug("Generating XLSX preview for file: {}", file.getAbsolutePath());
        try (org.apache.poi.ss.usermodel.Workbook workbook = org.apache.poi.ss.usermodel.WorkbookFactory.create(file)) {
            StringBuilder html = new StringBuilder();
            html.append("<html><body>");
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                org.apache.poi.ss.usermodel.Sheet sheet = workbook.getSheetAt(i);
                html.append("<h3>").append(org.springframework.web.util.HtmlUtils.htmlEscape(sheet.getSheetName())).append("</h3>");
                html.append("<table border='1' style='border-collapse: collapse; margin-bottom: 16px;'>");
                int maxCols = 0;
                for (org.apache.poi.ss.usermodel.Row row : sheet) {
                    maxCols = Math.max(maxCols, row.getLastCellNum());
                }
                for (org.apache.poi.ss.usermodel.Row row : sheet) {
                    html.append("<tr>");
                    for (int c = 0; c < maxCols; c++) {
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(c);
                        String text = "";
                        if (cell != null) {
                            switch (cell.getCellType()) {
                                case STRING:
                                    text = cell.getStringCellValue();
                                    break;
                                case NUMERIC:
                                    if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                                        java.util.Date date = cell.getDateCellValue();
                                        text = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
                                    } else {
                                        // 使用DataFormatter来获取格式化后的值，避免科学计数法
                                        org.apache.poi.ss.usermodel.DataFormatter formatter = new org.apache.poi.ss.usermodel.DataFormatter();
                                        text = formatter.formatCellValue(cell);
                                    }
                                    break;
                                case BOOLEAN:
                                    text = String.valueOf(cell.getBooleanCellValue());
                                    break;
                                case FORMULA:
                                    try {
                                        // 尝试获取计算后的值
                                        org.apache.poi.ss.usermodel.FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
                                        org.apache.poi.ss.usermodel.CellValue cellValue = evaluator.evaluate(cell);
                                        switch (cellValue.getCellType()) {
                                            case BOOLEAN:
                                                text = String.valueOf(cellValue.getBooleanValue());
                                                break;
                                            case NUMERIC:
                                                text = String.valueOf(cellValue.getNumberValue());
                                                break;
                                            case STRING:
                                                text = cellValue.getStringValue();
                                                break;
                                            default:
                                                text = "";
                                        }
                                    } catch (Exception e) {
                                        text = cell.getCellFormula();
                                    }
                                    break;
                                case BLANK:
                                case _NONE:
                                case ERROR:
                                default:
                                    text = "";
                            }
                        }
                        html.append("<td style='padding: 5px;'>")
                                .append(org.springframework.web.util.HtmlUtils.htmlEscape(text))
                                .append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</table>");
            }
            html.append("</body></html>");
            String sanitizedHtml = Jsoup.clean(html.toString(), Safelist.relaxed()
                    .addTags("table", "thead", "tbody", "tr", "td", "th", "h3")
                    .addAttributes("table", "border", "style")
                    .addAttributes("td", "style")
                    .addAttributes("th", "style")
            );
            PreviewContent previewContent = new PreviewContent(
                    "text/html",
                    sanitizedHtml,
                    true
            );
            return Result.success(previewContent);
        } catch (Exception e) {
            logger.error("Failed to generate XLSX preview", e);
            return Result.failure("Failed to generate XLSX preview: " + e.getMessage());
        }
    }
    
    /**
     * Generates preview content for a text file (log or txt).
     * 
     * @param file The text file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    private Result<PreviewContent> generateTextPreview(File file) throws IOException {
        logger.debug("Generating text preview for file: {}", file.getAbsolutePath());
        
        // Read text content
        String textContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        
        // Escape HTML characters and wrap in <pre> tag for proper formatting
        String escapedContent = org.springframework.web.util.HtmlUtils.htmlEscape(textContent);
        String htmlContent = "<html><body><pre style='white-space: pre-wrap; word-wrap: break-word; font-family: monospace; background-color: #f5f5f5; padding: 10px; border-radius: 4px;'>" + 
                           escapedContent + 
                           "</pre></body></html>";
        
        PreviewContent previewContent = new PreviewContent(
                "text/html",
                htmlContent,
                true
        );
        
        return Result.success(previewContent);
    }
}
