package com.documentpreview.modules.preview.service;
import com.documentpreview.modules.document.domain.PreviewContent;import com.documentpreview.shared.ddd.Result;import java.io.File;
public interface PreviewService {
    Result<PreviewContent> generatePreview(String filePath);
    Result<PreviewContent> generatePreview(File file);
    Result<PreviewContent> generatePdfPreview(String filePath, int startPage, int endPage);
    boolean isPreviewSupported(String fileType);
    long getMaxSupportedFileSize();
}
