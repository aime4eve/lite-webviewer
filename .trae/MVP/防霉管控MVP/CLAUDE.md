# CLAUDE.md

本文件为Claude Code (claude.ai/code) 在此代码库中工作时提供指导。

## 项目概述

SmartMoldGuard 是一个基于微服务的智能防霉监控和管理平台，具有设备管理、AI风险预测、远程控制、订阅服务和报表分析功能。系统采用微服务架构，后端使用Spring Cloud，前端使用Vue 3。

### 核心组件
- **后端服务**: Java Spring Boot微服务，使用Spring Cloud Netflix/Eureka进行服务发现
- **前端**: Vue 3 + Vite单页应用，具有基于角色的界面
- **基础设施**: PostgreSQL、Redis、InfluxDB、Kafka，通过Docker Compose和Kubernetes部署
- **测试**: 后端使用JUnit，前端使用Playwright进行端到端测试

## 常用开发命令

### 后端 (Java微服务)
每个服务都是独立的Maven项目。从服务目录构建和运行：
```bash
cd backend/device-service  # 或其他服务目录
mvn clean package          # 构建JAR
mvn spring-boot:run        # 使用默认端口运行（参见application.yml）
mvn test                   # 运行单元测试
```

**启动所有后端服务**（需要预构建的JAR）：
```bash
cd backend
./start_all.sh             # 启动设备、控制、AI、订阅、报表服务
```

**完整的Docker环境**：
```bash
./start_docker.sh          # 通过Docker Compose构建并启动所有服务
./start_docker.sh --force  # 强制终止占用端口的进程
```

### 前端 (Vue 3)
```bash
cd frontend
npm install                # 安装依赖
npm run dev                # 开发服务器（默认端口）
npm run dev:b              # B端商户界面（端口6699）
npm run dev:operator       # 运维人员界面（端口6677）
npm run build              # 生产构建
npm run preview            # 预览生产构建
```

**前端测试** (Playwright)：
```bash
cd frontend
npx playwright test        # 运行端到端测试
npx playwright show-report # 查看测试报告
```

### Kubernetes部署
```bash
cd k8s
./deploy.sh                # 部署到Kubernetes（需要k8s集群）
kubectl apply -f .         # 应用所有清单文件
```

## 架构和关键技术

### 服务端口映射
| 服务 | 默认端口 | 描述 |
|------|----------|------|
| 网关服务 | 9999 | API网关 (Spring Cloud Gateway) |
| 发现服务 | 8761 | Eureka服务注册中心 |
| 设备服务 | 8081 | 设备生命周期管理 |
| 控制服务 | 8084 (通常8089) | 防霉控制策略（start_all.sh使用8089） |
| AI服务 | 8083 | 风险预测和健康分析 |
| 订阅服务 | 8085 | 租户订阅和计费 |
| 报表服务 | 8087 | 分析和仪表板 |
| 前端 | 6690 | Vue应用（开发端口因角色而异） |

**注意**: 端口可以通过 `-Dserver.port` 或 `SERVER_PORT` 环境变量覆盖。`start_all.sh` 脚本使用特定端口（控制服务使用8089）。Docker Compose将容器端口映射到主机端口，如 `docker-compose.yml` 中定义。

### 数据库和中间件
- **PostgreSQL**: 主要关系型数据库（端口5432）
- **Redis**: 缓存（端口16379）
- **InfluxDB**: 遥测数据的时间序列数据库（端口8086）
- **Kafka**: 异步通信的消息队列（端口19092）
- **Zookeeper**: Kafka协调（端口2181）

### API网关路由
所有API请求都通过网关（端口9999）路由：
- `/api/v1/devices/**` → 设备服务
- `/api/v1/risk-prediction/**` → AI服务
- `/api/v1/auto-mold-strategy/**` → 控制服务
- `/api/v1/subscription/**` → 订阅服务
- `/api/v1/reports/**` → 报表服务

## 代码结构

### 后端微服务
每个服务都遵循简洁/DDD风格的结构：
```
src/main/java/com/smartmoldguard/{service}/
├── application/           # 应用服务、DTO、用例
├── domain/               # 领域模型、事件、业务逻辑
├── infrastructure/       # 持久化、消息传递、外部集成
├── interfaces/           # REST控制器、API模型
└── config/               # Spring配置
```

关键模式：
- **领域驱动设计**: 聚合、领域事件、仓储
- **六边形架构**: 领域、应用、基础设施的分离
- **事件驱动**: 使用Kafka进行服务间通信
- **CQRS**: 在适当的地方分离读写模型

### 前端结构
```
src/
├── api/                  # API客户端定义
├── components/           # 可复用的Vue组件
├── router/               # Vue Router配置
├── views/                # 页面级组件
│   ├── b端/             # B端界面
│   ├── c端/             # C端界面
│   ├── operator/         # 运维人员界面
│   └── ...              # 功能特定视图
└── assets/               # 静态资源
```

## 开发工作流

### 本地开发
1. **启动基础设施**: `docker-compose up` 启动数据库和消息队列
2. **构建服务**: 在每个服务目录中运行 `mvn clean package`
3. **运行服务**: 使用 `start_all.sh` 或单独运行 `mvn spring-boot:run`
4. **运行前端**: 使用适当的角色端口运行 `npm run dev`
5. **测试**: 使用 `mvn test` 运行单元测试，使用Playwright运行端到端测试

### 测试策略
- **单元测试**: 每个服务中使用JUnit 5和Mockito
- **集成测试**: 使用Testcontainers的Spring Boot测试切片
- **端到端测试**: 使用Playwright进行前端用户流程测试
- **自动化测试**: 专门的 `automation-tests` 模块用于跨服务场景

### Kubernetes开发
- `k8s/` 中的清单文件遵循编号部署顺序
- 为 `smartmoldguard.local` 域配置Ingress
- 服务使用ClusterIP，网关作为入口点

## 配置

### 后端配置
每个服务都有 `src/main/resources/application.yml`，包含：
- 服务器端口配置
- 数据库连接（每个服务的PostgreSQL）
- Eureka客户端配置
- Kafka引导服务器
- 功能开关和业务规则

### 环境变量
关键变量（在Docker/Kubernetes中设置）：
- `SPRING_DATASOURCE_URL`: PostgreSQL连接字符串
- `SPRING_KAFKA_BOOTSTRAP_SERVERS`: Kafka代理地址
- `EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE`: Eureka服务器位置
- `INFLUX_URL`: 时间序列数据的InfluxDB连接

## 重要注意事项

- **服务依赖**: 必须先启动发现服务，然后是网关，最后是业务服务
- **端口冲突**: 使用 `start_docker.sh --force` 释放占用的端口
- **数据库模式**: 每个服务都有独立的PostgreSQL数据库（device_db、ai_db等）
- **Kafka主题**: 服务通过特定领域的Kafka主题进行通信
- **前端角色**: 不同用户角色使用不同端口（B端、运维人员、客户）

## 参考文档
- [README.md](README.md) - 项目概述和快速开始
- 每个服务目录中的后端服务README
- 每个服务的Swagger UI API文档，地址为 `http://localhost:{port}/swagger-ui.html`