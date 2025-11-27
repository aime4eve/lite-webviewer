package com.documentpreview.modules.document.domain;

import com.documentpreview.shared.ddd.AggregateRoot;

import java.util.Objects;

/**
 * Aggregate root representing a document in the domain model.
 * A document is identified by its file path and contains metadata, content, and preview information.
 */
public class Document extends AggregateRoot<String> {
    private final String fileName;
    private final FileType fileType;
    private final String parentDir;
    private FileMetadata fileMetadata;
    private TOONStructure toonStructure;
    private PreviewContent previewContent;

    /**
     * Creates a new Document instance with the given attributes.
     * 
     * @param filePath The full path to the document, serving as the aggregate root ID.
     * @param fileName The name of the document file.
     * @param fileType The type of the document.
     * @param parentDir The parent directory path.
     */
    public Document(String filePath, String fileName, FileType fileType, String parentDir) {
        super(filePath);
        this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
        this.fileType = Objects.requireNonNull(fileType, "File type cannot be null");
        this.parentDir = Objects.requireNonNull(parentDir, "Parent directory cannot be null");
    }

    /**
     * Gets the name of the document file.
     * 
     * @return The file name.
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the type of the document.
     * 
     * @return The file type.
     */
    public FileType getFileType() {
        return fileType;
    }

    /**
     * Gets the parent directory path.
     * 
     * @return The parent directory path.
     */
    public String getParentDir() {
        return parentDir;
    }

    /**
     * Gets the metadata of the document.
     * 
     * @return The file metadata, or null if not set.
     */
    public FileMetadata getFileMetadata() {
        return fileMetadata;
    }

    /**
     * Sets the metadata of the document.
     * 
     * @param fileMetadata The file metadata to set.
     */
    public void setFileMetadata(FileMetadata fileMetadata) {
        this.fileMetadata = Objects.requireNonNull(fileMetadata, "File metadata cannot be null");
    }

    /**
     * Checks if the document has metadata set.
     * 
     * @return true if metadata is set, false otherwise.
     */
    public boolean hasMetadata() {
        return fileMetadata != null;
    }

    /**
     * Gets the TOON structure of the document.
     * 
     * @return The TOON structure, or null if not generated yet.
     */
    public TOONStructure getToonStructure() {
        return toonStructure;
    }

    /**
     * Sets the TOON structure of the document.
     * 
     * @param toonStructure The TOON structure to set.
     */
    public void setToonStructure(TOONStructure toonStructure) {
        this.toonStructure = Objects.requireNonNull(toonStructure, "TOON structure cannot be null");
    }

    /**
     * Checks if the document has a TOON structure generated.
     * 
     * @return true if TOON structure is generated, false otherwise.
     */
    public boolean hasToonStructure() {
        return toonStructure != null;
    }

    /**
     * Gets the preview content of the document.
     * 
     * @return The preview content, or null if not generated yet.
     */
    public PreviewContent getPreviewContent() {
        return previewContent;
    }

    /**
     * Sets the preview content of the document.
     * 
     * @param previewContent The preview content to set.
     */
    public void setPreviewContent(PreviewContent previewContent) {
        this.previewContent = Objects.requireNonNull(previewContent, "Preview content cannot be null");
    }

    /**
     * Checks if the document has preview content generated.
     * 
     * @return true if preview content is generated, false otherwise.
     */
    public boolean hasPreviewContent() {
        return previewContent != null;
    }

    @Override
    public String toString() {
        return String.format("Document{filePath='%s', fileName='%s', fileType=%s, parentDir='%s', hasMetadata=%b, hasToon=%b, hasPreview=%b}",
                getId(), fileName, fileType, parentDir, hasMetadata(), hasToonStructure(), hasPreviewContent());
    }
}