package com.documentpreview.modules.document.domain;

import com.documentpreview.shared.ddd.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Value object representing a TOON (Token-Oriented Object Notation) structure.
 * TOON is a structured data format used for representing document content in a machine-readable way.
 */
public class TOONStructure extends ValueObject {
    private final String rootNode;
    private final int elementCount;
    private final List<String> fields;
    private final List<Map<String, Object>> data;
    private final String toonString;

    /**
     * Creates a new TOONStructure instance with the given attributes.
     * 
     * @param rootNode The name of the root node.
     * @param elementCount The number of elements in the data list.
     * @param fields The list of field names.
     * @param data The list of data records, each represented as a map of field names to values.
     * @param toonString The raw TOON string representation.
     */
    public TOONStructure(String rootNode, int elementCount, List<String> fields, List<Map<String, Object>> data, String toonString) {
        this.rootNode = Objects.requireNonNull(rootNode, "Root node cannot be null");
        this.elementCount = elementCount;
        this.fields = Objects.requireNonNull(fields, "Fields list cannot be null");
        this.data = Objects.requireNonNull(data, "Data list cannot be null");
        this.toonString = Objects.requireNonNull(toonString, "TOON string cannot be null");
    }

    /**
     * Creates a new TOONStructure instance with an empty data list.
     * 
     * @param rootNode The name of the root node.
     * @param fields The list of field names.
     * @param toonString The raw TOON string representation.
     */
    public TOONStructure(String rootNode, List<String> fields, String toonString) {
        this(rootNode, 0, fields, new ArrayList<>(), toonString);
    }

    /**
     * Gets the name of the root node.
     * 
     * @return The root node name.
     */
    public String getRootNode() {
        return rootNode;
    }

    /**
     * Gets the number of elements in the data list.
     * 
     * @return The element count.
     */
    public int getElementCount() {
        return elementCount;
    }

    /**
     * Gets the list of field names.
     * 
     * @return An unmodifiable list of field names.
     */
    public List<String> getFields() {
        return List.copyOf(fields);
    }

    /**
     * Gets the list of data records.
     * 
     * @return An unmodifiable list of data records.
     */
    public List<Map<String, Object>> getData() {
        return List.copyOf(data);
    }

    /**
     * Gets the raw TOON string representation.
     * 
     * @return The TOON string.
     */
    public String getToonString() {
        return toonString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TOONStructure that = (TOONStructure) o;
        return elementCount == that.elementCount &&
                Objects.equals(rootNode, that.rootNode) &&
                Objects.equals(fields, that.fields) &&
                Objects.equals(data, that.data) &&
                Objects.equals(toonString, that.toonString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rootNode, elementCount, fields, data, toonString);
    }

    @Override
    public String toString() {
        return String.format("TOONStructure{rootNode='%s', elementCount=%d, fields=%s, dataSize=%d}",
                rootNode, elementCount, fields, data.size());
    }
}