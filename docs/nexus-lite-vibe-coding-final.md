# Nexus-Lite 知识预览系统 - Vibe Coding 最终提示词清单

## 第一阶段：项目骨架与共享内核 (Foundation & Shared Kernel)

### 目标：建立符合 DDD 边界的单体项目结构，并配置轻量级基础设施。

#### 1. 初始化模块化单体架构与技术栈

**Prompt:**
"你是一个精通 DDD 和 Clean Architecture 的 Java 架构师。请为我搭建一个名为 'Nexus-Lite' 的 Spring Boot 3 模块化单体项目骨架。

**核心约束：**
- **零外部依赖**：单节点部署，不需要 Redis/PostgreSQL/Nginx。
- **包结构（严格遵循）**：根包 `com.documentpreview`。下设模块包：
  - `shared` (共享内核：DDD基础类, 工具类)
  - `modules.document` (文档结构与模型)
  - `modules.scan` (文件扫描与索引生成)
  - `modules.preview` (内容预览与转换)
  - `infra` (基础设施：文件IO, 解析库封装)
  - `web` (API控制器, 静态资源)
  - `test` (测试工具类与配置)

**技术栈：** Java 17+, Spring Boot 3, Lombok, H2 Database, Caffeine Cache。

**关键依赖：** 
- `spring-boot-starter-web`
- `spring-boot-starter-data-jpa`
- `spring-boot-starter-logging`
- `spring-boot-starter-actuator` (基础监控)
- `com.github.ben-manes.caffeine`
- `org.apache.pdfbox` (PDF处理)
- `org.apache.poi` 或 `mammoth` (DOCX处理)
- `com.opencsv`
- `org.apache.tika` (文件类型识别)
- `spring-boot-starter-test` (测试框架)
- `junit-jupiter` (单元测试)
- `mockito` (模拟测试)

请提供 `pom.xml` 的关键依赖配置，并展示推荐的目录结构树。"

#### 2. 配置嵌入式基础设施与 DDD 基础类

**Prompt:**
"请完成基础设施配置并实现 DDD 基础类。

**H2 配置：** 配置 H2 数据库为文件模式 (jdbc:h2:file:./data/nexus_db)。

**Caffeine 配置：** 实现 `CacheConfig`。配置 `toon-index` (TTL 1小时) 和 `preview-content` (TTL 10分钟) 两个缓存空间。

**配置项管理：** 在 `application.yml` 中添加以下可配置项：
- 扫描根目录：`app.scan.root-dirs` (默认：`["/home/agentic/hkt-knowledge"]`)
- 扫描周期（分钟）：`app.scan.cron` (默认：`60`)
- 最大文件大小（MB）：`app.preview.max-file-size` (默认：`20`)
- 缓存TTL配置：`app.cache.toon-ttl` (默认：`3600`), `app.cache.preview-ttl` (默认：`600`)

**DDD 基础类** (`com.documentpreview.shared`)：
- 实现 `Entity<ID>`
- 实现 `ValueObject`
- 实现 `AggregateRoot<ID>` (包含领域事件发布能力)
- 实现 `Result<T>` (用于替代异常抛出)
- 实现 `DomainEvent` 基础类

**日志配置：** 配置 SLF4J + Logback，设置合适的日志级别，在关键操作处添加日志记录。

**监控配置：** 启用 Spring Boot Actuator 的基本端点，包括健康检查和指标。"

## 第二阶段：核心领域建模 (Domain Modeling)

### 目标：实现文档、TOON 结构和目录树等核心业务对象。

#### 3. 实现文档与 TOON 核心模型 (modules.document)

**Prompt:**
"请在 `com.documentpreview.modules.document.domain` 实现核心模型，严格参照产品设计文档的通用语言和战术设计：

- **Document (AggregateRoot)**: ID 为 `filePath`。包含 `fileName`, `fileType`, `parentDir`。
- **FileType (Enum)**: 包含 `MD`, `DOCX`, `PDF`, `CSV`, `SVG`。
- **FileMetadata (ValueObject)**: 包含 `fileSize`, `lastModifiedTime`, `charset`。
- **TOONStructure (ValueObject)**: 封装 TOON 结构，包含 `rootNode`, `elementCount`, `fields` (List), `data` (List<Map>)。
- **DirectoryTree (ValueObject/Read Model)**: 递归结构，用于目录树展示。

**测试要求：** 为每个核心模型编写单元测试，验证模型的正确性和完整性。"

## 第三阶段：扫描与索引模块 (Scan & Index)

### 目标：实现高性能扫描、变更检测、TOON 格式化、以及索引的持久化。

#### 4. 实现扫描服务与治理 (modules.scan)

**Prompt:**
"请在 `com.documentpreview.modules.scan` 实现 `FileScanService`。

**I/O 优化：** 使用 `ForkJoinPool` 或 Java NIO 实现高效并发的文件系统遍历。

**定时扫描：** 使用 `@Scheduled` 实现可配置的定时全量扫描，扫描周期从 `application.yml` 读取。

**变更检测：** 扫描时，只对 `lastModifiedTime` 发生变化的文件进行重新处理。

**合规治理：** 
- 实现隐藏文件和黑名单目录过滤（黑名单：`.git/`, `.svn/`, `node_modules/`, `target/`, `.DS_Store`）
- 实现符号链接安全检查，禁止跟随指向配置目录外的链接
- 实现路径规范化，防止路径遍历攻击

**状态与事件：** 
- 实现 `ScanStatusManager`，管理扫描状态
- 在扫描完成后发布 `ScanFinishedEvent`
- 添加详细的日志记录，包括扫描开始、完成、跳过的文件等信息

**测试要求：** 编写单元测试和集成测试，验证扫描逻辑、过滤规则和安全检查的正确性。"

#### 5. 实现 TOON 生成器与持久化 (modules.scan & infra)

**Prompt:**
"请实现 TOON 格式的核心逻辑和持久化机制。

**TOON 生成器** (`com.documentpreview.modules.scan.TOONGeneratorService`)：
- **语法实现**：严格按照产品设计文档第5章实现 TOON 语法生成
- **多块支持**：支持生成包含 `files` 和 `metadata` 等多个数据块的 TOON 字符串
- **数据提取**：
  - CSV：解析表头为 `fields`，行数据为 `data`
  - Markdown：提取表格和列表结构化数据
  - SVG：提取元数据（width, height, elementsCount）
  - 通用回退：生成默认的 `content` 字段

**索引持久化** (`com.documentpreview.modules.scan.IndexRepository`)：
- **TOON 持久化**：将生成的 TOON 字符串写入本地文件 `data/index.toon`
- **JSON 派生**：同时生成一份可直接加载的 JSON 索引到 `data/index.json`
- **索引加载**：实现 `loadIndex()`，优先读取缓存，其次读取 `index.json`，最后解析 `index.toon`
- **更新时间管理**：记录并更新索引的最后修改时间

**测试要求：** 编写单元测试，验证 TOON 生成的正确性、索引持久化和加载的完整性。"

## 第四阶段：预览与适配 (Preview & Infra)

### 目标：实现高效、安全的本地文件预览功能。

#### 6. 实现本地预览适配器与降级 (modules.preview & infra)

**Prompt:**
"请实现预览模块 `com.documentpreview.modules.preview`，并依赖 `com.documentpreview.infra` 中的解析库封装。

**Markdown 处理：**
- 使用库（如 `flexmark-java`）将 Markdown 转换为 HTML
- 实现表格提取和标题目录结构化
- 添加代码高亮支持

**DOCX 处理：**
- 使用 `mammoth` 将 DOCX 转换为 HTML
- 重点实现表格优先、段落标题提取

**PDF 处理：**
- 使用 `PDFBox` 实现文本提取和基本结构化（如返回前几页的纯文本）
- 将 PDF 文件流直接返回给前端

**CSV 处理：**
- 使用 `OpenCSV` 实现完整的结构化解析并返回 JSON 格式数据

**SVG 处理：**
- 直接读取 SVG 文件内容
- 提取 SVG 元数据
- 进行安全过滤，移除可疑脚本
- 返回原始 SVG 文本或嵌入 HTML

**缓存策略：**
- 在 `PreviewService` 中对预览结果应用 `@Cacheable(value = "preview-content")`
- 缓存键包含文件路径和修改时间，确保缓存有效性

**降级处理：**
- 如果文件过大（>配置的最大文件大小）或解析失败，返回通用的错误/降级提示信息
- 记录降级处理的日志，便于后续分析

**测试要求：** 为每种文件格式的预览实现单元测试，验证转换结果的正确性和降级处理的有效性。"

## 第五阶段：Web API 与前端集成 (Web & Interface)

### 目标：暴露 REST API，支持内容协商和静态资源托管。

#### 7. 实现 REST API Controller (web)

**Prompt:**
"请在 `com.documentpreview.web` 实现 `DocumentController` 和 `IndexController`。

**API 规范：**
- `GET /api/v1/document/tree` : 获取目录树结构
  - 支持参数：`rootDir` (默认：配置的第一个根目录), `path` (指定路径), `depth` (默认：2)
- `GET /api/v1/document/toon?filePath=...` : 获取单个文档的 TOON 结构
  - 支持参数：`format` (both/toon/json, 默认：both)
- `GET /api/v1/document/preview?filePath=...` : 获取文档预览内容 (HTML/JSON/Binary)
  - 支持参数：`startPage`, `endPage` (PDF 分页)
- `POST /api/v1/document/scan` : 触发手动扫描
  - 支持参数：`dirPath` (指定目录), `force` (是否强制扫描)
- `GET /api/v1/index/toon` : 返回持久化的完整索引 TOON
- `GET /api/v1/index/json`: 返回索引的 JSON 派生结构

**治理与合规：**
- 实现内容协商机制 (`application/x-toon`, `application/json`)
- 实现 `ETag` 和 `Last-Modified` 支持，利用 `FileMetadata` 优化浏览器缓存
- 添加请求日志记录，包括请求路径、参数、响应状态等
- 实现基本的限流机制，防止恶意请求

**测试要求：** 编写集成测试，验证 API 的正确性、内容协商和缓存机制的有效性。"

#### 8. 错误处理与静态资源托管 (web)

**Prompt:**
"请实现全局的错误处理与前端静态资源托管。

**全局异常处理：**
- 实现 `GlobalExceptionHandler`，统一错误响应格式 `{ code, message, traceId, details }`
- 将 `SecurityException` 映射为 403，`FileNotFoundException` 映射为 404
- 记录异常的详细日志，包括堆栈信息

**SPA 托管：**
- 配置 `WebMvcConfig`，将 React 构建产物（前端的 `dist` 目录）映射到 Spring Boot 的静态资源路径 (`/static`)

**前端路由支持：**
- 配置视图控制器，将所有非 `/api` 且非静态资源的路由请求转发到 `/index.html`，以支持 React Router

**CORS 配置：**
- 在开发环境下允许所有跨域请求
- 在生产环境下限制允许的来源

**测试要求：** 编写测试，验证错误处理机制和静态资源托管的正确性。"

## 第六阶段：前端实现与打包 (Frontend & Deployment)

### 目标：构建 React 应用并完成单体打包脚本。

#### 9. 前端实现增强 (React + Ant Design)

**Prompt:**
"现在切换到 React 前端。请实现核心组件：

**文件树组件：**
- 实现左侧目录树，使用 AntD Tree
- 显示文件图标、区分目录/文件
- 支持展开/折叠、懒加载
- 支持按文件名搜索和过滤

**预览组件：**
- 实现右侧 `PreviewPane`，根据文件类型动态加载渲染器
- **Markdown**: 使用 `react-markdown`，支持代码高亮、标题目录
- **PDF**: 使用 `pdfjs-dist`，支持滚动/缩放、页码导航
- **CSV**: 使用 AntD Table，支持排序和过滤
- **DOCX**: 渲染 HTML 内容，支持样式保留
- **SVG**: 直接嵌入 SVG 或使用 `react-svg` 组件

**状态管理：**
- 使用 React Context 或 Zustand 管理应用状态
- 实现前端缓存策略（如 LocalStorage 缓存目录树）

**错误处理：**
- 在 API 调用失败时显示统一的错误降级提示
- 实现加载状态和错误状态的 UI 展示

**测试要求：** 使用 Jest 和 React Testing Library 编写前端组件测试，验证组件的渲染和交互逻辑。"

#### 10. 最终单体打包脚本

**Prompt:**
"请提供一个完整的 Shell 脚本示例，用于将前后端项目打包成一个可部署的 JAR 文件。

**步骤：**
1. 编译 React 项目 (`npm run build`)
2. 将 React 的 `dist` 目录复制到 Spring Boot 的 `src/main/resources/static`
3. 使用 Maven 命令打包 Spring Boot (`mvn clean package`)
4. 运行测试套件 (`mvn test`)

**运行命令：**
- 给出最终 JAR 文件在单节点服务器上的运行命令：`java -jar nexus-lite.jar`
- 支持通过命令行参数或环境变量覆盖配置：`java -jar nexus-lite.jar --app.scan.root-dirs=["/custom/path"]`

**部署建议：**
- 在生产环境中，建议使用 `systemd` 或其他进程管理工具管理应用
- 配置日志轮转，防止日志文件过大
- 定期备份 `data` 目录，确保索引数据安全"

## 第七阶段：测试与验证 (Testing & Validation)

### 目标：确保系统质量和稳定性。

#### 11. 实现测试套件

**Prompt:**
"请实现完整的测试套件，确保系统的质量和稳定性。

**单元测试：**
- 为所有核心服务和工具类编写单元测试
- 使用 Mockito 模拟外部依赖
- 测试覆盖率目标：核心业务逻辑 > 80%

**集成测试：**
- 测试模块间的交互逻辑
- 测试 API 端点的正确性
- 测试文件扫描和 TOON 生成的完整流程

**端到端测试：**
- 测试完整的用户流程：扫描 -> 浏览目录 -> 预览文档
- 测试错误场景和降级处理

**性能测试：**
- 测试大目录扫描的性能
- 测试预览大文件的响应时间
- 测试缓存机制的有效性

**测试报告：**
- 配置 Maven Surefire 插件生成测试报告
- 集成 JaCoCo 生成代码覆盖率报告

**持续集成：**
- 提供 GitHub Actions 或 GitLab CI 配置示例
- 实现自动化测试和构建流程"

## 部署与运维建议

1. **环境准备：**
   - 确保 Java 17+ 已安装
   - 确保目标目录有读写权限

2. **配置调整：**
   - 根据实际需求修改 `application.yml` 中的配置项
   - 调整 JVM 参数，优化内存使用：`java -Xms512m -Xmx2g -jar nexus-lite.jar`

3. **监控与日志：**
   - 访问 `/actuator/health` 检查应用健康状态
   - 访问 `/actuator/metrics` 查看应用指标
   - 配置日志收集工具（如 ELK Stack），集中管理日志

4. **备份策略：**
   - 定期备份 `data` 目录，包含索引文件和数据库
   - 实现增量备份，减少备份时间和空间占用

5. **升级策略：**
   - 停止当前应用
   - 备份数据目录
   - 部署新版本 JAR 文件
   - 启动应用并验证

## 扩展建议

1. **支持更多文件格式：**
   - 逐步添加对 `.xlsx`、`.pptx` 等格式的支持
   - 实现插件机制，方便扩展新的文件格式

2. **增强搜索功能：**
   - 实现基于内容的全文搜索
   - 支持高级搜索条件（如文件类型、修改时间）

3. **添加用户权限管理：**
   - 实现基于角色的访问控制
   - 支持文档级别的权限设置

4. **实现分布式部署：**
   - 引入消息队列，实现异步扫描和预览
   - 支持多节点部署，实现负载均衡

5. **增强监控和告警：**
   - 集成 Prometheus 和 Grafana，实现可视化监控
   - 配置告警规则，及时发现和处理问题

这份 Vibe Coding 提示词清单提供了完整的 Nexus-Lite 知识预览系统实现指南，涵盖了从项目初始化到部署运维的各个方面。按照这个清单逐步实现，可以构建一个功能完整、性能优良、易于维护的模块化单体应用。