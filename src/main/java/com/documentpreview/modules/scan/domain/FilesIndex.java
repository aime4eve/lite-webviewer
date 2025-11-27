package com.documentpreview.modules.scan.domain;

import com.documentpreview.shared.ddd.ValueObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Value object representing the entire files index, containing all scanned files and directories.
 */
public class FilesIndex extends ValueObject {
    private final List<FilesIndexItem> items;
    private final String rootDir;
    private final long lastUpdated;

    /**
     * Creates a new FilesIndex instance with the given attributes.
     * 
     * @param items The list of files and directories in the index.
     * @param rootDir The root directory that was scanned.
     * @param lastUpdated The timestamp when the index was last updated (in milliseconds since epoch).
     */
    @JsonCreator
    public FilesIndex(@JsonProperty("items") List<FilesIndexItem> items, 
                     @JsonProperty("rootDir") String rootDir, 
                     @JsonProperty("lastUpdated") long lastUpdated) {
        this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items, "Items list cannot be null")));
        this.rootDir = Objects.requireNonNull(rootDir, "Root directory cannot be null");
        this.lastUpdated = lastUpdated;
    }

    /**
     * Creates a new FilesIndex instance with the current timestamp.
     * 
     * @param items The list of files and directories in the index.
     * @param rootDir The root directory that was scanned.
     * @return A new FilesIndex instance.
     */
    public static FilesIndex create(List<FilesIndexItem> items, String rootDir) {
        return new FilesIndex(items, rootDir, System.currentTimeMillis());
    }

    /**
     * Gets the list of files and directories in the index.
     * 
     * @return An unmodifiable list of index items.
     */
    public List<FilesIndexItem> getItems() {
        return items;
    }

    /**
     * Gets the root directory that was scanned.
     * 
     * @return The root directory path.
     */
    public String getRootDir() {
        return rootDir;
    }

    /**
     * Gets the timestamp when the index was last updated (in milliseconds since epoch).
     * 
     * @return The last updated timestamp.
     */
    public long getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Gets the number of items in the index.
     * 
     * @return The total number of items.
     */
    @JsonIgnore
    public int getTotalItems() {
        return items.size();
    }

    /**
     * Gets the number of files in the index.
     * 
     * @return The file count.
     */
    @JsonIgnore
    public int getFileCount() {
        return (int) items.stream().filter(FilesIndexItem::isFile).count();
    }

    /**
     * Gets the number of directories in the index.
     * 
     * @return The directory count.
     */
    @JsonIgnore
    public int getDirectoryCount() {
        return (int) items.stream().filter(FilesIndexItem::isDirectory).count();
    }

    /**
     * Gets all directories in the index.
     * 
     * @return A list of directory index items.
     */
    @JsonIgnore
    public List<FilesIndexItem> getDirectories() {
        return items.stream()
                .filter(FilesIndexItem::isDirectory)
                .collect(Collectors.toList());
    }

    /**
     * Gets all files in the index.
     * 
     * @return A list of file index items.
     */
    @JsonIgnore
    public List<FilesIndexItem> getFiles() {
        return items.stream()
                .filter(FilesIndexItem::isFile)
                .collect(Collectors.toList());
    }

    /**
     * Gets all items in the specified directory path.
     * 
     * @param dirPath The directory path to filter by.
     * @return A list of index items in the specified directory.
     */
    @JsonIgnore
    public List<FilesIndexItem> getItemsByPath(String dirPath) {
        Objects.requireNonNull(dirPath, "Directory path cannot be null");
        
        // Normalize the path to ensure consistent matching
        String normalizedPath = dirPath.endsWith("/") ? dirPath : dirPath + "/";
        
        return items.stream()
                .filter(item -> {
                    if (item.isDirectory()) {
                        // For directories, exact match or direct child
                        return item.getPath().equals(dirPath) || item.getPath().equals(normalizedPath);
                    } else {
                        // For files, check if parent directory matches
                        String parentPath = item.getPath().substring(0, item.getPath().lastIndexOf('/') + 1);
                        return parentPath.equals(normalizedPath);
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Checks if the index contains the specified item.
     * 
     * @param item The item to check for.
     * @return true if the index contains the item, false otherwise.
     */
    @JsonIgnore
    public boolean contains(FilesIndexItem item) {
        return items.contains(item);
    }

    /**
     * Checks if the index is empty.
     * 
     * @return true if the index has no items, false otherwise.
     */
    @JsonIgnore
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilesIndex that = (FilesIndex) o;
        return lastUpdated == that.lastUpdated &&
                Objects.equals(items, that.items) &&
                Objects.equals(rootDir, that.rootDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, rootDir, lastUpdated);
    }

    @Override
    public String toString() {
        return String.format("FilesIndex{rootDir='%s', totalItems=%d, files=%d, directories=%d, lastUpdated=%d}",
                rootDir, getTotalItems(), getFileCount(), getDirectoryCount(), lastUpdated);
    }
}