package com.documentpreview.modules.document.domain;

/**
 * Enum representing the supported file types in the document preview system.
 */
public enum FileType {
    MD("md"),
    DOCX("docx"),
    PDF("pdf"),
    CSV("csv"),
    SVG("svg"),
    HTML("html"),
    HTM("htm"),
    XLSX("xlsx");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    /**
     * Gets the file extension associated with this file type.
     * 
     * @return The file extension (without the dot).
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Converts a file extension to a FileType enum value.
     * 
     * @param extension The file extension (with or without the dot).
     * @return The corresponding FileType enum value, or null if the extension is not supported.
     */
    public static FileType fromExtension(String extension) {
        if (extension == null) {
            return null;
        }
        
        String normalizedExtension = extension.toLowerCase().replace(".", "");
        for (FileType type : FileType.values()) {
            if (type.extension.equals(normalizedExtension)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Converts a file name to a FileType enum value by extracting its extension.
     * 
     * @param fileName The file name.
     * @return The corresponding FileType enum value, or null if the extension is not supported.
     */
    public static FileType fromFileName(String fileName) {
        if (fileName == null) {
            return null;
        }
        
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return null;
        }
        
        String extension = fileName.substring(dotIndex + 1);
        return fromExtension(extension);
    }
}
