package com.documentpreview.modules.scan.domain;

import com.documentpreview.shared.ddd.ValueObject;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Value object representing a single item in the files index.
 * This can be either a file or a directory.
 */
public class FilesIndexItem extends ValueObject {
    private final String path;
    private final String type;
    private final String name;

    /**
     * Creates a new FilesIndexItem instance with the given attributes.
     * 
     * @param path The path of the item relative to the root directory.
     * @param type The type of the item, either "file" or "directory".
     * @param name The name of the item (file or directory name).
     */
    @JsonCreator
    public FilesIndexItem(@JsonProperty("path") String path, 
                         @JsonProperty("type") String type, 
                         @JsonProperty("name") String name) {
        this.path = Objects.requireNonNull(path, "Path cannot be null");
        this.type = Objects.requireNonNull(type, "Type cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        
        if (!type.equals("file") && !type.equals("directory")) {
            throw new IllegalArgumentException("Type must be either 'file' or 'directory'");
        }
    }

    /**
     * Creates a new directory index item.
     * 
     * @param path The path of the directory relative to the root directory.
     * @param name The name of the directory.
     * @return A new FilesIndexItem representing a directory.
     */
    public static FilesIndexItem directory(String path, String name) {
        return new FilesIndexItem(path, "directory", name);
    }

    /**
     * Creates a new file index item.
     * 
     * @param path The path of the file relative to the root directory.
     * @param name The name of the file.
     * @return A new FilesIndexItem representing a file.
     */
    public static FilesIndexItem file(String path, String name) {
        return new FilesIndexItem(path, "file", name);
    }

    /**
     * Gets the path of the item relative to the root directory.
     * 
     * @return The item path.
     */
    public String getPath() {
        return path;
    }

    /**
     * Gets the type of the item, either "file" or "directory".
     * 
     * @return The item type.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the name of the item (file or directory name).
     * 
     * @return The item name.
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this item is a directory.
     * 
     * @return true if this item is a directory, false otherwise.
     */
    @JsonIgnore
    public boolean isDirectory() {
        return "directory".equals(type);
    }

    /**
     * Checks if this item is a file.
     * 
     * @return true if this item is a file, false otherwise.
     */
    @JsonIgnore
    public boolean isFile() {
        return "file".equals(type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilesIndexItem that = (FilesIndexItem) o;
        return Objects.equals(path, that.path) &&
                Objects.equals(type, that.type) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, type, name);
    }

    @Override
    public String toString() {
        return String.format("FilesIndexItem{path='%s', type='%s', name='%s'}",
                path, type, name);
    }
}