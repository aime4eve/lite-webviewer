# Nexus-Lite 全文检索实现文档

## 1. 系统概述

Nexus-Lite的全文检索功能提供了高效的文档内容搜索能力，支持多种文件格式的内容提取、索引和检索。该功能基于Elasticsearch实现，支持全文搜索、多条件筛选、结果高亮等高级特性。

## 2. 核心工作流程

全文检索功能的完整工作流程包含以下主要步骤：

1. **文档扫描与内容提取**：系统扫描配置的根目录，提取支持的文件格式的内容
2. **元数据生成与存储**：将提取的内容和文件元数据生成SearchMeta对象并本地存储
3. **Elasticsearch索引同步**：将SearchMeta数据转换并同步到Elasticsearch索引
4. **用户查询处理**：接收并处理用户的搜索请求
5. **结果高亮与返回**：处理搜索结果，高亮关键字并返回给用户

## 3. 核心组件详解

### 3.1 文档扫描与内容提取

#### FileScanServiceImpl

负责扫描文件系统，检测文件变化，并触发内容提取：

- 扫描配置的根目录（通过`app.scan.root-dirs`配置）
- 使用多线程高效处理大量文件
- 调用SearchMetaBuilder提取文件内容
- 将提取的SearchMeta保存到本地文件系统
- 发布扫描完成事件（ScanFinishedEvent）

关键代码流程：
```java
// 扫描目录并收集文件信息
for (String dirPath : dirPaths) {
    Path rootPath = Paths.get(dirPath);
    // 递归遍历文件树
    Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            // 检查文件是否需要处理
            if (shouldProcessFile(file)) {
                // 提取文件元数据
                metas.add(searchMetaBuilder.build(rootPath, file, lastModified));
            }
            return FileVisitResult.CONTINUE;
        }
    });
}
// 保存搜索元数据
searchMetaRepository.saveAll(metas);
```

#### SearchMetaBuilder

负责从不同格式的文件中提取内容：

- 支持多种文件格式的内容提取：PDF、DOCX、XLSX、MD、HTML、CSV等
- 针对不同文件类型使用专门的解析器
- 提取文件名、路径、内容、修改时间等元数据
- 处理大文件时进行智能截断，避免内存溢出

关键实现：
```java
public SearchMeta build(Path rootPath, Path filePath, long modifiedAt) {
    // 提取基本信息
    String rel = rootPath.relativize(filePath).toString().replace("\\", "/");
    String fileName = filePath.getFileName().toString();
    FileType type = FileType.fromExtension(ext);
    
    // 根据文件类型提取内容
    if (type == FileType.PDF) {
        // 使用PDFBox提取PDF内容
        // 只提取头部和尾部几页，兼顾内容覆盖和性能
    } else if (type == FileType.DOCX) {
        // 使用Apache POI提取Word文档内容
    } else if (type == FileType.MD) {
        // 直接读取文本内容
    }
    
    // 限制内容长度，避免过大的JSON
    if (contentText != null && contentText.length() > 20000) {
        contentText = contentText.substring(0, 20000);
    }
    
    // 创建并返回SearchMeta对象
    return new SearchMeta(rel, fileName, parentDir, type, title, contentText, size, modifiedAt);
}
```

### 3.2 元数据存储与管理

#### SearchMetaRepository

负责SearchMeta数据的持久化和加载：

- 将SearchMeta列表保存为JSON文件
- 从JSON文件加载SearchMeta数据
- 处理文件IO异常
- 使用Jackson库进行JSON序列化和反序列化

关键代码：
```java
public Result<List<SearchMeta>> saveAll(List<SearchMeta> list) {
    try {
        // 确保数据目录存在
        Path dir = Paths.get(dataDir);
        if (!Files.exists(dir)) Files.createDirectories(dir);
        // 保存为JSON文件
        Path jsonPath = dir.resolve(SEARCH_META_JSON);
        objectMapper.writeValue(jsonPath.toFile(), list);
        return Result.success(list);
    } catch (Exception e) {
        // 错误处理
        return Result.failure("Failed to save search meta: " + e.getMessage());
    }
}
```

### 3.3 Elasticsearch索引同步

#### EsIndexService

负责将本地SearchMeta数据同步到Elasticsearch索引：

- 只有当`app.search.use-es=true`时才会启用
- 从SearchMetaRepository加载所有元数据
- 将SearchMeta转换为EsDocument对象
- 批量保存到Elasticsearch索引
- 支持清除旧索引并重建

核心逻辑：
```java
@ConditionalOnProperty("app.search.use-es")
public Result<Long> reindexFromMeta(boolean clear) {
    // 加载所有SearchMeta数据
    Result<List<SearchMeta>> loadResult = searchMetaRepository.loadAll();
    if (loadResult.isFailure()) {
        return Result.failure(loadResult.getErrorMessage().orElse("Failed to load search meta"));
    }
    
    // 转换为EsDocument
    List<EsDocument> docs = loadResult.getValue().get().stream()
        .map(meta -> new EsDocument(
            meta.getFilePath(),
            meta.getFileName(),
            meta.getParentDir(),
            meta.getFileType(),
            meta.getTitle(),
            meta.getContent(),
            meta.getSize(),
            meta.getModifiedAt()
        ))
        .collect(Collectors.toList());
    
    // 批量保存到Elasticsearch
    repo.saveAll(docs);
    return Result.success((long) docs.size());
}
```

#### EsDocumentRepository

基于Spring Data Elasticsearch的仓库接口：

- 继承ElasticsearchRepository提供基本数据访问功能
- 支持Elasticsearch的CRUD操作
- 自动映射EsDocument对象到Elasticsearch索引

```java
public interface EsDocumentRepository extends ElasticsearchRepository<EsDocument, String> {
    // 提供基本的数据访问功能
}
```

### 3.4 搜索服务实现

#### EsSearchService

实现全文检索功能：

- 构建Elasticsearch查询DSL
- 支持多字段搜索（content、title、fileName等）
- 处理过滤条件（文件类型、目录、大小、时间等）
- 实现搜索结果高亮显示
- 截取关键片段展示给用户

搜索实现：
```java
public Result<List<SearchResult>> search(SearchRequest request, int limit) {
    // 构建搜索查询
    Map<String, Object> query = new HashMap<>();
    // 多字段匹配查询
    Map<String, Object> multiMatch = new HashMap<>();
    multiMatch.put("query", request.getKeyword());
    multiMatch.put("fields", Arrays.asList("content", "title", "fileName"));
    multiMatch.put("type", "best_fields");
    
    // 添加过滤条件
    if (request.getFileType() != null) {
        // 添加文件类型过滤
    }
    
    // 执行搜索
    SearchHits<EsDocument> searchHits = operations.search(searchQuery, EsDocument.class);
    
    // 处理结果和高亮
    List<SearchResult> results = new ArrayList<>();
    for (SearchHit<EsDocument> hit : searchHits.getSearchHits()) {
        EsDocument doc = hit.getContent();
        // 处理高亮显示
        String snippet = extractSnippetWithHighlight(hit, request.getKeyword());
        results.add(new SearchResult(
            doc.getFilePath(),
            doc.getFileName(),
            doc.getFileType(),
            snippet,
            hit.getScore(),
            doc.getModifiedAt()
        ));
    }
    
    return Result.success(results);
}
```

#### SearchController

处理HTTP搜索请求：

- 提供基础搜索和高级搜索API
- 支持重建索引操作
- 实现ES搜索失败时自动降级到本地搜索的机制
- 增强的ES可用性检测和故障转移
- 统一的错误处理和日志记录

```java
@PostMapping("/advanced")
public ResponseEntity<?> advancedSearch(@RequestBody SearchRequest request, @RequestParam int limit) {
    String keyword = (request.getKeyword() == null ? "" : request.getKeyword()).trim();
    boolean useEs = (esSearchService != null && esSearchService.enabled());
    logger.info("Advanced search request received with keyword: '{}', limit: {}, using ES: {}", keyword, limit, useEs);
    
    Result<List<SearchResult>> r;
    if (useEs) {
        // 首先尝试使用ES搜索
        r = esSearchService.search(request, limit);
        // 如果ES搜索失败，自动降级到本地搜索
        if (r.isFailure()) {
            logger.warn("ES search failed, falling back to local search: {}", r.getErrorMessage().orElse("Unknown error"));
            r = advancedSearchService.search(request, limit);
        }
    } else {
        logger.info("Elasticsearch not enabled or unavailable, using local search");
        r = advancedSearchService.search(request, limit);
    }
    
    if (r.isFailure()) {
        logger.warn("Advanced search failed for keyword: '{}', error: {}", 
                    keyword, r.getErrorMessage().orElse("Unknown error"));
        return ResponseEntity.status(500).body(r.getErrorMessage().orElse("Advanced search failed"));
    }
    List<SearchResult> list = r.getValue().get();
    int originalSize = list.size();
    if (!keyword.isEmpty()) {
        list = list.stream().limit(Math.max(1, limit)).collect(Collectors.toList());
    }
    logger.info("Advanced search completed with {} filtered results ({} original results) for keyword: '{}', using ES: {}", 
                list.size(), originalSize, keyword, useEs);
    return ResponseEntity.ok(list);
}
```

## 4. 关键数据结构

### 4.1 SearchMeta

文件搜索元数据，包含文件的基本信息和内容：

- `filePath`: 文件相对路径
- `fileName`: 文件名
- `parentDir`: 父目录
- `fileType`: 文件类型
- `title`: 标题（可能从文档内容提取）
- `content`: 提取的文本内容
- `size`: 文件大小
- `modifiedAt`: 最后修改时间

### 4.2 EsDocument

映射到Elasticsearch索引的文档对象：

- 与SearchMeta类似的字段结构
- 专为Elasticsearch索引优化的设计
- 包含TOON结构嵌套字段（用于文档结构索引）

### 4.3 SearchRequest

搜索请求参数：

- `keyword`: 搜索关键词
- `fileType`: 文件类型过滤
- `parentDir`: 目录过滤
- `minSize`/`maxSize`: 大小范围过滤
- `startTime`/`endTime`: 时间范围过滤

### 4.4 SearchResult

搜索结果对象：

- `filePath`: 文件路径
- `fileName`: 文件名
- `fileType`: 文件类型
- `snippet`: 包含关键词的内容片段（带高亮）
- `score`: 相关性评分
- `modifiedAt`: 最后修改时间

## 5. 配置与依赖

### 5.1 核心配置项

在`application.yml`中配置：

```yaml
# 扫描配置
app:
  scan:
    root-dirs: /path/to/docs  # 扫描的根目录
  
# Elasticsearch配置
app:
  search:
    use-es: true  # 是否启用Elasticsearch
    es-index-name: nexus-lite-docs  # 索引名称

# Elasticsearch连接配置
spring:
  elasticsearch:
    uris: http://localhost:9200  # Elasticsearch服务地址
```

### 5.2 主要依赖

- **Elasticsearch 8.x**: 全文搜索引擎
- **Spring Data Elasticsearch**: Elasticsearch集成
- **PDFBox**: PDF文件处理
- **Apache POI**: Office文件处理
- **JSoup**: HTML解析
- **Jackson**: JSON处理

## 6. 错误处理与日志

系统在关键环节都有完善的错误处理和日志记录：

- 文件内容提取失败时记录详细错误信息
- 增强的Elasticsearch可用性检测，包括连接状态和索引存在性验证
- 搜索请求失败时的自动降级机制，确保即使ES不可用也能提供基本搜索功能
- 搜索请求失败的友好错误响应
- 性能关键操作的详细日志记录
- 完整的错误追踪和调试信息

## 7. 性能优化

### 7.1 索引优化

- 合理的分片和副本设置（单节点环境下副本为0）
- 字段权重设置，优先匹配更重要的字段
- 适当的分词器配置，优化中文搜索效果

### 7.2 查询优化

- 使用过滤查询提高性能
- 合理的分页设置
- 避免全表扫描操作

### 7.3 缓存策略

- 利用Elasticsearch自身缓存
- 应用层缓存热点搜索结果
- 智能缓存失效策略

## 8. 扩展性设计

- **模块化架构**：搜索功能与其他模块解耦
- **可插拔设计**：支持切换不同的搜索引擎实现
- **自定义扩展点**：支持添加新的文件类型处理器
- **配置驱动**：通过配置灵活控制功能行为

## 9. 监控与维护

- 索引健康状态监控
- 搜索性能指标收集
- 定期重建索引机制
- 索引大小和文档数量监控

## 10. 总结

Nexus-Lite的全文检索功能通过模块化设计和优秀的架构，提供了高效、可扩展的文档搜索能力。系统集成了多种文件格式处理、智能内容提取、Elasticsearch索引管理和高级搜索功能，为用户提供了良好的文档检索体验。

通过合理的性能优化和错误处理，系统能够稳定高效地运行，支持大规模文档集合的检索需求。同时，灵活的配置和扩展性设计也使得系统能够适应不同的部署环境和业务需求。