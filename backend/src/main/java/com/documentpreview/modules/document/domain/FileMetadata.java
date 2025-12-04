package com.documentpreview.modules.document.domain;

import com.documentpreview.shared.ddd.ValueObject;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing metadata about a file.
 */
public class FileMetadata extends ValueObject {
    private final long fileSize;
    private final Instant lastModifiedTime;
    private final String charset;

    /**
     * Creates a new FileMetadata instance with the given attributes.
     * 
     * @param fileSize The size of the file in bytes.
     * @param lastModifiedTime The last modified time of the file.
     * @param charset The charset of the file. If null, UTF-8 is used.
     */
    public FileMetadata(long fileSize, Instant lastModifiedTime, String charset) {
        this.fileSize = fileSize;
        this.lastModifiedTime = Objects.requireNonNull(lastModifiedTime, "Last modified time cannot be null");
        this.charset = charset != null ? charset : StandardCharsets.UTF_8.name();
    }

    /**
     * Creates a new FileMetadata instance with UTF-8 charset.
     * 
     * @param fileSize The size of the file in bytes.
     * @param lastModifiedTime The last modified time of the file.
     */
    public FileMetadata(long fileSize, Instant lastModifiedTime) {
        this(fileSize, lastModifiedTime, StandardCharsets.UTF_8.name());
    }

    /**
     * Gets the size of the file in bytes.
     * 
     * @return The file size in bytes.
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * Gets the last modified time of the file.
     * 
     * @return The last modified time.
     */
    public Instant getLastModifiedTime() {
        return lastModifiedTime;
    }

    /**
     * Gets the charset of the file.
     * 
     * @return The charset name.
     */
    public String getCharset() {
        return charset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return fileSize == that.fileSize &&
                Objects.equals(lastModifiedTime, that.lastModifiedTime) &&
                Objects.equals(charset, that.charset);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileSize, lastModifiedTime, charset);
    }

    @Override
    public String toString() {
        return String.format("FileMetadata{fileSize=%d, lastModifiedTime=%s, charset='%s'}", 
                fileSize, lastModifiedTime, charset);
    }
}