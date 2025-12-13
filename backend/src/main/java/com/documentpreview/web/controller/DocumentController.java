package com.documentpreview.web.controller;

import com.documentpreview.modules.document.domain.DirectoryTree;
import com.documentpreview.modules.document.domain.PreviewContent;
import com.documentpreview.modules.document.domain.TOONStructure;
import com.documentpreview.modules.preview.service.PreviewService;
import com.documentpreview.modules.scan.service.FileScanService;
import com.documentpreview.modules.scan.service.TOONGeneratorService;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * REST API controller for document-related operations.
 * This controller handles requests for document tree, TOON structure, and preview content.
 */
@RestController
@RequestMapping("/api/v1/document")
public class DocumentController {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentController.class);
    
    private final FileScanService fileScanService;
    private final TOONGeneratorService toonGeneratorService;
    private final PreviewService previewService;
    
    public DocumentController(FileScanService fileScanService, 
                             TOONGeneratorService toonGeneratorService, 
                             PreviewService previewService) {
        this.fileScanService = fileScanService;
        this.toonGeneratorService = toonGeneratorService;
        this.previewService = previewService;
    }
    
    /**
     * Gets the directory tree structure.
     * 
     * @param rootDir The root directory to start from (optional, defaults to configured root).
     * @param path The specific path to get the tree for (optional).
     * @param depth The depth of the tree to return (optional, defaults to 2).
     * @return The directory tree structure.
     */
    @GetMapping("/tree")
    public ResponseEntity<?> getDirectoryTree(@RequestParam(required = false) String rootDir, 
                                            @RequestParam(required = false) String path, 
                                            @RequestParam(defaultValue = "2") int depth) {
        logger.info("GET /api/v1/document/tree - rootDir: {}, path: {}, depth: {}", rootDir, path, depth);
        
        // For now, we'll return a simple response indicating this endpoint is not yet fully implemented
        // In a real implementation, we would generate and return a DirectoryTree object
        return ResponseEntity.ok("Directory tree endpoint - not fully implemented yet");
    }
    
    /**
     * Gets the TOON structure for a specific document.
     * 
     * @param filePath The path to the document.
     * @param format The format to return (both, toon, or json, defaults to both).
     * @return The TOON structure for the document.
     */
    @GetMapping("/toon")
    public ResponseEntity<?> getDocumentTOON(@RequestParam String filePath, 
                                           @RequestParam(defaultValue = "both") String format) {
        logger.info("GET /api/v1/document/toon - filePath: {}, format: {}", filePath, format);
        
        // Generate TOON from file
        Result<TOONStructure> toonResult = toonGeneratorService.generateFromFilePath(filePath);
        
        if (toonResult.isFailure()) {
            String errorMessage = toonResult.getErrorMessage().orElse("Failed to generate TOON");
            logger.error("Failed to generate TOON for {}: {}", filePath, errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }
        
        TOONStructure toonStructure = toonResult.getValue().get();
        
        // Return based on format
        switch (format.toLowerCase()) {
            case "toon":
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/x-toon"));
                return new ResponseEntity<>(toonStructure.getToonString(), headers, HttpStatus.OK);
            case "json":
                return ResponseEntity.ok(toonStructure);
            case "both":
            default:
                return ResponseEntity.ok(toonStructure);
        }
    }
    
    /**
     * Gets the preview content for a specific document.
     * 
     * @param filePath The path to the document.
     * @param startPage The starting page for PDF preview (optional).
     * @param endPage The ending page for PDF preview (optional).
     * @return The preview content for the document.
     */
    @GetMapping("/preview")
    public ResponseEntity<?> getDocumentPreview(@RequestParam String filePath, 
                                              @RequestParam(required = false) Integer startPage, 
                                              @RequestParam(required = false) Integer endPage) {
        logger.info("GET /api/v1/document/preview - filePath: {}, startPage: {}, endPage: {}", 
                filePath, startPage, endPage);
        
        Result<PreviewContent> previewResult;
        
        // Check if it's a PDF with page range specified
        if (filePath.toLowerCase().endsWith(".pdf") && startPage != null && endPage != null) {
            previewResult = previewService.generatePdfPreview(filePath, startPage, endPage);
        } else {
            previewResult = previewService.generatePreview(filePath);
        }
        
        if (previewResult.isFailure()) {
            String errorMessage = previewResult.getErrorMessage().orElse("Failed to generate preview");
            logger.error("Failed to generate preview for {}: {}", filePath, errorMessage);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
        }

        PreviewContent previewContent = previewResult.getValue().get();

        HttpHeaders headers = new HttpHeaders();
        // 对于PDF，我们需要返回正确的Content-Type，并且不使用Content-Disposition: inline
        // 这样浏览器会尝试在iframe中直接显示PDF
        headers.setContentType(org.springframework.http.MediaType.parseMediaType(previewContent.getContentType()));
        
        if (previewContent.isPdf()) {
            // PDF特殊处理：不设置Content-Disposition，或者设置为inline但不带filename
            // 这有助于解决某些浏览器/iframe组合下的加载问题
            headers.set("Content-Disposition", "inline");
        }

        Object body = previewContent.getContent();
        if (previewContent.isPdf() && body instanceof byte[]) {
            return new ResponseEntity<>((byte[]) body, headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(body, headers, HttpStatus.OK);
    }
    
    /**
     * Triggers a forced full scan of the file system.
     * 
     * @return A response indicating the scan status.
     */
    @PostMapping("/force-scan")
    public ResponseEntity<?> triggerForceScan() {
        logger.info("POST /api/v1/document/force-scan - executing forced full scan");
        
        if (fileScanService.isScanInProgress()) {
            logger.info("Scan skipped: another scan is already in progress");
            return ResponseEntity.ok("Scan already in progress");
        }
        
        Result<?> scanResult;
        String scanType = "forced full scan";
        
        try {
            logger.info("Starting {}...", scanType);
            scanResult = fileScanService.forceFullScan();
            
            if (scanResult.isFailure()) {
                String errorMessage = scanResult.getErrorMessage().orElse("Scan failed");
                logger.error("{} failed: {}", scanType, errorMessage);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(errorMessage);
            }
            
            logger.info("{} completed successfully", scanType);
            return ResponseEntity.ok("Scan completed successfully");
        } catch (Exception e) {
            logger.error("Unexpected error during {}: {}", scanType, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error during scan: " + e.getMessage());
        }
    }
}
