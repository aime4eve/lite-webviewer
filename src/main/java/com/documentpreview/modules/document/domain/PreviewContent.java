package com.documentpreview.modules.document.domain;

import com.documentpreview.shared.ddd.ValueObject;

import java.util.Objects;

/**
 * Value object representing the preview content of a document.
 * This can be HTML, PDF binary data, SVG text, or other formats depending on the document type.
 */
public class PreviewContent extends ValueObject {
    private final String contentType;
    private final Object content;
    private final boolean supportFullPreview;

    /**
     * Creates a new PreviewContent instance with the given attributes.
     * 
     * @param contentType The MIME type of the content (e.g., text/html, application/pdf, image/svg+xml).
     * @param content The actual content, which can be a String (HTML/SVG), byte array (PDF), or other format.
     * @param supportFullPreview Whether the content supports full preview (true) or only partial (false).
     */
    public PreviewContent(String contentType, Object content, boolean supportFullPreview) {
        this.contentType = Objects.requireNonNull(contentType, "Content type cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.supportFullPreview = supportFullPreview;
    }

    /**
     * Gets the MIME type of the content.
     * 
     * @return The content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Gets the actual content.
     * 
     * @return The content object.
     */
    public Object getContent() {
        return content;
    }

    /**
     * Checks if the content supports full preview.
     * 
     * @return true if full preview is supported, false otherwise.
     */
    public boolean isSupportFullPreview() {
        return supportFullPreview;
    }

    /**
     * Checks if the content is HTML.
     * 
     * @return true if the content type is HTML, false otherwise.
     */
    public boolean isHtml() {
        return contentType.equalsIgnoreCase("text/html") || contentType.equalsIgnoreCase("text/xhtml+xml");
    }

    /**
     * Checks if the content is PDF.
     * 
     * @return true if the content type is PDF, false otherwise.
     */
    public boolean isPdf() {
        return contentType.equalsIgnoreCase("application/pdf");
    }

    /**
     * Checks if the content is SVG.
     * 
     * @return true if the content type is SVG, false otherwise.
     */
    public boolean isSvg() {
        return contentType.equalsIgnoreCase("image/svg+xml");
    }

    /**
     * Checks if the content is text-based.
     * 
     * @return true if the content type is text-based, false otherwise.
     */
    public boolean isTextBased() {
        return contentType.startsWith("text/") || isSvg();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PreviewContent that = (PreviewContent) o;
        return supportFullPreview == that.supportFullPreview &&
                Objects.equals(contentType, that.contentType) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentType, content, supportFullPreview);
    }

    @Override
    public String toString() {
        return String.format("PreviewContent{contentType='%s', contentSize=%d, supportFullPreview=%b}",
                contentType, getContentSize(), supportFullPreview);
    }

    /**
     * Helper method to get the size of the content.
     * For strings, returns the length; for byte arrays, returns the length; otherwise returns -1.
     * 
     * @return The size of the content, or -1 if unknown.
     */
    private int getContentSize() {
        if (content instanceof String) {
            return ((String) content).length();
        } else if (content instanceof byte[]) {
            return ((byte[]) content).length;
        } else {
            return -1;
        }
    }
}