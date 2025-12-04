package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.domain.SearchResult;
import com.documentpreview.shared.ddd.Result;

import java.util.List;

/**
 * Elasticsearch搜索服务接口
 */
public interface EsSearchServiceInterface {
    
    /**
     * 检查Elasticsearch连接
     */
    Result<Boolean> checkConnection();
    
    /**
     * 验证索引是否存在
     */
    boolean indexExists();
    
    /**
     * 简单文本搜索
     */
    Result<List<SearchResult>> simpleSearch(String keyword, int limit);
    
    /**
     * 高级搜索
     */
    Result<List<SearchResult>> advancedSearch(String keyword, int limit);
    
    /**
     * 获取搜索建议
     */
    List<String> getSuggestions(String keyword);
    
    /**
     * 获取搜索统计信息
     */
    SearchStats getSearchStats();
    
    /**
     * 搜索统计信息类
     */
    class SearchStats {
        private final long totalDocuments;
        private final long totalSearches;
        private final long avgResponseTime;
        
        public SearchStats(long totalDocuments, long totalSearches, long avgResponseTime) {
            this.totalDocuments = totalDocuments;
            this.totalSearches = totalSearches;
            this.avgResponseTime = avgResponseTime;
        }
        
        public long getTotalDocuments() { return totalDocuments; }
        public long getTotalSearches() { return totalSearches; }
        public long getAvgResponseTime() { return avgResponseTime; }
    }
}