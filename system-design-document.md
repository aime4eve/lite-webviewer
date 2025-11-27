# Nexus-Lite 知识预览系统设计文档

## 1. 项目概述

### 1.1 项目背景
Nexus-Lite 是一个轻量级的知识预览系统，旨在为用户提供高效、便捷的文档预览服务。系统能够自动扫描指定目录下的文档，生成结构化索引，并支持多种文件格式的在线预览，帮助用户快速获取文档内容，提高知识管理效率。

### 1.2 业务需求
- 自动扫描指定目录下的文档，生成文件索引
- 支持多种文件格式的在线预览（PDF、DOCX、Markdown、CSV、HTML、SVG、XLSX等）
- 生成文档的结构化内容（TOON结构）
- 提供高效的缓存机制，提高预览性能
- 支持定期扫描和手动触发扫描
- 提供RESTful API接口，方便集成到其他系统

### 1.3 系统目标
- 高可用性：确保系统稳定运行，提供可靠的预览服务
- 高性能：快速生成预览内容，响应迅速
- 可扩展性：支持添加新的文件格式预览
- 易用性：提供简洁的API接口，方便集成
- 安全性：确保文件访问安全，防止恶意访问

## 2. 系统架构

### 2.1 整体架构
Nexus-Lite 采用分层架构设计，基于Spring Boot框架开发，主要分为以下几层：

```
┌─────────────────────────────────────────────────────────────────┐
│                       表现层 (Web Layer)                       │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                     RESTful API                          │  │
│  │  - DocumentController                                     │  │
│  │  - FileController                                         │  │
│  │  - IndexController                                        │  │
│  └───────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                       应用层 (Application Layer)               │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                      服务层                               │  │
│  │  - PreviewService                                         │  │
│  │  - FileScanService                                        │  │
│  │  - TOONGeneratorService                                   │  │
│  └───────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                       领域层 (Domain Layer)                    │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                     领域模型                               │  │
│  │  - Document                                               │  │
│  │  - FileMetadata                                            │  │
│  │  - TOONStructure                                           │  │
│  │  - FilesIndex                                              │  │
│  │  - DirectoryTree                                           │  │
│  └───────────────────────────────────────────────────────────┘  │
├─────────────────────────────────────────────────────────────────┤
│                       基础设施层 (Infrastructure Layer)        │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                     数据访问层                             │  │
│  │  - IndexRepository                                        │  │
│  │                     缓存层                                 │  │
│  │  - Caffeine Cache                                         │  │
│  │                     事件机制                               │  │
│  │  - Spring Event                                            │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 技术栈

| 类别 | 技术/框架 | 版本 | 用途 |
|------|-----------|------|------|
| 核心框架 | Spring Boot | 3.2.0 | 应用开发框架 |
| 编程语言 | Java | 17 | 开发语言 |
| 数据库 | H2 | 2.2.224 | 数据存储 |
| ORM框架 | Spring Data JPA | 3.2.0 | 数据库访问 |
| 缓存 | Caffeine | 3.1.8 | 内容缓存 |
| 文档处理 | PDFBox | 3.0.1 | PDF文件处理 |
|  | Apache POI | 5.2.5 | Office文件处理 |
|  | Mammoth | 1.11.0 | DOCX转HTML |
|  | Flexmark | 0.64.8 | Markdown处理 |
|  | OpenCSV | 5.8 | CSV文件处理 |
|  | Tika | 2.9.1 | 内容检测 |
| 构建工具 | Maven | 3.8.6 | 项目构建 |

## 3. 模块划分与职责

### 3.1 文件扫描模块

**职责**：负责扫描指定目录下的文件，生成文件索引，并检测文件变化。

**核心组件**：
- `FileScanService`：定义文件扫描相关的接口
- `FileScanServiceImpl`：实现文件扫描逻辑，包括目录遍历、文件过滤、索引生成
- `TOONGeneratorService`：生成文档的TOON结构
- `ScanEventListener`：处理扫描完成事件

**主要流程**：
1. 接收扫描请求（定时触发或手动触发）
2. 遍历指定目录下的文件和子目录
3. 过滤掉黑名单目录和不支持的文件类型
4. 检测文件是否有变化（通过比较最后修改时间）
5. 生成文件索引
6. 发布扫描完成事件
7. 生成文档的TOON结构

### 3.2 预览服务模块

**职责**：负责生成各种文件格式的预览内容。

**核心组件**：
- `PreviewService`：定义预览生成相关的接口
- `PreviewServiceImpl`：实现预览生成逻辑，支持多种文件格式

**支持的文件格式**：
- PDF：直接返回PDF字节流
- DOCX：转换为HTML格式
- Markdown：转换为HTML格式
- CSV：转换为HTML表格
- HTML：清理并返回安全的HTML
- SVG：清理并返回安全的SVG
- XLSX：转换为HTML表格

**主要流程**：
1. 接收预览请求
2. 验证文件是否存在和可访问
3. 检查文件大小是否超过限制
4. 根据文件类型选择相应的预览生成器
5. 生成预览内容
6. 缓存预览结果
7. 返回预览内容

### 3.3 文档处理模块

**职责**：处理文档的元数据、结构等信息。

**核心领域模型**：
- `Document`：文档聚合根，包含文档的基本信息和关联的元数据、TOON结构、预览内容
- `FileMetadata`：文件元数据，包含文件大小、创建时间、修改时间等
- `TOONStructure`：文档的结构化内容，用于快速导航和理解文档结构
- `PreviewContent`：预览内容，包含预览的媒体类型和内容
- `DirectoryTree`：目录树结构，用于展示文件组织

### 3.4 Web接口模块

**职责**：提供RESTful API接口，方便外部系统调用。

**核心控制器**：
- `DocumentController`：处理文档相关的请求
- `FileController`：处理文件相关的请求
- `IndexController`：处理索引相关的请求

**主要接口**：
- 扫描相关：触发扫描、获取扫描状态
- 索引相关：获取文件索引、获取目录树
- 预览相关：生成预览、获取预览内容
- 文档相关：获取文档元数据、获取文档结构

## 4. 核心功能实现方案

### 4.1 文件扫描实现

**实现细节**：
- 使用Java NIO的`Files.walkFileTree`进行高效的目录遍历
- 采用ForkJoinPool实现并行扫描，提高扫描效率
- 使用ConcurrentHashMap缓存文件的最后修改时间，用于检测文件变化
- 支持跳过黑名单目录（如.git、node_modules等）
- 支持安全的符号链接处理，只允许指向扫描目录内的链接

**关键代码**：
```java
// 并行扫描目录
ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
forkJoinPool.submit(() -> {
    Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
        // 处理目录
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // 检查是否跳过目录
            if (shouldSkipDirectory(dir)) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            // 添加目录到索引
            // ...
            return FileVisitResult.CONTINUE;
        }
        
        // 处理文件
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            // 检查是否处理文件
            if (shouldProcessFile(file)) {
                // 检查文件是否有变化
                // ...
                // 添加文件到索引
                // ...
            }
            return FileVisitResult.CONTINUE;
        }
    });
}).get();
```

### 4.2 预览生成实现

**实现细节**：
- 针对不同文件类型使用不同的处理库
- 使用缓存机制减少重复生成预览的开销
- 对生成的HTML内容进行安全清理，防止XSS攻击
- 限制PDF预览的页数，提高性能
- 支持大文件处理（通过限制最大文件大小）

**缓存策略**：
- 使用Caffeine缓存预览内容，默认过期时间为10分钟
- 缓存键包含文件路径和处理类名，确保缓存的唯一性
- 支持缓存失效和刷新

**关键代码**：
```java
// 根据文件类型生成预览
FileType fileType = FileType.fromExtension(fileExtension);
switch (fileType) {
    case MD:
        return generateMarkdownPreview(file);
    case DOCX:
        return generateDocxPreview(file);
    case PDF:
        return generatePdfPreview(file.getAbsolutePath(), 1, maxPdfPagesPerRequest);
    case CSV:
        return generateCsvPreview(file);
    case SVG:
        return generateSvgPreview(file);
    case HTML:
    case HTM:
        return generateHtmlPreview(file);
    case XLSX:
        return generateXlsxPreview(file);
    default:
        return Result.failure(String.format("Preview not supported for file type: %s", fileExtension));
}
```

### 4.3 TOON结构生成

**实现细节**：
- TOON（Table of Contents Navigation）结构用于快速导航文档
- 支持从文档中提取标题、章节等结构化信息
- 生成层次化的导航树
- 支持缓存TOON结构，默认过期时间为1小时

## 5. 数据模型设计

### 5.1 核心领域模型

#### Document（文档）
| 属性名 | 类型 | 描述 |
|--------|------|------|
| filePath | String | 文档路径（聚合根ID） |
| fileName | String | 文件名 |
| fileType | FileType | 文件类型 |
| parentDir | String | 父目录路径 |
| fileMetadata | FileMetadata | 文件元数据 |
| toonStructure | TOONStructure | TOON结构 |
| previewContent | PreviewContent | 预览内容 |

#### FileMetadata（文件元数据）
| 属性名 | 类型 | 描述 |
|--------|------|------|
| fileSize | long | 文件大小（字节） |
| createdAt | Instant | 创建时间 |
| modifiedAt | Instant | 修改时间 |
| mimeType | String | MIME类型 |
| pages | Integer | 页数（仅PDF） |
| author | String | 作者 |
| title | String | 标题 |

#### TOONStructure（TOON结构）
| 属性名 | 类型 | 描述 |
|--------|------|------|
| root | TOONNode | 根节点 |
| nodes | List<TOONNode> | 所有节点列表 |

#### TOONNode（TOON节点）
| 属性名 | 类型 | 描述 |
|--------|------|------|
| id | String | 节点ID |
| title | String | 节点标题 |
| level | int | 层级 |
| content | String | 节点内容 |
| children | List<TOONNode> | 子节点列表 |
| pageNumber | Integer | 页码（仅PDF） |

#### FilesIndex（文件索引）
| 属性名 | 类型 | 描述 |
|--------|------|------|
| items | List<FilesIndexItem> | 索引项列表 |
| rootPath | String | 根路径 |
| totalFiles | int | 文件总数 |
| totalDirectories | int | 目录总数 |
| createdAt | Instant | 创建时间 |

#### FilesIndexItem（文件索引项）
| 属性名 | 类型 | 描述 |
|--------|------|------|
| path | String | 相对路径 |
| name | String | 名称 |
| type | String | 类型（file/directory） |
| size | long | 大小（仅文件） |
| modifiedAt | Instant | 修改时间 |

### 5.2 数据库设计

系统使用H2数据库存储文件索引和元数据，主要表结构如下：

#### files_index（文件索引表）
| 列名 | 数据类型 | 约束 | 描述 |
|------|----------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 索引ID |
| root_path | VARCHAR(255) | NOT NULL | 根路径 |
| total_files | INT | NOT NULL | 文件总数 |
| total_directories | INT | NOT NULL | 目录总数 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |

#### files_index_item（文件索引项表）
| 列名 | 数据类型 | 约束 | 描述 |
|------|----------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 索引项ID |
| index_id | BIGINT | FOREIGN KEY | 所属索引ID |
| path | VARCHAR(255) | NOT NULL | 相对路径 |
| name | VARCHAR(255) | NOT NULL | 名称 |
| type | VARCHAR(10) | NOT NULL | 类型（file/directory） |
| size | BIGINT | | 大小（仅文件） |
| modified_at | TIMESTAMP | | 修改时间 |

#### file_metadata（文件元数据表）
| 列名 | 数据类型 | 约束 | 描述 |
|------|----------|------|------|
| id | BIGINT | PRIMARY KEY AUTO_INCREMENT | 元数据ID |
| file_path | VARCHAR(255) | UNIQUE NOT NULL | 文件路径 |
| file_size | BIGINT | NOT NULL | 文件大小 |
| created_at | TIMESTAMP | NOT NULL | 创建时间 |
| modified_at | TIMESTAMP | NOT NULL | 修改时间 |
| mime_type | VARCHAR(50) | | MIME类型 |
| pages | INT | | 页数（仅PDF） |
| author | VARCHAR(100) | | 作者 |
| title | VARCHAR(255) | | 标题 |

## 6. 接口规范

### 6.1 扫描相关接口

#### 触发扫描
- **URL**：`/api/scan`
- **方法**：`POST`
- **参数**：无
- **响应**：
  ```json
  {
    "success": true,
    "message": "Scan started successfully",
    "data": null
  }
  ```

#### 获取扫描状态
- **URL**：`/api/scan/status`
- **方法**：`GET`
- **响应**：
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "isScanning": false,
      "lastScanStatus": "Completed: 123 files, 45 directories",
      "lastScanTimestamp": 1635768000000
    }
  }
  ```

### 6.2 索引相关接口

#### 获取文件索引
- **URL**：`/api/index`
- **方法**：`GET`
- **响应**：
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "items": [
        {
          "path": "docs/",
          "name": "docs",
          "type": "directory",
          "size": 0,
          "modifiedAt": 1635768000000
        },
        {
          "path": "docs/README.md",
          "name": "README.md",
          "type": "file",
          "size": 1024,
          "modifiedAt": 1635768000000
        }
      ],
      "rootPath": "/root/lite-webviewer/docs",
      "totalFiles": 123,
      "totalDirectories": 45,
      "createdAt": 1635768000000
    }
  }
  ```

#### 获取目录树
- **URL**：`/api/index/tree`
- **方法**：`GET`
- **响应**：
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "name": "docs",
      "type": "directory",
      "children": [
        {
          "name": "README.md",
          "type": "file",
          "children": []
        },
        {
          "name": "subdir",
          "type": "directory",
          "children": [
            {
              "name": "file.txt",
              "type": "file",
              "children": []
            }
          ]
        }
      ]
    }
  }
  ```

### 6.3 预览相关接口

#### 生成预览
- **URL**：`/api/preview/{filePath}`
- **方法**：`GET`
- **参数**：
  - `filePath`：文件路径（相对于根目录）
- **响应**：
  - 根据文件类型返回不同的内容
  - PDF：返回PDF字节流
  - 其他类型：返回HTML内容

#### 获取PDF预览（指定页码范围）
- **URL**：`/api/preview/pdf/{filePath}?startPage={startPage}&endPage={endPage}`
- **方法**：`GET`
- **参数**：
  - `filePath`：文件路径（相对于根目录）
  - `startPage`：起始页码
  - `endPage`：结束页码
- **响应**：PDF字节流

### 6.4 文档相关接口

#### 获取文档元数据
- **URL**：`/api/document/{filePath}/metadata`
- **方法**：`GET`
- **参数**：
  - `filePath`：文件路径（相对于根目录）
- **响应**：
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "fileSize": 1024,
      "createdAt": 1635768000000,
      "modifiedAt": 1635768000000,
      "mimeType": "text/markdown",
      "pages": null,
      "author": "John Doe",
      "title": "README"
    }
  }
  ```

#### 获取文档TOON结构
- **URL**：`/api/document/{filePath}/toon`
- **方法**：`GET`
- **参数**：
  - `filePath`：文件路径（相对于根目录）
- **响应**：
  ```json
  {
    "success": true,
    "message": null,
    "data": {
      "root": {
        "id": "root",
        "title": "Root",
        "level": 0,
        "content": "",
        "children": [
          {
            "id": "node-1",
            "title": "1. Introduction",
            "level": 1,
            "content": "Introduction content...",
            "children": [],
            "pageNumber": 1
          }
        ],
        "pageNumber": null
      },
      "nodes": [
        // 所有节点列表
      ]
    }
  }
  ```

## 7. 技术选型说明

### 7.1 核心框架选择
- **Spring Boot**：选择Spring Boot作为核心框架，主要考虑其：
  - 快速开发能力：提供了丰富的starter依赖，简化配置
  - 良好的生态系统：集成了大量成熟的开源库
  - 强大的社区支持：遇到问题容易找到解决方案
  - 适合构建RESTful API：内置了Spring MVC框架

### 7.2 数据库选择
- **H2**：选择H2作为数据库，主要考虑其：
  - 轻量级：适合嵌入式应用
  - 内存模式：提高性能，适合开发和测试
  - 支持SQL标准：易于迁移到其他数据库
  - 无需额外安装：简化部署

### 7.3 缓存选择
- **Caffeine**：选择Caffeine作为缓存框架，主要考虑其：
  - 高性能：吞吐量高，延迟低
  - 灵活的配置：支持多种缓存策略
  - 良好的Spring集成：与Spring Cache无缝集成
  - 支持异步加载：提高并发性能

### 7.4 文档处理库选择
- **PDFBox**：用于PDF文件处理，支持PDF文本提取和渲染
- **Apache POI**：用于Office文件处理，支持DOCX、XLSX等格式
- **Mammoth**：用于DOCX转HTML，生成的HTML质量较高
- **Flexmark**：用于Markdown处理，性能好，功能丰富
- **OpenCSV**：用于CSV文件处理，简单易用
- **Tika**：用于内容检测，支持多种文件格式

## 8. 性能与安全考量

### 8.1 性能优化
- **缓存机制**：使用Caffeine缓存预览内容和TOON结构，减少重复生成的开销
- **并行扫描**：采用ForkJoinPool实现并行文件扫描，提高扫描效率
- **文件变化检测**：通过缓存文件的最后修改时间，只处理变化的文件
- **分页预览**：PDF预览支持分页，限制每次返回的页数，提高响应速度
- **文件大小限制**：设置最大文件大小限制，防止过大文件影响系统性能

### 8.2 安全考量
- **文件路径安全**：严格验证文件路径，防止路径遍历攻击
- **文件类型验证**：只处理支持的文件类型，防止恶意文件
- **HTML清理**：对生成的HTML内容进行安全清理，防止XSS攻击
- **CORS配置**：配置适当的CORS策略，允许合法的跨域请求
- **安全的符号链接处理**：只允许指向扫描目录内的符号链接，防止访问系统文件
- **Rate Limiting**：配置请求速率限制，防止恶意请求

## 9. 部署架构

### 9.1 部署方式
- **单机部署**：系统设计为单机部署，适合中小型应用场景
- **Docker部署**：支持Docker容器化部署，简化部署流程
- **配置文件**：通过application.yml配置文件定制系统参数

### 9.2 配置参数
- **扫描配置**：
  - `app.scan.root-dirs`：扫描根目录
  - `app.scan.cron`：定时扫描间隔（分钟）
- **预览配置**：
  - `app.preview.max-file-size`：最大文件大小（MB）
  - `app.preview.pdf.max-pages-per-request`：PDF每页请求的最大页数
- **缓存配置**：
  - `app.cache.toon-ttl`：TOON结构缓存过期时间（秒）
  - `app.cache.preview-ttl`：预览内容缓存过期时间（秒）
- **Web配置**：
  - `server.port`：服务端口
  - `server.servlet.context-path`：上下文路径
- **CORS配置**：
  - `cors.allowed-origins`：允许的源
  - `cors.allowed-methods`：允许的HTTP方法
  - `cors.allowed-headers`：允许的HTTP头

### 9.3 日志配置
- **日志级别**：可配置不同包的日志级别
- **日志格式**：支持控制台和文件日志，格式可定制
- **日志文件**：默认存储在`./logs/nexus-lite.log`

## 10. 扩展性设计

### 10.1 支持新的文件格式
系统设计支持方便地添加新的文件格式预览，主要步骤：
1. 在`FileType`枚举中添加新的文件类型
2. 在`PreviewServiceImpl`中添加对应的预览生成方法
3. 实现具体的预览生成逻辑

### 10.2 支持分布式部署
虽然当前设计为单机部署，但系统架构支持扩展为分布式部署：
1. 将H2数据库替换为分布式数据库（如MySQL、PostgreSQL）
2. 使用分布式缓存（如Redis）替换Caffeine
3. 添加服务注册与发现机制（如Eureka、Consul）
4. 实现负载均衡

### 10.3 支持更多的文档处理功能
系统设计支持扩展更多的文档处理功能：
- 文档内容搜索
- 文档版本管理
- 文档协作编辑
- 文档转换为其他格式

## 11. 监控与维护

### 11.1 监控指标
- **系统健康状态**：通过Spring Boot Actuator暴露健康检查接口
- **性能指标**：
  - 扫描时间
  - 预览生成时间
  - 缓存命中率
  - 请求响应时间
- **错误日志**：记录系统错误信息，便于排查问题

### 11.2 维护建议
- **定期备份**：定期备份H2数据库文件，防止数据丢失
- **监控日志**：定期查看系统日志，及时发现问题
- **更新依赖**：定期更新系统依赖，修复安全漏洞
- **性能调优**：根据实际使用情况调整缓存大小和过期时间

## 12. 总结

Nexus-Lite 是一个设计精良的轻量级知识预览系统，采用了分层架构设计，具有良好的扩展性和可维护性。系统支持多种文件格式的在线预览，提供高效的缓存机制，能够自动扫描和索引文档，帮助用户快速获取文档内容。

系统的核心优势包括：
- 轻量级设计：部署简单，资源占用低
- 高性能：使用缓存和并行处理提高性能
- 安全可靠：采用多种安全措施保护系统
- 易于扩展：支持添加新的文件格式和功能
- 良好的API设计：提供简洁易用的RESTful API

Nexus-Lite 适合作为知识管理系统的一部分，为用户提供高效、便捷的文档预览服务，提高知识管理效率。