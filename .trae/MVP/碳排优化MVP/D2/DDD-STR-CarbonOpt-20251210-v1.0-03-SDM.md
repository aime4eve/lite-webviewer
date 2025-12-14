# 03-SDM 子域优先级矩阵 (Subdomain Priority Matrix)

> **编号**：DDD-STR-CarbonOpt-20251210-v2.0  
> **状态**：Final  
> **版本说明**：深化版，明确自研/外采策略

---

## 1. 矩阵坐标定义 (Axes Definition)

*   **X轴: 业务差异化 (Business Differentiation)**
    *   `1`: 行业标准功能 (如登录注册)。
    *   `5`: 决定客户购买的关键卖点 (如 AI 削峰)。
*   **Y轴: 技术复杂度 (Technical Complexity)**
    *   `1`: 简单的 CRUD。
    *   `5`: 需要高级算法、实时并发或硬件交互。

---

## 2. 子域评估与决策表 (Subdomain Assessment)

| 子域名称 (Subdomain) | 类型 | 差异化 (X) | 复杂度 (Y) | 建设策略 | 决策理由 (Rationale) |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **EnergyOptimization**<br>(能源调优) | **Core** | **5.0** | **5.0** | **全自研** | 核心竞争力所在。需针对不同园区场景定制负荷预测与寻优算法，市面上无现成可用产品。 |
| **EdgeControl**<br>(边缘控制) | **Core** | **4.5** | **4.5** | **自研内核** | 涉及底层硬件交互与毫秒级实时响应，需自研网关 OS 与规则引擎以保障安全性与低时延。 |
| **CarbonCompliance**<br>(碳排合规) | **Support** | **4.0** | **3.0** | **自研+引入** | 需自研核算逻辑以快速响应政策变化，但底层的"碳因子库"应引入外部权威数据源。 |
| **DigitalTwinVis**<br>(数字孪生) | **Support** | **3.5** | **4.0** | **二次开发** | 炫酷的 3D 效果是加分项，但图形引擎技术门槛极高。应基于成熟商业引擎 (UE/Unity) 进行业务层封装。 |
| **DeviceAsset**<br>(设备资产) | **Generic** | **2.0** | **2.0** | **外采/开源** | 标准的 IoT 设备台账管理，行业方案极其成熟 (如 ThingsBoard)，无需重复造轮子。 |
| **UserAuth**<br>(用户权限) | **Generic** | **1.0** | **2.0** | **开源框架** | 采用 Spring Security + OAuth2 标准实现，或集成 Keycloak。 |
| **ReportEngine**<br>(报表引擎) | **Generic** | **2.0** | **3.0** | **商业集成** | 复杂的拖拉拽报表，直接集成 FineReport 或 Superset 等成熟产品。 |

---

## 3. 优先级矩阵图 (Priority Matrix)

```mermaid
quadrantChart
    title 子域优先级矩阵
    x-axis 业务差异化 (Low -> High)
    y-axis 技术复杂度 (Low -> High)
    quadrant-1 核心域 (Core) - 战略投资
    quadrant-2 支撑域 (Supporting) - 合作/外包
    quadrant-3 通用域 (Generic) - 购买/开源
    quadrant-4 暂缓/不重要
    
    "UserAuth": [0.10, 0.20]
    "DeviceAsset": [0.25, 0.25]
    "ReportEngine": [0.30, 0.50]
    "DigitalTwinVis": [0.60, 0.75]
    "CarbonCompliance": [0.75, 0.55]
    "EdgeControl": [0.85, 0.85]
    "EnergyOptimization": [0.95, 0.95]
```

---

## 4. 资源投入策略 (Resource Allocation)

1.  **核心域 (Core Domains)**: **EnergyOptimization, EdgeControl**
    *   **资源配比**: **60%** 的研发人力。
    *   **团队配置**: 博士级算法专家 + 资深嵌入式工程师。
    *   **KPI**: 预测准确率、控制响应时延、节能率。

2.  **支撑域 (Supporting Domains)**: **CarbonCompliance, DigitalTwinVis**
    *   **资源配比**: **30%** 的研发人力。
    *   **团队配置**: 全栈工程师 + 3D 美术/前端。
    *   **KPI**: 报表合规性、界面加载速度、视觉还原度。

3.  **通用域 (Generic Domains)**: **DeviceAsset, UserAuth, ReportEngine**
    *   **资源配比**: **10%** 的研发人力 (主要是集成工作)。
    *   **团队配置**: 初中级工程师或外包。
    *   **KPI**: 系统稳定性、Bug 修复速度。
