# Nexus-Lite 全文检索功能技术方案（Docker单节点部署）

## 1. 项目技术栈分析

### 1.1 现有技术栈
- **核心框架**：Spring Boot 3.2.0
- **编程语言**：Java 17
- **数据库**：H2（文件索引和元数据存储）
- **ORM框架**：Spring Data JPA
- **缓存**：Caffeine
- **文档处理**：PDFBox、Apache POI、Mammoth、Flexmark等
- **检索引擎**：Elasticsearch 8.x

### 1.2 Docker部署需求分析
- **容器化**：将应用和Elasticsearch打包为Docker镜像
- **服务编排**：使用docker-compose管理多容器服务
- **配置管理**：通过环境变量和配置文件实现灵活配置
- **数据持久化**：确保容器重启后数据不丢失
- **网络隔离**：容器间安全通信
- **资源限制**：合理分配CPU和内存资源
- **环境一致性**：开发、测试、生产环境保持一致

## 2. 检索引擎选型

### 2.1 选型建议
**推荐使用：Elasticsearch 8.x Docker镜像（单节点模式）**

### 2.2 选型理由
1. **官方支持**：Elasticsearch提供官方Docker镜像，更新及时
2. **易于部署**：通过Docker镜像快速部署，无需复杂的安装步骤
3. **环境隔离**：容器化部署，与宿主环境隔离，避免依赖冲突
4. **资源可控**：可通过Docker配置限制CPU和内存使用
5. **易于扩展**：未来可轻松扩展为集群模式

## 3. Docker部署架构设计

### 3.1 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         宿主机 (Host)                          │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │                       Docker网络                          │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                   应用服务容器                      │  │  │
│  │  │  - Spring Boot应用                                  │  │  │
│  │  │  - 端口：8080                                      │  │  │
│  │  │  - 依赖：Elasticsearch                              │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                 Elasticsearch容器                   │  │  │
│  │  │  - 版本：8.x                                         │  │  │
│  │  │  - 端口：9200（HTTP）                                │  │  │
│  │  │  - 单节点模式                                       │  │  │
│  │  │  - 数据卷挂载：/usr/share/elasticsearch/data         │  │  │
│  │  │  - 数据卷挂载：/usr/share/elasticsearch/logs         │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  │  ┌─────────────────────────────────────────────────────┐  │  │
│  │  │                     数据卷                          │  │  │
│  │  │  - nexus-lite-es-data：Elasticsearch数据             │  │  │
│  │  │  - nexus-lite-es-logs：Elasticsearch日志             │  │  │
│  │  │  - nexus-lite-app-data：应用数据（H2数据库）          │  │  │
│  │  └─────────────────────────────────────────────────────┘  │  │
│  └───────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 3.2 服务依赖关系

```
应用服务容器 ──> Elasticsearch容器
         │
         └─> 数据卷（应用数据）
```

## 4. Docker配置方案

### 4.1 Dockerfile（应用服务）

```dockerfile
# 基础镜像
FROM openjdk:17-jdk-slim

# 维护者信息
LABEL maintainer="nexus-lite@example.com"

# 设置工作目录
WORKDIR /app

# 复制jar包到容器
COPY target/nexus-lite-1.0.0-SNAPSHOT.jar app.jar

# 复制配置文件（可选，用于挂载外部配置）
COPY src/main/resources/application.yml application.yml

# 暴露端口
EXPOSE 8080

# 设置环境变量
ENV JAVA_OPTS="-Xms512m -Xmx1g"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 4.2 docker-compose.yml

```yaml
version: '3.8'

services:
  # Elasticsearch服务
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: nexus-lite-es
    environment:
      # 集群配置
      - cluster.name=nexus-lite-es
      - node.name=nexus-lite-node-1
      - cluster.initial_master_nodes=nexus-lite-node-1
      - discovery.type=single-node
      # 网络配置
      - network.host=0.0.0.0
      # 内存配置
      - bootstrap.memory_lock=true
      - "ES_JAVA_OPTS=-Xms2g -Xmx2g"
      # 安全配置
      - xpack.security.enabled=false
      - xpack.monitoring.enabled=false
      - xpack.watcher.enabled=false
      - xpack.ml.enabled=false
      # 索引配置
      - index.max_result_window=10000
      - index.mapping.total_fields.limit=1000
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      # 数据卷挂载
      - nexus-lite-es-data:/usr/share/elasticsearch/data
      - nexus-lite-es-logs:/usr/share/elasticsearch/logs
      # IK分词器挂载（可选，用于自定义词典）
      # - ./ik/config:/usr/share/elasticsearch/plugins/ik/config
    ports:
      - "9200:9200"
    networks:
      - nexus-lite-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9200/_cluster/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    # 资源限制
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: '4g'
        reservations:
          cpus: '1'
          memory: '2g'

  # 应用服务
  nexus-lite-app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: nexus-lite-app
    environment:
      # Spring配置
      - SPRING_PROFILES_ACTIVE=docker
      # Elasticsearch配置
      - SPRING_ELASTICSEARCH_URIS=http://elasticsearch:9200
      # 数据库配置
      - SPRING_DATASOURCE_URL=jdbc:h2:file:/app/data/nexus_db
      - SPRING_DATASOURCE_USERNAME=sa
      - SPRING_DATASOURCE_PASSWORD=password
      # 应用配置
      - APP_SCAN_ROOT-DIRS=/app/docs
      - APP_PREVIEW_MAX-FILE-SIZE=20
      - APP_CACHE_TOON-TTL=3600
      - APP_CACHE_PREVIEW-TTL=600
    volumes:
      # 应用数据挂载
      - nexus-lite-app-data:/app/data
      # 文档目录挂载
      - ./docs:/app/docs
      # 日志目录挂载
      - ./logs:/app/logs
    ports:
      - "8080:8080"
    networks:
      - nexus-lite-network
    depends_on:
      elasticsearch:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped
    # 资源限制
    deploy:
      resources:
        limits:
          cpus: '1'
          memory: '2g'
        reservations:
          cpus: '0.5'
          memory: '1g'

# 网络配置
networks:
  nexus-lite-network:
    driver: bridge
    ipam:
      config:
        - subnet: 172.20.0.0/16

# 数据卷配置
volumes:
  nexus-lite-es-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: ./docker/elasticsearch/data
  nexus-lite-es-logs:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: ./docker/elasticsearch/logs
  nexus-lite-app-data:
    driver: local
    driver_opts:
      type: none
      o: bind
      device: ./docker/app/data
```

### 4.3 应用配置文件（application-docker.yml）

```yaml
spring:
  application:
    name: nexus-lite
  
  # 数据库配置
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:h2:file:./data/nexus_db}
    driver-class-name: org.h2.Driver
    username: ${SPRING_DATASOURCE_USERNAME:sa}
    password: ${SPRING_DATASOURCE_PASSWORD:password}
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        format_sql: false
        dialect: org.hibernate.dialect.H2Dialect
  
  # Elasticsearch配置
  elasticsearch:
    uris: ${SPRING_ELASTICSEARCH_URIS:http://localhost:9200}
    connection-timeout: 5s
    socket-timeout: 30s
  
  # 缓存配置
  cache:
    type: caffeine
    caffeine:
      spec: maximumSize=1000,expireAfterWrite=3600s

# 应用配置
app:
  # 扫描配置
  scan:
    root-dirs: ${APP_SCAN_ROOT-DIRS:./docs}
    cron: 60  # minutes
  
  # 预览配置
  preview:
    max-file-size: ${APP_PREVIEW_MAX-FILE-SIZE:20}  # MB
    pdf:
      max-pages-per-request: 20
  
  # 缓存配置
  cache:
    toon-ttl: ${APP_CACHE_TOON-TTL:3600}  # seconds
    preview-ttl: ${APP_CACHE_PREVIEW-TTL:600}  # seconds
  
  # 数据目录
  data:
    dir: ${APP_DATA_DIR:./data}

# Web配置
server:
  port: 8080
  servlet:
    context-path: /

# Actuator配置
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# 日志配置
logging:
  level:
    root: INFO
    com.documentpreview: DEBUG
    org.springframework: INFO
    org.hibernate: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ./logs/nexus-lite.log

# CORS配置
cors:
  allowed-origins: ["*"]
  allowed-methods: [GET, POST, PUT, DELETE, OPTIONS]
  allowed-headers: ["*"]
  exposed-headers: [ETag, Last-Modified]
  allow-credentials: true
  max-age: 3600

# Rate Limiting Configuration
rate-limiting:
  enabled: true
  requests-per-minute: 120
  burst-capacity: 30
```

## 4. 索引设计方案

### 4.1 索引模板配置

与单节点部署方案相同，但需通过Docker环境下的Elasticsearch API创建索引模板。

### 4.2 IK分词器配置

#### 4.2.1 安装IK分词器

在Elasticsearch容器启动时自动安装IK分词器：

```dockerfile
# 在Dockerfile中添加IK分词器安装命令
RUN bin/elasticsearch-plugin install https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.0/elasticsearch-analysis-ik-8.11.0.zip
```

或在docker-compose.yml中通过命令安装：

```yaml
elasticsearch:
  # ... 其他配置
  command: >
    /bin/bash -c "
    bin/elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.0/elasticsearch-analysis-ik-8.11.0.zip &&
    /usr/local/bin/docker-entrypoint.sh eswrapper
    "
```

#### 4.2.2 自定义词典挂载

将自定义词典挂载到Elasticsearch容器中：

```yaml
elasticsearch:
  # ... 其他配置
  volumes:
    # ... 其他挂载
    - ./ik/config:/usr/share/elasticsearch/plugins/ik/config
```

## 5. 数据同步策略

### 5.1 同步方式

与单节点部署方案相同，采用实时同步和定时全量同步的混合策略。

### 5.2 Docker环境下的优化

1. **网络优化**：应用服务与Elasticsearch通过Docker网络通信，减少网络延迟
2. **资源隔离**：通过Docker资源限制，确保同步过程不会影响其他服务
3. **日志收集**：同步日志通过Docker日志驱动收集，便于监控和排查问题

## 6. 核心检索功能实现方案

与单节点部署方案相同，但需注意以下Docker环境下的调整：

1. **Elasticsearch连接配置**：通过环境变量`SPRING_ELASTICSEARCH_URIS`配置Elasticsearch地址
2. **资源限制**：根据容器的CPU和内存限制，调整检索线程池大小
3. **健康检查**：应用服务添加健康检查端点，确保服务正常运行

## 7. 性能优化措施

### 7.1 Docker层面优化

1. **镜像优化**：
   - 使用轻量级基础镜像（如openjdk:17-jdk-slim）
   - 采用多阶段构建，减少镜像大小
   - 清理不必要的依赖和文件

2. **资源限制**：
   - 根据实际需求设置CPU和内存限制
   - 合理配置JVM堆内存大小（建议为容器内存的50%）
   - 启用内存锁定，防止Swap使用

3. **存储优化**：
   - 使用SSD存储数据卷
   - 合理配置Elasticsearch的索引刷新间隔
   - 定期清理过期索引和日志

4. **网络优化**：
   - 使用自定义Docker网络，减少网络开销
   - 配置合理的网络MTU
   - 启用TCP keepalive

### 7.2 应用层面优化

与单节点部署方案相同，包括索引优化、查询优化、缓存策略等。

## 8. 安全加固措施

### 8.1 Docker安全配置

1. **镜像安全**：
   - 使用官方镜像或可信镜像源
   - 定期更新镜像，修复安全漏洞
   - 扫描镜像中的安全漏洞（如使用Trivy）

2. **容器安全**：
   - 以非root用户运行容器
   - 限制容器的特权（如--privileged=false）
   - 禁用不必要的容器功能（如--cap-drop=ALL）
   - 启用容器只读文件系统（部分服务适用）

3. **网络安全**：
   - 使用自定义网络，隔离容器通信
   - 限制容器的端口映射，只暴露必要端口
   - 配置防火墙规则，限制外部访问

4. **数据安全**：
   - 加密敏感数据
   - 定期备份数据卷
   - 配置数据卷权限，防止未授权访问

### 8.2 应用安全

与单节点部署方案相同，包括输入验证、访问控制、安全编码等。

## 9. 监控与日志管理

### 9.1 Docker监控

1. **容器监控**：
   - 使用Docker内置的stats命令监控容器资源使用情况
   - 部署Prometheus + Grafana监控容器性能
   - 使用cAdvisor收集容器指标

2. **日志管理**：
   - 配置Docker日志驱动，收集容器日志
   - 使用ELK Stack或Loki收集和分析日志
   - 配置日志轮转，防止日志过大

### 9.2 应用监控

1. **Elasticsearch监控**：
   - 启用Elasticsearch的监控API
   - 监控集群健康状态、索引状态、节点状态等
   - 设置告警规则，当指标异常时发送告警

2. **应用监控**：
   - 使用Spring Boot Actuator暴露应用指标
   - 监控JVM性能、请求响应时间、错误率等
   - 设置告警规则，当应用异常时发送告警

## 10. 测试方案

### 10.1 Docker环境下的测试

1. **功能测试**：
   - 使用docker-compose启动服务
   - 执行与单节点部署相同的功能测试用例
   - 验证Docker环境下的功能正确性

2. **性能测试**：
   - 在Docker环境下执行性能测试
   - 验证容器资源限制下的性能表现
   - 比较Docker环境与原生环境的性能差异

3. **压力测试**：
   - 在Docker环境下执行压力测试
   - 验证容器在高并发场景下的稳定性
   - 测试容器的故障恢复能力

4. **集成测试**：
   - 测试应用服务与Elasticsearch容器之间的通信
   - 测试数据卷挂载的正确性
   - 测试环境变量配置的有效性

### 10.2 Docker特定测试

| 测试场景 | 测试步骤 | 预期结果 |
|----------|----------|----------|
| 容器重启测试 | 1. 运行系统一段时间
2. 重启Elasticsearch容器
3. 验证数据完整性和服务可用性 | 数据完整，服务正常恢复 |
| 容器重建测试 | 1. 运行系统一段时间
2. 删除并重建Elasticsearch容器
3. 验证数据完整性 | 数据完整（通过数据卷持久化） |
| 资源限制测试 | 1. 设置较低的内存限制
2. 执行检索操作
3. 验证系统处理方式 | 系统能够优雅处理，不崩溃 |
| 网络隔离测试 | 1. 测试容器间通信
2. 测试外部访问限制
3. 验证网络安全配置 | 容器间通信正常，外部访问受限 |

## 11. 实施步骤建议

### 11.1 第一阶段：Docker环境准备

1. **安装Docker和Docker Compose**：
   - 在宿主机上安装Docker
   - 安装Docker Compose
   - 配置Docker镜像加速（可选）

2. **创建项目目录结构**：
   ```
   nexus-lite/
   ├── docker/
   │   ├── elasticsearch/
   │   │   ├── data/
   │   │   └── logs/
   │   └── app/
   │       └── data/
   ├── docs/
   ├── ik/
   │   └── config/
   ├── logs/
   ├── Dockerfile
   ├── docker-compose.yml
   └── pom.xml
   ```

### 11.2 第二阶段：应用构建和镜像创建

1. **构建应用**：
   - 编译Spring Boot应用
   - 生成jar包

2. **构建Docker镜像**：
   - 编写Dockerfile
   - 构建应用镜像
   - 验证镜像创建成功

### 11.3 第三阶段：服务部署和配置

1. **编写docker-compose.yml**：
   - 配置Elasticsearch服务
   - 配置应用服务
   - 配置网络和数据卷

2. **配置环境变量**：
   - 编写application-docker.yml
   - 配置必要的环境变量

3. **启动服务**：
   ```bash
   docker-compose up -d
   ```

4. **验证服务状态**：
   ```bash
   docker-compose ps
   docker-compose logs -f
   ```

### 11.4 第四阶段：索引创建和数据同步

1. **创建索引模板**：
   - 通过Elasticsearch API创建索引模板
   - 配置分词器和字段映射

2. **初始化数据同步**：
   - 执行全量数据同步
   - 验证索引数据完整性

### 11.5 第五阶段：测试和优化

1. **执行测试**：
   - 功能测试
   - 性能测试
   - 压力测试
   - Docker特定测试

2. **优化配置**：
   - 根据测试结果调整Docker配置
   - 优化应用和Elasticsearch配置
   - 调整资源限制

### 11.6 第六阶段：监控和维护

1. **设置监控**：
   - 部署Prometheus + Grafana
   - 配置监控指标和告警规则

2. **配置日志管理**：
   - 部署日志收集系统
   - 配置日志轮转和清理策略

3. **定期维护**：
   - 定期更新镜像和依赖
   - 定期备份数据
   - 定期清理过期索引和日志

## 12. 部署脚本示例

### 12.1 启动脚本（start.sh）

```bash
#!/bin/bash

# 确保脚本在错误时退出
set -e

echo "=== 启动Nexus-Lite服务 ==="

# 检查Docker和Docker Compose是否安装
if ! command -v docker &> /dev/null; then
    echo "错误：Docker未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误：Docker Compose未安装"
    exit 1
fi

# 构建应用镜像
echo "构建应用镜像..."
docker-compose build

# 启动服务
echo "启动服务..."
docker-compose up -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 验证服务状态
echo "验证服务状态..."
docker-compose ps

# 显示日志
echo "显示服务日志..."
docker-compose logs -f
```

### 12.2 停止脚本（stop.sh）

```bash
#!/bin/bash

# 确保脚本在错误时退出
set -e

echo "=== 停止Nexus-Lite服务 ==="

# 检查Docker和Docker Compose是否安装
if ! command -v docker &> /dev/null; then
    echo "错误：Docker未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误：Docker Compose未安装"
    exit 1
fi

# 停止服务
echo "停止服务..."
docker-compose down

# 显示状态
echo "服务已停止"
docker-compose ps
```

### 12.3 备份脚本（backup.sh）

```bash
#!/bin/bash

# 确保脚本在错误时退出
set -e

echo "=== 备份Nexus-Lite数据 ==="

# 定义备份目录
BACKUP_DIR="/backup/nexus-lite/$(date +%Y%m%d_%H%M%S)"
mkdir -p $BACKUP_DIR

# 备份Elasticsearch数据
echo "备份Elasticsearch数据..."
docker cp nexus-lite-es:/usr/share/elasticsearch/data $BACKUP_DIR/es-data

# 备份应用数据
echo "备份应用数据..."
docker cp nexus-lite-app:/app/data $BACKUP_DIR/app-data

# 备份配置文件
echo "备份配置文件..."
cp -r ./docker-compose.yml ./application-docker.yml $BACKUP_DIR/

# 压缩备份文件
echo "压缩备份文件..."
tar -czf $BACKUP_DIR.tar.gz $BACKUP_DIR
rm -rf $BACKUP_DIR

echo "备份完成：$BACKUP_DIR.tar.gz"
```

## 13. 结论

本技术方案针对Docker单节点部署场景，对Nexus-Lite全文检索功能进行了全面设计和优化，主要包括：

1. **Docker部署架构**：设计了包含应用服务和Elasticsearch服务的Docker部署架构
2. **镜像和容器配置**：提供了Dockerfile和docker-compose.yml配置示例
3. **环境变量和配置管理**：通过环境变量实现灵活配置
4. **数据持久化**：配置了数据卷挂载，确保数据不丢失
5. **资源限制和性能优化**：合理配置CPU、内存和JVM参数
6. **安全加固**：实施了Docker层面和应用层面的安全措施
7. **监控和日志管理**：配置了监控指标和日志收集
8. **完整的测试方案**：包括功能测试、性能测试、压力测试和Docker特定测试
9. **部署和维护脚本**：提供了启动、停止和备份脚本

该方案充分利用了Docker的优势，实现了环境隔离、资源可控、易于部署和维护的目标，同时保持了与原有单节点方案的兼容性。通过合理的配置和优化，能够确保系统在Docker环境下稳定运行，满足全文检索的需求。