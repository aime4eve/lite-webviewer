package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.es.EsDocument;
import com.documentpreview.modules.search.es.EsDocumentRepository;
import com.documentpreview.modules.search.repository.SearchMetaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简化的搜索服务，不再依赖Elasticsearch
 * 当Elasticsearch被禁用时，此类仅作为占位符，实际搜索操作由SimpleSearchService处理
 */
@Service
@ConditionalOnProperty(name = "search.use-es", havingValue = "false")
public class EsSearchService {
    private static final Logger logger = LoggerFactory.getLogger(EsSearchService.class);
    
    @Autowired
    private EsDocumentRepository esDocumentRepository;
    
    @Autowired
    private SearchMetaRepository searchMetaRepository;
    
    /**
     * 检查Elasticsearch连接
     * 由于Elasticsearch被禁用，返回false
     */
    public boolean checkConnection() {
        logger.info("Elasticsearch is disabled, connection check returns false");
        return false;
    }
    
    /**
     * 验证索引是否存在
     * 由于Elasticsearch被禁用，返回false
     */
    public boolean indexExists() {
        logger.info("Elasticsearch is disabled, indexExists returns false");
        return false;
    }
    
    /**
     * 简单文本搜索
     * 由于Elasticsearch被禁用，返回空结果
     */
    public List<EsDocument> simpleSearch(String query) {
        logger.info("Elasticsearch is disabled, simpleSearch returns empty result");
        return new ArrayList<>();
    }
    
    /**
     * 高级搜索
     * 由于Elasticsearch被禁用，返回空结果
     */
    public List<EsDocument> advancedSearch(String query, List<String> fileTypes, 
                                           String dateRange, String sizeRange) {
        logger.info("Elasticsearch is disabled, advancedSearch returns empty result");
        return new ArrayList<>();
    }
    
    /**
     * 获取搜索建议
     * 由于Elasticsearch被禁用，返回空列表
     */
    public List<String> getSuggestions(String query) {
        logger.info("Elasticsearch is disabled, getSuggestions returns empty list");
        return new ArrayList<>();
    }
    
    /**
     * 获取搜索统计信息
     * 由于Elasticsearch被禁用，返回默认值
     */
    public SearchStats getSearchStats() {
        logger.info("Elasticsearch is disabled, returning default search stats");
        return new SearchStats(0, 0, 0);
    }
    
    /**
     * 搜索统计信息类
     */
    public static class SearchStats {
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