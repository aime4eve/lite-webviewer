package com.documentpreview.modules.search.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 搜索服务状态信息
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchStatus {
    private final boolean esEnabled;
    private final boolean esConnected;
    private final boolean indexExists;
    private final long totalDocuments;
    private final long totalSearches;
    private final long avgResponseTime;
    private final String searchMode;

    public SearchStatus(boolean esEnabled, boolean esConnected, boolean indexExists,
                       long totalDocuments, long totalSearches, long avgResponseTime,
                       String searchMode) {
        this.esEnabled = esEnabled;
        this.esConnected = esConnected;
        this.indexExists = indexExists;
        this.totalDocuments = totalDocuments;
        this.totalSearches = totalSearches;
        this.avgResponseTime = avgResponseTime;
        this.searchMode = searchMode;
    }

    public boolean isEsEnabled() {
        return esEnabled;
    }

    public boolean isEsConnected() {
        return esConnected;
    }

    public boolean isIndexExists() {
        return indexExists;
    }

    public long getTotalDocuments() {
        return totalDocuments;
    }

    public long getTotalSearches() {
        return totalSearches;
    }

    public long getAvgResponseTime() {
        return avgResponseTime;
    }

    public String getSearchMode() {
        return searchMode;
    }
}