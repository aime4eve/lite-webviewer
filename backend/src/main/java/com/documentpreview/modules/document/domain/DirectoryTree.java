package com.documentpreview.modules.document.domain;

import com.documentpreview.shared.ddd.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Value object representing a directory tree structure, used for displaying in the UI.
 * This is a recursive structure that can contain subdirectories and documents.
 */
public class DirectoryTree extends ValueObject {
    private final String dirPath;
    private final String dirName;
    private final List<DirectoryTree> subDirs;
    private final List<DocumentSummary> documents;

    /**
     * Creates a new DirectoryTree instance with the given attributes.
     * 
     * @param dirPath The full path to the directory.
     * @param dirName The name of the directory.
     * @param subDirs The list of subdirectories.
     * @param documents The list of documents in this directory.
     */
    public DirectoryTree(String dirPath, String dirName, List<DirectoryTree> subDirs, List<DocumentSummary> documents) {
        this.dirPath = Objects.requireNonNull(dirPath, "Directory path cannot be null");
        this.dirName = Objects.requireNonNull(dirName, "Directory name cannot be null");
        this.subDirs = Objects.requireNonNull(subDirs, "Subdirectories list cannot be null");
        this.documents = Objects.requireNonNull(documents, "Documents list cannot be null");
    }

    /**
     * Creates a new empty DirectoryTree instance with the given path and name.
     * 
     * @param dirPath The full path to the directory.
     * @param dirName The name of the directory.
     */
    public DirectoryTree(String dirPath, String dirName) {
        this(dirPath, dirName, new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Gets the full path to the directory.
     * 
     * @return The directory path.
     */
    public String getDirPath() {
        return dirPath;
    }

    /**
     * Gets the name of the directory.
     * 
     * @return The directory name.
     */
    public String getDirName() {
        return dirName;
    }

    /**
     * Gets the list of subdirectories.
     * 
     * @return An unmodifiable list of subdirectories.
     */
    public List<DirectoryTree> getSubDirs() {
        return List.copyOf(subDirs);
    }

    /**
     * Gets the list of documents in this directory.
     * 
     * @return An unmodifiable list of documents.
     */
    public List<DocumentSummary> getDocuments() {
        return List.copyOf(documents);
    }

    /**
     * Gets the total number of items in this directory tree, including all subdirectories and documents.
     * 
     * @return The total number of items.
     */
    public int getTotalItems() {
        int total = subDirs.size() + documents.size();
        for (DirectoryTree subDir : subDirs) {
            total += subDir.getTotalItems();
        }
        return total;
    }

    /**
     * Checks if this directory tree is empty (no subdirectories and no documents).
     * 
     * @return true if the directory tree is empty, false otherwise.
     */
    public boolean isEmpty() {
        return subDirs.isEmpty() && documents.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DirectoryTree that = (DirectoryTree) o;
        return Objects.equals(dirPath, that.dirPath) &&
                Objects.equals(dirName, that.dirName) &&
                Objects.equals(subDirs, that.subDirs) &&
                Objects.equals(documents, that.documents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dirPath, dirName, subDirs, documents);
    }

    @Override
    public String toString() {
        return String.format("DirectoryTree{dirPath='%s', dirName='%s', subDirsSize=%d, documentsSize=%d}",
                dirPath, dirName, subDirs.size(), documents.size());
    }
}