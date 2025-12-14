# 05-BCX 限界上下文地图 (Bounded Context Map)

> **编号**：DDD-STR-CarbonOpt-20251210-v2.0  
> **状态**：Final  
> **版本说明**：深化版，包含核心聚合与集成模式详解

---

## 1. 上下文详细定义 (Context Definitions)

| 上下文名称 | 类别 | 核心职责 | 核心聚合 (Aggregate Roots) | 发布的领域事件 (Domain Events) |
| :--- | :--- | :--- | :--- | :--- |
| **EnergyOptimization**<br>(能源调优) | **Core** | 负荷预测、削峰策略生成、多目标寻优。 | `ForecastModel` (预测模型)<br>`OptimizationStrategy` (调优策略)<br>`EnergyPlan` (用能计划) | `StrategyGenerated`<br>`LoadForecasted` |
| **EdgeControl**<br>(边缘控制) | **Core** | 协议适配、指令下发、安全校验、离线控制。 | `DeviceConnection` (连接)<br>`ControlCommand` (指令)<br>`SafetyRule` (安全规则) | `CommandExecuted`<br>`DeviceOfflineDetected` |
| **CarbonCore**<br>(碳排核心) | **Core** | 碳因子管理、碳核算、合规报告生成。 | `CarbonFactor` (碳因子)<br>`EmissionLedger` (排放台账)<br>`ComplianceReport` (合规报告) | `EmissionCalculated`<br>`ReportGenerated` |
| **EnergyMonitoring**<br>(能源监测) | **Support** | 原始数据采集、清洗、时序存储、告警检测。 | `MeterReading` (读数)<br>`Alarm` (告警)<br>`EnergyConsumption` (能耗统计) | `ReadingCollected`<br>`AlarmTriggered` |
| **DigitalTwin**<br>(数字孪生) | **Support** | 3D 场景管理、视效渲染、状态映射。 | `TwinScene` (场景)<br>`ModelAsset` (模型资产) | N/A (消费端) |
| **DeviceAsset**<br>(设备资产) | **Generic** | 设备台账维护、维保记录、生命周期管理。 | `Device` (设备)<br>`MaintenanceOrder` (工单) | `DeviceRegistered`<br>`DeviceDecommissioned` |
| **AuthContext**<br>(认证授权) | **Generic** | 租户管理、用户认证、权限控制。 | `Tenant` (租户)<br>`User` (用户)<br>`Role` (角色) | `UserLoggedIn`<br>`TenantCreated` |

---

## 2. 上下文映射图 (Context Map)

```mermaid
graph TD
    subgraph Core Domain [核心域]
        EOC[EnergyOptimization<br>能源调优]
        ECC[EdgeControl<br>边缘控制]
        CCC[CarbonCore<br>碳排核心]
    end
    
    subgraph Supporting Domain [支撑域]
        EMC[EnergyMonitoring<br>能源监测]
        DTC[DigitalTwin<br>数字孪生]
    end
    
    subgraph Generic Domain [通用域]
        DAC[DeviceAsset<br>设备资产]
        AC[Auth<br>认证授权]
    end

    %% Relationships
    EMC -->|Customer-Supplier<br>(Async Event)| EOC
    EMC -->|Customer-Supplier<br>(Async Event)| CCC
    EMC -->|Open Host Service<br>(API)| DTC
    
    EOC <-->|Partnership<br>(RPC+Event)| ECC
    
    DAC -->|Published Language<br>(Event)| EMC
    DAC -->|Published Language<br>(Event)| ECC
    
    AC -.->|Shared Kernel<br>(Lib)| All[All Contexts]
    
    classDef core fill:#ffcccc,stroke:#333,stroke-width:2px;
    classDef support fill:#e6f3ff,stroke:#333,stroke-width:1px;
    classDef generic fill:#f0f0f0,stroke:#333,stroke-width:1px;
    
    class EOC,ECC,CCC core;
    class EMC,DTC support;
    class DAC,AC generic;
```

---

## 3. 关键集成模式说明 (Integration Patterns)

### 1. EnergyOptimization <-> EdgeControl (Partnership)
*   **模式**: **Partnership (合作伙伴)**
*   **理由**: 调优上下文生成的策略必须被边缘上下文无损理解并执行，两者紧密配合完成闭环控制。
*   **协作方式**: 双方团队每周同步接口定义 (Protobuf)，共同维护"控制指令集"标准。如果一方变更，另一方必须同步修改。

### 2. EnergyMonitoring -> EnergyOptimization (Customer-Supplier)
*   **模式**: **Customer-Supplier (客户-供应商)**
*   **理由**: 调优算法强依赖监测数据的质量。Monitoring 是上游 (Supplier)，必须满足 Downstream (Optimization) 对数据实时性 (<1s) 和完整性的要求。
*   **契约**: 下游提出数据需求（如"需要 1min 粒度的电压波动数据"），上游负责开发满足。

### 3. DeviceAsset -> All (Published Language)
*   **模式**: **Published Language (发布语言)**
*   **理由**: `DeviceID`、`DeviceType`、`ModelNumber` 等基础元数据贯穿所有上下文。
*   **实现**: Asset 上下文发布统一的 `DeviceRegistered` 事件，包含标准化的设备信息 Schema。其他上下文订阅并存储副本，但不直接查询 Asset 数据库。

### 4. EnergyMonitoring -> DigitalTwin (Open Host Service)
*   **模式**: **Open Host Service (开放主机服务)**
*   **理由**: 监测数据不仅供给内部，未来可能供给第三方大屏或政府平台。
*   **实现**: Monitoring 暴露一套标准的 REST/WebSocket API，任何消费者（包括 DigitalTwin）都通过该标准协议接入，无需定制。
