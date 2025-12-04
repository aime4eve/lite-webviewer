package com.documentpreview.modules.scan.repository;

import com.documentpreview.modules.document.domain.TOONStructure;
import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.shared.ddd.Result;

/**
 * Repository interface for index persistence operations.
 * This repository is responsible for saving and loading file indexes and TOON structures.
 */
public interface IndexRepository {
    
    /**
     * Saves a FilesIndex to the local file system in TOON format.
     * 
     * @param index The FilesIndex to save.
     * @return A Result containing the saved TOONStructure if successful, or an error message otherwise.
     */
    Result<TOONStructure> saveIndexAsTOON(FilesIndex index);
    
    /**
     * Saves a FilesIndex to the local file system in JSON format.
     * 
     * @param index The FilesIndex to save.
     * @return A Result containing the saved FilesIndex if successful, or an error message otherwise.
     */
    Result<FilesIndex> saveIndexAsJSON(FilesIndex index);
    
    /**
     * Loads a FilesIndex from the local file system.
     * 
     * @return A Result containing the loaded FilesIndex if successful, or an error message otherwise.
     */
    Result<FilesIndex> loadIndex();
    
    /**
     * Loads a TOONStructure from the local file system.
     * 
     * @return A Result containing the loaded TOONStructure if successful, or an error message otherwise.
     */
    Result<TOONStructure> loadTOON();
    
    /**
     * Deletes the existing index files from the local file system.
     * 
     * @return A Result indicating success or failure.
     */
    Result<Void> deleteIndex();
    
    /**
     * Checks if an index file exists in the local file system.
     * 
     * @return true if an index file exists, false otherwise.
     */
    boolean indexExists();
    
    /**
     * Gets the last modified time of the index file in milliseconds since epoch.
     * 
     * @return A Result containing the last modified time if successful, or an error message otherwise.
     */
    Result<Long> getIndexLastModifiedTime();
}