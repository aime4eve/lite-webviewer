package com.documentpreview.modules.scan.service;

import com.documentpreview.modules.document.domain.TOONStructure;
import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.domain.FilesIndexItem;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TOONGeneratorService interface.
 * This service handles the conversion of files and indexes into TOON format.
 */
@Service
public class TOONGeneratorServiceImpl implements TOONGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(TOONGeneratorServiceImpl.class);
    
    // TOON syntax constants
    private static final String BLOCK_START = "{";
    private static final String BLOCK_END = "}";
    private static final String FIELD_SEPARATOR = ",";
    private static final String RECORD_SEPARATOR = ";";
    private static final String BLOCK_HEADER_SEPARATOR = ":";
    private static final String ESCAPE_CHAR = "\\";
    
    // TOON block names
    private static final String FILES_BLOCK_NAME = "files";
    private static final String METADATA_BLOCK_NAME = "metadata";
    
    // TOON fields for different block types
    private static final List<String> FILES_FIELDS = Arrays.asList("path", "type", "name");
    private static final List<String> METADATA_FIELDS = Arrays.asList("total", "last_updated", "root");
    
    // Date formatter for TOON metadata (Beijing time, ISO-8601)
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX")
            .withZone(ZoneId.of("Asia/Shanghai"));
    
    @Override
    public Result<TOONStructure> generateFromIndex(FilesIndex index) {
        Objects.requireNonNull(index, "FilesIndex cannot be null");
        
        try {
            logger.info("Generating TOON from index: {}", index);
            
            // Generate files block
            String filesBlock = generateFilesBlock(index);
            
            // Generate metadata block
            String metadataBlock = generateMetadataBlock(index);
            
            // Combine blocks into full TOON string
            String toonString = filesBlock + "\n" + metadataBlock;
            
            // Create TOONStructure
            TOONStructure toonStructure = new TOONStructure(
                    "files_index",
                    index.getTotalItems(),
                    Arrays.asList("path", "type", "name"),
                    convertIndexToData(index),
                    toonString
            );
            
            return Result.success(toonStructure);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to generate TOON from index: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public Result<TOONStructure> generateFromFile(File file) {
        Objects.requireNonNull(file, "File cannot be null");
        
        try {
            logger.info("Generating TOON from file: {}", file);
            
            // Read file content
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            
            // Generate TOON based on file type
            TOONStructure toonStructure = generateFromFileContent(file, content);
            
            return Result.success(toonStructure);
            
        } catch (IOException e) {
            String errorMessage = String.format("Failed to read file: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        } catch (Exception e) {
            String errorMessage = String.format("Failed to generate TOON from file: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    @Override
    public Result<TOONStructure> generateFromFilePath(String filePath) {
        Objects.requireNonNull(filePath, "File path cannot be null");
        return generateFromFile(new File(filePath));
    }
    
    @Override
    public Result<String> formatTOON(TOONStructure toonStructure) {
        Objects.requireNonNull(toonStructure, "TOONStructure cannot be null");
        return Result.success(toonStructure.getToonString());
    }
    
    @Override
    public Result<TOONStructure> parseTOON(String toonString) {
        Objects.requireNonNull(toonString, "TOON string cannot be null");
        
        try {
            logger.info("Parsing TOON string");
            
            // Simple implementation for now - will be enhanced later
            // For demo purposes, we'll just create a basic TOONStructure
            TOONStructure toonStructure = new TOONStructure(
                    "parsed",
                    0,
                    Collections.emptyList(),
                    Collections.emptyList(),
                    toonString
            );
            
            return Result.success(toonStructure);
            
        } catch (Exception e) {
            String errorMessage = String.format("Failed to parse TOON string: %s", e.getMessage());
            logger.error(errorMessage, e);
            return Result.failure(errorMessage);
        }
    }
    
    /**
     * Generates the files block for the TOON output.
     * 
     * @param index The FilesIndex containing the file and directory information.
     * @return The generated files block as a string.
     */
    private String generateFilesBlock(FilesIndex index) {
        StringBuilder sb = new StringBuilder();
        
        // Block header: files[count]{path,type,name}:
        sb.append(FILES_BLOCK_NAME)
          .append("[")
          .append(index.getTotalItems())
          .append("]")
          .append(BLOCK_START)
          .append(String.join(FIELD_SEPARATOR, FILES_FIELDS))
          .append(BLOCK_END)
          .append(BLOCK_HEADER_SEPARATOR)
          .append("\n");
        
        // Add records for each item
        for (FilesIndexItem item : index.getItems()) {
            String path = escapeValue(item.getPath());
            String type = escapeValue(item.getType());
            String name = escapeValue(item.getName());
            
            sb.append(path)
              .append(FIELD_SEPARATOR)
              .append(type)
              .append(FIELD_SEPARATOR)
              .append(name)
              .append("\n");
        }
        
        return sb.toString().trim();
    }
    
    /**
     * Generates the metadata block for the TOON output.
     * 
     * @param index The FilesIndex containing the metadata information.
     * @return The generated metadata block as a string.
     */
    private String generateMetadataBlock(FilesIndex index) {
        StringBuilder sb = new StringBuilder();
        
        // Format last updated time in Beijing time, ISO-8601
        String lastUpdated = DATE_FORMATTER.format(Instant.ofEpochMilli(index.getLastUpdated()));
        
        // Block header: metadata{total,last_updated,root}:
        sb.append(METADATA_BLOCK_NAME)
          .append(BLOCK_START)
          .append(String.join(FIELD_SEPARATOR, METADATA_FIELDS))
          .append(BLOCK_END)
          .append(BLOCK_HEADER_SEPARATOR)
          .append(" ");
        
        // Metadata record
        sb.append(index.getTotalItems())
          .append(FIELD_SEPARATOR)
          .append(lastUpdated)
          .append(FIELD_SEPARATOR)
          .append(escapeValue(index.getRootDir()));
        
        return sb.toString();
    }
    
    /**
     * Converts a FilesIndex to a list of maps for the TOONStructure data field.
     * 
     * @param index The FilesIndex to convert.
     * @return A list of maps representing the index data.
     */
    private List<Map<String, Object>> convertIndexToData(FilesIndex index) {
        return index.getItems().stream()
                .map(item -> {
                    Map<String, Object> dataMap = new HashMap<>();
                    dataMap.put("path", item.getPath());
                    dataMap.put("type", item.getType());
                    dataMap.put("name", item.getName());
                    return dataMap;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Generates a TOON structure from a file's content based on its type.
     * 
     * @param file The file to generate TOON from.
     * @param content The content of the file.
     * @return The generated TOONStructure.
     */
    private TOONStructure generateFromFileContent(File file, String content) {
        // For now, we'll create a simple TOON structure for any file type
        // This will be enhanced later to handle different file types appropriately
        
        String fileName = file.getName();
        String rootNode = fileName.substring(0, fileName.lastIndexOf('.'));
        
        // Create a simple content block
        List<String> fields = Collections.singletonList("content");
        List<Map<String, Object>> data = new ArrayList<>();
        
        Map<String, Object> contentMap = new HashMap<>();
        contentMap.put("content", content);
        data.add(contentMap);
        
        // Generate TOON string
        String toonString = String.format("content[1]{content}: %s", escapeValue(content));
        
        return new TOONStructure(rootNode, 1, fields, data, toonString);
    }
    
    /**
     * Escapes special characters in a value according to TOON escape rules.
     * 
     * @param value The value to escape.
     * @return The escaped value.
     */
    private String escapeValue(String value) {
        if (value == null) {
            return "";
        }
        
        return value
                .replace(ESCAPE_CHAR, ESCAPE_CHAR + ESCAPE_CHAR)  // Escape backslashes first
                .replace(",", ESCAPE_CHAR + ",")  // Escape commas
                .replace(";", ESCAPE_CHAR + ";")  // Escape semicolons
                .replace("\n", ESCAPE_CHAR + "n");  // Escape newlines
    }
}