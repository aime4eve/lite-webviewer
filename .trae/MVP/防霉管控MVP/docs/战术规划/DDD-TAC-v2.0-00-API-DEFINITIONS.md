# SmartMoldGuard 防霉管控系统 - API 接口定义文档

> **编号**: API-DEFINITIONS-SmartMoldGuard-20251215-v1.0
> **状态**: Final
> **版本说明**: 基于前端高保真实现的后端API接口定义
> **依据**: 
> - UX-PROTOTYPES-SmartMoldGuard-20251212-v1.0
> - USER-STORIES-SmartMoldGuard-20251212-v2.0
> **术语引用**: 本文档使用《SmartMoldGuard-统一术语表》（v1.0）定义的标准术语

## 1. 接口规范

### 1.1 基本信息
- **协议**: HTTP/HTTPS
- **编码**: UTF-8
- **请求方法**: GET, POST, PUT, DELETE
- **响应格式**: JSON
- **API 版本**: v1
- **基础路径**: `/api/v1`

### 1.2 响应格式
```json
{
  "code": 200,          // 状态码
  "message": "success", // 响应信息
  "data": {}            // 响应数据
}
```

### 1.3 状态码
| 状态码 | 描述 |
| :--- | :--- |
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 2. 设备管理模块

### 2.1 获取设备列表
- **接口路径**: `/devices`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | page | int | 否 | 页码，默认1 |
  | pageSize | int | 否 | 每页条数，默认10 |
  | status | string | 否 | 设备状态：online/offline |

- **响应数据**:
```json
{
  "total": 3,
  "onlineCount": 2,
  "offlineCount": 1,
  "list": [
    {
      "id": 1,
      "name": "主卧浴室",
      "description": "温湿度传感器 + 3位开关面板",
      "status": "online",
      "icon": "🏠",
      "location": "主卧",
      "createdAt": "2025-12-01T10:00:00Z",
      "updatedAt": "2025-12-15T14:30:00Z"
    }
  ]
}
```

### 2.2 获取设备详情
- **接口路径**: `/devices/{id}`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |

- **响应数据**:
```json
{
  "id": 1,
  "name": "主卧浴室",
  "description": "温湿度传感器 + 3位开关面板",
  "status": "online",
  "icon": "🏠",
  "location": "主卧",
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "firmwareVersion": "v1.0.0",
  "lastOnlineTime": "2025-12-15T14:30:00Z",
  "createdAt": "2025-12-01T10:00:00Z",
  "updatedAt": "2025-12-15T14:30:00Z"
}
```

### 2.3 添加设备
- **接口路径**: `/devices`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | sn | string | 是 | 设备SN码 |
  | name | string | 是 | 设备名称 |
  | location | string | 否 | 设备位置 |
  | description | string | 否 | 设备描述 |

- **响应数据**:
```json
{
  "id": 4,
  "name": "书房",
  "status": "pending",
  "message": "设备绑定成功，正在初始化..."
}
```

### 2.4 更新设备信息
- **接口路径**: `/devices/{id}`
- **请求方法**: PUT
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |
  | name | string | 否 | 设备名称 |
  | location | string | 否 | 设备位置 |
  | description | string | 否 | 设备描述 |

- **响应数据**:
```json
{
  "id": 1,
  "name": "主卧浴室",
  "location": "主卧卫生间",
  "description": "温湿度传感器 + 3位开关面板",
  "updatedAt": "2025-12-15T14:35:00Z"
}
```

### 2.5 删除设备
- **接口路径**: `/devices/{id}`
- **请求方法**: DELETE
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |

- **响应数据**:
```json
{
  "message": "设备删除成功"
}
```

## 3. 环境监测模块

### 3.1 获取实时环境数据
- **接口路径**: `/devices/{id}/environment`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |

- **响应数据**:
```json
{
  "temperature": 23.5,
  "humidity": 72.0,
  "riskLevel": "medium",
  "riskIndex": 68.0,
  "dewPoint": 18.2,
  "updateTime": "2025-12-15T14:30:00Z"
}
```

### 3.2 获取霉菌风险预测
- **接口路径**: `/devices/{id}/risk-prediction`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |
  | hours | int | 否 | 预测小时数，默认3 |

- **响应数据**:
```json
{
  "deviceId": 1,
  "predictionTime": "2025-12-15T14:30:00Z",
  "predictionHours": 3,
  "riskIndex": 68.0,
  "riskLevel": "medium",
  "riskDescription": "3小时后霉菌风险为中等，建议开启排风扇",
  "recommendedAction": "开启排风扇"
}
```

## 4. 自动防霉策略模块

### 4.1 获取自动防霉策略
- **接口路径**: `/devices/{id}/auto-mold-strategy`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |

- **响应数据**:
```json
{
  "deviceId": 1,
  "enabled": true,
  "rules": [
    {
      "id": 1,
      "condition": "humidity > 85% and duration > 30min",
      "action": "turn_on_fan",
      "delay": 0
    },
    {
      "id": 2,
      "condition": "humidity > 60% after 30min",
      "action": "turn_on_heater",
      "delay": 30
    }
  ]
}
```

### 4.2 更新自动防霉策略
- **接口路径**: `/devices/{id}/auto-mold-strategy`
- **请求方法**: PUT
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |
  | enabled | boolean | 是 | 是否开启自动防霉 |
  | rules | array | 是 | 防霉规则列表 |

- **响应数据**:
```json
{
  "deviceId": 1,
  "enabled": true,
  "message": "自动防霉策略更新成功"
}
```

## 5. 设备联动映射模块

### 5.1 获取设备联动映射
- **接口路径**: `/devices/{id}/linkage-mapping`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |

- **响应数据**:
```json
{
  "deviceId": 1,
  "mappings": [
    {
      "switchPosition": 1,
      "deviceType": "fan",
      "deviceName": "排风扇",
      "icon": "🌀"
    },
    {
      "switchPosition": 2,
      "deviceType": "heater",
      "deviceName": "加热器",
      "icon": "🔥"
    },
    {
      "switchPosition": 3,
      "deviceType": "light",
      "deviceName": "照明灯",
      "icon": "💡"
    }
  ]
}
```

### 5.2 更新设备联动映射
- **接口路径**: `/devices/{id}/linkage-mapping`
- **请求方法**: PUT
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |
  | mappings | array | 是 | 联动映射列表 |

- **响应数据**:
```json
{
  "deviceId": 1,
  "message": "设备联动映射更新成功"
}
```

## 6. 订阅管理模块

### 6.1 获取订阅状态
- **接口路径**: `/subscription`
- **请求方法**: GET

- **响应数据**:
```json
{
  "status": "trial",
  "planName": "全功能防霉版",
  "startDate": "2025-12-15T00:00:00Z",
  "expiryDate": "2026-01-30T23:59:59Z",
  "remainingDays": 7,
  "features": [
    "实时监测与风险预警",
    "智能联动 (自动排风/加热)",
    "每日防霉报告 & 积分奖励"
  ]
}
```

### 6.2 获取订阅方案
- **接口路径**: `/subscription/plans`
- **请求方法**: GET

- **响应数据**:
```json
[
  {
    "id": 1,
    "name": "1年卡",
    "price": 240,
    "duration": 12,
    "unitPrice": 20,
    "description": "折合 ¥20/月",
    "recommended": false
  },
  {
    "id": 2,
    "name": "2年卡",
    "price": 440,
    "duration": 24,
    "unitPrice": 18.3,
    "description": "折合 ¥18.3/月 (立省 ¥40)",
    "recommended": false
  },
  {
    "id": 3,
    "name": "3年卡",
    "price": 600,
    "duration": 36,
    "unitPrice": 16.7,
    "description": "折合 ¥16.7/月 (立省 ¥120)",
    "recommended": true
  }
]
```

### 6.3 立即订阅
- **接口路径**: `/subscription/subscribe`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | planId | int | 是 | 订阅方案ID |
  | paymentMethod | string | 是 | 支付方式：wechat/alipay |

- **响应数据**:
```json
{
  "orderId": "ORD2025121514450001",
  "planId": 3,
  "amount": 600,
  "paymentUrl": "https://payment.example.com/pay?orderId=ORD2025121514450001",
  "message": "订阅订单创建成功，请完成支付"
}
```

## 7. 积分管理模块

### 7.1 获取积分信息
- **接口路径**: `/points`
- **请求方法**: GET

- **响应数据**:
```json
{
  "totalPoints": 180,
  "availablePoints": 180,
  "expiringPoints": 0,
  "pointsToMoneyRatio": 0.1
}
```

## 8. 用户管理模块

### 8.1 获取用户信息
- **接口路径**: `/user/profile`
- **请求方法**: GET

- **响应数据**:
```json
{
  "id": "SMG-20251215-001",
  "name": "张三",
  "avatar": "https://example.com/avatar.jpg",
  "phone": "13800138000",
  "email": "zhangsan@example.com",
  "createdAt": "2025-12-15T10:00:00Z"
}
```

### 8.2 更新用户信息
- **接口路径**: `/user/profile`
- **请求方法**: PUT
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | name | string | 否 | 用户名 |
  | avatar | string | 否 | 头像URL |
  | phone | string | 否 | 手机号 |
  | email | string | 否 | 邮箱 |

- **响应数据**:
```json
{
  "name": "张三",
  "avatar": "https://example.com/avatar.jpg",
  "updatedAt": "2025-12-15T14:50:00Z",
  "message": "用户信息更新成功"
}
```

## 9. 报告管理模块

### 9.1 获取防霉报告列表
- **接口路径**: `/reports`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | page | int | 否 | 页码，默认1 |
  | pageSize | int | 否 | 每页条数，默认10 |
  | type | string | 否 | 报告类型：daily/weekly/monthly |

- **响应数据**:
```json
{
  "total": 12,
  "list": [
    {
      "id": 1,
      "title": "2025-12-15 防霉日报",
      "type": "daily",
      "date": "2025-12-15",
      "riskLevel": "safe",
      "summary": "今日浴室湿度正常，无霉菌风险",
      "createdAt": "2025-12-15T23:00:00Z"
    }
  ]
}
```

## 10. 预测反馈模块

### 10.1 提交预测反馈
- **接口路径**: `/prediction-feedback`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | deviceId | int | 是 | 设备ID |
  | rating | int | 是 | 评分（1-5） |
  | comment | string | 否 | 反馈内容 |
  | riskLevel | string | 是 | 实际风险等级：safe/low/medium/high |

- **响应数据**:
```json
{
  "id": 1,
  "deviceId": 1,
  "rating": 5,
  "status": "submitted",
  "message": "反馈提交成功，感谢您的宝贵意见"
}
```

## 11. 防霉战报模块

### 11.1 获取防霉战报
- **接口路径**: `/dashboard/stats`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | period | string | 否 | 统计周期：week/month/year，默认month |
  | role | string | 否 | 用户角色：user/merchant/operator，默认user |

- **响应数据**:
```json
{
  "period": "month",
  "startDate": "2025-12-01",
  "endDate": "2025-12-31",
  "stats": {
    "moldBlockedCount": 12,
    "energySaved": 4.8,
    "pointsEarned": 180,
    "averageHumidity": 62.5,
    "maxHumidity": 85.0,
    "riskDays": 3
  }
}
```

## 12. B端商户管理模块

### 12.1 获取风险概览
- **接口路径**: `/b/dashboard/risk-overview`
- **请求方法**: GET

- **响应数据**:
```json
{
  "highRiskCount": 2,
  "mediumRiskCount": 5,
  "lowRiskCount": 18,
  "totalRooms": 25
}
```

### 12.2 获取今日高风险房间
- **接口路径**: `/b/dashboard/high-risk-rooms`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | page | int | 否 | 页码，默认1 |
  | pageSize | int | 否 | 每页条数，默认10 |

- **响应数据**:
```json
{
  "total": 7,
  "list": [
    {
      "id": 1,
      "name": "302室主卧浴室",
      "location": "金南家园1号楼1单元",
      "riskLevel": "high",
      "riskValue": 86,
      "humidity": 88,
      "temperature": 22,
      "updateTime": "2025-12-15T14:30:00Z"
    }
  ]
}
```

### 12.3 批量操作
- **接口路径**: `/b/batch-actions`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | action | string | 是 | 操作类型：assign_cleaning/mark_as_handled/export_report |
  | roomIds | array | 是 | 房间ID列表 |
  | cleanerId | int | 否 | 保洁员ID（仅assign_cleaning操作需要） |
  | note | string | 否 | 备注信息（仅assign_cleaning操作需要） |

- **响应数据**:
```json
{
  "message": "批量操作成功",
  "successCount": 3,
  "failedCount": 0
}
```

### 12.4 获取空间列表
- **接口路径**: `/b/spaces`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | page | int | 否 | 页码，默认1 |
  | pageSize | int | 否 | 每页条数，默认10 |
  | building | string | 否 | 楼栋筛选 |

- **响应数据**:
```json
{
  "total": 25,
  "list": [
    {
      "id": 1,
      "name": "302室主卧浴室",
      "building": "金南家园1号楼1单元",
      "deviceCount": 2,
      "riskLevel": "high",
      "lastUpdateTime": "2025-12-15T14:30:00Z"
    }
  ]
}
```

### 12.5 指派保洁
- **接口路径**: `/b/assign-cleaning`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | roomIds | array | 是 | 房间ID列表 |
  | cleanerId | int | 是 | 保洁员ID |
  | note | string | 否 | 备注信息 |

- **响应数据**:
```json
{
  "id": 1,
  "taskId": "TASK-20251216-001",
  "roomCount": 2,
  "status": "assigned",
  "message": "保洁任务已成功指派"
}
```

### 12.6 提交保洁反馈
- **接口路径**: `/b/cleaning-feedback`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | taskId | string | 是 | 任务ID |
  | content | string | 是 | 反馈内容 |
  | images | array | 否 | 图片URL列表 |
  | result | string | 是 | 处理结果：success/failed/partial |

- **响应数据**:
```json
{
  "id": 1,
  "taskId": "TASK-20251216-001",
  "status": "submitted",
  "message": "保洁反馈已成功提交"
}
```

## 13. 运营端管理模块

### 13.1 获取系统概览
- **接口路径**: `/operator/dashboard/overview`
- **请求方法**: GET

- **响应数据**:
```json
{
  "totalUsers": 1256,
  "totalDevices": 3892,
  "todayAlarms": 45,
  "activeSubscriptions": 987,
  "userTrend": { "text": "↑ 增长", "value": "+12.5%" },
  "deviceTrend": { "text": "↑ 增长", "value": "+8.3%" },
  "alarmTrend": { "text": "↓ 下降", "value": "-23.1%" },
  "subscriptionTrend": { "text": "↑ 增长", "value": "+15.7%" }
}
```

### 13.2 获取设备状态分布
- **接口路径**: `/operator/dashboard/device-status`
- **请求方法**: GET

- **响应数据**:
```json
{
  "onlineDevices": 3689,
  "offlineDevices": 178,
  "warningDevices": 25,
  "totalDevices": 3892
}
```

### 13.3 获取告警列表
- **接口路径**: `/operator/alarms`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | page | int | 否 | 页码，默认1 |
  | pageSize | int | 否 | 每页条数，默认20 |
  | type | string | 否 | 告警类型：tamper/offline/risk/all，默认all |
  | status | string | 否 | 告警状态：unhandled/handled/all，默认all |

- **响应数据**:
```json
{
  "total": 45,
  "list": [
    {
      "id": 1,
      "title": "金南家园三期 3502 防拆告警",
      "type": "tamper",
      "location": "金南家园三期 3502",
      "time": "2025-12-15T14:30:00Z",
      "status": "unhandled"
    }
  ]
}
```

### 13.4 处理告警
- **接口路径**: `/operator/alarms/{id}/handle`
- **请求方法**: PUT
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 告警ID |
  | remark | string | 否 | 处理备注 |
  | step | int | 是 | 当前处理步骤（1-3） |
  | processNotes | string | 否 | 流程处理备注 |

- **响应数据**:
```json
{
  "id": 1,
  "status": "handled",
  "message": "告警处理成功"
}
```

### 13.5 获取用户列表
- **接口路径**: `/operator/users`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | page | int | 否 | 页码，默认1 |
  | pageSize | int | 否 | 每页条数，默认20 |
  | role | string | 否 | 用户角色：user/merchant/operator |
  | status | string | 否 | 用户状态：active/inactive |

- **响应数据**:
```json
{
  "total": 1256,
  "list": [
    {
      "id": "SMG-20251215-001",
      "name": "张三",
      "role": "user",
      "phone": "13800138000",
      "email": "zhangsan@example.com",
      "status": "active",
      "createdAt": "2025-12-15T10:00:00Z"
    }
  ]
}
```

### 13.6 远程设备诊断
- **接口路径**: `/operator/devices/{id}/diagnose`
- **请求方法**: POST
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 设备ID |

- **响应数据**:
```json
{
  "id": 1,
  "deviceId": 1,
  "diagnoseId": "DIAG-20251216-001",
  "status": "diagnosing",
  "message": "远程诊断已开始，请稍候"
}
```

### 13.7 获取诊断结果
- **接口路径**: `/operator/diagnose/{diagnoseId}`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | diagnoseId | string | 是 | 诊断ID |

- **响应数据**:
```json
{
  "id": "DIAG-20251216-001",
  "deviceId": 1,
  "deviceName": "温湿度传感器",
  "deviceSn": "SN123456",
  "status": "completed",
  "signalStrength": -55,
  "batteryLevel": 78,
  "packetLoss": 2,
  "lastHeartbeat": "2025-12-15T14:29:30",
  "diagnostics": [
    {
      "item": "设备状态",
      "status": "异常",
      "message": "设备防拆开关触发"
    },
    {
      "item": "网络连接",
      "status": "正常",
      "message": "信号强度良好"
    },
    {
      "item": "电池电量",
      "status": "正常",
      "message": "电量充足"
    },
    {
      "item": "传感器数据",
      "status": "异常",
      "message": "温度传感器数据异常"
    }
  ],
  "recommendations": [
    "检查设备安装情况，确认防拆开关是否被触发",
    "重新校准温度传感器",
    "考虑更换设备外壳"
  ],
  "createdAt": "2025-12-16T15:00:00Z",
  "completedAt": "2025-12-16T15:02:30Z"
}
```

## 14. 告警与工单模块

### 14.1 获取告警详情
- **接口路径**: `/alarms/{id}`
- **请求方法**: GET
- **请求参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | id | int | 是 | 告警ID |

- **响应数据**:
```json
{
  "id": 1,
  "title": "金南家园三期 3502 防拆告警",
  "type": "tamper",
  "location": "金南家园三期 3502",
  "deviceSn": "SN123456",
  "time": "2025-12-15T14:30:00Z",
  "status": "unhandled",
  "description": "设备防拆开关触发，可能被非法拆除",
  "recommendedAction": "联系用户确认情况"
}
```

## 14. 部署与测试

### 14.1 本地开发环境
- **API Base URL**: `http://localhost:8080/api/v1`
- **数据库**: PostgreSQL
- **开发工具**: Spring Boot/Node.js

### 14.2 测试环境
- **API Base URL**: `https://test-api.smartmoldguard.com/api/v1`
- **认证方式**: JWT Token

### 14.3 生产环境
- **API Base URL**: `https://api.smartmoldguard.com/api/v1`
- **认证方式**: JWT Token

## 15. 安全规范

1. **认证与授权**:
   - 所有API请求必须包含有效的JWT Token
   - Token有效期为2小时，过期后需要重新获取

2. **数据加密**:
   - 所有敏感数据（如用户信息、设备SN码）必须加密存储
   - API传输必须使用HTTPS协议

3. **速率限制**:
   - 每个用户每分钟最多请求60次
   - 设备数据上报限制为每10秒一次

4. **日志记录**:
   - 所有API请求必须记录日志
   - 日志包含请求IP、用户ID、请求路径、响应状态码等信息

## 16. 版本管理

| 版本 | 日期 | 变更说明 | 变更人 |
| :--- | :--- | :--- | :--- |
| v1.0 | 2025-12-15 | 初始版本，包含核心功能API | 技术架构师 |
| v1.1 | 2026-01-15 | 新增设备批量管理API | 后端开发 |
| v2.0 | 2026-03-30 | 重构API，支持更多设备类型 | 技术架构师 |

## 17. 联系方式

- **API维护团队**: 后端开发组
- **联系邮箱**: api-support@smartmoldguard.com
- **文档更新周期**: 每月更新一次
- **紧急问题处理**: 24小时内响应

## 18. 附录

### 18.1 数据格式说明
- **日期时间格式**: ISO 8601，如：2025-12-15T14:30:00Z
- **温度单位**: 摄氏度（°C）
- **湿度单位**: 百分比（%）
- **能耗单位**: 千瓦时（kWh）

### 18.2 错误码说明
| 错误码 | 描述 |
| :--- | :--- |
| DEVICE_NOT_FOUND | 设备不存在 |
| INVALID_DEVICE_SN | 无效的设备SN码 |
| DEVICE_ALREADY_BOUND | 设备已被绑定 |
| SUBSCRIPTION_EXPIRED | 订阅已过期 |
| INSUFFICIENT_PERMISSIONS | 权限不足 |

### 18.3 设备状态说明
| 状态 | 描述 |
| :--- | :--- |
| online | 设备在线 |
| offline | 设备离线 |
| pending | 设备待激活 |
| error | 设备异常 |
| maintenance | 设备维护中 |

### 18.4 风险等级说明
| 等级 | 描述 | 风险指数范围 |
| :--- | :--- | :--- |
| safe | 安全 | 0-40 |
| low | 低风险 | 41-60 |
| medium | 中风险 | 61-80 |
| high | 高风险 | 81-100 |

---

**文档变更记录**:
- 2025-12-15: 初始版本
- 2025-12-15: 更新设备管理模块接口
- 2025-12-15: 补充环境监测模块接口
- 2025-12-15: 完善订阅管理模块接口
- 2025-12-15: 添加报告管理模块接口
- 2025-12-15: 补充防霉战报模块接口
- 2025-12-15: 添加安全规范和版本管理
- 2025-12-15: 补充附录说明
- 2025-12-16: 新增B端商户管理模块API
- 2025-12-16: 新增运营端管理模块API
- 2025-12-16: 新增告警与工单模块API
- 2025-12-16: 更新防霉战报模块，添加角色参数
- 2025-12-16: 修正文档章节编号

**文档审核**: 技术架构师
**文档批准**: 产品经理
**文档发布日期**: 2025-12-15
