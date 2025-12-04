package com.documentpreview.modules.search.service;

import com.documentpreview.modules.search.es.EsDocument;
import com.documentpreview.modules.search.es.EsDocumentRepository;
import com.documentpreview.modules.search.domain.SearchMeta;
import com.documentpreview.modules.search.repository.SearchMetaRepository;
import com.documentpreview.shared.ddd.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "app.search.use-es", havingValue = "true")
public class EsIndexService {
    private static final Logger logger = LoggerFactory.getLogger(EsIndexService.class);
    
    private final SearchMetaRepository searchMetaRepository;
    private final EsDocumentRepository esDocumentRepository;
    
    @Value("${app.search.es-index-name}")
    private String indexName;
    
    public EsIndexService(SearchMetaRepository searchMetaRepository, 
                         EsDocumentRepository esDocumentRepository) {
        this.searchMetaRepository = searchMetaRepository;
        this.esDocumentRepository = esDocumentRepository;
    }
    
    public boolean enabled() {
        return true; // ES is enabled for Docker deployment
    }
    
    public Result<Long> reindexFromMeta(boolean clear) {
        try {
            logger.info("Starting Elasticsearch reindex operation, clear existing: {}", clear);
            
            if (clear) {
                clearIndex();
            }
            
            Result<List<SearchMeta>> result = searchMetaRepository.loadAll();
            if (!result.isSuccess()) {
                return Result.failure("Failed to load metadata: " + result.getErrorMessage().orElse("Unknown error"));
            }
            
            List<SearchMeta> metas = result.getValue().orElse(new ArrayList<>());
            logger.info("Found {} documents to index", metas.size());
            
            List<EsDocument> documents = metas.stream()
                    .map(this::convertToEsDocument)
                    .collect(Collectors.toList());
            
            Iterable<EsDocument> saved = esDocumentRepository.saveAll(documents);
            long count = documents.size();
            
            logger.info("Successfully indexed {} documents to Elasticsearch", count);
            return Result.success(count);
        } catch (Exception e) {
            logger.error("Error during reindex: {}", e.getMessage(), e);
            return Result.failure("Reindex operation failed: " + e.getMessage());
        }
    }
    
    public Result<Long> reindexFromMeta() {
        return reindexFromMeta(true);
    }
    
    public Result<List<EsDocument>> indexDocuments(List<SearchMeta> metas) {
        try {
            logger.info("Indexing {} documents to Elasticsearch", metas.size());
            
            List<EsDocument> documents = metas.stream()
                    .map(this::convertToEsDocument)
                    .collect(Collectors.toList());
            
            Iterable<EsDocument> saved = esDocumentRepository.saveAll(documents);
            
            // Convert Iterable to List for return
            List<EsDocument> resultList = new ArrayList<>();
            saved.forEach(resultList::add);
            
            logger.info("Successfully indexed {} documents", resultList.size());
            return Result.success(resultList);
        } catch (Exception e) {
            logger.error("Error indexing documents: {}", e.getMessage(), e);
            return Result.failure("Indexing failed: " + e.getMessage());
        }
    }
    
    public Result<EsDocument> indexDocument(SearchMeta meta) {
        try {
            logger.info("Indexing document: {}", meta.getFilePath());
            
            EsDocument document = convertToEsDocument(meta);
            EsDocument saved = esDocumentRepository.save(document);
            
            logger.info("Successfully indexed document: {}", saved.getFilePath());
            return Result.success(saved);
        } catch (Exception e) {
            logger.error("Error indexing document {}: {}", meta.getFilePath(), e.getMessage(), e);
            return Result.failure("Document indexing failed: " + e.getMessage());
        }
    }
    
    public Result<Long> getDocumentCount() {
        try {
            long count = esDocumentRepository.count();
            logger.info("Current document count in Elasticsearch: {}", count);
            return Result.success(count);
        } catch (Exception e) {
            logger.error("Error getting document count: {}", e.getMessage(), e);
            return Result.failure("Failed to get document count: " + e.getMessage());
        }
    }
    
    public Result<Void> deleteDocument(String filePath) {
        try {
            logger.info("Deleting document from Elasticsearch: {}", filePath);
            esDocumentRepository.deleteByFilePath(filePath);
            logger.info("Successfully deleted document: {}", filePath);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("Error deleting document {}: {}", filePath, e.getMessage(), e);
            return Result.failure("Document deletion failed: " + e.getMessage());
        }
    }
    
    public Result<Void> clearIndex() {
        try {
            logger.info("Clearing Elasticsearch index: {}", indexName);
            esDocumentRepository.deleteAll();
            logger.info("Successfully cleared index: {}", indexName);
            return Result.success(null);
        } catch (Exception e) {
            logger.error("Error clearing index: {}", e.getMessage(), e);
            return Result.failure("Index clearing failed: " + e.getMessage());
        }
    }
    
    private EsDocument convertToEsDocument(SearchMeta meta) {
        EsDocument document = new EsDocument();
        document.setId(meta.getId());
        document.setFilePath(meta.getFilePath());
        document.setFileName(meta.getFileName());
        document.setFileType(meta.getFileType());
        document.setContent(meta.getContent());
        document.setSize(meta.getSize());
        document.setModifiedAt(meta.getLastModified());
        document.setParentDir(meta.getParentDir());
        return document;
    }
}