# SmartMoldGuard 防霉管控系统 - DevOps 规划文档

> **编号**: DDD-TAC-SmartMoldGuard-20251217-v1.0-DevOps
> **状态**: Final
> **版本说明**: 轻量级单服务器 DevOps 落地操作规划
> **依据**: DDD-TAC-SmartMoldGuard-20251216-v1.0
> **目标**: 实现成本效益高、资源消耗低的完整 DevOps 工具链

基于 SmartMoldGuard 防霉管控系统的 DDD 战术设计，以下是在单台服务器上实现轻量级 DevOps 工具链的详细操作规划。本规划兼顾成本效益、可维护性和扩展性，采用轻量级工具链降低资源消耗。

## 一、环境准备与架构设计

### 1.1 服务器资源配置建议
**最低配置要求：**
- CPU: 8核
- 内存: 32GB
- 存储: 500GB SSD
- 操作系统: Ubuntu 22.04 LTS

**资源分配规划：**
```
┌─────────────────────────────────────────────────────┐
│ 单台服务器 (32GB RAM, 8核)                          │
├─────────────────────────────────────────────────────┤
│ Docker & Docker Compose (基础运行环境)              │
│                                                     │
│ ┌──────────────┐  ┌──────────────┐  ┌────────────┐│
│ │  DevOps工具区 │  │   服务运行区  │  │  数据存储区 ││
│ │ (约6GB内存)  │  │ (约18GB内存) │  │ (约8GB内存) ││
│ ├──────────────┤  ├──────────────┤  ├────────────┤│
│ │ GitLab CE    │  │ 8个微服务    │  │ PostgreSQL ││
│ │ Jenkins      │  │ API网关      │  │ InfluxDB   ││
│ │ Nexus        │  │              │  │ Redis      ││
│ │ ELK Stack    │  │              │  │            ││
│ │ Prometheus   │  │              │  │            ││
│ └──────────────┘  └──────────────┘  └────────────┘│
└─────────────────────────────────────────────────────┘
```

### 1.2 基础环境安装

```bash
# 1. 系统更新
sudo apt update && sudo apt upgrade -y

# 2. 安装 Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

# 3. 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 4. 配置 Docker 镜像加速（国内环境建议）
sudo mkdir -p /etc/docker
sudo tee /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://mirror.gcr.io", "https://docker-proxy.com"],
  "log-driver": "json-file",
  "log-opts": {"max-size": "10m", "max-file": "3"},
  "memory": "28g",
  "cpus": 7.5
}
EOF
sudo systemctl restart docker

# 5. 创建项目目录结构
mkdir -p /opt/smartmoldguard/{devops,services,data/{postgres,influxdb,redis},logs,backups}
cd /opt/smartmoldguard
```

---

## 二、DevOps 工具链部署

### 2.1 Gitea (代码管理)

```bash
# 创建 Gitea 配置
cat > devops/docker-compose.gitea.yml <<EOF
version: '3.8'
services:
  gitea:
    image: gitea/gitea:1.21.5
    container_name: gitea
    restart: unless-stopped
    environment:
      - GITEA__database__DB_TYPE=postgres
      - GITEA__database__HOST=postgres:5432
      - GITEA__database__NAME=gitea_db
      - GITEA__database__USER=gitea_user
      - GITEA__database__PASSWD=gitea_password
      - GITEA__server__DOMAIN=localhost
      - GITEA__server__HTTP_PORT=8080
      - GITEA__server__ROOT_URL=http://gitea.smartmoldguard.local:8080/
      - GITEA__security__INSTALL_LOCK=true
    ports:
      - "8080:8080"
      - "2224:22"
    volumes:
      - ./data/gitea:/data
      - /etc/timezone:/etc/timezone:ro
      - /etc/localtime:/etc/localtime:ro
    networks:
      - devops-net
    depends_on:
      - postgres
    mem_limit: 1g
    cpus: 0.5

  postgres:
    image: postgres:15-alpine
    container_name: gitea-postgres
    environment:
      POSTGRES_DB: gitea_db
      POSTGRES_USER: gitea_user
      POSTGRES_PASSWORD: gitea_password
    volumes:
      - ./data/postgres/gitea:/var/lib/postgresql/data
    networks:
      - devops-net
    restart: unless-stopped
    mem_limit: 512m

networks:
  devops-net:
    driver: bridge
EOF

# 启动 Gitea
docker-compose -f devops/docker-compose.gitea.yml up -d

# 访问 http://<服务器IP>:8080，首次访问需要完成初始化配置
# 默认管理员账号：admin / admin123
```

**Gitea 配置任务清单：**
- [ ] 登录后修改管理员密码
- [ ] 创建 smartmoldguard 组织
- [ ] 创建 5 个微服务项目仓库（对应 DDD 设计的微服务）
- [ ] 创建 api-gateway 仓库
- [ ] 创建 infra 仓库（存放部署配置）
- [ ] 创建 shared-lib 仓库（共享库）
- [ ] 配置 Deploy Keys 和 Webhooks

### 2.2 Woodpecker CI (CI/CD 流水线)

```bash
# 创建 Woodpecker CI 配置
cat > devops/docker-compose.woodpecker.yml <<EOF
version: '3.8'
services:
  woodpecker-server:
    image: woodpeckerci/woodpecker-server:latest
    container_name: woodpecker-server
    restart: unless-stopped
    environment:
      - WOODPECKER_OPEN=false
      - WOODPECKER_HOST=http://woodpecker.smartmoldguard.local:8081
      - WOODPECKER_GITEA=true
      - WOODPECKER_GITEA_URL=http://gitea:8080
      - WOODPECKER_GITEA_CLIENT=34320244-9711-47cb-b123-565408b3f010
      - WOODPECKER_GITEA_SECRET=your-gitea-secret
      - WOODPECKER_AGENT_SECRET=your-agent-secret
    ports:
      - "8081:8000"
    volumes:
      - ./data/woodpecker/server:/var/lib/woodpecker
    networks:
      - devops-net
    depends_on:
      - gitea
    mem_limit: 1g
    cpus: 0.5

  woodpecker-agent:
    image: woodpeckerci/woodpecker-agent:latest
    container_name: woodpecker-agent
    restart: unless-stopped
    command: agent
    environment:
      - WOODPECKER_SERVER=woodpecker-server:9000
      - WOODPECKER_AGENT_SECRET=your-agent-secret
      - WOODPECKER_HOSTNAME=agent-1
      - WOODPECKER_MAX_WORKFLOWS=5
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    networks:
      - devops-net
    depends_on:
      - woodpecker-server
    mem_limit: 512m
    cpus: 0.3

networks:
  devops-net:
    external: true
EOF

# 启动 Woodpecker CI
docker-compose -f devops/docker-compose.woodpecker.yml up -d

# 访问 http://<服务器IP>:8081，使用 Gitea 账号登录
```

**Woodpecker CI 配置任务清单：**
- [ ] 在 Gitea 中创建 OAuth 应用，获取 Client ID 和 Secret
- [ ] 更新 Woodpecker CI 配置文件，填入正确的 Gitea Client ID 和 Secret
- [ ] 访问 http://<服务器IP>:8081，使用 Gitea 账号登录
- [ ] 为每个微服务仓库配置 CI/CD 流水线
- [ ] 创建 .woodpecker.yml 模板（见下文）

### 2.3 制品库管理 (轻量级方案)

采用公共制品库 + 本地缓存的轻量级方案，无需部署私有仓库，大幅降低资源消耗：

- **Docker 镜像**: 使用 Docker Hub 公共仓库 + 本地 Docker 镜像缓存
- **Maven 依赖**: 使用 Maven Central + 本地 Maven 缓存
- **其他依赖**: 根据需要使用对应的公共仓库

**配置任务清单：**
- [ ] 在 Gitea 中配置 Docker Hub 凭据
- [ ] 配置 Maven 本地缓存
- [ ] 配置 Docker 镜像加速（国内环境建议）

```bash
# 配置 Docker 镜像加速（国内环境建议）
sudo tee /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": ["https://registry.docker-cn.com", "https://docker.mirrors.ustc.edu.cn"],
  "log-driver": "json-file",
  "log-opts": {"max-size": "10m", "max-file": "3"}
}
EOF
sudo systemctl restart docker
```

### 2.4 Loki + Promtail (日志收集)

```bash
# 创建 Loki + Promtail 配置
cat > devops/docker-compose.loki.yml <<EOF
version: '3.8'
services:
  loki:
    image: grafana/loki:3.0.0
    container_name: loki
    restart: unless-stopped
    ports:
      - "3100:3100"
    command: -config.file=/etc/loki/local-config.yaml
    volumes:
      - ./data/loki:/loki
    networks:
      - devops-net
    mem_limit: 1g
    cpus: 0.5

  promtail:
    image: grafana/promtail:3.0.0
    container_name: promtail
    restart: unless-stopped
    command: -config.file=/etc/promtail/config.yml
    volumes:
      - ./devops/promtail/config.yml:/etc/promtail/config.yml
      - /var/lib/docker/containers:/var/lib/docker/containers:ro
      - /var/run/docker.sock:/var/run/docker.sock:ro
    networks:
      - devops-net
    depends_on:
      - loki
    mem_limit: 512m
    cpus: 0.3

  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
    volumes:
      - ./data/grafana:/var/lib/grafana
    networks:
      - devops-net
    depends_on:
      - loki
    mem_limit: 1g
    cpus: 0.5

networks:
  devops-net:
    external: true
EOF

# 创建 Promtail 配置
mkdir -p devops/promtail
cat > devops/promtail/config.yml <<EOF
server:
  http_listen_port: 9080
  grpc_listen_port: 0

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

snippets:
  pipeline_stages:
    - json:
        expressions:
          time: time
          level: level
          message: message
          service_name: service_name
    - timestamp:
        source: time
        format: RFC3339Nano
    - labels:
        level:
        service_name:

scrape_configs:
  - job_name: system
    static_configs:
    - targets:
        - localhost
      labels:
        job: varlogs
        host: localhost
        __path__: /var/log/*log

  - job_name: docker
    docker_sd_configs:
    - host: unix:///var/run/docker.sock
      refresh_interval: 5s
    relabel_configs:
    - source_labels: [__meta_docker_container_name]
      regex: /(.*)
      target_label: container
    - source_labels: [__meta_docker_container_log_stream]
      target_label: log_stream
    - source_labels: [__meta_docker_container_label_com_docker_compose_service]
      target_label: service_name
    pipeline_stages:
      - json:
          expressions:
            log: log
            stream: stream
            time: time
      - timestamp:
          source: time
          format: RFC3339Nano
      - labels:
          stream:
      - output:
          source: log
EOF

# 启动 Loki + Promtail
docker-compose -f devops/docker-compose.loki.yml up -d

# 配置 Grafana 数据源
# 1. 访问 http://<服务器IP>:3000，登录后添加 Loki 数据源
# 2. URL: http://loki:3100
# 3. 保存并测试连接
```

### 2.5 Prometheus + Node Exporter + cAdvisor (监控告警)

```bash
# 创建 Prometheus 配置
mkdir -p devops/prometheus
cat > devops/prometheus/prometheus.yml <<EOF
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']

  - job_name: 'cadvisor'
    static_configs:
      - targets: ['cadvisor:8080']

  - job_name: 'eureka'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']

  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    eureka_sd_configs:
      - server: http://eureka-server:8761/eureka
    relabel_configs:
      - source_labels: [__meta_eureka_app_name]
        target_label: application
EOF

# 创建监控 Docker Compose
cat > devops/docker-compose.monitoring.yml <<EOF
version: '3.8'
services:
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./devops/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - ./data/prometheus:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--storage.tsdb.retention.time=30d'
    networks:
      - devops-net
    mem_limit: 1g
    cpus: 0.5

  node-exporter:
    image: prom/node-exporter:latest
    container_name: node-exporter
    restart: unless-stopped
    ports:
      - "9100:9100"
    networks:
      - devops-net
    mem_limit: 256m
    cpus: 0.2

  cadvisor:
    image: google/cadvisor:latest
    container_name: cadvisor
    restart: unless-stopped
    ports:
      - "8083:8080"
    volumes:
      - /:/rootfs:ro
      - /var/run:/var/run:ro
      - /sys:/sys:ro
      - /var/lib/docker/:/var/lib/docker:ro
      - /dev/disk/:/dev/disk:ro
    networks:
      - devops-net
    mem_limit: 512m
    cpus: 0.3

networks:
  devops-net:
    external: true
EOF

# 启动监控
docker-compose -f devops/docker-compose.monitoring.yml up -d
```

### 2.6 Eureka (服务注册与发现)

```bash
# 创建 Eureka 配置
cat > devops/docker-compose.eureka.yml <<EOF
version: '3.8'
services:
  eureka-server:
    image: springcloud/eureka-server:3.1.2
    container_name: eureka-server
    restart: unless-stopped
    environment:
      - EUREKA_SERVER_PORT=8761
      - EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
      - EUREKA_CLIENT_FETCH_REGISTRY=false
      - EUREKA_INSTANCE_HOSTNAME=eureka-server
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    ports:
      - "8761:8761"
    networks:
      - devops-net
    mem_limit: 512m
    cpus: 0.3

networks:
  devops-net:
    external: true
EOF

# 启动 Eureka
docker-compose -f devops/docker-compose.eureka.yml up -d
```

### 2.7 Spring Cloud Config (配置中心)

```bash
# 创建 Spring Cloud Config 配置
cat > devops/docker-compose.config.yml <<EOF
version: '3.8'
services:
  config-server:
    image: springcloud/config-server:3.1.2
    container_name: config-server
    restart: unless-stopped
    environment:
      - SPRING_PROFILES_ACTIVE=native
      - SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=/config
      - SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://gitea.smartmoldguard.local/smartmoldguard/config-repo.git
      - SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME=admin
      - SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD=admin123
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    ports:
      - "8888:8888"
    volumes:
      - ./data/config:/config
    networks:
      - devops-net
    depends_on:
      - eureka-server
    mem_limit: 512m
    cpus: 0.3

networks:
  devops-net:
    external: true
EOF

# 启动 Spring Cloud Config
docker-compose -f devops/docker-compose.config.yml up -d
```

**访问地址：**
- Gitea: http://<服务器IP>:8080
- Woodpecker CI: http://<服务器IP>:8081
- Grafana: http://<服务器IP>:3000
- Prometheus: http://<服务器IP>:9090
- Eureka: http://<服务器IP>:8761
- Spring Cloud Config: http://<服务器IP>:8888
- Loki: http://<服务器IP>:3100

---

## 三、DDD 微服务项目结构模板

### 3.1 Maven 父 POM 配置

```xml
<!-- 根目录 pom.xml -->
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.smartmoldguard</groupId>
    <artifactId>smartmoldguard-parent</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <modules>
        <module>device-service</module>
        <module>control-service</module>
        <module>ai-service</module>
        <module>subscription-service</module>
        <module>report-service</module>
        <module>api-gateway</module>
        <module>shared-library</module>
    </modules>

    <properties>
        <java.version>17</java.version>
        <spring-boot.version>3.2.0</spring-boot.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <postgresql.version>42.6.0</postgresql.version>
        <influxdb.version>6.10.0</influxdb.version>
        <kafka.version>3.6.0</kafka.version>
        <testcontainers.version>1.19.3</testcontainers.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>${spring-boot.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <image>
                        <name>${project.artifactId}:${project.version}</name>
                    </image>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### 3.2 单个微服务 DDD 结构

以 `device-service` 为例：

```
device-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/smartmoldguard/device/
│   │   │       ├── DeviceServiceApplication.java
│   │   │       ├── domain/                 # 领域层
│   │   │       │   ├── model/              # 聚合根、实体、值对象
│   │   │       │   │   ├── Device.java
│   │   │       │   │   ├── Room.java
│   │   │       │   │   └── ButtonMapping.java
│   │   │       │   ├── repository/         # 仓储接口
│   │   │       │   │   └── DeviceRepository.java
│   │   │       │   └── service/            # 领域服务
│   │   │       │       └── DeviceManagementService.java
│   │   │       ├── application/            # 应用层
│   │   │       │   ├── dto/                # DTO
│   │   │       │   ├── command/            # 命令
│   │   │       │   ├── query/              # 查询
│   │   │       │   └── service/            # 应用服务
│   │   │       │       └── DeviceAppService.java
│   │   │       ├── infrastructure/         # 基础设施层
│   │   │       │   ├── config/             # 配置
│   │   │       │   ├── persistence/        # 仓储实现
│   │   │       │   │   └── DeviceRepositoryImpl.java
│   │   │       │   ├── messaging/          # 消息队列
│   │   │       │   └── iot/                # IoT 模拟客户端
│   │   │       └── interfaces/             # 接口层
│   │   │           ├── rest/               # REST API
│   │   │           │   └── DeviceController.java
│   │   │           └── event/              # 事件监听
│   │   └── resources/
│   │       ├── application.yml
│   │       └── logback-spring.xml
│   └── test/
│       ├── unit/                           # 单元测试
│       ├── integration/                    # 集成测试
│       └── resources/
└── pom.xml
```

---

## 四、CI/CD 流水线实现

### 4.1 .woodpecker.yml 模板

```yaml
# 在 Gitea 每个服务仓库根目录创建 .woodpecker.yml
pipeline:
  
  检出代码:
    image: woodpeckerci/plugin-git:latest
    settings:
      depth: 1

  运行单元测试:
    image: maven:3.9-eclipse-temurin-17
    commands:
      - mvn clean test
    when:
      branch: [main, develop]

  代码质量检查:
    image: maven:3.9-eclipse-temurin-17
    commands:
      - mvn checkstyle:check
    when:
      branch: [main, develop]

  构建 Docker 镜像:
    image: docker:24
    commands:
      - docker build -t ${CI_REPO_NAME}:${CI_COMMIT_SHA} .
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    when:
      branch: [main, develop]

  部署到测试环境:
    image: docker:24
    commands:
      - docker-compose -f services/docker-compose.${CI_REPO_NAME}.yml pull
      - docker-compose -f services/docker-compose.${CI_REPO_NAME}.yml up -d
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    when:
      branch: develop

  集成测试:
    image: maven:3.9-eclipse-temurin-17
    commands:
      - mvn verify -Pintegration-test
    when:
      branch: [main, develop]

  部署到生产环境:
    image: docker:24
    commands:
      - docker-compose -f services/docker-compose.prod.yml pull ${CI_REPO_NAME}
      - docker-compose -f services/docker-compose.prod.yml up -d ${CI_REPO_NAME}
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    when:
      branch: main
      event: push

  发送通知:
    image: plugins/slack
    settings:
      webhook: ${SLACK_WEBHOOK_URL}
      channel: '#ci-cd'
      username: Woodpecker CI
      message: "构建 ${CI_BUILD_STATUS}: ${CI_REPO_NAME}@${CI_COMMIT_SHA}"
    when:
      status: [success, failure]
```

### 4.2 Gitea Webhook 配置

在每个仓库的设置中配置：
- URL: `http://<服务器IP>:8081/hook`
- Trigger: Push events, Pull request events
- Secret: 从 Woodpecker CI 获取

---

## 五、应用服务部署配置

### 5.1 核心服务 Docker Compose 配置

```bash
# 创建服务部署目录
mkdir -p services/{postgres,influxdb,redis,kafka,eureka,config}

# PostgreSQL 配置（共享数据库，按 schema 隔离）
cat > services/docker-compose.postgres.yml <<EOF
version: '3.8'
services:
  postgres:
    image: postgres:15-alpine
    container_name: smg-postgres
    restart: unless-stopped
    environment:
      POSTGRES_USER: smg_admin
      POSTGRES_PASSWORD: smg_password_2025
    volumes:
      - ./data/postgres:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - smg-network
    mem_limit: 4g
    cpus: 1.5

networks:
  smg-network:
    driver: bridge
EOF

# InfluxDB 配置（时序数据）
cat > services/docker-compose.influxdb.yml <<EOF
version: '3.8'
services:
  influxdb:
    image: influxdb:2.7-alpine
    container_name: smg-influxdb
    restart: unless-stopped
    environment:
      DOCKER_INFLUXDB_INIT_MODE: setup
      DOCKER_INFLUXDB_INIT_USERNAME: smg_admin
      DOCKER_INFLUXDB_INIT_PASSWORD: smg_password_2025
      DOCKER_INFLUXDB_INIT_ORG: smartmoldguard
      DOCKER_INFLUXDB_INIT_BUCKET: telemetry
    volumes:
      - ./data/influxdb:/var/lib/influxdb2
    ports:
      - "8086:8086"
    networks:
      - smg-network
    mem_limit: 2g
    cpus: 1
EOF

# Redis 配置（缓存）
cat > services/docker-compose.redis.yml <<EOF
version: '3.8'
services:
  redis:
    image: redis:7-alpine
    container_name: smg-redis
    restart: unless-stopped
    command: ["redis-server", "--appendonly", "yes", "--maxmemory", "2gb", "--maxmemory-policy", "allkeys-lru"]
    volumes:
      - ./data/redis:/data
    ports:
      - "6379:6379"
    networks:
      - smg-network
    mem_limit: 2.5g
    cpus: 0.5
EOF

# Kafka 配置（事件总线）
cat > services/docker-compose.kafka.yml <<EOF
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: smg-zookeeper
    restart: unless-stopped
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
    networks:
      - smg-network
    mem_limit: 512m
    cpus: 0.3

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: smg-kafka
    restart: unless-stopped
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://<服务器IP>:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    volumes:
      - ./data/kafka:/var/lib/kafka/data
    networks:
      - smg-network
    mem_limit: 3g
    cpus: 1
EOF

# Eureka 配置（服务注册与发现）
cat > services/docker-compose.eureka.yml <<EOF
version: '3.8'
services:
  eureka-server:
    image: springcloud/eureka-server:3.1.2
    container_name: smg-eureka
    restart: unless-stopped
    environment:
      - EUREKA_SERVER_PORT=8761
      - EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
      - EUREKA_CLIENT_FETCH_REGISTRY=false
      - EUREKA_INSTANCE_HOSTNAME=eureka-server
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    ports:
      - "8761:8761"
    networks:
      - smg-network
    mem_limit: 512m
    cpus: 0.3

networks:
  smg-network:
    external: true
EOF

# Spring Cloud Config 配置（配置中心）
cat > services/docker-compose.config.yml <<EOF
version: '3.8'
services:
  config-server:
    image: springcloud/config-server:3.1.2
    container_name: smg-config
    restart: unless-stopped
    environment:
      - SPRING_PROFILES_ACTIVE=native
      - SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=/config
      - SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://gitea.smartmoldguard.local/smartmoldguard/config-repo.git
      - SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME=admin
      - SPRING_CLOUD_CONFIG_SERVER_GIT_PASSWORD=admin123
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka
      - MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED=true
    ports:
      - "8888:8888"
    volumes:
      - ./data/config:/config
    networks:
      - smg-network
    depends_on:
      - eureka-server
    mem_limit: 512m
    cpus: 0.3

networks:
  smg-network:
    external: true
EOF

# 启动基础服务
docker-compose -f services/docker-compose.postgres.yml -f services/docker-compose.influxdb.yml \
  -f services/docker-compose.redis.yml -f services/docker-compose.kafka.yml \
  -f services/docker-compose.eureka.yml -f services/docker-compose.config.yml up -d
```

### 5.2 微服务 Docker Compose 模板

```bash
# 设备服务配置
cat > services/docker-compose.device-service.yml <<EOF
version: '3.8'
services:
  device-service:
    image: device-service:latest
    container_name: smg-device-service
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/device_db
      SPRING_DATASOURCE_USERNAME: device_user
      SPRING_DATASOURCE_PASSWORD: device_pass_2025
      SPRING_REDIS_HOST: redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      INFLUXDB_URL: http://influxdb:8086
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
      MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: true
    networks:
      - smg-network
    depends_on:
      - postgres
      - redis
      - kafka
      - eureka-server
      - config-server
    mem_limit: 2g
    cpus: 1
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  smg-network:
    external: true
EOF

# 控制服务配置
cat > services/docker-compose.control-service.yml <<EOF
version: '3.8'
services:
  control-service:
    image: control-service:latest
    container_name: smg-control-service
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/control_db
      SPRING_DATASOURCE_USERNAME: control_user
      SPRING_DATASOURCE_PASSWORD: control_pass_2025
      SPRING_REDIS_HOST: redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
      MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: true
    networks:
      - smg-network
    depends_on:
      - postgres
      - redis
      - kafka
      - eureka-server
      - config-server
    mem_limit: 2g
    cpus: 1
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  smg-network:
    external: true
EOF

# AI服务配置
cat > services/docker-compose.ai-service.yml <<EOF
version: '3.8'
services:
  ai-service:
    image: ai-service:latest
    container_name: smg-ai-service
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/ai_db
      SPRING_DATASOURCE_USERNAME: ai_user
      SPRING_DATASOURCE_PASSWORD: ai_pass_2025
      SPRING_REDIS_HOST: redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
      MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: true
    networks:
      - smg-network
    depends_on:
      - postgres
      - redis
      - kafka
      - eureka-server
      - config-server
    mem_limit: 2g
    cpus: 1
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  smg-network:
    external: true
EOF

# 订阅服务配置
cat > services/docker-compose.subscription-service.yml <<EOF
version: '3.8'
services:
  subscription-service:
    image: subscription-service:latest
    container_name: smg-subscription-service
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/subscription_db
      SPRING_DATASOURCE_USERNAME: subscription_user
      SPRING_DATASOURCE_PASSWORD: subscription_pass_2025
      SPRING_REDIS_HOST: redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
      MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: true
    networks:
      - smg-network
    depends_on:
      - postgres
      - redis
      - kafka
      - eureka-server
      - config-server
    mem_limit: 2g
    cpus: 1
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  smg-network:
    external: true
EOF

# 报告服务配置
cat > services/docker-compose.report-service.yml <<EOF
version: '3.8'
services:
  report-service:
    image: report-service:latest
    container_name: smg-report-service
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/report_db
      SPRING_DATASOURCE_USERNAME: report_user
      SPRING_DATASOURCE_PASSWORD: report_pass_2025
      SPRING_REDIS_HOST: redis
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:9092
      INFLUXDB_URL: http://influxdb:8086
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
      MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: true
    networks:
      - smg-network
    depends_on:
      - postgres
      - redis
      - kafka
      - eureka-server
      - config-server
    mem_limit: 2g
    cpus: 1
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  smg-network:
    external: true
EOF
```

### 5.3 API 网关配置

```yaml
# API网关 Docker Compose 配置
cat > services/docker-compose.api-gateway.yml <<EOF
version: '3.8'
services:
  api-gateway:
    image: api-gateway:latest
    container_name: smg-api-gateway
    restart: unless-stopped
    environment:
      SPRING_PROFILES_ACTIVE: prod
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-server:8761/eureka
      SPRING_CLOUD_CONFIG_URI: http://config-server:8888
      MANAGEMENT_METRICS_EXPORT_PROMETHEUS_ENABLED: true
    ports:
      - "8085:8085"
    networks:
      - smg-network
    depends_on:
      - eureka-server
      - config-server
    mem_limit: 1.5g
    cpus: 0.8
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

networks:
  smg-network:
    external: true
EOF

# API网关配置文件
cat > services/gateway/application.yml <<'EOF'
server:
  port: 8085

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        # 设备管理API
        - id: device-service
          uri: lb://device-service
          predicates:
            - Path=/api/v1/devices/**, /api/v1/rooms/**, /api/v1/spaces/**
          filters:
            - StripPrefix=2

        # 环境监测API
        - id: device-service-env
          uri: lb://device-service
          predicates:
            - Path=/api/v1/environment/**
          filters:
            - StripPrefix=2

        # 智能控制API
        - id: control-service
          uri: lb://control-service
          predicates:
            - Path=/api/v1/interventions/**, /api/v1/strategies/**, /api/v1/auto-mold-strategy/**, /api/v1/linkage-mapping/**
          filters:
            - StripPrefix=2

        # AI服务API
        - id: ai-service
          uri: lb://ai-service
          predicates:
            - Path=/api/v1/ai/**, /api/v1/predictions/**, /api/v1/risk-prediction/**, /api/v1/prediction-feedback/**
          filters:
            - StripPrefix=2

        # 订阅管理API
        - id: subscription-service
          uri: lb://subscription-service
          predicates:
            - Path=/api/v1/subscription/**, /api/v1/plans/**, /api/v1/points/**
          filters:
            - StripPrefix=2

        # 报告管理API
        - id: report-service
          uri: lb://report-service
          predicates:
            - Path=/api/v1/reports/**, /api/v1/dashboard/**
          filters:
            - StripPrefix=2

        # B端商户管理API
        - id: device-service-b
          uri: lb://device-service
          predicates:
            - Path=/api/v1/b/**
          filters:
            - StripPrefix=2

        # 运营端管理API
        - id: device-service-operator
          uri: lb://device-service
          predicates:
            - Path=/api/v1/operator/**
          filters:
            - StripPrefix=2

        # 告警与工单API
        - id: device-service-ops
          uri: lb://device-service
          predicates:
            - Path=/api/v1/alarms/**, /api/v1/workorder/**, /api/v1/repair/**, /api/v1/asset-compensate/**
          filters:
            - StripPrefix=2

management:
  endpoints:
    web:
      exposure:
        include: health, info, metrics, prometheus
  metrics:
    export:
      prometheus:
        enabled: true
EOF
```

---

## 六、监控与日志配置

### 6.1 Prometheus 服务发现配置

```yaml
# 在 prometheus.yml 中配置 Eureka 服务发现
scrape_configs:
  - job_name: 'eureka'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']

  - job_name: 'config-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['config-server:8888']

  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8085']

  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    eureka_sd_configs:
      - server: http://eureka-server:8761/eureka
    relabel_configs:
      - source_labels: [__meta_eureka_app_name]
        target_label: application
      - source_labels: [__meta_eureka_instance_port]
        target_label: port
```

### 6.2 Grafana 仪表板配置

```bash
# 登录 Grafana (admin/admin123)
# 1. 添加数据源
#   - Prometheus: http://prometheus:9090
#   - Loki: http://loki:3100

# 2. 导入仪表板
#   - JVM Micrometer (ID: 4701)
#   - Spring Boot Actuator (ID: 14405)
#   - Loki Logs (ID: 13105)
#   - Eureka Dashboard (ID: 15578)

# 3. 为B端和运营端创建专用仪表板
#   - B端商户运营概览
#   - 设备状态监控
#   - 告警统计分析
```

### 6.3 日志收集配置

在每个微服务的 `logback-spring.xml` 中配置 Loki 日志收集：

```xml
<configuration>
    <appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
        <http>
            <url>http://loki:3100/loki/api/v1/push</url>
        </http>
        <format>
            <label>
                <pattern>app=${spring.application.name},host=${HOSTNAME},level=%level</pattern>
            </label>
            <message>
                <pattern>{} {}- ${spring.application.name} - %msg%n</pattern>
            </message>
            <sortByTime>true</sortByTime>
        </format>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="LOKI"/>
    </root>
</configuration>
```

### 6.4 告警规则配置

```yaml
# prometheus/alert-rules.yml
groups:
- name: smartmoldguard-alerts
  rules:
  # 服务可用性告警
  - alert: ServiceDown
    expr: up == 0
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "服务不可用"
      description: "服务 {{ $labels.application }} 已停止运行超过5分钟"

  # JVM内存使用率告警
  - alert: HighMemoryUsage
    expr: (jvm_memory_used_bytes / jvm_memory_max_bytes) * 100 > 85
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "JVM内存使用率过高"
      description: "服务 {{ $labels.application }} JVM内存使用率超过85%"

  # B端商户高风险告警
  - alert: HighRiskRooms
    expr: sum by (application) (rate(high_risk_rooms_total[5m])) > 10
    for: 5m
    labels:
      severity: critical
    annotations:
      summary: "高风险房间数量过多"
      description: "B端商户高风险房间数量超过10个"

  # 设备离线告警
  - alert: DeviceOffline
    expr: sum by (application) (rate(device_offline_events_total[5m])) > 5
    for: 5m
    labels:
      severity: warning
    annotations:
      summary: "设备离线数量过多"
      description: "设备离线数量超过5个"
```

---

## 七、自动化脚本与工具

### 7.1 一键部署脚本

```bash
#!/bin/bash
# /opt/smartmoldguard/deploy.sh

set -e

SERVICE_NAME=$1
ENVIRONMENT=$2

if [ -z "$SERVICE_NAME" ]; then
    echo "Usage: ./deploy.sh <service-name> [prod|test]"
    exit 1
fi

ENVIRONMENT=${ENVIRONMENT:-test}
COMPOSE_FILE="services/docker-compose.$SERVICE_NAME.yml"

echo "部署服务: $SERVICE_NAME 到环境: $ENVIRONMENT"

# 1. 构建最新镜像
docker build -t $SERVICE_NAME:latest -f ./$SERVICE_NAME/Dockerfile ./$SERVICE_NAME

# 2. 执行数据库迁移（如果有）
docker-compose -f $COMPOSE_FILE run --rm $SERVICE_NAME \
  java -jar app.jar --spring.profiles.active=migrate

# 3. 滚动更新服务
docker-compose -f $COMPOSE_FILE up -d --no-deps $SERVICE_NAME

# 4. 健康检查
echo "等待服务启动..."
sleep 30

# 获取服务端口
case $SERVICE_NAME in
    "device-service") PORT=8080 ;;
    "control-service") PORT=8081 ;;
    "ai-service") PORT=8082 ;;
    "subscription-service") PORT=8083 ;;
    "report-service") PORT=8084 ;;
    "api-gateway") PORT=8085 ;;
    *) PORT=8080 ;;
esac

HEALTH=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/actuator/health)
if [ "$HEALTH" != "200" ]; then
    echo "服务健康检查失败!"
    exit 1
fi

echo "部署完成!"
```

### 7.2 一键启动所有服务脚本

```bash
#!/bin/bash
# /opt/smartmoldguard/start-all.sh

set -e

echo "启动所有基础服务..."
docker-compose -f services/docker-compose.postgres.yml -f services/docker-compose.influxdb.yml \
  -f services/docker-compose.redis.yml -f services/docker-compose.kafka.yml \
  -f services/docker-compose.eureka.yml -f services/docker-compose.config.yml up -d

# 等待基础服务启动
sleep 60

echo "启动微服务..."
docker-compose -f services/docker-compose.device-service.yml -f services/docker-compose.control-service.yml \
  -f services/docker-compose.ai-service.yml -f services/docker-compose.subscription-service.yml \
  -f services/docker-compose.report-service.yml -f services/docker-compose.api-gateway.yml up -d

echo "所有服务启动完成!"
echo "访问地址: http://localhost:8085/swagger-ui.html"
```

### 7.3 备份脚本

```bash
#!/bin/bash
# /opt/smartmoldguard/backup.sh

BACKUP_DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/opt/smartmoldguard/backups/$BACKUP_DATE"

mkdir -p $BACKUP_DIR

# 备份 PostgreSQL
docker exec smg-postgres pg_dumpall -U smg_admin > $BACKUP_DIR/postgres.sql

# 备份 InfluxDB
docker exec smg-influxdb influx backup /tmp/backup
docker cp smg-influxdb:/tmp/backup $BACKUP_DIR/influxdb

# 备份 Redis
docker exec smg-redis redis-cli BGSAVE
docker cp smg-redis:/data/dump.rdb $BACKUP_DIR/redis.rdb

# 备份配置文件
cp -r /opt/smartmoldguard/services $BACKUP_DIR/
cp -r /opt/smartmoldguard/data/config $BACKUP_DIR/

# 清理旧备份（保留7天）
find /opt/smartmoldguard/backups -type d -mtime +7 -exec rm -rf {} \;

echo "备份完成: $BACKUP_DIR"
```

### 7.4 B端和运营端管理工具脚本

```bash
#!/bin/bash
# /opt/smartmoldguard/business-tools.sh

# 功能菜单
show_menu() {
    echo "=== B端和运营端管理工具 ==="
    echo "1. 查看高风险房间列表"
    echo "2. 导出风险报告"
    echo "4. 查看设备状态分布"
    echo "5. 查看系统概览"
    echo "6. 退出"
    echo "========================"
}

# 查看高风险房间列表
view_high_risk_rooms() {
    curl -s http://localhost:8085/api/v1/b/dashboard/high-risk-rooms | jq .
}

# 导出风险报告
export_risk_report() {
    curl -s -o risk_report_$(date +%Y%m%d).json http://localhost:8085/api/v1/b/batch-actions -X POST -H "Content-Type: application/json" -d '{"action": "export_report", "roomIds": []}'
    echo "风险报告已导出: risk_report_$(date +%Y%m%d).json"
}


# 查看系统概览
view_system_overview() {
    curl -s http://localhost:8085/api/v1/operator/dashboard/overview | jq .
}

# 主循环
while true; do
    show_menu
    read -p "请选择操作: " OPTION
    
    case $OPTION in
        1) view_high_risk_rooms ;;
        2) export_risk_report ;;
        4) view_device_status ;;
        5) view_system_overview ;;
        6) echo "退出工具"; break ;;
        *) echo "无效选项，请重新选择" ;;
    esac
    
    echo "按回车键继续..."
    read
    clear
done
```

### 7.5 非轻量级工具生产环境规划

对于生产环境，我们推荐使用以下非轻量级工具，提供更强大的功能和更好的扩展性：

#### 7.5.1 GitLab CE（代码管理）

**功能优势**：完整的DevOps平台，包含代码管理、CI/CD、容器注册表、问题跟踪等

**部署配置**：
```bash
# 创建 GitLab CE 配置
cat > devops/docker-compose.gitlab.yml <<EOF
version: '3.8'
services:
  gitlab:
    image: gitlab/gitlab-ce:latest
    container_name: gitlab
    restart: unless-stopped
    environment:
      GITLAB_OMNIBUS_CONFIG: |
        external_url 'http://gitlab.smartmoldguard.local:8080'
        gitlab_rails['gitlab_shell_ssh_port'] = 2224
        postgresql['enable'] = false
        redis['enable'] = false
        gitlab_rails['db_adapter'] = 'postgresql'
        gitlab_rails['db_encoding'] = 'utf8'
        gitlab_rails['db_host'] = 'gitlab-postgres'
        gitlab_rails['db_port'] = '5432'
        gitlab_rails['db_username'] = 'gitlab'
        gitlab_rails['db_password'] = 'gitlab_password'
        gitlab_rails['redis_host'] = 'gitlab-redis'
        gitlab_rails['redis_port'] = '6379'
        gitlab_rails['gitlab_default_projects_features_issues'] = true
        gitlab_rails['gitlab_default_projects_features_merge_requests'] = true
        gitlab_rails['gitlab_default_projects_features_pipelines'] = true
        gitlab_rails['gitlab_default_projects_features_registry'] = true
    ports:
      - "8080:80"
      - "8443:443"
      - "2224:22"
    volumes:
      - ./data/gitlab/config:/etc/gitlab
      - ./data/gitlab/logs:/var/log/gitlab
      - ./data/gitlab/data:/var/opt/gitlab
    networks:
      - devops-net
    depends_on:
      - gitlab-postgres
      - gitlab-redis
    mem_limit: 8g
    cpus: 2

  gitlab-postgres:
    image: postgres:15-alpine
    container_name: gitlab-postgres
    environment:
      POSTGRES_DB: gitlabhq_production
      POSTGRES_USER: gitlab
      POSTGRES_PASSWORD: gitlab_password
    volumes:
      - ./data/postgres/gitlab:/var/lib/postgresql/data
    networks:
      - devops-net
    restart: unless-stopped
    mem_limit: 2g
    cpus: 0.5

  gitlab-redis:
    image: redis:7-alpine
    container_name: gitlab-redis
    command: ["redis-server", "--appendonly", "yes"]
    volumes:
      - ./data/redis/gitlab:/data
    networks:
      - devops-net
    restart: unless-stopped
    mem_limit: 1g
    cpus: 0.3

networks:
  devops-net:
    driver: bridge
EOF
```

#### 7.5.2 Jenkins（CI/CD 流水线）

**功能优势**：成熟的CI/CD工具，拥有丰富的插件生态，支持复杂流水线设计

**部署配置**：
```bash
# 创建 Jenkins 配置
cat > devops/docker-compose.jenkins.yml <<EOF
version: '3.8'
services:
  jenkins:
    image: jenkins/jenkins:lts-jdk17
    container_name: jenkins
    restart: unless-stopped
    user: root
    ports:
      - "8081:8080"
      - "50000:50000"
    environment:
      - JAVA_OPTS=-Xmx4g -Xms4g
      - JENKINS_OPTS=--prefix=/jenkins
    volumes:
      - ./data/jenkins:/var/jenkins_home
      - /var/run/docker.sock:/var/run/docker.sock
      - /usr/local/bin/docker:/usr/local/bin/docker
    networks:
      - devops-net
    mem_limit: 6g
    cpus: 2

networks:
  devops-net:
    external: true
EOF
```

#### 7.5.3 ELK Stack（日志收集与分析）

**功能优势**：企业级日志管理解决方案，支持复杂日志查询、可视化和告警

**部署配置**：
```bash
# 创建 ELK 配置
cat > devops/docker-compose.elk.yml <<EOF
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
    volumes:
      - ./data/elasticsearch:/usr/share/elasticsearch/data
    ports:
      - "9200:9200"
    networks:
      - devops-net
    mem_limit: 4g
    cpus: 1.5

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    container_name: logstash
    volumes:
      - ./devops/logstash/pipeline:/usr/share/logstash/pipeline
    ports:
      - "5000:5000"
    networks:
      - devops-net
    depends_on:
      - elasticsearch
    mem_limit: 2g
    cpus: 1

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    networks:
      - devops-net
    depends_on:
      - elasticsearch
    mem_limit: 2g
    cpus: 1

networks:
  devops-net:
    external: true
EOF

# 创建 Logstash 配置
mkdir -p devops/logstash/pipeline
cat > devops/logstash/pipeline/logstash.conf <<EOF
input {
  tcp {
    port => 5000
    codec => json
  }
  udp {
    port => 5000
    codec => json
  }
}
filter {
  if [type] == "spring-boot" {
    grok {
      match => { "message" => "%{TIMESTAMP_ISO8601:timestamp}\s+%{LOGLEVEL:level}\s+%{NUMBER:pid}\s+---\s+\[(?<thread>[A-Za-z0-9-]+)\]\s+(?<class>[A-Za-z0-9.#-]+)\s+:\s+(?<logmessage>.*)" }
    }
  }
}
output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "smartmoldguard-%{+YYYY.MM.dd}"
  }
}
EOF
```

#### 7.5.4 Nexus Repository（制品库）

**功能优势**：企业级制品管理，支持多种格式（Maven、Docker、npm等）

**部署配置**：
```bash
# 创建 Nexus 配置
cat > devops/docker-compose.nexus.yml <<EOF
version: '3.8'
services:
  nexus:
    image: sonatypecommunity/nexus3:latest
    container_name: nexus
    restart: unless-stopped
    ports:
      - "8082:8081"
      - "8083:5000"  # Docker 仓库端口
    volumes:
      - ./data/nexus:/nexus-data
    networks:
      - devops-net
    mem_limit: 4g
    cpus: 1.5
EOF
```

#### 7.5.5 SonarQube（代码质量分析）

**功能优势**：全面的代码质量分析，支持多种语言，提供详细的代码质量报告

**部署配置**：
```bash
# 创建 SonarQube 配置
cat > devops/docker-compose.sonarqube.yml <<EOF
version: '3.8'
services:
  sonarqube:
    image: sonarqube:9.9.0-community
    container_name: sonarqube
    restart: unless-stopped
    environment:
      SONAR_JDBC_URL: jdbc:postgresql://sonar-postgres:5432/sonarqube
      SONAR_JDBC_USERNAME: sonar
      SONAR_JDBC_PASSWORD: sonar_password
      SONAR_ES_BOOTSTRAP_CHECKS_DISABLE: "true"
    ports:
      - "9000:9000"
    volumes:
      - ./data/sonarqube/data:/opt/sonarqube/data
      - ./data/sonarqube/extensions:/opt/sonarqube/extensions
      - ./data/sonarqube/logs:/opt/sonarqube/logs
    networks:
      - devops-net
    depends_on:
      - sonar-postgres
    mem_limit: 4g
    cpus: 1.5

  sonar-postgres:
    image: postgres:13-alpine
    container_name: sonar-postgres
    environment:
      POSTGRES_USER: sonar
      POSTGRES_PASSWORD: sonar_password
      POSTGRES_DB: sonarqube
      POSTGRES_HOST_AUTH_METHOD: trust
    volumes:
      - ./data/postgres/sonar:/var/lib/postgresql/data
    networks:
      - devops-net
    restart: unless-stopped
    mem_limit: 2g
    cpus: 0.5

networks:
  devops-net:
    external: true
EOF
```

#### 7.5.6 HashiCorp Vault（敏感信息管理）

**功能优势**：企业级密钥管理解决方案，支持动态密钥生成、加密和访问控制

**部署配置**：
```bash
# 创建 Vault 配置
cat > devops/docker-compose.vault.yml <<EOF
version: '3.8'
services:
  vault:
    image: hashicorp/vault:1.14.0
    container_name: vault
    restart: unless-stopped
    ports:
      - "8200:8200"
    environment:
      VAULT_ADDR: http://0.0.0.0:8200
      VAULT_DEV_ROOT_TOKEN_ID: "root"
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
    cap_add:
      - IPC_LOCK
    volumes:
      - ./data/vault:/vault/data
    command: server -dev
    networks:
      - devops-net
    mem_limit: 2g
    cpus: 1

networks:
  devops-net:
    external: true
EOF
```

#### 7.5.7 非轻量级工具集成方案

**CI/CD流水线集成**：
```groovy
// Jenkinsfile 示例，集成非轻量级工具
pipeline {
    agent any
    
    stages {
        stage('代码质量分析') {
            steps {
                withSonarQubeEnv('sonarqube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('安全扫描') {
            steps {
                // 使用 Nexus IQ 或其他安全扫描工具
                sh 'nexus-iq-cli -i ${PROJECT_ID} -s http://nexus:8081 -a admin:admin123 -t build target/*.jar'
            }
        }
        
        stage('构建 Docker 镜像') {
            steps {
                sh 'docker build -t ${REGISTRY}/${IMAGE_NAME}:${VERSION} .'
                sh 'docker push ${REGISTRY}/${IMAGE_NAME}:${VERSION}'
            }
        }
    }
    
    post {
        always {
            junit 'target/surefire-reports/*.xml'
            archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
        }
        success {
            notifySlack channel: '#builds', message: '构建成功'
        }
        failure {
            notifySlack channel: '#builds', message: '构建失败'
        }
    }
}
```

**日志收集集成**：
```xml
<!-- logback-spring.xml 中配置 ELK 集成 -->
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
    <destination>logstash:5000</destination>
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <customFields>{"service_name":"device-service"}</customFields>
    </encoder>
</appender>

<root level="INFO">
    <appender-ref ref="LOGSTASH"/>
</root>
```

#### 7.5.8 非轻量级工具与轻量级工具对比

| 工具类型 | 轻量级工具 | 非轻量级工具 | 优势 | 劣势 | 适用场景 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| 代码管理 | Gitea | GitLab CE | 完整DevOps平台，内置CI/CD | 资源消耗高 | 生产环境，需要完整DevOps功能 |
| CI/CD | Woodpecker CI | Jenkins | 丰富插件生态，支持复杂流水线 | 配置复杂，资源消耗高 | 生产环境，需要复杂流水线 |
| 日志管理 | Loki + Promtail | ELK Stack | 强大的日志查询和可视化，支持告警 | 资源消耗高，配置复杂 | 生产环境，需要高级日志分析 |
| 制品管理 | Docker Hub | Nexus Repository | 私有制品库，支持多种格式 | 资源消耗高 | 生产环境，需要私有制品管理 |
| 代码质量 | 简单测试 | SonarQube | 全面的代码质量分析 | 资源消耗高 | 生产环境，需要严格的代码质量控制 |
| 敏感信息管理 | 环境变量 | HashiCorp Vault | 企业级密钥管理，动态密钥生成 | 配置复杂 | 生产环境，需要严格的密钥管理 |

#### 7.5.9 生产环境部署建议

1. **资源规划**：
   - 建议使用独立服务器部署非轻量级工具
   - 每个工具至少分配4核8GB内存
   - 采用分布式部署，确保高可用性

2. **网络隔离**：
   - 为DevOps工具创建独立网络
   - 配置防火墙，限制访问IP
   - 启用HTTPS加密传输

3. **监控告警**：
   - 为DevOps工具配置监控
   - 设置告警规则，及时发现问题
   - 定期备份工具数据

4. **升级策略**：
   - 制定详细的升级计划
   - 在测试环境验证升级
   - 执行升级时确保数据备份

5. **安全加固**：
   - 定期更新工具版本
   - 配置强密码和访问控制
   - 启用审计日志

通过以上非轻量级工具的部署和集成，可以为SmartMoldGuard系统提供企业级的DevOps支持，确保系统在生产环境中的可靠性、安全性和可扩展性。

---

## 八、安全设计与实现

### 8.1 JWT认证配置

#### 8.1.1 API网关JWT配置

```yaml
# 在API网关的application.yml中添加JWT配置
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://auth-service:8086/auth/realms/smartmoldguard
          jwk-set-uri: ${spring.security.oauth2.resourceserver.jwt.issuer-uri}/protocol/openid-connect/certs

# 添加JWT认证过滤器
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/v1/public/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        return http.build();
    }

    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new JwtGrantedAuthoritiesConverter());
        return converter;
    }
}
```

#### 8.1.2 微服务JWT配置

在每个微服务的application.yml中添加JWT依赖：

```xml
<!-- Maven依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

### 8.2 HTTPS配置

#### 8.2.1 API网关HTTPS配置

```bash
# 生成自签名证书
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# 将证书复制到网关配置目录
mkdir -p services/gateway/certs
cp key.pem cert.pem services/gateway/certs/
```

```yaml
# API网关HTTPS配置
server:
  port: 8443
  ssl:
    enabled: true
    key-store-type: PKCS12
    key-store: classpath:keystore.p12
    key-store-password: changeit
    key-alias: smartmoldguard
    trust-store: classpath:truststore.p12
    trust-store-password: changeit

# Docker Compose中添加HTTPS端口映射
ports:
  - "8085:8085"
  - "8443:8443"
```

#### 8.2.2 自动重定向HTTP到HTTPS

```java
@Configuration
public class HttpsRedirectConfig {
    @Bean
    public WebFilter httpsRedirectFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            if (request.getURI().getScheme().equals("http")) {
                URI httpsUri = UriComponentsBuilder.fromHttpRequest(request)
                    .scheme("https")
                    .port(8443)
                    .build()
                    .toUri();
                return exchange.getResponse().setComplete();
            }
            return chain.filter(exchange);
        };
    }
}
```

### 8.3 RBAC授权配置

```yaml
# 在Spring Cloud Config中配置角色权限映射
rbac:
  roles:
    - name: "ROLE_ADMIN"
      permissions:
        - "device:read"
        - "device:write"
        - "control:read"
        - "control:write"
        - "ai:read"
        - "ai:write"
        - "subscription:read"
        - "subscription:write"
        - "report:read"
        - "report:write"
    - name: "ROLE_USER"
      permissions:
        - "device:read"
        - "control:read"
        - "ai:read"
        - "subscription:read"
        - "report:read"
    - name: "ROLE_BUSINESS"
      permissions:
        - "device:read"
        - "report:read"
        - "b:read"
        - "b:write"
    - name: "ROLE_OPERATOR"
      permissions:
        - "device:read"
        - "report:read"
        - "operator:read"
        - "operator:write"
```

### 8.4 敏感数据加密

#### 8.4.1 数据库敏感数据加密

```java
@Entity
public class Device {
    @Id
    private UUID deviceId;
    
    @Column(length = 100)
    private String deviceName;
    
    @Encrypted
    @Column(length = 255)
    private String deviceSn;
    
    @Encrypted
    @Column(length = 100)
    private String macAddress;
    
    // 其他字段...
}
```

#### 8.4.2 配置文件敏感数据加密

```bash
# 使用Spring Cloud Config加密敏感配置
java -jar spring-cloud-config-server-3.1.2.jar encrypt --key my-secret-key --value "smg_password_2025"
# 输出加密后的字符串: {cipher}AQB...
```

在配置文件中使用加密值：
```yaml
spring:
  datasource:
    password: '{cipher}AQB...'
```

### 8.5 安全审计日志

```xml
<!-- 在logback-spring.xml中添加安全审计日志配置 -->
<appender name="AUDIT" class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOG_PATH}/audit.log</file>
    <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
        <fileNamePattern>${LOG_PATH}/audit.%d{yyyy-MM-dd}.log</fileNamePattern>
        <maxHistory>30</maxHistory>
    </rollingPolicy>
    <encoder>
        <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
</appender>

<logger name="com.smartmoldguard.security" level="INFO" additivity="false">
    <appender-ref ref="AUDIT"/>
    <appender-ref ref="LOKI"/>
</logger>
```

### 8.6 安全加固配置

#### 8.6.1 API网关安全加固

```yaml
# 在API网关的application.yml中添加安全加固配置
spring:
  cloud:
    gateway:
      httpclient:
        pool:
          max-idle-time: 60000
          max-connections: 1000
        ssl:
          handshake-timeout: 10000
          close-notify-flush-timeout: 3000
          close-notify-read-timeout: 0
      filter:
        request-rate-limiter:
          redis-rate-limiter:
            replenishRate: 100
            burstCapacity: 200

# 添加请求限流过滤器
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
        .route("device-service", r -> r.path("/api/v1/devices/**")
            .filters(f -> f.requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter())))\
            .uri("lb://device-service"))
        // 其他路由...
        .build();
}
```

---

## 九、与 DDD 战术设计的结合点

### 9.1 限界上下文的部署映射

| 限界上下文 | 微服务 | 部署方式 | 数据库 Schema |
| :--- | :--- | :--- | :--- |
| 防霉预测上下文 | ai-service | 独立容器 | ai_schema |
| 智能控制上下文 | control-service | 独立容器 | control_schema |
| 设备连接上下文 | device-service | 独立容器 | device_schema |
| 交付运维上下文 | 集成在 device-service | 共享服务 | delivery_schema |
| 防霉报告上下文 | report-service | 独立容器 | report_schema |
| 客户与订阅上下文 | subscription-service | 独立容器 | subscription_schema |

### 8.2 聚合根的实现映射

以 **Device 聚合根** 为例：

```java
// 领域模型 (对应文档 4.2.1)
@Entity
@Table(name = "devices")
public class Device {
    @Id
    private UUID deviceId;
    private String deviceSn;
    // ... 其他属性
    
    // 命令方法
    public void registerDevice(String sn, String name, String location) {
        if (this.status != DeviceStatus.UNREGISTERED) {
            throw new DomainException("设备已注册");
        }
        this.deviceSn = sn;
        this.deviceName = name;
        this.location = location;
        this.status = DeviceStatus.REGISTERED;
        
        // 发布领域事件
        DomainEventPublisher.publish(new DeviceRegisteredEvent(this));
    }
    
    // 其他命令方法...
}

// 仓储实现
@Repository
public class DeviceRepositoryImpl implements DeviceRepository {
    @Override
    public Device save(Device device) {
        // 保存到 PostgreSQL
        // 发布事件到 Kafka
        return device;
    }
}
```

### 8.3 领域服务的实现

```java
// InterventionService (对应文档 5)
@Service
public class InterventionService {
    private final InterventionPlanRepository planRepository;
    private final DeviceRepository deviceRepository;
    private final IoTMockService iotService;
    
    public InterventionPlan generatePlan(RiskIndex riskIndex) {
        // 业务逻辑实现
        InterventionPlan plan = new InterventionPlan();
        plan.generatePlan(riskIndex);
        
        // 保存到数据库
        planRepository.save(plan);
        
        // 发布事件
        eventPublisher.publish(new InterventionGeneratedEvent(plan));
        
        return plan;
    }
}
```

### 8.4 模拟服务对接

```java
// AI 模拟服务客户端 (对应文档 6.1)
@Component
public class AIMockService {
    private final RestTemplate restTemplate;
    
    public PredictionResult predictRisk(String deviceId, List<TelemetryData> history) {
        String url = "http://ai-mock:8080/api/v1/ai/mock/predict";
        
        AIMockRequest request = new AIMockRequest(deviceId, history);
        AIMockResponse response = restTemplate.postForObject(url, request, AIMockResponse.class);
        
        return mapToPredictionResult(response);
    }
}
```

---

## 九、测试策略实施

### 9.1 单元测试执行

```bash
# 在 Jenkins 流水线中
mvn test
# 生成报告
mvn surefire-report:report
```

### 9.2 集成测试执行

```bash
# 使用 Testcontainers
mvn verify -Pintegration-test
```

### 9.3 端到端测试

```bash
# 使用 Cypress 或 Selenium
npm run e2e:test
```

---

## 十、安全加固

### 10.1 配置防火墙

```bash
# 仅开放必要端口
sudo ufw allow 8080,8081,8082,8083,8084,8085,3000,9090,5601/tcp
sudo ufw allow 2224/tcp  # GitLab SSH
sudo ufw enable
```

### 10.2 配置 HTTPS（使用自签名证书）

```bash
# 在 API 网关上配置 SSL 终止
# 使用 Let's Encrypt (生产环境)
sudo apt install certbot
sudo certbot certonly --standalone -d yourdomain.com
```

### 10.3 敏感信息管理

```bash
# 使用 Docker secrets 或环境变量
echo "smg_password_2025" | docker secret create db_password -
```

---

## 十一、运维手册

### 11.1 日常运维命令

```bash
# 查看所有服务状态
docker-compose -f services/ ps

# 重启某个服务
docker-compose -f services/docker-compose.device-service.yml restart device-service

# 查看日志
docker logs -f --tail 100 smg-device-service

# 性能监控
docker stats

# 清理无用镜像
docker image prune -a
```

### 11.2 故障排查指南

| 问题现象 | 检查步骤 | 解决方案 |
| :--- | :--- | :--- |
| 服务无法启动 | 1. `docker logs <container>`<br>2. 检查端口占用 | 修改端口或停止冲突服务 |
| 数据库连接失败 | 1. `docker exec -it postgres psql`<br>2. 检查网络 | 重启数据库容器 |
| 内存不足 | `docker stats` | 增加 swap 或优化 JVM 参数 |
| 磁盘满 | `du -h /opt/smartmoldguard` | 清理旧日志和备份 |

---

## 十二、总结与后续优化

### 12.1 已完成的 DevOps 能力

✅ **版本控制**：GitLab CE 完整代码管理  
✅ **持续集成**：Jenkins 自动化构建、测试、打包  
✅ **持续部署**：Docker Compose 滚动部署  
✅ **制品管理**：Nexus 镜像和依赖管理  
✅ **监控告警**：Prometheus + Grafana 完整监控  
✅ **日志管理**：ELK Stack 集中日志收集  
✅ **自动化**：Shell 脚本实现一键部署和备份  

### 12.2 后续优化方向

1. **性能优化**：根据监控数据调整 JVM 参数和容器资源限制
2. **高可用**：研究 Docker Swarm 或 K3s 实现多节点部署
3. **GitOps**：引入 ArgoCD 实现声明式部署
4. **混沌工程**：使用 ChaosMesh 进行故障注入测试
5. **成本优化**：评估使用轻量级替代方案（如 Gitea 替代 GitLab）

### 12.3 与真实 AI/IoT 平台对接准备

当需要替换模拟服务时：

```bash
# 1. 修改服务配置
sed -i 's/ai-mock/ai-platform/g' services/*-service.yml

# 2. 重新部署
docker-compose -f services/ up -d

# 3. 验证接口
curl http://ai-platform/api/v1/ai/predict -X POST -d @test-data.json
```

---

**操作规划执行时间估算：**
- 环境准备：2 小时
- DevOps 工具部署：4 小时
- 微服务项目创建：3 小时
- CI/CD 配置：2 小时
- 监控日志配置：2 小时
- **总计：约 13 小时**（可 1-2 个工作日内完成）

所有配置文件和脚本已提供，可直接复制使用。建议在测试环境先验证，再应用到生产服务器。