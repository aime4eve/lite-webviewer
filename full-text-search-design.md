# Nexus-Lite 全文检索功能技术方案

## 1. 项目技术栈分析

### 1.1 现有技术栈
- **核心框架**：Spring Boot 3.2.0
- **编程语言**：Java 17
- **数据库**：H2（文件索引和元数据存储）
- **ORM框架**：Spring Data JPA
- **缓存**：Caffeine
- **文档处理**：PDFBox、Apache POI、Mammoth、Flexmark等

### 1.2 全文检索需求分析
- **检索范围**：文档内容、文件名、元数据、文档结构
- **数据量**：初期预计数千到数万份文档，未来可扩展到百万级
- **实时性要求**：近实时检索（文档更新后秒级可检索）
- **检索精度**：支持精确匹配、模糊匹配、短语匹配
- **检索速度**：毫秒级响应
- **语言支持**：主要支持中文和英文

## 2. 检索引擎选型

### 2.1 可选方案对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| Elasticsearch | 功能强大，分布式支持，社区活跃，中文分词成熟，与Spring Boot集成良好 | 部署维护复杂，资源消耗较大 |
| Solr | 成熟稳定，管理界面友好，支持多种数据源 | 功能相对有限，社区活跃度不如Elasticsearch |
| 数据库内置全文检索 | 部署简单，无需额外服务 | 性能有限，不适合大规模数据和复杂查询 |
| Lucene | 轻量级，可嵌入应用 | 开发成本高，需自行封装大量功能 |

### 2.2 选型建议
**推荐使用：Elasticsearch 8.x**

### 2.3 选型理由
1. **功能完整性**：提供完整的全文检索功能，支持复杂查询、过滤、排序、高亮等
2. **中文支持**：成熟的中文分词器（如IK Analyzer、HanLP），适合中文文档检索
3. **扩展性**：支持分布式部署，可根据数据量横向扩展
4. **Spring集成**：Spring Data Elasticsearch提供了良好的集成支持
5. **社区活跃度**：拥有庞大的社区和丰富的文档资源
6. **性能优异**：针对检索场景优化，毫秒级响应
7. **近实时性**：文档更新后秒级可检索

## 3. 索引设计方案

### 3.1 索引结构设计

#### 3.1.1 主索引（documents）

| 字段名 | 类型 | 分词器 | 索引 | 存储 | 描述 |
|--------|------|--------|------|------|------|
| filePath | keyword | - | Yes | Yes | 文档路径（唯一标识） |
| fileName | text | ik_max_word | Yes | Yes | 文件名 |
| fileName.raw | keyword | - | Yes | No | 文件名原始值（用于精确匹配） |
| fileType | keyword | - | Yes | Yes | 文件类型（PDF、DOCX、MD等） |
| content | text | ik_max_word | Yes | No | 文档内容（全文检索核心字段） |
| author | keyword | - | Yes | Yes | 作者 |
| title | text | ik_max_word | Yes | Yes | 文档标题 |
| size | long | - | Yes | Yes | 文件大小（字节） |
| createdAt | date | - | Yes | Yes | 创建时间 |
| modifiedAt | date | - | Yes | Yes | 修改时间 |
| mimeType | keyword | - | Yes | Yes | MIME类型 |
| pages | integer | - | Yes | Yes | 页数（仅PDF） |
| toonStructure | nested | - | Yes | Yes | TOON结构（嵌套类型） |
| parentDir | keyword | - | Yes | Yes | 父目录路径 |
| isFavorite | boolean | - | Yes | Yes | 是否收藏 |

#### 3.1.2 TOON结构嵌套字段

| 字段名 | 类型 | 分词器 | 索引 | 存储 | 描述 |
|--------|------|--------|------|------|------|
| toonStructure.id | keyword | - | Yes | Yes | 节点ID |
| toonStructure.title | text | ik_max_word | Yes | Yes | 节点标题 |
| toonStructure.level | integer | - | Yes | Yes | 层级 |
| toonStructure.content | text | ik_max_word | Yes | No | 节点内容 |
| toonStructure.pageNumber | integer | - | Yes | Yes | 页码 |

### 3.2 分词器配置

#### 3.2.1 中文分词器
- **推荐使用**：IK Analyzer
- **配置方案**：
  - `ik_max_word`：最大粒度分词，适合全文检索
  - `ik_smart`：智能分词，适合搜索建议

#### 3.2.2 英文分词器
- 使用Elasticsearch默认的`standard`分词器

### 3.3 索引模板配置

```json
{
  "index_patterns": ["documents*"],
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1,
    "analysis": {
      "analyzer": {
        "ik_max_word": {
          "type": "ik_max_word"
        },
        "ik_smart": {
          "type": "ik_smart"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "filePath": {
        "type": "keyword"
      },
      "fileName": {
        "type": "text",
        "analyzer": "ik_max_word",
        "fields": {
          "raw": {
            "type": "keyword"
          }
        }
      },
      "fileType": {
        "type": "keyword"
      },
      "content": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "author": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "analyzer": "ik_max_word"
      },
      "size": {
        "type": "long"
      },
      "createdAt": {
        "type": "date"
      },
      "modifiedAt": {
        "type": "date"
      },
      "mimeType": {
        "type": "keyword"
      },
      "pages": {
        "type": "integer"
      },
      "toonStructure": {
        "type": "nested",
        "properties": {
          "id": {
            "type": "keyword"
          },
          "title": {
            "type": "text",
            "analyzer": "ik_max_word"
          },
          "level": {
            "type": "integer"
          },
          "content": {
            "type": "text",
            "analyzer": "ik_max_word"
          },
          "pageNumber": {
            "type": "integer"
          }
        }
      },
      "parentDir": {
        "type": "keyword"
      },
      "isFavorite": {
        "type": "boolean"
      }
    }
  }
}
```

## 4. 数据同步策略

### 4.1 同步方式

**采用应用程序直接同步 + 定时全量同步**的混合策略：

1. **实时同步**：在文档创建、更新、删除时，通过事件机制触发Elasticsearch索引更新
2. **定时全量同步**：每天凌晨执行一次全量同步，确保数据一致性

### 4.2 实时同步实现

#### 4.2.1 事件机制
- 利用Spring Event机制，定义文档变更事件
- 在文档相关操作中发布事件
- 事件监听器处理Elasticsearch索引更新

#### 4.2.2 核心代码示例

```java
// 定义文档变更事件
public class DocumentChangedEvent extends ApplicationEvent {
    private final Document document;
    private final ChangeType changeType; // CREATE, UPDATE, DELETE
    
    // 构造函数和getter方法
}

// 发布事件
@Service
public class DocumentService {
    private final ApplicationEventPublisher eventPublisher;
    
    public void saveDocument(Document document) {
        // 保存到数据库
        documentRepository.save(document);
        // 发布事件
        eventPublisher.publishEvent(new DocumentChangedEvent(this, document, ChangeType.CREATE));
    }
}

// 事件监听器
@Component
public class ElasticsearchSyncListener {
    private final ElasticsearchOperations elasticsearchOperations;
    
    @EventListener
    public void handleDocumentChanged(DocumentChangedEvent event) {
        Document document = event.getDocument();
        ChangeType changeType = event.getChangeType();
        
        switch (changeType) {
            case CREATE:
            case UPDATE:
                elasticsearchOperations.save(document);
                break;
            case DELETE:
                elasticsearchOperations.delete(document);
                break;
        }
    }
}
```

### 4.3 定时全量同步实现

- 使用Spring Scheduling实现定时任务
- 每天凌晨执行一次全量同步
- 同步流程：
  1. 从数据库获取所有文档
  2. 批量更新Elasticsearch索引
  3. 记录同步日志

## 5. 核心检索功能实现方案

### 5.1 基础检索

#### 5.1.1 实现方式
- 使用Spring Data Elasticsearch的`ElasticsearchRepository`或`ElasticsearchOperations`
- 支持简单的全文检索，根据关键词匹配文档内容、文件名、标题等

#### 5.1.2 核心代码示例

```java
@Repository
public interface DocumentRepository extends ElasticsearchRepository<Document, String> {
    // 基础全文检索
    @Query("{
        "multi_match": {
            "query": "?0",
            "fields": ["fileName^3", "title^2", "content", "toonStructure.title", "toonStructure.content"]
        }
    }")
    Page<Document> search(String keyword, Pageable pageable);
}
```

### 5.2 高级检索

#### 5.2.1 实现方式
- 支持多条件组合查询
- 支持字段级精确匹配
- 支持范围查询（如文件大小、创建时间）
- 支持布尔逻辑组合（AND/OR/NOT）

#### 5.2.2 核心代码示例

```java
public Page<Document> advancedSearch(SearchRequest request, Pageable pageable) {
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    
    // 关键词匹配
    if (StringUtils.hasText(request.getKeyword())) {
        boolQuery.must(QueryBuilders.multiMatchQuery(
            request.getKeyword(),
            "fileName^3", "title^2", "content", "toonStructure.title", "toonStructure.content"
        ));
    }
    
    // 文件类型过滤
    if (CollectionUtils.isNotEmpty(request.getFileTypes())) {
        boolQuery.filter(QueryBuilders.termsQuery("fileType", request.getFileTypes()));
    }
    
    // 文件大小范围
    if (request.getMinSize() != null) {
        boolQuery.filter(QueryBuilders.rangeQuery("size").gte(request.getMinSize()));
    }
    if (request.getMaxSize() != null) {
        boolQuery.filter(QueryBuilders.rangeQuery("size").lte(request.getMaxSize()));
    }
    
    // 创建时间范围
    if (request.getStartDate() != null) {
        boolQuery.filter(QueryBuilders.rangeQuery("createdAt").gte(request.getStartDate()));
    }
    if (request.getEndDate() != null) {
        boolQuery.filter(QueryBuilders.rangeQuery("createdAt").lte(request.getEndDate()));
    }
    
    // 执行查询
    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(boolQuery)
        .withPageable(pageable)
        .build();
    
    return elasticsearchOperations.search(searchQuery, Document.class, IndexCoordinates.of("documents")).map(SearchHit::getContent);
}
```

### 5.3 过滤与排序

#### 5.3.1 过滤功能
- 支持按文件类型过滤
- 支持按目录路径过滤
- 支持按创建时间范围过滤
- 支持按文件大小范围过滤

#### 5.3.2 排序功能
- 支持按相关性排序（默认）
- 支持按创建时间排序
- 支持按修改时间排序
- 支持按文件名排序
- 支持按文件大小排序

### 5.4 结果高亮

#### 5.4.1 实现方式
- 使用Elasticsearch的高亮功能
- 对匹配的关键词进行高亮显示
- 支持自定义高亮样式

#### 5.4.2 核心代码示例

```java
public Page<SearchResult> searchWithHighlight(String keyword, Pageable pageable) {
    HighlightBuilder highlightBuilder = new HighlightBuilder()
        .field("fileName")
        .field("title")
        .field("content")
        .field("toonStructure.title")
        .field("toonStructure.content")
        .preTags("<em class='highlight'>")
        .postTags("</em>")
        .fragmentSize(150)
        .numOfFragments(3);
    
    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withQuery(QueryBuilders.multiMatchQuery(
            keyword,
            "fileName^3", "title^2", "content", "toonStructure.title", "toonStructure.content"
        ))
        .withHighlightBuilder(highlightBuilder)
        .withPageable(pageable)
        .build();
    
    SearchHits<Document> searchHits = elasticsearchOperations.search(searchQuery, Document.class, IndexCoordinates.of("documents"));
    
    // 处理高亮结果
    List<SearchResult> results = new ArrayList<>();
    for (SearchHit<Document> hit : searchHits) {
        Document document = hit.getContent();
        Map<String, List<String>> highlights = hit.getHighlightFields();
        
        // 构建SearchResult对象，包含高亮信息
        SearchResult result = new SearchResult();
        result.setDocument(document);
        result.setHighlights(highlights);
        results.add(result);
    }
    
    return new PageImpl<>(results, pageable, searchHits.getTotalHits());
}
```

## 6. 检索性能优化措施

### 6.1 索引优化

1. **合理设置分片和副本**：
   - 分片数：根据数据量和节点数设置，一般每个分片大小控制在10-50GB
   - 副本数：根据高可用性要求设置，生产环境建议至少1个副本

2. **字段权重优化**：
   - 对重要字段设置更高的权重（如文件名^3，标题^2）
   - 合理选择索引字段，避免不必要的字段索引

3. **使用合适的分词器**：
   - 中文使用IK Analyzer，英文使用standard分词器
   - 根据检索场景选择合适的分词模式（ik_max_word或ik_smart）

### 6.2 查询优化

1. **使用过滤查询**：
   - 对不需要评分的条件使用filter查询，利用缓存提高性能
   - 避免在查询中使用复杂的脚本

2. **分页优化**：
   - 使用`search_after`代替`from/size`进行深分页
   - 限制最大返回结果数

3. **避免全表扫描**：
   - 确保查询条件包含索引字段
   - 合理使用复合查询

### 6.3 缓存策略

1. **Elasticsearch缓存**：
   - 利用Elasticsearch的查询缓存和过滤缓存
   - 合理设置缓存失效时间

2. **应用层缓存**：
   - 使用Caffeine缓存热门查询结果
   - 缓存搜索建议和拼写纠错结果

### 6.4 硬件优化

1. **存储选择**：
   - 使用SSD存储提高I/O性能
   - 确保足够的磁盘空间

2. **内存配置**：
   - 为Elasticsearch分配足够的堆内存（建议不超过物理内存的50%，且不超过32GB）
   - 确保系统有足够的可用内存

3. **CPU配置**：
   - 选择多核CPU，提高并发处理能力

## 7. 用户检索交互体验增强功能

### 7.1 搜索建议

#### 7.1.1 实现方式
- 使用Elasticsearch的Completion Suggester
- 基于文件名、标题和常用搜索词生成建议
- 支持前缀匹配和模糊匹配

#### 7.1.2 核心代码示例

```java
public List<String> getSearchSuggestions(String prefix) {
    CompletionSuggestionBuilder suggestionBuilder = SuggestBuilders.completionSuggestion("suggest")
        .prefix(prefix)
        .skipDuplicates(true)
        .size(10);
    
    SuggestBuilder suggestBuilder = new SuggestBuilder();
    suggestBuilder.addSuggestion("search_suggestions", suggestionBuilder);
    
    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withSuggestBuilder(suggestBuilder)
        .build();
    
    SearchHits<Document> searchHits = elasticsearchOperations.search(searchQuery, Document.class, IndexCoordinates.of("documents"));
    
    // 处理搜索建议结果
    List<String> suggestions = new ArrayList<>();
    Suggest suggest = searchHits.getSuggest();
    if (suggest != null) {
        CompletionSuggestion completionSuggestion = suggest.getSuggestion("search_suggestions");
        for (CompletionSuggestion.Entry entry : completionSuggestion.getEntries()) {
            for (CompletionSuggestion.Entry.Option option : entry.getOptions()) {
                suggestions.add(option.getText().string());
            }
        }
    }
    
    return suggestions;
}
```

### 7.2 拼写纠错

#### 7.2.1 实现方式
- 使用Elasticsearch的Term Suggester或Phrase Suggester
- 基于索引中的词项生成拼写建议
- 支持单个词和短语的拼写纠错

#### 7.2.2 核心代码示例

```java
public String getSpellingSuggestion(String text) {
    TermSuggestionBuilder suggestionBuilder = SuggestBuilders.termSuggestion("content")
        .text(text)
        .suggestMode(SuggestMode.POPULAR)
        .size(1);
    
    SuggestBuilder suggestBuilder = new SuggestBuilder();
    suggestBuilder.addSuggestion("spelling_suggestions", suggestionBuilder);
    
    NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
        .withSuggestBuilder(suggestBuilder)
        .build();
    
    SearchHits<Document> searchHits = elasticsearchOperations.search(searchQuery, Document.class, IndexCoordinates.of("documents"));
    
    // 处理拼写纠错结果
    Suggest suggest = searchHits.getSuggest();
    if (suggest != null) {
        TermSuggestion termSuggestion = suggest.getSuggestion("spelling_suggestions");
        for (TermSuggestion.Entry entry : termSuggestion.getEntries()) {
            if (!entry.getOptions().isEmpty()) {
                TermSuggestion.Entry.Option option = entry.getOptions().get(0);
                return option.getText().string();
            }
        }
    }
    
    return text;
}
```

### 7.3 结果高亮显示

- 在搜索结果中高亮显示匹配的关键词
- 使用不同的颜色或样式突出显示
- 显示匹配的上下文片段

### 7.4 搜索历史记录

- 记录用户的搜索历史
- 支持快速重新执行之前的搜索
- 支持清除搜索历史

## 8. 测试方案

### 8.1 功能测试

#### 8.1.1 测试目标
- 验证检索功能的正确性
- 验证检索结果的相关性
- 验证过滤和排序功能
- 验证高亮显示功能

#### 8.1.2 测试用例

| 测试用例 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 基础检索 | 1. 输入关键词"测试"
2. 执行搜索 | 返回包含"测试"关键词的文档，文件名和标题匹配的文档排在前面 |
| 高级检索 | 1. 输入关键词"测试"
2. 选择文件类型"PDF"
3. 设置文件大小范围1MB-10MB
4. 执行搜索 | 返回包含"测试"关键词、文件类型为PDF且大小在1MB-10MB之间的文档 |
| 过滤功能 | 1. 选择文件类型"DOCX"
2. 执行搜索 | 返回所有DOCX类型的文档 |
| 排序功能 | 1. 执行搜索
2. 按创建时间降序排序 | 返回的文档按创建时间从新到旧排序 |
| 高亮显示 | 1. 输入关键词"测试"
2. 执行搜索 | 返回结果中"测试"关键词被高亮显示 |
| 搜索建议 | 1. 输入"测"
2. 查看搜索建议 | 显示包含"测"前缀的搜索建议，如"测试"、"测量"等 |
| 拼写纠错 | 1. 输入"测式"
2. 查看拼写建议 | 显示拼写建议"测试" |

### 8.2 性能测试

#### 8.2.1 测试目标
- 验证检索响应时间
- 验证索引更新性能
- 验证系统在不同数据量下的性能表现

#### 8.2.2 测试指标
- 平均响应时间：< 200ms
- 95%响应时间：< 500ms
- 99%响应时间：< 1000ms
- 索引更新时间：< 1s

#### 8.2.3 测试工具
- JMeter：用于模拟并发请求
- Elasticsearch Performance Analyzer：用于分析Elasticsearch性能

#### 8.2.4 测试场景

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 单用户检索 | 1. 模拟单个用户执行搜索
2. 记录响应时间 | 响应时间 < 200ms |
| 并发检索 | 1. 模拟100个并发用户执行搜索
2. 记录响应时间 | 平均响应时间 < 200ms，95%响应时间 < 500ms |
| 索引更新 | 1. 批量导入1000份文档
2. 记录索引更新时间 | 索引更新时间 < 1s |
| 大数据量检索 | 1. 导入10万份文档
2. 执行搜索
3. 记录响应时间 | 响应时间 < 500ms |

### 8.3 压力测试

#### 8.3.1 测试目标
- 验证系统在高并发场景下的稳定性
- 验证系统的最大承载能力
- 验证系统在压力下的错误率

#### 8.3.2 测试指标
- 最大并发用户数：> 500
- 错误率：< 0.1%
- 系统稳定性：持续运行24小时无崩溃

#### 8.3.3 测试工具
- JMeter：用于模拟高并发请求
- Grafana + Prometheus：用于监控系统指标

#### 8.3.4 测试场景

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 逐步增加并发 | 1. 从100并发开始，每5分钟增加100并发
2. 持续增加到系统瓶颈
3. 记录最大并发数和错误率 | 最大并发数 > 500，错误率 < 0.1% |
| 长时间压力测试 | 1. 设置300并发用户
2. 持续运行24小时
3. 记录系统稳定性和错误率 | 系统稳定运行，错误率 < 0.1% |
| 突发流量测试 | 1. 突然增加并发数到500
2. 持续10分钟
3. 记录系统响应和错误率 | 系统能够处理突发流量，错误率 < 0.5% |

## 9. 实施步骤建议

### 9.1 第一阶段：环境搭建和基础配置

1. **安装Elasticsearch**：
   - 安装Elasticsearch 8.x
   - 配置集群名称、节点名称、网络设置等
   - 安装IK Analyzer插件

2. **集成Spring Data Elasticsearch**：
   - 添加Spring Data Elasticsearch依赖
   - 配置Elasticsearch连接信息
   - 创建索引模板

3. **设计索引结构**：
   - 定义文档实体类
   - 配置字段映射和分词器

### 9.2 第二阶段：数据同步实现

1. **实现实时同步**：
   - 定义文档变更事件
   - 实现事件发布和监听机制
   - 实现Elasticsearch索引更新逻辑

2. **实现定时全量同步**：
   - 创建定时任务
   - 实现全量同步逻辑
   - 记录同步日志

3. **测试数据同步**：
   - 验证文档创建、更新、删除时索引同步情况
   - 验证定时全量同步效果

### 9.3 第三阶段：检索功能实现

1. **实现基础检索**：
   - 实现简单的全文检索功能
   - 测试检索结果的相关性

2. **实现高级检索**：
   - 实现多条件组合查询
   - 实现过滤和排序功能
   - 实现结果高亮显示

3. **实现用户体验增强功能**：
   - 实现搜索建议功能
   - 实现拼写纠错功能
   - 实现搜索历史记录功能

### 9.4 第四阶段：性能优化和测试

1. **性能优化**：
   - 优化索引结构
   - 优化查询语句
   - 配置缓存策略

2. **功能测试**：
   - 执行功能测试用例
   - 修复发现的问题

3. **性能测试**：
   - 执行性能测试用例
   - 分析测试结果
   - 进一步优化性能

4. **压力测试**：
   - 执行压力测试用例
   - 验证系统稳定性
   - 记录系统最大承载能力

### 9.5 第五阶段：部署和监控

1. **部署到生产环境**：
   - 配置生产环境Elasticsearch集群
   - 部署应用程序
   - 配置监控和日志

2. **设置监控**：
   - 使用Elasticsearch Monitoring监控集群状态
   - 使用Grafana + Prometheus监控系统指标
   - 设置告警规则

3. **持续优化**：
   - 定期分析检索日志和性能指标
   - 根据实际使用情况优化索引结构和查询语句
   - 持续改进用户体验

## 10. 结论

本技术方案基于Elasticsearch实现了Nexus-Lite知识预览系统的全文检索功能，包括：

1. **强大的检索能力**：支持基础检索、高级检索、过滤、排序和高亮显示
2. **良好的用户体验**：提供搜索建议、拼写纠错等增强功能
3. **高性能**：通过索引优化、查询优化和缓存策略确保毫秒级响应
4. **高可用性**：支持分布式部署和数据备份
5. **易于扩展**：可根据数据量和用户量横向扩展

该方案与项目现有技术栈兼容，实施步骤清晰，测试方案完整，能够满足系统的全文检索需求，并为未来的功能扩展和性能优化提供了基础。