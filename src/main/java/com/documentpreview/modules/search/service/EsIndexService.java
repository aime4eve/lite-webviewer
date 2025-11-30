package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.es.EsDocument;
import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.repository.SearchMetaRepository;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "search.use-es", havingValue = "false", matchIfMissing = true)
public class EsIndexService {
    private static final Logger logger = LoggerFactory.getLogger(EsIndexService.class);
    
    private final SearchMetaRepository searchMetaRepository;
    
    public EsIndexService(SearchMetaRepository searchMetaRepository) {
        this.searchMetaRepository = searchMetaRepository;
    }
    
    public boolean enabled() {
        return false; // ES is disabled
    }
    
    public Result<Long> reindexFromMeta(boolean clear) {
        logger.info("Elasticsearch is disabled. Reindex operation skipped.");
        return Result.success(0L);
    }
    
    public Result<Long> reindexFromMeta() {
        logger.info("Elasticsearch is disabled. Reindex operation skipped.");
        return Result.success(0L);
    }
    
    public Result<List<EsDocument>> indexDocuments(List<SearchMeta> metas) {
        logger.info("Elasticsearch is disabled. Document indexing skipped.");
        return Result.success(new ArrayList<>());
    }
    
    public Result<EsDocument> indexDocument(SearchMeta meta) {
        logger.info("Elasticsearch is disabled. Document indexing skipped.");
        return Result.success(new EsDocument());
    }
    
    public Result<Long> getDocumentCount() {
        // Get count from SearchMetaRepository instead
        Result<List<SearchMeta>> result = searchMetaRepository.loadAll();
        if (result.isSuccess()) {
            return Result.success((long) result.getValue().get().size());
        } else {
            return Result.failure("Failed to get document count: " + result.getErrorMessage().orElse("Unknown error"));
        }
    }
    
    public Result<Void> deleteDocument(String filePath) {
        logger.info("Elasticsearch is disabled. Document deletion skipped.");
        return Result.success(null);
    }
    
    public Result<Void> clearIndex() {
        logger.info("Elasticsearch is disabled. Index clearing skipped.");
        return Result.success(null);
    }
}