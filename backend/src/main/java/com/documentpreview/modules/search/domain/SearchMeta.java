package com.documentpreview.modules.search.domain;

import com.documentpreview.modules.document.domain.FileType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchMeta {
    private String filePath;
    private String fileName;
    private String parentDir;
    private FileType fileType;
    private String title;
    private String contentText;
    private Long size;
    private Long modifiedAt;

    public SearchMeta() {}

    public SearchMeta(String filePath, String fileName, String parentDir, FileType fileType, String title, String contentText, Long size, Long modifiedAt) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.parentDir = parentDir;
        this.fileType = fileType;
        this.title = title;
        this.contentText = contentText;
        this.size = size;
        this.modifiedAt = modifiedAt;
    }

    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getParentDir() { return parentDir; }
    public FileType getFileType() { return fileType; }
    public String getTitle() { return title; }
    public String getContentText() { return contentText; }
    public Long getSize() { return size; }
    public Long getModifiedAt() { return modifiedAt; }

    public void setFilePath(String v) { this.filePath = v; }
    public void setFileName(String v) { this.fileName = v; }
    public void setParentDir(String v) { this.parentDir = v; }
    public void setFileType(FileType v) { this.fileType = v; }
    public void setTitle(String v) { this.title = v; }
    public void setContentText(String v) { this.contentText = v; }
    public void setSize(Long v) { this.size = v; }
    public void setModifiedAt(Long v) { this.modifiedAt = v; }

    // 添加缺少的方法
    public String getId() {
        return filePath; // 使用文件路径作为ID
    }
    
    public String getContent() {
        return contentText; // 返回内容文本
    }
    
    public Long getLastModified() {
        return modifiedAt; // 返回最后修改时间
    }
}
