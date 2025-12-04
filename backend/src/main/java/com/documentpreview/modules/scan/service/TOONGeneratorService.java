package com.documentpreview.modules.scan.service;

import com.documentpreview.modules.document.domain.TOONStructure;
import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.shared.ddd.Result;

import java.io.File;

/**
 * Service interface for TOON (Token-Oriented Object Notation) generation operations.
 * This service is responsible for converting files and indexes into TOON format.
 */
public interface TOONGeneratorService {
    
    /**
     * Generates a TOON structure from a FilesIndex.
     * 
     * @param index The FilesIndex to convert to TOON format.
     * @return A Result containing the generated TOONStructure if successful, or an error message otherwise.
     */
    Result<TOONStructure> generateFromIndex(FilesIndex index);
    
    /**
     * Generates a TOON structure from a single file.
     * 
     * @param file The file to convert to TOON format.
     * @return A Result containing the generated TOONStructure if successful, or an error message otherwise.
     */
    Result<TOONStructure> generateFromFile(File file);
    
    /**
     * Generates a TOON structure from a file path.
     * 
     * @param filePath The path to the file to convert to TOON format.
     * @return A Result containing the generated TOONStructure if successful, or an error message otherwise.
     */
    Result<TOONStructure> generateFromFilePath(String filePath);
    
    /**
     * Formats a TOONStructure into a standard TOON string.
     * 
     * @param toonStructure The TOONStructure to format.
     * @return A Result containing the formatted TOON string if successful, or an error message otherwise.
     */
    Result<String> formatTOON(TOONStructure toonStructure);
    
    /**
     * Parses a TOON string into a TOONStructure.
     * 
     * @param toonString The TOON string to parse.
     * @return A Result containing the parsed TOONStructure if successful, or an error message otherwise.
     */
    Result<TOONStructure> parseTOON(String toonString);
}