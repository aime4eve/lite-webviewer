# SmartMoldGuard - 智能模具监控系统

SmartMoldGuard 是一个基于微服务架构的智能模具监控与管理平台，集成了设备管理、AI 风险预测、远程控制、订阅服务及报表分析等功能。本项目采用前后端分离架构，后端基于 Spring Cloud Alibaba/Netflix 技术栈，前端基于 Vue 3 + Vite，并支持 Kubernetes 容器化部署。

## � 项目目录结构 (Project Structure)

*   **`backend/`**: 后端微服务代码库
    *   `gateway-service`: API 网关 (Spring Cloud Gateway)
    *   `discovery-service`: 服务注册与发现 (Eureka)
    *   `device-service`: 设备核心业务 (MyBatis Plus, Kafka)
    *   `control-service`: 模具控制策略与计划
    *   `ai-service`: AI 风险预测与健康指纹分析
    *   `subscription-service`: 订阅管理与积分系统
    *   `report-service`: 数据报表与仪表盘
    *   `automation-tests`: 自动化测试模块
*   **`frontend/`**: 前端应用代码库 (Vue 3, Vite, Pinia, Axios)
*   **`k8s/`**: Kubernetes 部署清单 (Deployment, Service, Ingress, ConfigMap)

## 🏗️ 系统架构 (Architecture)

### 后端技术栈 (Backend)
*   **核心框架**: Spring Boot 3.2.0, Spring Cloud 2023.0.0
*   **服务治理**: Netflix Eureka (注册中心), Spring Cloud Gateway (网关)
*   **数据库**: PostgreSQL (业务数据), InfluxDB (时序数据), Redis (缓存)
*   **消息队列**: Apache Kafka (异步解耦与削峰)
*   **API 文档**: SpringDoc OpenAPI (Swagger UI)
*   **构建工具**: Maven

### 前端技术栈 (Frontend)
*   **框架**: Vue 3.5.24
*   **构建工具**: Vite 7.2.4
*   **路由**: Vue Router 4.6.4
*   **HTTP 客户端**: Axios
*   **测试**: Playwright

### 基础设施 (Infrastructure)
*   **容器化**: Docker, Kubernetes
*   **Ingress**: Nginx Ingress Controller

## 🔌 服务与端口映射 (Services & Ports)

| 服务名称 | 服务 ID | 默认端口 | 职责描述 |
| :--- | :--- | :--- | :--- |
| **Gateway Service** | `gateway-service` | **9999** | 统一 API 入口，路由转发，鉴权 |
| **Discovery Service** | `discovery-service`| **8761** | Eureka 服务注册中心 |
| **Device Service** | `device-service` | **8081** | 设备生命周期管理，告警，诊断 |
| **Control Service** | `control-service` | **8089*** | 自动化策略，模具控制 (端口已调整) |
| **AI Service** | `ai-service` | **8083** | 风险预测模型，健康度分析 |
| **Subscription** | `subscription-service`| **8084** | 租户订阅，计费，积分 |
| **Report Service** | `report-service` | **8085** | 统计报表，数据可视化 |

*\*注：Control Service 端口在本地开发环境中可能调整为 8089 以避免冲突。*

## �️ API 路由规则 (Gateway Routes)

所有 API 请求通过 Gateway (`port: 9999`) 转发：

*   `/api/v1/devices/**` -> `device-service`
*   `/api/v1/risk-prediction/**` -> `ai-service`
*   `/api/v1/auto-mold-strategy/**` -> `control-service`
*   `/api/v1/subscription/**` -> `subscription-service`
*   `/api/v1/reports/**` -> `report-service`

## ☸️ Kubernetes 部署 (K8s Deployment)

Kubernetes 配置文件位于 `k8s/` 目录下，包含完整的环境搭建脚本：

1.  **基础组件**: `01-postgres.yaml`, `02-redis.yaml`, `03-kafka.yaml`, `04-influxdb.yaml`
2.  **核心服务**: `05-discovery.yaml`, `06-gateway.yaml`, `10-device-service.yaml` 等
3.  **Ingress**: `30-ingress.yaml` 配置了域名 `smartmoldguard.local`
    *   `http://smartmoldguard.local/api` -> Gateway
    *   `http://smartmoldguard.local/` -> Frontend

## 🚀 快速开始 (Quick Start)

### 本地开发 (Local Development)

1.  **启动后端**:
    *   确保本地已安装 JDK 21, Maven, Docker (用于中间件)。
    *   启动 PostgreSQL, Redis, Kafka。
    *   依次启动 Discovery, Gateway 及各微服务。
    
2.  **启动前端**:
    ```bash
    cd frontend
    npm install
    npm run dev        # 默认开发模式
    # 或指定角色端口
    npm run dev:b      # B端商户 (Port 6699)
    npm run dev:operator # 运维端 (Port 6677)
    ```

---

### 📝 当前环境状态 (Current Environment Status)

> 更新时间: 2025-12-28

已完成前端对接完善，并成功启动所有后端服务集群。目前全链路环境已就绪。

*   **Frontend**: Running at [http://localhost:6690/](http://localhost:6690/)
*   **Backend Cluster**: All services running. `control-service` using port **8089**.
*   **Kafka/DB**: Connected and healthy.
*   **Nacos**: Running at [http://localhost:8848/nacos/index.html](http://localhost:8848/nacos/index.html)
*   **Device Service API**: [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)
