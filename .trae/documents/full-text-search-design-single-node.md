# Nexus-Lite 全文检索功能技术方案（单节点部署）

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
- **部署模式**：单节点部署，资源有限

## 2. 检索引擎选型

### 2.1 可选方案对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| Elasticsearch | 功能强大，中文分词成熟，与Spring Boot集成良好，适合单节点部署 | 资源消耗较大，需要优化配置 |
| Solr | 成熟稳定，管理界面友好，支持单节点部署 | 功能相对有限，社区活跃度不如Elasticsearch |
| 数据库内置全文检索 | 部署简单，无需额外服务 | 性能有限，不适合大规模数据和复杂查询 |
| Lucene | 轻量级，可嵌入应用，资源消耗小 | 开发成本高，需自行封装大量功能 |

### 2.2 选型建议
**推荐使用：Elasticsearch 8.x（单节点模式）**

### 2.3 选型理由
1. **功能完整性**：提供完整的全文检索功能，支持复杂查询、过滤、排序、高亮等
2. **中文支持**：成熟的中文分词器（如IK Analyzer、HanLP），适合中文文档检索
3. **单节点支持**：支持单节点部署，无需复杂的集群配置
4. **Spring集成**：Spring Data Elasticsearch提供了良好的集成支持
5. **社区活跃度**：拥有庞大的社区和丰富的文档资源
6. **性能优异**：针对检索场景优化，毫秒级响应
7. **近实时性**：文档更新后秒级可检索
8. **可扩展性**：未来可轻松扩展为集群模式

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

### 3.3 单节点索引模板配置

```json
{
  "index_patterns": ["documents*"],
  "settings": {
    "number_of_shards": 1,  # 单节点模式下仅需1个分片
    "number_of_replicas": 0,  # 单节点模式下无需副本
    "analysis": {
      "analyzer": {
        "ik_max_word": {
          "type": "ik_max_word"
        },
        "ik_smart": {
          "type": "ik_smart"
        }
      }
    },
    "index": {
      "refresh_interval": "1s",  # 近实时检索
      "number_of_routing_shards": 1,
      "store": {
        "type": "fs"  # 文件系统存储
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

## 4. Elasticsearch 单节点配置优化

### 4.1 核心配置（elasticsearch.yml）

```yaml
# 集群配置（单节点模式）
cluster.name: nexus-lite-es
node.name: nexus-lite-node-1
cluster.initial_master_nodes: ["nexus-lite-node-1"]
discovery.type: single-node  # 单节点模式

# 网络配置
network.host: 127.0.0.1  # 仅监听本地地址
http.port: 9200

# 内存配置
bootstrap.memory_lock: true  # 锁定内存，防止swap

# 存储配置
path.data: /var/lib/elasticsearch  # 数据存储路径
path.logs: /var/log/elasticsearch  # 日志路径

# 安全配置
xpack.security.enabled: false  # 单节点模式下禁用安全功能
xpack.monitoring.enabled: false  # 禁用监控
xpack.watcher.enabled: false  # 禁用watcher
xpack.ml.enabled: false  # 禁用机器学习

# 索引配置
index.max_result_window: 10000  # 最大结果窗口
index.mapping.total_fields.limit: 1000  # 最大字段数

# 线程池配置
thread_pool.search.size: 4  # 搜索线程池大小（根据CPU核心数调整）
thread_pool.search.queue_size: 100  # 搜索队列大小
thread_pool.write.size: 2  # 写入线程池大小
thread_pool.write.queue_size: 50  # 写入队列大小
```

### 4.2 JVM 配置（jvm.options）

```
# 堆内存配置（单节点模式下建议为物理内存的1/4，最大不超过8GB）
-Xms2g
-Xmx2g

# GC配置
-XX:+UseG1GC
-XX:MaxGCPauseMillis=200
-XX:+ParallelRefProcEnabled
-XX:+DisableExplicitGC

# 内存分配
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=/var/lib/elasticsearch/heapdump.hprof
```

## 5. 数据同步策略

### 5.1 同步方式

**采用应用程序直接同步 + 定时全量同步**的混合策略：

1. **实时同步**：在文档创建、更新、删除时，通过事件机制触发Elasticsearch索引更新
2. **定时全量同步**：每天凌晨执行一次全量同步，确保数据一致性

### 5.2 实时同步实现

#### 5.2.1 事件机制
- 利用Spring Event机制，定义文档变更事件
- 在文档相关操作中发布事件
- 事件监听器处理Elasticsearch索引更新

#### 5.2.2 核心代码示例

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

### 5.3 定时全量同步实现

- 使用Spring Scheduling实现定时任务
- 每天凌晨执行一次全量同步
- 同步流程：
  1. 从数据库获取所有文档
  2. 批量更新Elasticsearch索引（使用Bulk API提高性能）
  3. 记录同步日志

### 5.4 单节点优化

1. **批量处理**：使用Bulk API批量更新索引，减少网络请求
2. **异步处理**：使用CompletableFuture异步处理索引更新，提高系统响应速度
3. **限流控制**：对索引更新进行限流，避免单节点负载过高

## 6. 核心检索功能实现方案

### 6.1 基础检索

#### 6.1.1 实现方式
- 使用Spring Data Elasticsearch的`ElasticsearchRepository`或`ElasticsearchOperations`
- 支持简单的全文检索，根据关键词匹配文档内容、文件名、标题等

#### 6.1.2 核心代码示例

```java
@Repository
public interface DocumentRepository extends ElasticsearchRepository<Document, String> {
    // 基础全文检索
    @Query("{
        \"multi_match\": {
            \"query\": \"?0\",
            \"fields\": [\"fileName^3\", \"title^2\", \"content\", \"toonStructure.title\", \"toonStructure.content\"]
        }
    }")
    Page<Document> search(String keyword, Pageable pageable);
}
```

### 6.2 高级检索

#### 6.2.1 实现方式
- 支持多条件组合查询
- 支持字段级精确匹配
- 支持范围查询（如文件大小、创建时间）
- 支持布尔逻辑组合（AND/OR/NOT）

#### 6.2.2 核心代码示例

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

### 6.3 过滤与排序

#### 6.3.1 过滤功能
- 支持按文件类型过滤
- 支持按目录路径过滤
- 支持按创建时间范围过滤
- 支持按文件大小范围过滤

#### 6.3.2 排序功能
- 支持按相关性排序（默认）
- 支持按创建时间排序
- 支持按修改时间排序
- 支持按文件名排序
- 支持按文件大小排序

### 6.4 结果高亮

#### 6.4.1 实现方式
- 使用Elasticsearch的高亮功能
- 对匹配的关键词进行高亮显示
- 支持自定义高亮样式

#### 6.4.2 核心代码示例

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
        .fragmentSize(150)  // 片段大小，减少内存占用
        .numOfFragments(3);  // 片段数量，减少内存占用
    
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

### 6.5 单节点优化

1. **查询优化**：
   - 减少返回字段数量，只返回必要字段
   - 限制高亮片段数量和大小
   - 使用filter查询替代must查询，利用缓存

2. **分页优化**：
   - 限制最大分页大小
   - 对深分页使用`search_after`替代`from/size`

3. **结果集优化**：
   - 限制单次查询返回的最大结果数
   - 使用scroll API处理大量结果集

## 7. 检索性能优化措施

### 7.1 索引优化

1. **合理设置分片和副本**：
   - 分片数：单节点模式下仅需1个分片
   - 副本数：单节点模式下无需副本

2. **字段优化**：
   - 对重要字段设置更高的权重（如文件名^3，标题^2）
   - 合理选择索引字段，避免不必要的字段索引
   - 对大字段使用`store: false`，减少索引大小

3. **分词器优化**：
   - 中文使用IK Analyzer，英文使用standard分词器
   - 根据检索场景选择合适的分词模式（ik_max_word或ik_smart）
   - 定期更新分词器词典

### 7.2 查询优化

1. **使用过滤查询**：
   - 对不需要评分的条件使用filter查询，利用缓存提高性能
   - 避免在查询中使用复杂的脚本

2. **分页优化**：
   - 使用`search_after`代替`from/size`进行深分页
   - 限制最大返回结果数为10000

3. **避免全表扫描**：
   - 确保查询条件包含索引字段
   - 合理使用复合查询

4. **减少返回字段**：
   - 使用`_source`过滤，只返回必要字段
   - 避免使用`*`通配符查询

### 7.3 缓存策略

1. **Elasticsearch缓存**：
   - 利用Elasticsearch的查询缓存和过滤缓存
   - 合理设置缓存失效时间

2. **应用层缓存**：
   - 使用Caffeine缓存热门查询结果
   - 缓存搜索建议和拼写纠错结果
   - 缓存频繁访问的文档元数据

### 7.4 硬件优化

1. **存储选择**：
   - 使用SSD存储提高I/O性能
   - 确保足够的磁盘空间

2. **内存配置**：
   - 为Elasticsearch分配足够的堆内存（建议为物理内存的1/4，最大不超过8GB）
   - 确保系统有足够的可用内存

3. **CPU配置**：
   - 选择多核CPU，提高并发处理能力
   - 根据CPU核心数调整线程池大小

### 7.5 JVM优化

1. **堆内存配置**：
   - 设置合理的堆内存大小（Xms=Xmx）
   - 避免堆内存过大导致GC时间过长

2. **GC策略**：
   - 使用G1GC收集器
   - 调整MaxGCPauseMillis参数
   - 启用ParallelRefProcEnabled

## 8. 用户检索交互体验增强功能

### 8.1 搜索建议

#### 8.1.1 实现方式
- 使用Elasticsearch的Completion Suggester
- 基于文件名、标题和常用搜索词生成建议
- 支持前缀匹配

#### 8.1.2 单节点优化
- 限制建议数量（默认10条）
- 缓存建议结果，减少Elasticsearch查询

### 8.2 拼写纠错

#### 8.2.1 实现方式
- 使用Elasticsearch的Term Suggester
- 基于索引中的词项生成拼写建议
- 支持单个词的拼写纠错

#### 8.2.2 单节点优化
- 限制建议数量（默认1条）
- 缓存拼写纠错结果

### 8.3 结果高亮显示

- 在搜索结果中高亮显示匹配的关键词
- 使用不同的颜色或样式突出显示
- 显示匹配的上下文片段（限制片段数量和大小）

### 8.4 搜索历史记录

- 记录用户的搜索历史到本地存储
- 支持快速重新执行之前的搜索
- 支持清除搜索历史

## 9. 安全加固措施

### 9.1 Elasticsearch安全配置

1. **网络安全**：
   - 仅监听本地地址（127.0.0.1），避免外部访问
   - 关闭不必要的端口
   - 使用防火墙限制访问

2. **认证授权**：
   - 单节点模式下可禁用xpack.security，但生产环境建议启用
   - 设置强密码策略
   - 限制API访问权限

3. **数据安全**：
   - 定期备份Elasticsearch数据目录
   - 启用审计日志（生产环境）
   - 加密敏感数据

### 9.2 应用程序安全

1. **输入验证**：
   - 对所有输入参数进行验证
   - 防止SQL注入和XSS攻击
   - 验证文件路径，防止路径遍历攻击

2. **访问控制**：
   - 实现API访问控制
   - 限制请求频率，防止DDoS攻击
   - 记录API访问日志

3. **安全编码**：
   - 使用参数化查询
   - 避免硬编码敏感信息
   - 定期更新依赖库，修复安全漏洞

## 10. 测试方案

### 10.1 功能测试

#### 10.1.1 测试目标
- 验证检索功能的正确性
- 验证检索结果的相关性
- 验证过滤和排序功能
- 验证高亮显示功能

#### 10.1.2 测试用例

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

### 10.2 性能测试

#### 10.2.1 测试目标
- 验证检索响应时间
- 验证索引更新性能
- 验证系统在不同数据量下的性能表现

#### 10.2.2 测试指标
- 平均响应时间：< 200ms
- 95%响应时间：< 500ms
- 99%响应时间：< 1000ms
- 索引更新时间：< 1s

#### 10.2.3 测试工具
- JMeter：用于模拟并发请求
- Elasticsearch Performance Analyzer：用于分析Elasticsearch性能
- VisualVM：用于监控JVM性能

#### 10.2.4 测试场景

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 单用户检索 | 1. 模拟单个用户执行搜索
2. 记录响应时间 | 响应时间 < 200ms |
| 并发检索 | 1. 模拟50个并发用户执行搜索
2. 记录响应时间 | 平均响应时间 < 200ms，95%响应时间 < 500ms |
| 索引更新 | 1. 批量导入1000份文档
2. 记录索引更新时间 | 索引更新时间 < 1s |
| 大数据量检索 | 1. 导入10万份文档
2. 执行搜索
3. 记录响应时间 | 响应时间 < 500ms |

### 10.3 压力测试

#### 10.3.1 测试目标
- 验证系统在高并发场景下的稳定性
- 验证系统的最大承载能力
- 验证系统在压力下的错误率

#### 10.3.2 测试指标
- 最大并发用户数：> 200
- 错误率：< 0.1%
- 系统稳定性：持续运行12小时无崩溃

#### 10.3.3 测试工具
- JMeter：用于模拟高并发请求
- Grafana + Prometheus：用于监控系统指标
- Elasticsearch Monitoring：用于监控Elasticsearch状态

#### 10.3.4 测试场景

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 逐步增加并发 | 1. 从50并发开始，每5分钟增加50并发
2. 持续增加到系统瓶颈
3. 记录最大并发数和错误率 | 最大并发数 > 200，错误率 < 0.1% |
| 长时间压力测试 | 1. 设置150并发用户
2. 持续运行12小时
3. 记录系统稳定性和错误率 | 系统稳定运行，错误率 < 0.1% |
| 突发流量测试 | 1. 突然增加并发数到200
2. 持续10分钟
3. 记录系统响应和错误率 | 系统能够处理突发流量，错误率 < 0.5% |

### 10.4 单节点特定测试

#### 10.4.1 测试目标
- 验证单节点模式下的性能表现
- 验证单节点模式下的数据一致性
- 验证单节点模式下的故障恢复能力

#### 10.4.2 测试场景

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 单节点重启测试 | 1. 运行系统一段时间
2. 重启Elasticsearch服务
3. 验证数据完整性和服务可用性 | 数据完整，服务正常恢复 |
| 磁盘空间不足测试 | 1. 模拟磁盘空间不足场景
2. 执行索引更新操作
3. 验证系统处理方式 | 系统能够优雅处理，不崩溃 |
| 内存压力测试 | 1. 模拟内存不足场景
2. 执行检索操作
3. 验证系统处理方式 | 系统能够优雅处理，不崩溃 |

## 11. 实施步骤建议

### 11.1 第一阶段：环境搭建和基础配置（已部分落地）

1. **安装Elasticsearch**：
   - 安装Elasticsearch 8.x
   - 配置单节点模式
   - 安装IK Analyzer插件
   - 优化核心配置和JVM配置

2. **集成Spring Data Elasticsearch**：
   - 添加Spring Data Elasticsearch依赖
   - 配置Elasticsearch连接信息
   - 创建索引模板

3. **设计索引结构**：
   - 定义文档实体类
   - 配置字段映射和分词器

### 11.2 第二阶段：数据同步实现（已部分落地）

1. **实现实时同步**：
   - 定义文档变更事件
   - 实现事件发布和监听机制
   - 实现Elasticsearch索引更新逻辑
   - 测试实时同步功能

2. **实现定时全量同步**：
   - 创建定时任务
   - 实现全量同步逻辑（使用Bulk API）
   - 记录同步日志
   - 测试定时全量同步功能

### 11.3 第三阶段：检索功能实现（阶段1增量实现）

已在当前项目中完成如下阶段1增量能力，以本地文件为数据源，无需外部服务即可获得基础全文检索体验：

1. 扫描阶段产出轻量全文元数据
   - 新增 `search.meta.json`（路径：`./data/search.meta.json`），随扫描生成，包含：
     - `filePath`、`fileName`、`parentDir`、`fileType`、`title`、`contentText`（截断上限 20k 字符）、`size`、`modifiedAt`
   - 文本抽取策略：
     - `MD/CSV`：直接读取文本
     - `HTML/HTM`：用 Jsoup 提取 `title` 与纯文本
     - `PDF`：用 PDFBox 轻量抽取前 5 页文本
     - `DOCX`：用 Apache POI 提取前 200 段落文本

2. 后端检索接口
   - 基础检索：`GET /api/v1/search/basic?q=关键词&limit=50`
     - 文件名/标题/内容片段命中；返回 `SearchResult` 列表（`filePath`、`fileName`、`parentDir`、`fileType`、`score`、`snippet`）
     - 支持空查询返回索引前 N 条作为默认建议
-   高级检索：`POST /api/v1/search/advanced?limit=50`（已实现接口与服务，前端已接入筛选与排序）
     - 请求体 `SearchRequest`：`keyword`、`fileTypes`、`parentDir`、`minSize/maxSize`、`startDate/endDate`、`sort`、`order`
     - 基于 `search.meta.json` 的本地过滤与相关性排序

3. 前端 UI 升级
-   侧栏搜索框与结果列表：回车或点击搜索触发，显示命中片段与得分，点击打开右侧预览
-   新增文件类型多选筛选（MD/DOCX/PDF/CSV/HTML/XLSX）与排序（相关性、名称，升/降序）并调用高级检索接口
-   当无结果或清空关键字时回退到文件树视图

4. 编码与兼容
   - 统一用 `encodeURIComponent` 传递中文关键字，后端默认接收 UTF-8
   - 保持与现有扫描/预览能力兼容，不影响已有接口

5. 验证示例
   - `curl 'http://localhost:8080/api/v1/search/basic?q=%E6%B5%81%E7%A8%8B&limit=10'`
   - `curl -X POST 'http://localhost:8080/api/v1/search/advanced?limit=10' -H 'Content-Type: application/json' -d '{"keyword":"流程","fileTypes":["DOCX","MD"],"sort":"relevance","order":"desc"}'`

后续阶段将把上述本地能力迁移到 Elasticsearch 单节点，并启用高亮和建议，继续沿用现有的接口形态以降低迁移成本。

### 11.3.1 阶段2脚手架与开关（新增）

1. 依赖与配置
   - 依赖：`spring-boot-starter-data-elasticsearch`
   - 配置：`spring.elasticsearch.uris: http://localhost:9200`
   - 开关：`app.search.use-es: false`（默认），置为 `true` 后使用 ES 路径

2. ES 实体与仓库
   - `EsDocument`（indexName=`documents`）：`filePath`、`fileName`、`parentDir`、`fileType`、`title`、`content`、`size`、`modifiedAt`
   - `EsDocumentRepository`：Spring Data 仓库

3. 检索服务与控制器切换
   - `EsSearchService`：根据 `SearchRequest` 构建 `bool` 查询（关键词 multi_match、类型 terms、目录 prefix、大小/时间 range），并返回高亮片段（fileName/title/content，片段大小150，最多3段）
   - 控制器：`POST /api/v1/search/advanced` 根据开关路由到 `EsSearchService` 或本地 `AdvancedSearchService`

4. 重建索引接口
   - `POST /api/v1/search/reindex?clear=true|false`：从 `./data/search.meta.json` 批量导入到 `documents` 索引，`clear=true` 先清空索引

5. 启动与验证
   - 启动 ES 单节点并安装 IK 分词器（后续子章节补充）
   - 设置 `app.search.use-es: true`
   - 执行 `POST /api/v1/search/reindex?clear=true` 或 `PUT /api/v1/search/admin/init?clear=true` 完成首初始化或重建
   - 使用前端筛选与高级检索验证 ES 路径（前端已安全渲染高亮片段）

6. 高亮片段前端渲染
   - 前端结果列表使用 `DOMPurify` 对 ES 返回的高亮 HTML 进行清洗后再注入，避免 XSS 风险
   - 片段样式采用 `<em class='highlight'>...</em>`，可在主题 CSS 中统一样式

7. 索引模板与IK分词器（待补充）
   - 在 `documents` 模板中为 `fileName/title/content` 设置 `ik_max_word` 分词器，并保留 `fileName.raw` 关键字子字段
   - 单节点下 `number_of_shards: 1`、`number_of_replicas: 0`，`refresh_interval: 1s`
   - 导入模板后再执行重建索引，确保正确分词与检索效果

1. **实现基础检索**：
   - 实现简单的全文检索功能
   - 测试检索结果的相关性

2. **实现高级检索**：
   - 实现多条件组合查询
   - 实现过滤和排序功能
   - 实现结果高亮显示
   - 测试高级检索功能

3. **实现用户体验增强功能**：
   - 实现搜索建议功能
   - 实现拼写纠错功能
   - 实现搜索历史记录功能
   - 测试用户体验功能

### 11.4 第四阶段：性能优化和安全加固

1. **性能优化**：
   - 优化索引结构
   - 优化查询语句
   - 配置缓存策略
   - 进行性能测试和优化

2. **安全加固**：
   - 配置Elasticsearch安全设置
   - 实现应用程序安全措施
   - 进行安全测试

### 11.5 第五阶段：测试和部署

1. **功能测试**：
   - 执行功能测试用例
   - 修复发现的问题

2. **性能测试**：
   - 执行性能测试用例
   - 分析测试结果
   - 进一步优化性能

3. **压力测试**：
   - 执行压力测试用例
   - 验证系统稳定性
   - 记录系统最大承载能力

4. **部署到生产环境**：
   - 配置生产环境Elasticsearch（单节点）
   - 部署应用程序
   - 配置监控和日志
   - 设置定期备份策略

### 11.6 第六阶段：监控和维护

1. **设置监控**：
   - 使用Elasticsearch Monitoring监控集群状态
   - 使用Grafana + Prometheus监控系统指标
   - 设置告警规则

2. **持续优化**：
   - 定期分析检索日志和性能指标
   - 根据实际使用情况优化索引结构和查询语句
   - 持续改进用户体验

3. **定期维护**：
   - 定期备份Elasticsearch数据
   - 定期更新Elasticsearch和依赖库
   - 定期清理过期索引和数据

## 12. 结论

本技术方案基于Elasticsearch单节点模式实现了Nexus-Lite知识预览系统的全文检索功能，针对单节点部署场景进行了全面优化：

1. **单节点配置优化**：调整了Elasticsearch核心配置和JVM配置，适应单节点环境
2. **资源分配优化**：合理配置内存、CPU和存储资源，确保单节点性能最大化
3. **功能模块调整**：移除了依赖集群环境的功能模块，保留核心检索功能
4. **性能优化**：通过索引优化、查询优化和缓存策略，提高单节点检索性能
5. **安全加固**：实施了网络安全、认证授权和数据安全措施，确保单节点部署的安全性
6. **完整的测试方案**：包括功能测试、性能测试、压力测试和单节点特定测试，验证单节点部署的稳定性和可靠性

该方案与项目现有技术栈兼容，实施步骤清晰，能够满足系统的全文检索需求，并为未来的功能扩展和性能优化提供了基础。单节点部署模式具有部署简单、资源消耗小、维护成本低等优点，适合中小型应用场景。
