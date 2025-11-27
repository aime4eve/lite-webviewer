package com.documentpreview.web.controller;

import com.documentpreview.modules.document.domain.TOONStructure;
import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.repository.IndexRepository;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for index-related operations.
 * This controller handles requests for index data in TOON and JSON formats.
 */
@RestController
@RequestMapping("/api/v1/index")
public class IndexController {
    
    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
    
    private final IndexRepository indexRepository;
    
    public IndexController(IndexRepository indexRepository) {
        this.indexRepository = indexRepository;
    }
    
    /**
     * Gets the index in TOON format.
     * 
     * @return The index in TOON format.
     */
    @GetMapping("/toon")
    public ResponseEntity<?> getIndexAsTOON() {
        logger.info("GET /api/v1/index/toon");
        
        // Load TOON from repository
        Result<TOONStructure> toonResult = indexRepository.loadTOON();
        
        if (toonResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(toonResult.getErrorMessage().orElse("Failed to load TOON index"));
        }
        
        TOONStructure toonStructure = toonResult.getValue().get();
        
        // Set TOON content type
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("application/x-toon"));
        
        return new ResponseEntity<>(toonStructure.getToonString(), headers, HttpStatus.OK);
    }
    
    /**
     * Gets the index in JSON format.
     * 
     * @return The index in JSON format.
     */
    @GetMapping("/json")
    public ResponseEntity<?> getIndexAsJSON() {
        logger.info("GET /api/v1/index/json");
        
        // Load index from repository
        Result<FilesIndex> indexResult = indexRepository.loadIndex();
        
        if (indexResult.isFailure()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(indexResult.getErrorMessage().orElse("Failed to load JSON index"));
        }
        
        FilesIndex index = indexResult.getValue().get();
        
        return ResponseEntity.ok(index);
    }
}