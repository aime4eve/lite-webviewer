package com.documentpreview.modules.scan.repository;

import com.documentpreview.modules.document.domain.TOONStructure;
import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.domain.FilesIndexItem;
import com.documentpreview.modules.scan.service.TOONGeneratorService;
import com.documentpreview.shared.ddd.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Implementation of the IndexRepository interface.
 * This repository handles the persistence of file indexes and TOON structures to the local file system.
 */
@Repository
public class IndexRepositoryImpl implements IndexRepository {
    
    private static final Logger logger = LoggerFactory.getLogger(IndexRepositoryImpl.class);
    
    // Index file names
    private static final String INDEX_TOON_FILE = "index.toon";
    private static final String INDEX_JSON_FILE = "index.json";
    
    @Value("${app.data.dir}")
    private String dataDir;
    
    private final TOONGeneratorService toonGeneratorService;
    private final ObjectMapper objectMapper;
    
    public IndexRepositoryImpl(TOONGeneratorService toonGeneratorService) {
        this.toonGeneratorService = toonGeneratorService;
        
        // Initialize ObjectMapper with Java 8 time support
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @Override
    public Result<TOONStructure> saveIndexAsTOON(FilesIndex index) {
        Objects.requireNonNull(index, "FilesIndex cannot be null");
        
        try {
            logger.info("Saving index as TOON: {}", index);
            
            // Generate TOON from index
            Result<TOONStructure> toonResult = toonGeneratorService.generateFromIndex(index);
            if (toonResult.isFailure()) {
                return toonResult;
            }
            
            TOONStructure toonStructure = toonResult.getValue().get();
            
            // Ensure data directory exists
            ensureDataDirectoryExists();
            
            // Write TOON to file
            Path toonPath = Paths.get(dataDir, INDEX_TOON_FILE);
            Files.writeString(toonPath, toonStructure.getToonString(), StandardCharsets.UTF_8);
            
            logger.info("Successfully saved index as TOON to: {}", toonPath);
            return Result.success(toonStructure);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to save index as TOON: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public Result<FilesIndex> saveIndexAsJSON(FilesIndex index) {
        Objects.requireNonNull(index, "FilesIndex cannot be null");
        
        try {
            logger.info("Saving index as JSON: {}", index);
            
            // Ensure data directory exists
            ensureDataDirectoryExists();
            
            // Write JSON to file
            Path jsonPath = Paths.get(dataDir, INDEX_JSON_FILE);
            objectMapper.writeValue(jsonPath.toFile(), index);
            
            logger.info("Successfully saved index as JSON to: {}", jsonPath);
            return Result.success(index);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to save index as JSON: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public Result<FilesIndex> loadIndex() {
        try {
            logger.info("Loading index from file system");
            
            // Check if JSON index exists (preferred format)
            Path jsonPath = Paths.get(dataDir, INDEX_JSON_FILE);
            if (Files.exists(jsonPath)) {
                logger.info("Loading index from JSON file: {}", jsonPath);
                FilesIndex index = objectMapper.readValue(jsonPath.toFile(), FilesIndex.class);
                return Result.success(index);
            }
            
            // Fallback to TOON format if JSON doesn't exist
            Path toonPath = Paths.get(dataDir, INDEX_TOON_FILE);
            if (Files.exists(toonPath)) {
                logger.info("Loading index from TOON file: {}", toonPath);
                
                // Load TOON string
                String toonString = Files.readString(toonPath, StandardCharsets.UTF_8);
                
                // Parse TOON string (this is a simplified implementation)
                // In a real implementation, we would properly parse the TOON string into a FilesIndex
                FilesIndex index = parseToonToIndex(toonString);
                return Result.success(index);
            }
            
            // No index files found
            return Result.failure("No index files found");
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to load index: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public Result<TOONStructure> loadTOON() {
        try {
            logger.info("Loading TOON structure from file system");
            
            Path toonPath = Paths.get(dataDir, INDEX_TOON_FILE);
            if (!Files.exists(toonPath)) {
                return Result.failure("TOON index file not found");
            }
            
            // Load TOON string
            String toonString = Files.readString(toonPath, StandardCharsets.UTF_8);
            
            // Create TOONStructure from string
            TOONStructure toonStructure = new TOONStructure(
                    "files_index",
                    0, // We don't know the count from just the string, but it's not critical here
                    List.of("path", "type", "name"),
                    new ArrayList<>(),
                    toonString
            );
            
            logger.info("Successfully loaded TOON from: {}", toonPath);
            return Result.success(toonStructure);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to load TOON: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public Result<Void> deleteIndex() {
        try {
            logger.info("Deleting index files from: {}", dataDir);
            
            // Delete TOON file if it exists
            Path toonPath = Paths.get(dataDir, INDEX_TOON_FILE);
            if (Files.exists(toonPath)) {
                Files.delete(toonPath);
                logger.info("Deleted TOON index file: {}", toonPath);
            }
            
            // Delete JSON file if it exists
            Path jsonPath = Paths.get(dataDir, INDEX_JSON_FILE);
            if (Files.exists(jsonPath)) {
                Files.delete(jsonPath);
                logger.info("Deleted JSON index file: {}", jsonPath);
            }
            
            return Result.success(null);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to delete index files: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public boolean indexExists() {
        Path toonPath = Paths.get(dataDir, INDEX_TOON_FILE);
        Path jsonPath = Paths.get(dataDir, INDEX_JSON_FILE);
        
        return Files.exists(toonPath) || Files.exists(jsonPath);
    }
    
    @Override
    public Result<Long> getIndexLastModifiedTime() {
        try {
            // Check JSON file first (preferred format)
            Path jsonPath = Paths.get(dataDir, INDEX_JSON_FILE);
            if (Files.exists(jsonPath)) {
                BasicFileAttributes attrs = Files.readAttributes(jsonPath, BasicFileAttributes.class);
                return Result.success(attrs.lastModifiedTime().toMillis());
            }
            
            // Fallback to TOON file
            Path toonPath = Paths.get(dataDir, INDEX_TOON_FILE);
            if (Files.exists(toonPath)) {
                BasicFileAttributes attrs = Files.readAttributes(toonPath, BasicFileAttributes.class);
                return Result.success(attrs.lastModifiedTime().toMillis());
            }
            
            return Result.failure("No index files found");
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to get index last modified time: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    /**
     * Ensures that the data directory exists, creating it if necessary.
     * 
     * @throws IOException If an error occurs while creating the directory.
     */
    private void ensureDataDirectoryExists() throws IOException {
        Path dataPath = Paths.get(dataDir);
        if (!Files.exists(dataPath)) {
            Files.createDirectories(dataPath);
            logger.info("Created data directory: {}", dataPath);
        }
    }
    
    /**
     * Parses a TOON string into a FilesIndex object.
     * This is a simplified implementation for demo purposes.
     * 
     * @param toonString The TOON string to parse.
     * @return The parsed FilesIndex object.
     */
    private FilesIndex parseToonToIndex(String toonString) {
        // This is a simplified implementation
        // In a real implementation, we would properly parse the TOON string
        // For now, we'll return an empty index with just the root directory
        List<FilesIndexItem> items = new ArrayList<>();
        items.add(FilesIndexItem.directory("/", "/"));
        
        return new FilesIndex(items, "/", Instant.now().toEpochMilli());
    }
}