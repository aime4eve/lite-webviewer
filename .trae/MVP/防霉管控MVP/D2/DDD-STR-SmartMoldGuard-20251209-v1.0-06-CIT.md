# 06-CIT 上下文集成表 (Context Integration Table)

> 编号：DDD-STR-SmartMoldGuard-20251209-v2.0
> 状态：Final
> 版本说明：深化版，包含详细API契约、SLA及一致性策略

## 1. 核心集成全景 (Integration Landscape)

| 消费者 (Consumer) | 提供者 (Provider) | 集成内容 | 模式 | 通信协议 | 一致性 | SLA (Latency/Reliability) |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Prediction** | **Connectivity** | 实时温湿度遥测数据 | Event | MQTT/Kafka | 最终一致 | < 1s / 99.9% |
| **Control** | **Prediction** | 霉菌风险预警事件 | Event | Kafka (Avro) | 最终一致 | < 500ms / 99.99% |
| **Control** | **Connectivity** | 设备控制指令 | Command | gRPC/HTTP | 强一致 | < 200ms / 99.5% |
| **Control** | **Subscription** | 权益校验 | Query | REST (Cache) | 弱一致(TTL 5m) | < 50ms / 99.9% |
| **Ops** | **Connectivity** | 实时信号诊断 (RSSI/SNR) | Query | gRPC/HTTP | 强一致 | < 500ms / 99.5% |
| **Ops** | **Connectivity** | 历史心跳/状态快照 | Query | REST | 弱一致 | < 200ms / 99.9% |
| **Subscription** | **PaymentGateway** | 订阅扣款(微信/支付宝) | Command | REST | 强一致 | < 3s / 99.99% |
| **WeChatApp** | **Control** | 用户配置联动 | Command | REST | 强一致 | < 500ms / 99.9% |
| **Reporting** | **Prediction** | 历史风险数据归档 | Batch | S3 + Spark | 每日一致 | T+1 / 99.9% |

## 2. 详细接口契约 (Interface Contracts)

### 2.1 霉菌风险检测事件 (Event: MoldRiskDetected)
*   **Topic**: `smartmoldguard.prediction.risk`
*   **Schema Registry**: `schema-registry:8081/subjects/risk-value/versions/latest`
*   **JSON Example**:
    ```json
    {
      "eventId": "evt_99887766",
      "eventType": "MoldRiskDetected",
      "timestamp": "2025-12-09T03:00:00Z",
      "data": {
        "deviceId": "dev_001",
        "riskScore": 0.85,          // 0.0 - 1.0
        "confidence": 0.92,         // 模型置信度
        "predictedGrowthTime": 360, // 预计发霉倒计时(分钟)
        "triggerFactors": [
          {"factor": "humidity", "value": 92.0, "weight": 0.7},
          {"factor": "temp_stable", "value": 24.0, "weight": 0.3}
        ]
      }
    }
    ```

### 2.2 设备控制指令 (Command: SendDeviceCommand)
*   **Service Definition**:
    ```protobuf
    service DeviceControlService {
      // 幂等接口：需携带 idempotency_key
      rpc SendCommand (CommandRequest) returns (CommandResponse);
    }

    message CommandRequest {
      string device_id = 1;
      string command_type = 2; // e.g., "SET_FAN_SPEED"
      map<string, string> params = 3; // {"speed": "low", "duration": "120m"}
      string idempotency_key = 4;
      int32 priority = 5; // 0=User, 1=AutoRule, 2=OpsDiagnosis
    }
    ```

## 3. 非功能性保障 (Non-Functional Requirements)

### 3.1 容错与重试 (Fault Tolerance)
*   **Control -> Connectivity**: 
    *   采用 **指数退避重试 (Exponential Backoff)** 策略：初始间隔 1s，最大重试 3 次。
    *   若最终失败，发送 `InterventionFailed` 事件到死信队列 (DLQ) 进行人工/后台审计。

### 3.2 流量削峰 (Throttling)
*   **Prediction -> Control**: 
    *   预测服务可能在短时间（如暴雨突袭）产生海量风险事件。
    *   控制服务需实现 **滑动窗口限流**，优先处理高风险（Risk > 0.9）事件，丢弃或延迟处理低风险事件。

### 3.3 契约测试策略 (Contract Testing)
*   工具：**Pact** (Consumer-Driven Contract)
*   流程：
    1.  `Control` (Consumer) 定义期望收到的 `RiskDetected` 事件格式 (Pact file)。
    2.  `Prediction` (Provider) 在 CI 流水线中运行 Pact Verifier，确保发出的事件符合 Consumer 期望。
    3.  任何破坏 Schema 兼容性的变更（如删除字段）将导致构建失败。
