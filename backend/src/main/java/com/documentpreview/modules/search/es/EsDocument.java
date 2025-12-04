package com.documentpreview.modules.search.es;

import com.documentpreview.modules.document.domain.FileType;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;

/**
 * 简单的文档实体类，不再依赖Elasticsearch
 * 当Elasticsearch被禁用时，此类仅作为数据传输对象使用
 */
@Document(indexName = "#{@environment.getProperty('app.search.es-index-name')}")
public class EsDocument {
    @Id
    private String id;
    
    private String filePath;
    private String fileName;
    private String parentDir;
    private FileType fileType;
    private String title;
    private String content;
    private Long size;
    private Long modifiedAt;
    
    @Transient
    private boolean indexed = false;

    public EsDocument() {}

    public EsDocument(String id, String filePath, String fileName, String parentDir, 
                     FileType fileType, String title, String content, 
                     Long size, Long modifiedAt) {
        this.id = id;
        this.filePath = filePath;
        this.fileName = fileName;
        this.parentDir = parentDir;
        this.fileType = fileType;
        this.title = title;
        this.content = content;
        this.size = size;
        this.modifiedAt = modifiedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getParentDir() { return parentDir; }
    public void setParentDir(String parentDir) { this.parentDir = parentDir; }

    public FileType getFileType() { return fileType; }
    public void setFileType(FileType fileType) { this.fileType = fileType; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }

    public Long getModifiedAt() { return modifiedAt; }
    public void setModifiedAt(Long modifiedAt) { this.modifiedAt = modifiedAt; }

    public boolean isIndexed() { return indexed; }
    public void setIndexed(boolean indexed) { this.indexed = indexed; }
}