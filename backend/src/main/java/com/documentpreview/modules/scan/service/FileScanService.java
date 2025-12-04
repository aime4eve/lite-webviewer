package com.documentpreview.modules.scan.service;

import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.shared.ddd.Result;

import java.util.List;

/**
 * Service interface for file scanning operations.
 * This service is responsible for scanning directories, detecting changes, and generating file indexes.
 */
public interface FileScanService {
    
    /**
     * Scans the configured root directories and generates a file index.
     * 
     * @return A Result containing the generated FilesIndex if successful, or an error message otherwise.
     */
    Result<FilesIndex> scanRootDirectories();
    
    /**
     * Scans a specific directory and generates a file index.
     * 
     * @param dirPath The directory path to scan.
     * @return A Result containing the generated FilesIndex if successful, or an error message otherwise.
     */
    Result<FilesIndex> scanDirectory(String dirPath);
    
    /**
     * Scans multiple directories and generates a combined file index.
     * 
     * @param dirPaths The list of directory paths to scan.
     * @return A Result containing the generated FilesIndex if successful, or an error message otherwise.
     */
    Result<FilesIndex> scanDirectories(List<String> dirPaths);
    
    /**
     * Forces a full scan of the configured root directories, ignoring any change detection optimizations.
     * 
     * @return A Result containing the generated FilesIndex if successful, or an error message otherwise.
     */
    Result<FilesIndex> forceFullScan();
    
    /**
     * Gets the last generated file index.
     * 
     * @return A Result containing the last generated FilesIndex if available, or an error message otherwise.
     */
    Result<FilesIndex> getLastIndex();
    
    /**
     * Checks if a scan is currently in progress.
     * 
     * @return true if a scan is in progress, false otherwise.
     */
    boolean isScanInProgress();
    
    /**
     * Gets the status of the last scan operation.
     * 
     * @return A string describing the last scan status.
     */
    String getLastScanStatus();
    
    /**
     * Gets the timestamp of the last successful scan.
     * 
     * @return The timestamp of the last successful scan in milliseconds since epoch, or -1 if no scan has been performed.
     */
    long getLastScanTimestamp();
}