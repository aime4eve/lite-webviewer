# 防霉管控MVP —— 事件风暴规划 (Event Storming Planning)

> 编号：DDD-TAC-SmartMoldGuard-20251209-v2.0
> 状态：Final
> 版本说明：深化版，基于D2战略设计v2.0的事件风暴
> 版本：v2.0 (2025-12-09)
> 依据：D1/产品设计和用户故事 & D2/实现战略设计
> 目标：将战略意图转化为战术设计蓝图，识别领域事件、命令与聚合根
> **术语引用**: 本文档使用《SmartMoldGuard-统一术语表》（v1.0）定义的标准术语

## 1. 风暴图例 (Legend)

在阅读本文档时，请对应以下颜色/符号理解：

*   **🟧 领域事件 (Domain Event)**: 过去发生的、对业务有价值的事实（动词过去式）。*例：RiskDetected*
*   **🟦 命令 (Command)**: 触发事件的动作/意图。*例：DetectRisk*
*   **🟨 聚合/实体 (Aggregate)**: 执行命令、产生事件的业务对象。*例：RiskModel*
*   **🟩 读模型 (Read Model)**: 用于展示的数据视图。*例：RiskDashboard*
*   **🟪 策略/规则 (Policy)**: "当...时，就..."的自动化业务规则。*例：If risk > 0.8 then start fan*
*   **🟥 外部系统 (External System)**: 第三方服务或硬件。*例：LoRaWAN Network*
*   **👤 角色 (Actor)**: 触发命令的人。*例：User*

---

## 2. 核心流程风暴 (Process Modeling)

### 流程一：环境感知与风险预测 (Sensing & Prediction)
> **业务价值**：将原本无意义的温湿度数据转化为具备业务含义的“霉菌风险”。

```mermaid
graph TD
    %% Actors & External
    Ext_Sensor["🟥 传感器"]
    Ext_Weather["🟥 天气服务"]

    %% Flow
    Ext_Sensor -->|上报数据| Cmd_Report["🟦 上报遥测数据<br>ReportTelemetry"]
    Cmd_Report --> Agg_Device["🟨 设备\nDevice"]
    Agg_Device --> Evt_EnvChanged["🟧 环境数据已变更<br>EnvironmentChanged"]
    
    Evt_EnvChanged --> Pol_RiskCheck["🟪 风险检测策略<br>(每10min或波动>5%)"]
    Pol_RiskCheck --> Cmd_Assess["🟦 评估霉菌风险<br>AssessMoldRisk"]
    
    Cmd_Assess --> Agg_RiskModel["🟨 风险模型<br>RiskModel"]
    Ext_Weather -.->|提供外部湿度| Agg_RiskModel
    
    Agg_RiskModel --> Evt_RiskDetected["🟧 霉菌风险已检出<br>MoldRiskDetected"]
    Agg_RiskModel --> Evt_Safe["🟧 环境安全<br>EnvironmentSafe"]

    %% Read Model
    Evt_RiskDetected --> View_App["🟩 小程序首页/健康卡片"]

```
流程描述：智能家居环境健康监测系统的核心流程。
1、外部传感器上报数据，触发一个上报遥测数据的命令。
2、该命令更新设备聚合的状态，并产生一个环境数据已变更的领域事件。
3、该事件触发一个风险检测策略（定时10分钟或数据波动大于5%时执行）。
4、策略执行评估霉菌风险的命令。
5、该命令由风险模型聚合处理，它结合外部天气服务提供的湿度数据，进行综合风险评估。
6、风险模型根据计算结果，发布霉菌风险已检出或环境安全的领域事件。
7、最后，霉菌风险已检出事件会更新小程序前端的视图（如首页的健康状态卡片），通知用户。

### 流程二：智能控制闭环 (Smart Control Loop)
> **业务价值**：基于风险和用户偏好，实现“无感”且“节能”的自动干预（先排风，后加热）。

```mermaid
graph TD
    %% Triggers
    Evt_RiskDetected["🟧 霉菌风险已检出"]
    
    %% Decision
    Evt_RiskDetected --> Pol_AutoIntervention["🟪 自动干预策略<br>(Check: 勿扰模式 & 极端天气)"]
    Pol_AutoIntervention --> Cmd_GenPlan["🟦 生成干预计划<br>GenerateInterventionPlan"]
    
    Cmd_GenPlan --> Agg_Plan["🟨 干预计划<br>InterventionPlan"]
    Agg_Plan --> Evt_PlanCreated["🟧 干预计划已生成<br>InterventionPlanCreated"]
    
    Evt_PlanCreated --> Pol_Dispatch["🟪 调度策略<br>(Step 1: Fan)"]
    Pol_Dispatch --> Cmd_SendCmd["🟦 下发排风指令<br>SendCommand"]
    
    %% Execution
    Cmd_SendCmd --> Agg_Device["🟨 设备<br>Device"]
    Agg_Device --> Ext_Hardware["🟥 3位开关(风)"]
    Ext_Hardware -->|ACK| Evt_CmdExecuted["🟧 排风已执行<br>CommandExecuted"]
    
    %% Step 2 Check
    Evt_CmdExecuted --> Agg_Plan
    Agg_Plan -->|Wait 30min & Check| Pol_CheckRisk["🟪 风险复核策略"]
    
    Pol_CheckRisk -->|Risk High| Cmd_Heat["🟦 下发加热指令<br>SendCommand(Heater)"]
    Pol_CheckRisk -->|Risk Low| Evt_Success["🟧 风险已解除<br>RiskCleared"]
    
    Cmd_Heat --> Agg_Device
    Agg_Device --> Ext_Heater["🟥 3位开关(暖)"]
    
    %% Feedback Loop
    Ext_Heater -->|ACK| Agg_Plan
    Agg_Plan -->|Monitor| Evt_Success
    Agg_Plan -->|Timeout| Evt_Failed["🟧 干预失败/超时<br>InterventionFailed"]
    
    %% Value
    Evt_Success --> Cmd_CalSave["🟦 计算节能收益<br>CalculateSavings"]
    Cmd_CalSave --> Evt_Saved["🟧 节能收益已记录<br>EnergySaved"]
    
    Evt_Saved --> Pol_Reward["🟪 积分奖励策略"]
    Pol_Reward --> Cmd_Award["🟦 发放积分<br>AwardPoints"]
    Cmd_Award --> Agg_Points["🟨 积分账户<br>LoyaltyPoints"]
    Agg_Points --> Evt_Awarded["🟧 积分已发放<br>PointsAwarded"]

```

流程描述：**智能家居自动干预系统**的流程图，展示了从风险检测到自动干预的完整闭环。

- **触发阶段**：由`霉菌风险已检出`事件触发整个流程。
- **决策阶段**：
  - `自动干预策略`会检查条件（如勿扰模式、极端天气），决定是否执行自动干预。
  - 策略通过后，系统`生成干预计划`，创建`干预计划`聚合。
  - 计划生成后触发`干预计划已生成`事件，由`调度策略`决定如何执行。
- **执行阶段 (分步执行)**：
  - **Step 1**: 系统优先下发指令开启`排风扇`。
  - **Step 2**: 30分钟后复核风险，若仍未解除，则下发指令开启`加热器`进行烘干。
- **反馈循环**：
  - 执行结果反馈给`干预计划`聚合进行监控。
  - 最终产生`节能收益已记录`事件，并触发`积分奖励策略`，为用户发放防霉积分。

### 流程三：酒店轻量化风险监测 (Lightweight Risk Monitoring)

> **业务价值**：帮助民宿/酒店业主通过"风险清单"精准维护，替代盲目巡检。

```mermaid
graph TD
    %% Triggers
    Timer_Daily["🕒 每日定时 (08:00 & 20:00)"]
    Actor_Manager["👤 经理"]
    
    %% Process
    Timer_Daily --> Cmd_GenReport["🟦 生成风险清单<br>GenerateRiskReport"]
    Cmd_GenReport --> Agg_Report["🟨 风险报告<br>RiskReport"]
    Agg_Report --> Evt_ReportReady["🟧 风险清单已生成<br>RiskListGenerated"]
    
    %% View & Action
    Evt_ReportReady --> View_Dashboard["🟩 经理小程序/首页仪表盘"]
    Actor_Manager --> Cmd_ViewDashboard["🟦 查看仪表盘<br>ViewDashboard"]
    Cmd_ViewDashboard --> View_Dashboard
    View_Dashboard --> Cmd_ViewAllRisks["🟦 查看全部风险<br>ViewAllRisks"]
    View_Dashboard --> Cmd_BatchActions["🟦 批量操作<br>BatchActions"]
    
    %% Batch Actions
    Cmd_BatchActions --> Cmd_MarkHandled["🟦 标记为已处理<br>MarkAsHandled"]
    Cmd_BatchActions --> Cmd_ExportReport["🟦 导出风险报告<br>ExportReport"]
    
    %% Task Dispatch
    Cmd_MarkHandled --> Agg_Report
    Agg_Report --> Evt_TaskDone["🟧 风险已处理<br>RiskHandled"]

```
流程描述：**酒店轻量化风险监测**流程，服务于 Story 3。
- **触发**：每日固定时间（**早8点 & 晚8点**）触发。
- **生成**：系统基于过去12小时湿度数据，生成《高风险房间清单》。
- **触达**：推送给经理端小程序，经理无需登录PC即可查看。
- **仪表盘视图**：经理可以查看风险概览、今日高风险房间列表和设备状态概览。
- **批量操作**：支持标记为已处理、导出风险报告。
- **闭环**：工作人员现场处理（通风/除湿）后标记反馈，系统记录处理结果以修正模型。

### 流程四：资产保全闭环 (Asset Protection Loop)

> **业务价值**：自动处理设备被拆除/损坏场景，实现资产零流失与零上门。

```mermaid
graph TD
    %% Triggers
    Ext_Device["🟥 设备"] -->|防拆开关弹起| Evt_Tampered["🟧 设备被拆除<br>DeviceTampered"]
    
    %% Ops Process
    Evt_Tampered --> Pol_Asset["🟪 资产保全策略"]
    Pol_Asset --> Cmd_Calc["🟦 计算赔付金<br>CalculateCompensation"]
    Cmd_Calc --> Agg_Asset["🟨 资产赔付<br>AssetCompensate"]
    Agg_Asset --> Evt_Pending["🟧 赔付待确认<br>CompensationPending"]
    
    %% Interaction
    Evt_Pending --> View_User["🟩 用户小程序/异常告警"]
    View_User --> Actor_User["👤 用户"]
    
    %% Branch
    Actor_User -->|确认赔付| Cmd_Pay["🟦 支付赔偿<br>PayCompensation"]
    Actor_User -->|申请返修| Cmd_Mail["🟦 邮寄返修<br>MailBackDevice"]
    Actor_User -->|误触恢复| Cmd_Restore["🟦 恢复设备<br>RestoreDevice"]
    
    %% Result
    Cmd_Pay --> Agg_Asset
    Agg_Asset --> Evt_Paid["🟧 赔付已完成<br>CompensationPaid"]
    
    Cmd_Mail --> Agg_Asset
    Agg_Asset --> Evt_Mailed["🟧 设备已寄回<br>DeviceMailed"]
    
    Cmd_Restore --> Agg_Device["🟨 设备"]
    Agg_Device --> Evt_Online["🟧 设备已恢复<br>DeviceRecovered"]
    Evt_Online --> Agg_Asset
    Agg_Asset --> Evt_Closed["🟧 工单自动关闭<br>TicketClosed"]

```
流程描述：**资产保全闭环**流程，服务于 Story 1。
- **触发**：设备防拆开关触发或长期异常离线。
- **计算**：运维上下文自动计算剩余租期价值与违约金；对于押金式租赁设备，系统需在 `CalculateCompensation` 中优先使用押金余额抵扣应付赔偿金，仅在押金不足时才生成补差支付请求。
- **触达**：直接推送给用户，告知"设备异常需赔付"。
- **分支**：
  - 用户承认损坏：系统先自动扣减押金，再引导用户支付超出押金部分，完成赔付后工单自动关闭。
  - **用户申请返修**：系统生成寄件码，用户寄回设备；设备验收正常后，系统根据结果退还部分或全部押金。
  - 用户误触：重新安装设备，设备上线后工单自动关闭，押金状态不发生变更。

### 流程五：首次配置与服务订阅 (Provisioning & Subscription)

> **业务价值**：实现设备上电即用，零技术门槛，并快速激活服务订阅，支撑"零上门"愿景。

```mermaid
graph TD
    %% Trigger
    Actor_User["👤 用户"] -->|通电| Ext_Device["🟥 设备"]
    Ext_Device -->|Join Request| Cmd_Join["🟦 请求入网<br>JoinNetwork"]
    
    %% IoT Platform
    Cmd_Join --> Agg_Device["🟨 设备<br>Device"]
    Agg_Device -->|Auth Check| Pol_Auth["🟪 鉴权策略"]
    Pol_Auth -->|Valid| Evt_Joined["🟧 设备已入网<br>DeviceJoined"]
    
    %% Provisioning
    Evt_Joined --> Cmd_Prov["🟦 自动配置<br>AutoProvision"]
    Cmd_Prov --> Agg_Device
    Agg_Device --> Evt_Ready["🟧 设备已就绪<br>DeviceProvisioned"]
    
    %% Binding & Subscription
    Evt_Ready --> View_App["🟩 用户小程序/发现设备"]
    View_App --> Actor_User
    Actor_User -->|输入SN码| Cmd_Bind["🟦 绑定设备<br>BindDevice"]
    Cmd_Bind --> Agg_Device
    Agg_Device --> Evt_Bound["🟧 设备已绑定<br>DeviceBound"]

    Evt_Bound --> Cmd_ActivateSub["🟦 激活试用订阅<br>ActivateTrialSubscription"]
    Cmd_ActivateSub --> Agg_Sub["🟨 订阅<br>Subscription"]
    Agg_Sub --> Evt_SubActive["🟧 订阅已激活<br>SubscriptionActivated"]
    
    %% Linkage Config
    Evt_SubActive --> View_Config["🟩 联动配置页"]
    View_Config --> Actor_User
    Actor_User --> Cmd_Config["🟦 配置按键映射<br>ConfigureButtonMapping"]
    Cmd_Config --> Agg_Device
    Agg_Device --> Evt_Configured["🟧 联动已配置<br>LinkageConfigured"]

    %% First Value
    Evt_Configured --> Timer_24h["🕒 24小时定时器"]
    Timer_24h --> Cmd_GenFirstReport["🟦 生成首份报告<br>GenerateFirstReport"]
    Cmd_GenFirstReport --> Agg_Report["🟨 风险报告"]
    Agg_Report --> Evt_ReportReady["🟧 首份报告已生成<br>FirstReportGenerated"]
    Evt_ReportReady --> View_Report["🟩 小程序/首份体验报告"]

```
流程描述：**首次配置与服务订阅**流程，服务于 Story 1 & 2。
- **触发**：用户给设备通电。
- **入网**：设备自动发送 LoRaWAN Join Request，平台自动鉴权并接受。
- **配置**：平台下发初始配置（上报频率、默认策略），设备进入就绪状态。
- **绑定**：用户打开小程序，**输入设备SN码**，完成绑定。
- **订阅**：绑定成功后，系统自动激活“首月免费试用订阅”。
- **配置**：用户在线配置LoRa开关面板的3个物理按键与排风扇/加热器的映射关系。
- **价值交付**：
    - 绑定后5分钟内（通过入网流程），用户可见实时温湿度。
    - 绑定24小时后，系统自动生成首份《霉菌风险评估报告》，展示服务价值。

### 流程六：异常与容错处理 (Exception Handling)

> **业务价值**：在网络或设备故障时保障系统鲁棒性，实现 CIT 定义的失败策略。

```mermaid
graph TD
    %% Scenario 1: Command Failure
    Cmd_Ctrl["🟦 下发控制指令<br>SendCommand"] --> Agg_Device["🟨 设备"]
    Agg_Device -.->|Timeout/NACK| Pol_Retry["🟪 重试策略<br>(Exp Backoff x3)"]
    Pol_Retry -->|Retry 1..3| Cmd_Ctrl
    Pol_Retry -->|Max Retries| Evt_Fail["🟧 指令执行失败<br>CommandFailed"]
    Evt_Fail --> Cmd_DLQ["🟦 进入死信队列<br>EnqueueDLQ"]
    Cmd_DLQ --> View_Ops["🟩 运维告警面板"]

    %% Scenario 2: Heartbeat Loss
    Timer_HB["🕒 心跳检测定时器"] --> Cmd_Check["🟦 检查在线状态<br>CheckConnectivity"]
    Cmd_Check --> Agg_Device
    Agg_Device -->|LastSeen > 1h| Evt_Offline["🟧 设备离线<br>DeviceOffline"]
    Evt_Offline --> Pol_Notify["🟪 通知策略<br>(User + Ops)"]
    Pol_Notify --> View_User["🟩 用户小程序/离线提示"]
    Pol_Notify --> View_OpsDashboard["🟩 运维控制台/告警列表"]
    
    %% Scenario 3: Device Tampering
    Ext_Device["🟥 设备"] -->|防拆开关弹起| Evt_Tampered["🟧 设备被拆除<br>DeviceTampered"]
    Evt_Tampered --> Pol_Asset["🟪 资产保全策略"]
    Pol_Asset --> Cmd_Calc["🟦 计算赔付金<br>CalculateCompensation"]
    Cmd_Calc --> Agg_Asset["🟨 资产赔付<br>AssetCompensate"]
    Agg_Asset --> Evt_Pending["🟧 赔付待确认<br>CompensationPending"]
    Evt_Pending --> View_OpsDashboard
    Evt_Pending --> View_User
    
    %% Scenario 4: High Risk Alert
    Evt_RiskHigh["🟧 高风险告警<br>HighRiskAlert"] --> Pol_RiskNotify["🟪 风险通知策略"]
    Pol_RiskNotify --> View_OpsDashboard
    Pol_RiskNotify --> View_Manager["🟩 经理小程序/风险告警"]
    
    %% Ops Actions
    Actor_Ops["👤 运维工程师"] --> Cmd_ViewAlarms["🟦 查看告警<br>ViewAlarms"]
    Cmd_ViewAlarms --> View_OpsDashboard
    View_OpsDashboard --> Cmd_HandleAlarm["🟦 处理告警<br>HandleAlarm"]
    Cmd_HandleAlarm --> Agg_Alarm["🟨 告警"]
    Agg_Alarm --> Evt_AlarmHandled["🟧 告警已处理<br>AlarmHandled"]
    
    %% Filtering
    Actor_Ops --> Cmd_FilterAlarms["🟦 筛选告警<br>FilterAlarms"]
    Cmd_FilterAlarms --> View_OpsDashboard
    
    %% System Overview
    Actor_Ops --> Cmd_ViewOverview["🟦 查看系统概览<br>ViewSystemOverview"]
    Cmd_ViewOverview --> View_OpsDashboard
    Evt_Offline --> Cmd_UpdateOverview["🟦 更新系统概览<br>UpdateSystemOverview"]
    Evt_Fail --> Cmd_UpdateOverview
    Evt_Tampered --> Cmd_UpdateOverview
    Cmd_UpdateOverview --> Agg_Overview["🟨 系统概览<br>SystemOverview"]
    Agg_Overview --> View_OpsDashboard
```
流程描述：**异常处理**流程。
- **指令失败**：控制指令下发后若无 ACK 或超时，触发指数退避重试（最多3次）。若仍失败，产生 `CommandFailed` 事件进入死信队列，人工介入。
- **心跳丢失**：系统定时检查设备 `LastSeen` 时间，若超过阈值（如1小时），产生 `DeviceOffline` 事件，通知用户检查电源或网络，并显示在运维控制台。
- **设备被拆**：设备防拆开关触发，产生 `DeviceTampered` 事件，系统自动计算赔付金额，并在用户小程序和运维控制台显示告警。
- **高风险告警**：当房间风险指数超过阈值时，产生 `HighRiskAlert` 事件，通知运维工程师和经理。
- **运维操作**：运维工程师可以查看系统概览、告警列表、筛选告警、处理告警，并更新系统状态。
- **系统概览**：实时更新用户总数、设备总数、今日告警数、活跃订阅数等关键指标。

---

## 3. 聚合根与命令映射 (Aggregate & Command Mapping)

### 3.1 核心域：防霉预测 (Mold Prediction Context)

| 聚合根 (Aggregate) | 命令 (Command) | 产生事件 (Domain Event) | 业务规则/不变量 (Invariant) |
| :--- | :--- | :--- | :--- |
| **RiskModel**<br>(风险模型) | `AssessMoldRisk`<br>(评估风险) | `MoldRiskDetected`<br>`EnvironmentSafe` | 1. 必须结合最近24h历史趋势计算<br>2. 必须加载该设备所在气候带参数 |
| **Microclimate**<br>(微气候档案) | `UpdateProfile`<br>(更新档案) | `ProfileOptimized` | 1. 档案数据至少每月更新一次 |
| **RiskReport**<br>(风险报告) | `GenerateRiskReport`<br>(生成报告) | `RiskListGenerated` | 1. 报告必须包含过去12小时的最高湿度和平均湿度 |

### 3.2 核心域：智能控制 (Smart Control Context)

| 聚合根 (Aggregate) | 命令 (Command) | 产生事件 (Domain Event) | 业务规则/不变量 (Invariant) |
| :--- | :--- | :--- | :--- |
| **InterventionPlan**<br>(干预计划) | `GeneratePlan`<br>`CompletePlan`<br>`CalculateEnergy` | `PlanCreated`<br>`RiskCleared`<br>`EnergySaved` | 1. 同一设备不能同时存在两个活跃计划<br>2. 勿扰时段禁止生成高噪音计划 |
| **UserPreference**<br>(用户偏好) | `UpdatePreference` | `PreferenceChanged` | 1. 舒适度阈值不能低于 40% (防过干) |

### 3.3 支撑域：设备连接 (Connectivity Context)

| 聚合根 (Aggregate) | 命令 (Command) | 产生事件 (Domain Event) | 业务规则/不变量 (Invariant) |
| :--- | :--- | :--- | :--- |
| **Device**<br>(设备) | `RegisterDevice`<br>`ReportTelemetry`<br>`ReportEvent`<br>`JoinNetwork`<br>`AutoProvision`<br>`BindDevice`<br>`ConfigureButtonMapping` | `DeviceRegistered`<br>`EnvironmentChanged`<br>`DeviceTampered`<br>`DeviceRecovered`<br>`DeviceJoined`<br>`DeviceBound`<br>`LinkageConfigured`<br>`CommandFailed`<br>`DeviceOffline` | 1. 设备ID全局唯一<br>2. 遥测数据不可篡改<br>3. 入网必须经过 DevEUI+AppKey 强校验 |

### 3.4 支撑域：交付运维 (Delivery & Ops Context)

| 聚合根 (Aggregate) | 命令 (Command) | 产生事件 (Domain Event) | 业务规则/不变量 (Invariant) |
| :--- | :--- | :--- | :--- |
| **WorkOrder**<br>(工单) | `CreateTicket`<br>`ResolveTicket` | `TicketCreated`<br>`HazardResolved` | 1. 只有"进行中"的工单才能被关闭<br>2. 必须关联有效的设备ID或房间号 |
| **DiagnosticReport**<br>(诊断报告) | `AutoDiagnose`<br>`ReportHealth` | `DiagnosticCompleted` | 1. 报告必须包含最近24小时的信号强度数据 |
| **AssetCompensate**<br>(资产赔付) | `CalculateCompensation`<br>`PayCompensation` | `CompensationPending`<br>`CompensationPaid` | 1. 赔付金额必须基于剩余租期或设备残值计算<br>2. 押金租赁场景下，赔付时必须优先扣减押金余额，并记录押金抵扣金额<br>3. 若押金不足以覆盖应付金额，必须生成补差支付请求，超出部分由用户支付<br>4. 只有 Pending 状态可支付 |

### 3.5 通用域：客户与订阅 (Customer & Subscription Context)

| 聚合根 (Aggregate) | 命令 (Command) | 产生事件 (Domain Event) | 业务规则/不变量 (Invariant) |
| :--- | :--- | :--- | :--- |
| **Subscription**<br>(订阅) | `Subscribe`<br>`PaySubscription` | `SubscriptionCreated`<br>`PaymentSuccess` | 1. 支付必须通过第三方支付网关回调确认 |
