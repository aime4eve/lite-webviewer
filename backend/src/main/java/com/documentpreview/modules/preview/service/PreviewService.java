package com.documentpreview.modules.preview.service;

import com.documentpreview.modules.document.domain.PreviewContent;
import com.documentpreview.shared.ddd.Result;

import java.io.File;

/**
 * Service interface for document preview operations.
 * This service is responsible for converting documents of various types into previewable formats.
 */
public interface PreviewService {
    
    /**
     * Generates preview content for a file at the specified path.
     * 
     * @param filePath The path to the file to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    Result<PreviewContent> generatePreview(String filePath);
    
    /**
     * Generates preview content for a File object.
     * 
     * @param file The File object to generate preview for.
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    Result<PreviewContent> generatePreview(File file);
    
    /**
     * Generates preview content for a specific page range of a PDF file.
     * 
     * @param filePath The path to the PDF file.
     * @param startPage The starting page number (1-based).
     * @param endPage The ending page number (inclusive).
     * @return A Result containing the generated PreviewContent if successful, or an error message otherwise.
     */
    Result<PreviewContent> generatePdfPreview(String filePath, int startPage, int endPage);
    
    /**
     * Checks if the specified file type is supported for preview.
     * 
     * @param fileType The file type to check (e.g., "md", "pdf", "docx", "csv", "svg").
     * @return true if the file type is supported, false otherwise.
     */
    boolean isPreviewSupported(String fileType);
    
    /**
     * Gets the maximum file size supported for preview, in bytes.
     * 
     * @return The maximum supported file size in bytes.
     */
    long getMaxSupportedFileSize();
}