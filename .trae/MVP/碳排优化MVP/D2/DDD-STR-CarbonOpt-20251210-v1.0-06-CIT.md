# 06-CIT 上下文集成表 (Context Integration Table)

> **编号**：DDD-STR-CarbonOpt-20251210-v2.0  
> **状态**：Final  
> **版本说明**：深化版，包含 Payload 定义与 SLA

---

## 1. 集成契约清单 (Integration Contracts)

| 集成方向 (A → B) | 业务场景 | 集成模式 | 协议/技术 | 防腐层 (ACL) | SLA 要求 | 失败策略 |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **Monitoring → Optimization** | 提供实时负荷数据用于 AI 预测 | **Async Event** | Kafka (`energy.meter.reading`) | **Yes** | 延迟 ≤ 1s<br>丢包率 = 0 | 忽略非关键丢包，断点续传 |
| **Optimization → EdgeControl** | 下发削峰/调优控制指令 | **RPC (Sync)** | gRPC / HTTP2 | **Yes** | 响应 ≤ 200ms | 重试 3 次，失败转人工告警 |
| **EdgeControl → Optimization** | 上报指令执行结果 (ACK) | **Async Event** | MQTT (`device.cmd.ack`) | **No** | 延迟 ≤ 1s | 至少一次 (At-least-once) |
| **DeviceAsset → Monitoring** | 同步设备基础信息变更 | **Domain Event** | Kafka (`asset.device.changed`) | **Yes** | 最终一致性 (≤ 1min) | 死信队列 (DLQ) 处理 |
| **Monitoring → DigitalTwin** | 推送实时状态至 3D 大屏 | **Stream** | WebSocket | **No** | 刷新率 ≥ 30fps | 自动重连 |
| **Auth → All** | 传递当前用户信息 | **Shared Kernel** | JWT Header | **No** | 解析耗时 ≤ 5ms | 拒绝请求 (401) |

---

## 2. 关键数据结构定义 (Payload Definitions)

### 2.1 实时能耗事件 (Event: MeterReading)
*   **Topic**: `energy.meter.reading`
*   **Schema Registry**: `v1.2`

```json
{
  "eventId": "evt_550e8400-e29b",
  "timestamp": 1733817600000,
  "producer": "service.monitoring",
  "data": {
    "deviceId": "dev_meter_001",
    "tenantId": "tnt_888",
    "metrics": {
      "activePower": 850.5,    // kW (有功功率)
      "reactivePower": 12.3,   // kVar (无功功率)
      "voltageA": 220.1,       // V (A相电压)
      "currentA": 10.5,        // A (A相电流)
      "powerFactor": 0.95      // 功率因数
    },
    "quality": "GOOD" // GOOD, BAD, UNCERTAIN
  }
}
```

### 2.2 控制指令 (Command: ControlDevice)
*   **Interface**: `EdgeControlService.ExecuteCommand` (gRPC)

```protobuf
message ControlCommand {
  string command_id = 1;      // 幂等性ID
  string target_device_id = 2;
  string strategy_id = 3;     // 关联的策略ID (用于溯源)
  
  enum ActionType {
    SWITCH_ON = 0;
    SWITCH_OFF = 1;
    SET_TEMP = 2;             // 设定温度
    SET_POWER_LIMIT = 3;      // 设定功率限制
  }
  ActionType action = 4;
  
  double value = 5;           // 参数值 (如 26.0)
  int32 priority = 6;         // 优先级 (0-10, 10最高)
  int64 expire_at = 7;        // 过期时间戳 (过期未执行则丢弃)
}
```

### 2.3 设备变更事件 (Event: DeviceRegistered)
*   **Topic**: `asset.device.changed`

```json
{
  "eventId": "evt_asset_001",
  "eventType": "DEVICE_REGISTERED", // or DEVICE_UPDATED, DEVICE_DELETED
  "data": {
    "deviceId": "dev_ac_001",
    "name": "办公区1号空调",
    "model": "Gree-GMV6",
    "type": "HVAC",
    "location": {
      "building": "A座",
      "floor": "3F",
      "zone": "East"
    },
    "protocols": ["Modbus-RTU", "BacNet"],
    "ratedPower": 15.0 // kW (额定功率)
  }
}
```

---

## 3. 防腐层设计 (ACL Design)

### Monitoring ACL (in Optimization Context)
*   **目的**: 隔离 Monitoring 上下文多变的原始数据格式，转化为 Optimization 上下文需要的纯净`TimeSeries`对象。
*   **转换逻辑**:
    1.  订阅 `energy.meter.reading`。
    2.  过滤掉 `quality != GOOD` 的脏数据。
    3.  将 `voltage`/`current` 等非必要字段丢弃，只保留 `power`。
    4.  将时间戳对齐到最近的分钟级 (Snap to grid)。

### Edge ACL (in Optimization Context)
*   **目的**: 屏蔽底层硬件协议差异。
*   **转换逻辑**:
    1.  Optimization 发出通用的 `SET_TEMP(26)` 指令。
    2.  ACL 根据设备型号将其翻译为特定的寄存器操作 (e.g., `Register: 4001, Value: 260`)。
    3.  *注：此 ACL 实际部署在 EdgeControl 上下文中，作为 OHS 的一部分提供给上游。*
