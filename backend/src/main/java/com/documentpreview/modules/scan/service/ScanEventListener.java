package com.documentpreview.modules.scan.service;

import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.domain.ScanFinishedEvent;
import com.documentpreview.modules.scan.repository.IndexRepository;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Event listener for ScanFinishedEvent. This listener saves the scan results to the file system
 * in both TOON and JSON formats.
 */
@Component
public class ScanEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ScanEventListener.class);
    
    private final IndexRepository indexRepository;
    private final FileScanService fileScanService;
    
    public ScanEventListener(IndexRepository indexRepository, FileScanService fileScanService) {
        this.indexRepository = indexRepository;
        this.fileScanService = fileScanService;
    }
    
    /**
     * Handles the ScanFinishedEvent by saving the scan results to the file system.
     * 
     * @param event The ScanFinishedEvent containing scan information.
     */
    @EventListener
    public void handleScanFinishedEvent(ScanFinishedEvent event) {
        logger.info("Handling ScanFinishedEvent: {}", event);
        
        try {
            // Get the latest index from the scan service
            Result<FilesIndex> indexResult = fileScanService.getLastIndex();
            
            if (indexResult.isFailure()) {
                logger.error("Failed to get last index: {}", indexResult.getErrorMessage().orElse("Unknown error"));
                return;
            }
            
            FilesIndex index = indexResult.getValue().get();
            
            // Save index in JSON format
            Result<FilesIndex> jsonResult = indexRepository.saveIndexAsJSON(index);
            if (jsonResult.isFailure()) {
                logger.error("Failed to save index as JSON: {}", jsonResult.getErrorMessage().orElse("Unknown error"));
            } else {
                logger.info("Successfully saved index as JSON: {}", index);
            }
            
            // Save index in TOON format
            Result<?> toonResult = indexRepository.saveIndexAsTOON(index);
            if (toonResult.isFailure()) {
                logger.error("Failed to save index as TOON: {}", toonResult.getErrorMessage().orElse("Unknown error"));
            } else {
                logger.info("Successfully saved index as TOON: {}", index);
            }
            
        } catch (Exception e) {
            logger.error("Error handling ScanFinishedEvent: {}", e.getMessage(), e);
        }
    }
}
