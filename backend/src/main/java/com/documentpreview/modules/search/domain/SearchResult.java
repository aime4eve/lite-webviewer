package com.documentpreview.modules.search.domain;

import com.documentpreview.modules.document.domain.FileType;

public class SearchResult {
    private final String filePath;
    private final String fileName;
    private final String parentDir;
    private final FileType fileType;
    private final int score;
    private final String snippet;

    public SearchResult(String filePath, String fileName, String parentDir, FileType fileType, int score, String snippet) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.parentDir = parentDir;
        this.fileType = fileType;
        this.score = score;
        this.snippet = snippet;
    }

    public String getFilePath() { return filePath; }
    public String getFileName() { return fileName; }
    public String getParentDir() { return parentDir; }
    public FileType getFileType() { return fileType; }
    public int getScore() { return score; }
    public String getSnippet() { return snippet; }
}
