package com.documentpreview.modules.search.service;

import com.documentpreview.modules.document.domain.FileType;
import com.documentpreview.modules.search.domain.SearchResult;
import com.documentpreview.modules.search.domain.SearchExpression;
import com.documentpreview.modules.search.es.EsDocument;
import com.documentpreview.modules.search.es.EsDocumentRepository;
import com.documentpreview.shared.ddd.Result;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 真正的Elasticsearch搜索服务实现类
 * 当app.search.use-es=true时启用
 */
@Service
@ConditionalOnProperty(name = "app.search.use-es", havingValue = "true")
public class EsSearchServiceImpl implements EsSearchServiceInterface {
    private static final Logger logger = LoggerFactory.getLogger(EsSearchServiceImpl.class);

    @Autowired
    private EsDocumentRepository esDocumentRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Value("${app.search.es-index-name:nexus-lite-docs}")
    private String indexName;
    
    private final SearchExpressionParser parser;

    /**
     * 构造函数
     */
    public EsSearchServiceImpl() {
        this.parser = new SearchExpressionParser();
    }

    /**
     * 检查Elasticsearch连接
     */
    public Result<Boolean> checkConnection() {
        try {
            logger.info("Checking Elasticsearch connection...");
            
            long startTime = System.currentTimeMillis();
            
            // 使用更可靠的方法检查连接：检查集群健康状态
            // 首先尝试检查索引是否存在，如果索引不存在，则检查集群是否可访问
            boolean indexExists = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists();
            
            // 如果索引存在，尝试执行一个简单的查询
            if (indexExists) {
                Criteria criteria = new Criteria("fileName").contains("test");
                Query query = new CriteriaQuery(criteria).setPageable(PageRequest.of(0, 1));
                elasticsearchOperations.search(query, EsDocument.class, IndexCoordinates.of(indexName));
            } else {
                // 如果索引不存在，尝试创建一个临时索引来验证连接
                IndexCoordinates tempIndex = IndexCoordinates.of("temp-connection-check");
                try {
                    elasticsearchOperations.indexOps(tempIndex).create();
                    elasticsearchOperations.indexOps(tempIndex).delete();
                } catch (Exception ex) {
                    // 如果创建临时索引失败，但能执行到这一步，说明连接是正常的
                    logger.debug("Temporary index creation failed but connection is valid: {}", ex.getMessage());
                }
            }
            
            long endTime = System.currentTimeMillis();
            
            boolean connected = true; // 如果没有异常，则认为连接成功
            logger.info("Elasticsearch connection check: {}, response time: {}ms", connected, (endTime - startTime));
            return Result.success(connected);
        } catch (Exception e) {
            logger.warn("Elasticsearch connection failed: {}. Will try to start anyway.", e.getMessage());
            return Result.failure("Elasticsearch connection failed: " + e.getMessage());
        }
    }

    /**
     * 验证索引是否存在
     */
    public boolean indexExists() {
        try {
            logger.info("Checking if index '{}' exists...", indexName);
            boolean exists = elasticsearchOperations.indexOps(IndexCoordinates.of(indexName)).exists();
            logger.info("Index '{}' exists: {}", indexName, exists);
            return exists;
        } catch (Exception e) {
            logger.warn("Error checking if index exists: {}. Will try to proceed anyway.", e.getMessage());
            return false;
        }
    }

    /**
     * 简单文本搜索
     */
    public Result<List<SearchResult>> simpleSearch(String keyword, int limit) {
        if (StringUtils.isBlank(keyword)) {
            return Result.success(Collections.emptyList());
        }

        try {
            // 尝试检查ES连接，但即使失败也继续执行
            boolean connected = checkConnection().getValue().orElse(false);
            if (!connected) {
                logger.warn("ES not connected, proceeding with limited functionality");
            }
            
            logger.info("Performing simple search with keyword: '{}'", keyword);
            
            // 解析搜索表达式
            SearchExpression expression;
            try {
                expression = parser.parse(keyword);
            } catch (IllegalArgumentException e) {
                // 搜索语法错误，返回错误信息
                return Result.failure("搜索语法错误: " + e.getMessage() + "\n\n" + parser.getSyntaxHelp());
            }
            
            // 构建ES查询条件
            Criteria criteria = expression.buildCriteria();
            Query searchQuery = new CriteriaQuery(criteria)
                    .setPageable(PageRequest.of(0, limit > 0 ? limit : 100))
                    .addSort(Sort.by(Sort.Direction.DESC, "modifiedAt"));

            SearchHits<EsDocument> searchHits = elasticsearchOperations.search(searchQuery, EsDocument.class, IndexCoordinates.of(indexName));
            
            // 转换搜索结果
            List<SearchResult> results = searchHits.getSearchHits().stream()
                    .map(hit -> {
                        EsDocument doc = hit.getContent();
                        int score = (int) Math.round(hit.getScore() * 100);
                        String snippet = buildSnippetFromContent(doc.getContent(), keyword);
                        return new SearchResult(doc.getFilePath(), doc.getFileName(), doc.getParentDir(), 
                                doc.getFileType(), score, snippet);
                    })
                    .collect(Collectors.toList());
                    
            return Result.success(results);
        } catch (Exception e) {
            logger.error("Error during simple search: {}", e.getMessage(), e);
            return Result.failure("Error during simple search: " + e.getMessage());
        }
    }

    /**
     * 高级搜索
     */
    public Result<List<SearchResult>> advancedSearch(String keyword, int limit) {
        if (StringUtils.isBlank(keyword)) {
            return Result.success(Collections.emptyList());
        }

        try {
            logger.info("Performing advanced search with keyword: '{}'", keyword);

            // 解析搜索表达式
            SearchExpression expression;
            try {
                expression = parser.parse(keyword);
            } catch (IllegalArgumentException e) {
                // 搜索语法错误，返回错误信息
                return Result.failure("搜索语法错误: " + e.getMessage() + "\n\n" + parser.getSyntaxHelp());
            }
            
            // 构建ES查询条件
            Criteria criteria = expression.buildCriteria();

            Query searchQuery = new CriteriaQuery(criteria)
                    .setPageable(PageRequest.of(0, limit > 0 ? limit : 50))
                    .addSort(Sort.by(Sort.Direction.DESC, "modifiedAt"));

            SearchHits<EsDocument> searchHits = elasticsearchOperations.search(searchQuery, EsDocument.class, IndexCoordinates.of(indexName));
            
            // 转换搜索结果
            List<SearchResult> results = searchHits.getSearchHits().stream()
                    .map(hit -> {
                        EsDocument doc = hit.getContent();
                        int score = (int) Math.round(hit.getScore() * 100);
                        String snippet = buildSnippetFromContent(doc.getContent(), keyword);
                        return new SearchResult(doc.getFilePath(), doc.getFileName(), doc.getParentDir(), 
                                doc.getFileType(), score, snippet);
                    })
                    .collect(Collectors.toList());
                    
            return Result.success(results);
        } catch (Exception e) {
            logger.error("Error during advanced search: {}", e.getMessage(), e);
            return Result.failure("Error during advanced search: " + e.getMessage());
        }
    }

    /**
     * 获取搜索建议
     */
    public List<String> getSuggestions(String keyword) {
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }

        try {
            logger.info("Getting search suggestions for keyword: '{}'", keyword);

            // 使用前缀查询获取文件名建议
            Criteria criteria = new Criteria("fileName").startsWith(keyword.toLowerCase());
            Query searchQuery = new CriteriaQuery(criteria)
                    .setPageable(PageRequest.of(0, 10));

            SearchHits<EsDocument> searchHits = elasticsearchOperations.search(searchQuery, EsDocument.class, IndexCoordinates.of(indexName));

            // 提取唯一的文件名建议
            Set<String> suggestions = new HashSet<>();
            for (SearchHit<EsDocument> hit : searchHits.getSearchHits()) {
                EsDocument doc = hit.getContent();
                if (StringUtils.isNotBlank(doc.getFileName())) {
                    suggestions.add(doc.getFileName());
                }
            }

            return new ArrayList<>(suggestions);
        } catch (Exception e) {
            logger.error("Error getting search suggestions: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 获取搜索统计信息
     */
    public SearchStats getSearchStats() {
        try {
            logger.info("Getting search statistics");

            // 获取总文档数
            Criteria criteria = new Criteria();
            Query countQuery = new CriteriaQuery(criteria);

            long totalDocuments = elasticsearchOperations.count(countQuery, EsDocument.class, IndexCoordinates.of(indexName));

            // 获取文件类型分布
            Query typeQuery = new CriteriaQuery(criteria);

            SearchHits<EsDocument> searchHits = elasticsearchOperations.search(typeQuery, EsDocument.class, IndexCoordinates.of(indexName));

            Map<String, Long> fileTypeDistribution = new HashMap<>();
            for (SearchHit<EsDocument> hit : searchHits.getSearchHits()) {
                EsDocument doc = hit.getContent();
                String fileType = doc.getFileType() != null ? doc.getFileType().name() : "unknown";
                fileTypeDistribution.put(fileType, fileTypeDistribution.getOrDefault(fileType, 0L) + 1);
            }

            // 获取索引大小信息
            long totalSize = 0;
            for (SearchHit<EsDocument> hit : searchHits.getSearchHits()) {
                EsDocument doc = hit.getContent();
                totalSize += doc.getSize() != null ? doc.getSize() : 0;
            }

            return new SearchStats(totalDocuments, 0, 0); // 暂时返回0作为搜索次数和平均响应时间
        } catch (Exception e) {
            logger.error("Error getting search statistics: {}", e.getMessage(), e);
            return new SearchStats(0, 0, 0);
        }
    }

    /**
     * 从内容构建片段
     */
    private String buildSnippetFromContent(String content, String keyword) {
        if (StringUtils.isBlank(content)) {
            return "(No preview available)";
        }
        
        StringBuilder snippet = new StringBuilder();
        
        if (StringUtils.isNotBlank(keyword)) {
            int index = content.toLowerCase().indexOf(keyword.toLowerCase());
            if (index >= 0) {
                int start = Math.max(0, index - 50);
                int end = Math.min(content.length(), index + keyword.length() + 100);
                snippet.append("...").append(content.substring(start, end)).append("...");
            } else {
                snippet.append(content.substring(0, Math.min(200, content.length()))).append("...");
            }
        } else {
            snippet.append(content.substring(0, Math.min(200, content.length()))).append("...");
        }
        
        return snippet.toString();
    }

    /**
     * 创建索引（如果不存在）
     */
    private void createIndexIfNotExists() {
        try {
            if (!indexExists()) {
                logger.info("Index does not exist, will not attempt to create it automatically to avoid configuration issues");
                // 这里不执行索引创建，由外部服务负责索引初始化
            }
        } catch (Exception e) {
            logger.warn("Error during index existence check: {}. Will continue without index creation.", e.getMessage());
            // 不抛出异常，允许应用继续运行
        }
    }
}