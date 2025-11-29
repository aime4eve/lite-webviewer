package com.documentpreview.modules.search.domain;

import java.util.List;

public class SearchRequest {
    private String keyword;
    private List<String> fileTypes;
    private String parentDir;
    private Long minSize;
    private Long maxSize;
    private Long startDate;
    private Long endDate;
    private String sort;
    private String order;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public List<String> getFileTypes() { return fileTypes; }
    public void setFileTypes(List<String> fileTypes) { this.fileTypes = fileTypes; }
    public String getParentDir() { return parentDir; }
    public void setParentDir(String parentDir) { this.parentDir = parentDir; }
    public Long getMinSize() { return minSize; }
    public void setMinSize(Long minSize) { this.minSize = minSize; }
    public Long getMaxSize() { return maxSize; }
    public void setMaxSize(Long maxSize) { this.maxSize = maxSize; }
    public Long getStartDate() { return startDate; }
    public void setStartDate(Long startDate) { this.startDate = startDate; }
    public Long getEndDate() { return endDate; }
    public void setEndDate(Long endDate) { this.endDate = endDate; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public String getOrder() { return order; }
    public void setOrder(String order) { this.order = order; }
}
