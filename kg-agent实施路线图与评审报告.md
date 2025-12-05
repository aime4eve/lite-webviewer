# kg-agent 系统实施路线图与评审报告

## 1. 代码实现评审报告

### 1.1 现状概述
通过对 `/root/lite-webviewer` 目录的扫描，**未发现 `kg-agent` 子目录**。
- **已完成模块**：0%
- **待开发模块**：100%

**结论**：目前项目处于**尚未启动**状态。设计文档 `kg-agent实施规划.md` 已经完备，但代码实现尚未开始。

### 1.2 设计文档完备性评估
- **架构设计**：清晰定义了单体后端服务 + 异步任务队列的架构，合理。
- **技术选型**：Flask + Celery + Milvus Lite + NebulaGraph，适合单机部署，选型合理。
- **实施步骤**：分阶段规划（初始化 -> 核心模块 -> API -> 异步任务）清晰可行。

### 1.3 风险识别
1.  **资源风险**：单机运行所有组件（ES, Nebula, Milvus, LLM推理）可能导致内存不足。
    *   *应对*：严格限制 Docker 容器内存，使用 Milvus Lite 替代独立服务，NLP 模型懒加载。
2.  **依赖风险**：NebulaGraph 单机版 Docker 镜像体积较大，拉取和启动可能较慢。
    *   *应对*：预先拉取镜像，使用国内镜像源。
3.  **开发风险**：从零开始构建，涉及多个存储组件的集成，初期调试成本高。
    *   *应对*：优先打通核心链路（文档->向量->检索），再逐步完善图谱功能。

---

## 2. 实施计划与任务清单

基于当前“零代码”状态，制定从零开始的实施计划。

### 阶段一：项目初始化 (Project Initialization)
**周期**：Day 1 - Day 2
**优先级**：P0 (最高)

| 任务ID | 任务名称 | 详细描述 | 技术难度 | 预计工时 |
| :--- | :--- | :--- | :--- | :--- |
| **INIT-01** | 创建项目结构 | 建立 `kg-agent` 目录树，配置 `.gitignore` | 低 | 0.5h |
| **INIT-02** | 依赖管理配置 | 创建 `pyproject.toml`，添加 Flask, Celery, Pydantic 等依赖 | 低 | 1h |
| **INIT-03** | 开发环境搭建 | 编写 `docker-compose.dev.yml` (Redis, MinIO, Nebula, ES) | 中 | 3h |
| **INIT-04** | 基础配置模块 | 实现 `src/backend/config.py` (Pydantic Settings) | 低 | 2h |
| **INIT-05** | 日志系统 | 集成 structlog 或标准 logging，实现 JSON 格式日志 | 低 | 1h |

### 阶段二：核心领域模型与工具 (Core Domain & Utils)
**周期**：Day 3 - Day 5
**优先级**：P0

| 任务ID | 任务名称 | 详细描述 | 技术难度 | 预计工时 |
| :--- | :--- | :--- | :--- | :--- |
| **CORE-01** | 数据模型定义 | 定义 Document, Chunk, Entity, Relation Pydantic 模型 | 低 | 2h |
| **CORE-02** | 存储适配器接口 | 定义 VectorStore, GraphStore, DocStore 抽象基类 | 中 | 2h |
| **CORE-03** | 文本处理工具 | 封装 LangChain TextSplitter，实现文本分块逻辑 | 中 | 3h |
| **CORE-04** | Milvus 适配实现 | 实现 Milvus Lite 的连接、插入、检索逻辑 | 中 | 4h |
| **CORE-05** | Nebula 适配实现 | 实现 NebulaGraph 的连接池、nGQL 执行封装 | 高 | 6h |

### 阶段三：API 服务骨架 (API Skeleton)
**周期**：Day 6 - Day 7
**优先级**：P1

| 任务ID | 任务名称 | 详细描述 | 技术难度 | 预计工时 |
| :--- | :--- | :--- | :--- | :--- |
| **API-01** | Flask 应用工厂 | 实现 `create_app`，注册 Blueprints, CORS, ErrorHandler | 中 | 3h |
| **API-02** | API 蓝图定义 | 创建 Search, KG, Admin 三大蓝图的路由文件 | 低 | 1h |
| **API-03** | Swagger 文档 | 集成 Flasgger，自动生成 OpenAPI 文档 | 低 | 2h |
| **API-04** | 健康检查接口 | 实现 `/health` 和 `/status` 接口 | 低 | 0.5h |

### 阶段四：异步任务流水线 (Async Pipeline)
**周期**：Day 8 - Day 12
**优先级**：P1

| 任务ID | 任务名称 | 详细描述 | 技术难度 | 预计工时 |
| :--- | :--- | :--- | :--- | :--- |
| **TASK-01** | Celery 基础配置 | 配置 Redis Broker/Backend，任务序列化 | 中 | 2h |
| **TASK-02** | 文档解析任务 | 实现 PDF/Docx/Txt 解析任务 (Unstructured/PyPDF) | 中 | 6h |
| **TASK-03** | 向量化任务 | 集成 BGE-M3 模型，实现文本转向量任务 | 高 | 8h |
| **TASK-04** | 知识抽取任务 | (Mock/LLM) 实现实体关系抽取逻辑 | 高 | 8h |
| **TASK-05** | 索引构建任务 | 协调各存储组件，实现原子性写入 | 高 | 6h |

---

## 3. 资源调配建议

1.  **开发环境**：
    *   建议开发者使用 Linux/WSL2 环境，因 NebulaGraph Docker 在 Windows 下可能有兼容性问题。
    *   内存要求：开发机至少 16GB RAM（运行所有容器组件）。

2.  **人力建议**：
    *   **1位 后端架构师** (兼任)：负责 INIT 和 CORE 阶段的架构设计与核心类实现。
    *   **1位 后端开发**：负责 API 和 TASK 具体业务逻辑实现。

3.  **关键里程碑**：
    *   **M1 (Day 2)**: 环境跑通，Docker 容器全绿，Hello World 接口可用。
    *   **M2 (Day 5)**: 能够通过 Python 脚本向 Milvus 和 Nebula 写入/读取数据。
    *   **M3 (Day 12)**: 上传一个 PDF，能够自动解析并在图谱和向量库中查到。

## 4. 下一步行动
**立即执行阶段一任务：创建项目结构与配置环境。**
请确认是否开始执行 `INIT-01` 至 `INIT-03` 任务？
