package com.documentpreview.modules.document.domain;

import com.documentpreview.shared.ddd.ValueObject;

import java.util.Objects;

/**
 * Value object representing a summary of a document, used for displaying in directory trees.
 */
public class DocumentSummary extends ValueObject {
    private final String filePath;
    private final String fileName;
    private final FileType fileType;
    private final long fileSize;

    /**
     * Creates a new DocumentSummary instance with the given attributes.
     * 
     * @param filePath The full path to the document.
     * @param fileName The name of the document file.
     * @param fileType The type of the document.
     * @param fileSize The size of the document in bytes.
     */
    public DocumentSummary(String filePath, String fileName, FileType fileType, long fileSize) {
        this.filePath = Objects.requireNonNull(filePath, "File path cannot be null");
        this.fileName = Objects.requireNonNull(fileName, "File name cannot be null");
        this.fileType = Objects.requireNonNull(fileType, "File type cannot be null");
        this.fileSize = fileSize;
    }

    /**
     * Gets the full path to the document.
     * 
     * @return The file path.
     */
    public String getFilePath() {
        return filePath;
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
     * Gets the size of the document in bytes.
     * 
     * @return The file size in bytes.
     */
    public long getFileSize() {
        return fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentSummary that = (DocumentSummary) o;
        return fileSize == that.fileSize &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(fileName, that.fileName) &&
                fileType == that.fileType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, fileName, fileType, fileSize);
    }

    @Override
    public String toString() {
        return String.format("DocumentSummary{filePath='%s', fileName='%s', fileType=%s, fileSize=%d}",
                filePath, fileName, fileType, fileSize);
    }
}