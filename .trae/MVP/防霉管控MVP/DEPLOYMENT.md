# SmartMoldGuard 部署指南

本指南详细说明了如何将 SmartMoldGuard 微服务系统部署到 Kubernetes 集群中。

## 1. 环境要求

在开始部署之前，请确保您的环境满足以下要求：

*   **Kubernetes Cluster**: 版本 1.20+ (推荐使用 Minikube, K3s 或云厂商 K8s)
*   **kubectl**: 已安装并配置好连接到您的集群
*   **Docker**: 用于构建镜像 (如果需要重新构建)
*   **资源需求**: 至少 4 vCPU, 8GB RAM (开发测试环境)

## 2. 目录结构

部署文件位于项目根目录下的 `k8s/` 文件夹中：

```
k8s/
├── 00-namespace.yaml       # 命名空间定义
├── 01-postgres.yaml        # PostgreSQL 数据库
├── 02-redis.yaml           # Redis 缓存
├── 03-kafka.yaml           # Kafka & Zookeeper 消息队列
├── 04-influxdb.yaml        # InfluxDB 时序数据库
├── 05-discovery.yaml       # Eureka 服务发现
├── 06-gateway.yaml         # Spring Cloud Gateway 网关
├── 10-device-service.yaml  # 设备服务
├── 11-ai-service.yaml      # AI 预测服务
├── 12-control-service.yaml # 智能控制服务
├── 13-subscription-service.yaml # 订阅服务
├── 14-report-service.yaml  # 报表服务
├── 20-frontend.yaml        # 前端应用 (Nginx)
├── 30-ingress.yaml         # Ingress 路由配置
└── deploy.sh               # 一键部署脚本
```

## 3. 快速部署

我们提供了一个 Shell 脚本来简化部署流程。

1.  进入部署目录：
    ```bash
    cd k8s
    ```

2.  赋予脚本执行权限（如果尚未拥有）：
    ```bash
    chmod +x deploy.sh
    ```

3.  执行部署脚本：
    ```bash
    ./deploy.sh
    ```

该脚本将按顺序应用 Kubernetes 配置文件，并在基础设施启动后部署微服务。

## 4. 手动部署

如果您更喜欢手动控制，可以按照以下顺序执行命令：

### 4.1 创建命名空间
```bash
kubectl apply -f 00-namespace.yaml
```

### 4.2 部署基础设施
```bash
kubectl apply -f 01-postgres.yaml
kubectl apply -f 02-redis.yaml
kubectl apply -f 03-kafka.yaml
kubectl apply -f 04-influxdb.yaml
```
*建议等待 30-60 秒，确保数据库和消息队列完全启动。*

### 4.3 部署基础服务
```bash
kubectl apply -f 05-discovery.yaml
kubectl apply -f 06-gateway.yaml
```

### 4.4 部署业务微服务
```bash
kubectl apply -f 10-device-service.yaml
kubectl apply -f 11-ai-service.yaml
kubectl apply -f 12-control-service.yaml
kubectl apply -f 13-subscription-service.yaml
kubectl apply -f 14-report-service.yaml
```

### 4.5 部署前端与 Ingress
```bash
kubectl apply -f 20-frontend.yaml
kubectl apply -f 30-ingress.yaml
```

## 5. 验证部署

查看所有 Pod 的状态：
```bash
kubectl get pods -n smartmoldguard
```
确保所有 Pod 的状态均为 `Running`。

## 6. 访问服务

### 配置 Hosts
如果使用的是本地集群（如 Minikube），您可能需要将 Ingress 域名添加到 `/etc/hosts` 文件中：

```
<MINIKUBE_IP> smartmoldguard.local
```

### 访问地址
*   **前端页面**: http://smartmoldguard.local
*   **API 网关**: http://smartmoldguard.local/api
*   **Eureka 控制台**: http://<NODE_IP>:30001 (需要通过 NodePort 暴露，默认配置为 ClusterIP)

## 7. 常见问题

*   **镜像拉取失败**: 默认配置使用 `imagePullPolicy: IfNotPresent`。如果您在本地构建了镜像，请确保它们在 Kubernetes 节点上可用（对于 Minikube，使用 `minikube docker-env`）。
*   **服务无法连接**: 检查 CoreDNS 是否正常工作，确保服务名称能够正确解析。
*   **数据库连接失败**: 检查 `postgres-init-script` 是否成功执行，数据库是否已创建。

---
**注意**: 生产环境部署建议修改默认密码和密钥，并配置持久化存储类 (StorageClass)。
