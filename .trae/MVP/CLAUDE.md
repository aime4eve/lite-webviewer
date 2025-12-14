CLAUDE.md

本文件为 Claude Code（claude.ai/code）在处理此仓库中的代码时提供指导。

项目概述

本目录包含两个最小可行产品（MVP）的领域驱动设计（DDD）战略设计文档：

1. 碳排优化MVP（Carbon Emission Optimization MVP）- 用于工业园区能源优化的 Smart CarbonZero 生态系统
2. 防霉管控MVP（Mold Prevention Control MVP）- 基于订阅模式的 SmartMoldGuard 防霉服务

这些是按照 DDD 方法论编写的设计文档，而非可执行代码。文档遵循标准化的战略设计三阶段结构。

文档结构与标准

阶段结构
- D1：产品愿景与用户故事
  - 产品愿景文档（`产品愿景.md`）
  - 用户故事与旅程地图（`用户故事与旅程.md`）
  - 背景调研（`背景资料.md`）
- D2：战略设计（7 个标准文档）
  - 01-BVC：商业价值画布（Business Value Canvas）- 商业模式与价值主张
  - 02-BCM：业务能力地图（Business Capability Map）- 业务能力与投资策略
  - 03-SDM：子域优先级矩阵（Subdomain Priority Matrix）- 领域分类（核心/支撑/通用）
  - 04-CDS：核心域场景（Core Domain Scenario）- 包含 Given/When/Then 的详细场景规范
  - 05-BCX：限界上下文地图（Bounded Context Map）- 系统边界与集成模式
  - 06-CIT：上下文集成的契约（Context Integration Table）- 集成契约与协议
  - 07-Roadmap：演进路线图（Evolution Roadmap）- 分阶段交付计划与 OKR
  - 08-CUJ：关键用户旅程（Critical User Journey，可选）- 关键用户体验流程
- D3：技术设计
  - 09-EventStorming：事件风暴研讨会结果（领域事件、命令、聚合）
  - ESP：EventStorming 流程（替代命名）
  - 技术架构与实现细节

文件命名规范
- D2 文档：`DDD-STR-{ProductLine}-{YYYYMMDD}-v{Version}-{Type}.md`
  - 示例：`DDD-STR-CarbonOpt-20251210-v1.0-01-BVC.md`
- 产品线名称：`CarbonOpt`（碳排优化）、`SmartMoldGuard`（防霉管控）
- 类型：`01-BVC`、`02-BCM`、`03-SDM`、`04-CDS`、`05-BCX`、`06-CIT`、`07-Roadmap`、`08-CUJ`

编写标准
请参考根目录下的四个标准文档：
1. `D2阶段文档写作标准.md` - D2 阶段编写标准
2. `DDD战略设计_Trae工作流指南.md` - DDD 文档的 AI 辅助工作流
3. `产品愿景写作标准.md` - 产品愿景编写标准
4. `用户故事-用户旅程写作标准.md` - 用户故事与旅程编写标准

关键原则：
- 使用 Mermaid 图表 进行所有可视化（sequenceDiagram、graph、quadrantChart、xychart-beta）
- 使用 Markdown 表格 呈现结构化数据
- 所有文档中保持术语一致
- 包含量化指标并附上具体数字（¥、%、时间单位）
- 采用基于场景的描述，配合具体的用户故事

常见开发任务

文档创建与编辑
由于这是一个文档仓库，常见任务包括：
1. 根据模板创建新的 DDD 战略设计文档
2. 基于新见解或优化更新现有文档
3. 确保所有文档的一致性（术语、结构、格式）
4. 生成 Mermaid 图表以可视化流程和关系

质量保证
- 根据相应标准模板验证文档结构
- 检查 Mermaid 图表语法是否正确
- 确保所有必需章节存在且格式正确
- 验证域术语在各文档中的一致性

AI 辅助工作流（Trae）
`DDD战略设计_Trae工作流指南.md` 提供了 AI 辅助文档编写的推荐流程：
1. 注入上下文：向 AI 提供规则、输入文档和参考示例
2. 生成初稿：生成覆盖核心业务逻辑的初始内容
3. 风格对齐：将草稿精炼为符合参考文档风格与结构的形式
4. 深度优化：补充边界情况、非功能性需求和量化分析

架构洞见

领域驱动设计模式
这些文档实现了 DDD 战略设计模式：
- 限界上下文：在 BCX 文档中定义的系统边界
- 核心/支撑/通用域：在 SDM 文档中的分类
- 聚合与领域事件：在 EventStorming 文档中识别
- 统一语言：在所有文档中保持一致的术语
- 上下文映射：在 BCX 和 CIT 中的集成模式（合作关系、防腐层、开放主机服务等）

业务架构
- 价值流：BCM 文档中的顶层业务流程
- 业务能力：在 BCM 中映射到技术的组织能力
- 北极星指标：BVC 文档中的量化成功衡量标准
- 演进路线图：平衡业务与技术目标的分阶段交付计划

重要说明
- 本目录仅包含设计文档 - 没有可执行代码、构建脚本或测试
- 所有文档均为中文，并包含部分英文技术术语
- 两个 MVP 目录遵循相同的结构与标准
- D3 文档可能包含用于实施参考的技术架构细节
- 文档采用语义化版本（v1.0、v2.0）并带有基于日期的标识符