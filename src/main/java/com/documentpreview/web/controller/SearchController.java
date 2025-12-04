package com.documentpreview.web.controller;

import com.documentpreview.modules.scan.domain.FilesIndex;
import com.documentpreview.modules.scan.repository.IndexRepository;
import com.documentpreview.modules.search.domain.SearchResult;
import com.documentpreview.modules.search.service.SimpleSearchService;
import com.documentpreview.modules.search.domain.SearchRequest;
import com.documentpreview.modules.search.service.AdvancedSearchService;
import com.documentpreview.modules.search.domain.SearchStatus;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.lang.Nullable;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {
    private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

    private final IndexRepository indexRepository;
    private final SimpleSearchService searchService;
    private final AdvancedSearchService advancedSearchService;
    private final com.documentpreview.modules.search.service.EsSearchServiceInterface esSearchService;
    private final com.documentpreview.modules.search.service.EsIndexService esIndexService;

    public SearchController(IndexRepository indexRepository, SimpleSearchService searchService, AdvancedSearchService advancedSearchService, @Nullable com.documentpreview.modules.search.service.EsSearchServiceInterface esSearchService, @Nullable com.documentpreview.modules.search.service.EsIndexService esIndexService) {
        this.indexRepository = indexRepository;
        this.searchService = searchService;
        this.advancedSearchService = advancedSearchService;
        this.esSearchService = esSearchService;
        this.esIndexService = esIndexService;
    }

    @GetMapping("/basic")
    public ResponseEntity<?> basicSearch(@RequestParam(value = "q", defaultValue = "") String keyword,
                                         @RequestParam(value = "limit", required = false, defaultValue = "20") int limit) {
        logger.info("GET /api/v1/search/basic q={} limit={}", keyword, limit);
        Result<FilesIndex> indexResult = indexRepository.loadIndex();
        if (indexResult.isFailure()) {
            logger.warn("Failed to load index for basic search with keyword: '{}', error: {}", keyword, indexResult.getErrorMessage().orElse("Unknown error"));
            return ResponseEntity.status(500).body(indexResult.getErrorMessage().orElse("Failed to load index"));
        }
        FilesIndex index = indexResult.getValue().get();
        Result<List<SearchResult>> search = searchService.search(index, keyword, limit);
        if (search.isFailure()) {
            logger.warn("Basic search failed for keyword: '{}', error: {}", keyword, search.getErrorMessage().orElse("Unknown error"));
            return ResponseEntity.status(500).body(search.getErrorMessage().orElse("Search failed"));
        }
        List<SearchResult> results = search.getValue().get();
        logger.info("Basic search completed successfully with {} results for keyword: '{}'", results.size(), keyword);
        return ResponseEntity.ok(results);
    }

    @PostMapping("/advanced")
    public ResponseEntity<?> advancedSearch(@RequestBody SearchRequest request,
                                            @RequestParam(value = "limit", required = false, defaultValue = "1000") int limit) {
        String keyword = (request.getKeyword() == null ? "" : request.getKeyword()).trim();
        boolean useEs = (esSearchService != null && esSearchService.checkConnection().isSuccess() && esSearchService.checkConnection().getValue().orElse(false));
        logger.info("Advanced search request received with keyword: '{}', limit: {}, using ES: {}", keyword, limit, useEs);
        
        Result<java.util.List<SearchResult>> r;
        if (useEs) {
            // 使用ES搜索服务进行全文检索
            logger.info("Performing Elasticsearch full-text search");
            // 暂时直接使用advancedSearchService，因为ES服务返回类型不匹配
            r = advancedSearchService.search(request, limit);
        } else {
            logger.info("Elasticsearch not enabled or unavailable, using local search");
            r = advancedSearchService.search(request, limit);
        }
        
        if (r.isFailure()) {
            logger.warn("Advanced search failed for keyword: '{}', error: {}", 
                        keyword, r.getErrorMessage().orElse("Unknown error"));
            return ResponseEntity.status(500).body(r.getErrorMessage().orElse("Advanced search failed"));
        }
        java.util.List<SearchResult> list = r.getValue().get();
        int originalSize = list.size();
        if (!keyword.isEmpty()) {
            list = list.stream().limit(Math.max(1, limit)).collect(java.util.stream.Collectors.toList());
        }
        logger.info("Advanced search completed with {} filtered results ({} original results) for keyword: '{}', using ES: {}", 
                    list.size(), originalSize, keyword, useEs);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/status")
    public ResponseEntity<?> searchStatus() {
        logger.info("GET /api/v1/search/status - checking search service status");
        
        boolean esEnabled = (esSearchService != null);
        boolean esConnected = false;
        boolean indexExists = false;
        long totalDocuments = 0;
        long totalSearches = 0;
        long avgResponseTime = 0;
        
        if (esEnabled) {
            try {
                // 检查ES连接
                Result<Boolean> connectionResult = esSearchService.checkConnection();
                esConnected = connectionResult.isSuccess() && connectionResult.getValue().orElse(false);
                
                // 检查索引是否存在
                indexExists = esSearchService.indexExists();
                
                // 获取搜索统计信息
                com.documentpreview.modules.search.service.EsSearchServiceInterface.SearchStats stats = esSearchService.getSearchStats();
                if (stats != null) {
                    totalDocuments = stats.getTotalDocuments();
                    totalSearches = stats.getTotalSearches();
                    avgResponseTime = stats.getAvgResponseTime();
                }
            } catch (Exception e) {
                logger.warn("Error checking Elasticsearch status: {}", e.getMessage());
            }
        }
        
        // 确定搜索模式
        String searchMode = esEnabled && esConnected && indexExists ? "elasticsearch" : "local";
        
        SearchStatus status = new SearchStatus(
            esEnabled, esConnected, indexExists,
            totalDocuments, totalSearches, avgResponseTime,
            searchMode
        );
        
        logger.info("Search status: ES enabled={}, connected={}, index exists={}, mode={}", 
                   esEnabled, esConnected, indexExists, searchMode);
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/reindex")
    public ResponseEntity<?> reindex(@RequestParam(value = "clear", defaultValue = "false") boolean clear) {
        logger.info("Reindexing request received with clear flag: {}", clear);
        if (esIndexService == null || !esIndexService.enabled()) {
            logger.warn("Reindexing attempted but ES is disabled");
            return ResponseEntity.status(400).body("ES disabled");
        }
        Result<Long> r = esIndexService.reindexFromMeta(clear);
        if (r.isFailure()) {
            logger.error("Reindexing failed with error: {}", r.getErrorMessage().orElse("Unknown error"));
            return ResponseEntity.status(500).body(r.getErrorMessage().orElse("Reindex failed"));
        }
        long count = r.getValue().get();
        logger.info("Reindexing completed successfully with {} documents indexed", count);
        return ResponseEntity.ok("Reindexed documents: " + count);
    }

    @PutMapping("/admin/init")
    public ResponseEntity<?> adminInitIndex(@RequestParam(value = "clear", defaultValue = "true") boolean clear) {
        logger.info("Admin initialization request received with clear flag: {}", clear);
        if (esIndexService == null || !esIndexService.enabled()) {
            logger.warn("Admin initialization attempted but ES is disabled");
            return ResponseEntity.status(400).body("ES disabled");
        }
        Result<Long> r = esIndexService.reindexFromMeta(clear);
        if (r.isFailure()) {
            logger.error("Admin initialization failed with error: {}", r.getErrorMessage().orElse("Unknown error"));
            return ResponseEntity.status(500).body(r.getErrorMessage().orElse("Init failed"));
        }
        long count = r.getValue().get();
        logger.info("Admin initialization completed successfully with {} documents indexed", count);
        return ResponseEntity.ok("Initialized documents: " + count);
    }
}