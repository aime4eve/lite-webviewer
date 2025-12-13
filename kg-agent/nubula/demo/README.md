# Nebula Graph Demo 使用指南

## 概述

本Demo展示了如何使用Nebula Graph图数据库构建知识图谱应用，包括数据存储、查询、可视化和Web API等功能。

## 目录结构

```
demo/
├── docker-compose.yml          # Docker Compose配置文件
├── nebula_client.py           # Nebula Graph客户端封装
├── graph_operations.py        # 图数据库操作封装
├── advanced_graph_operations.py # 高级图操作功能
├── web_api.py                 # Web API服务
├── start.sh                   # 统一启动脚本
├── scripts/                   # 脚本目录
│   ├── init_nebula.sh         # Nebula Graph初始化脚本
│   ├── test_nebula_console.sh # Nebula控制台测试脚本
│   └── test_nebula_minimal.sh # 最小化测试脚本
├── web/                       # Web界面目录
│   ├── index.html             # 主页面
│   └── graph-visualization.js # 图形可视化脚本
└── tests/                     # 测试目录
    ├── test_nebula_direct.py  # 直接连接测试
    ├── test_suite.py          # 功能测试套件
    ├── performance_test.py    # 性能测试
    ├── consistency_test.py    # 一致性测试
    ├── load_test.py           # 负载测试
    └── run_all_tests.py       # 测试运行器
```

## 快速开始

### 1. 环境要求

- Docker
- Docker Compose
- Python 3.6+
- Python依赖包（见requirements.txt）

### 2. 启动服务

使用统一启动脚本一键启动所有服务：

```bash
chmod +x start.sh
./start.sh start
```

或者手动启动：

```bash
# 启动Nebula Graph服务
docker-compose up -d

# 初始化数据
./scripts/init_nebula.sh

# 启动Web API服务
python3 web_api.py

# 访问Web界面
# 浏览器打开 http://localhost:5000
```

### 3. 访问界面

- Web界面: http://localhost:5000
- API文档: http://localhost:5000/api/docs

## 功能说明

### 1. 图数据库操作

#### 基本操作

- 插入实体: `nebula_client.insert_entity()`
- 插入关系: `nebula_client.insert_relationship()`
- 查询实体: `nebula_client.query_entities()`
- 查询关系: `nebula_client.query_relationships()`

#### 高级操作

- 查找最短路径: `advanced_graph_operations.find_shortest_path()`
- 查找相关实体: `advanced_graph_operations.find_related_entities()`
- 按类型查找实体: `advanced_graph_operations.find_entities_by_type()`
- 按关键词查找实体: `advanced_graph_operations.find_entities_by_keyword()`
- 计算节点中心性: `advanced_graph_operations.calculate_centrality()`
- 查找社区结构: `advanced_graph_operations.find_communities()`

### 2. Web API接口

#### 健康检查

```bash
GET /health
```

#### 实体查询

```bash
GET /api/entities?keywords=人工智能,机器学习&depth=2
```

#### 相关实体获取

```bash
GET /api/related_entities?entity_id=entity_1&depth=2
```

#### 最短路径查找

```bash
GET /api/shortest_path?source=entity_1&target=entity_2
```

#### 图谱导出

```bash
GET /api/export_graph?format=json
```

#### 统计信息

```bash
GET /api/stats
```

### 3. Web界面功能

- 实体查询和可视化
- 关系路径展示
- 图谱导航和缩放
- 节点和边的过滤
- 图谱导出功能
- 统计信息展示

## 测试

### 运行所有测试

```bash
./start.sh test
```

### 单独运行测试

```bash
# 基础连接测试
python3 tests/test_nebula_direct.py

# 功能测试
python3 tests/test_suite.py

# 性能测试
python3 tests/performance_test.py

# 一致性测试
python3 tests/consistency_test.py

# 负载测试
python3 tests/load_test.py
```

## 脚本说明

### start.sh

统一启动脚本，支持以下命令：

```bash
./start.sh start     # 启动所有服务
./start.sh stop      # 停止所有服务
./start.sh restart   # 重启所有服务
./start.sh nebula    # 仅启动Nebula Graph服务
./start.sh api       # 仅启动Web API服务
./start.sh web       # 仅启动Web界面
./start.sh init      # 仅初始化Nebula Graph数据
./start.sh test      # 运行测试套件
./start.sh status    # 显示服务状态
./start.sh logs      # 显示服务日志
./start.sh help      # 显示帮助信息
```

### init_nebula.sh

Nebula Graph初始化脚本，执行以下操作：

1. 等待Nebula Graph服务启动
2. 创建kg_agent_space空间
3. 定义entity和document标签
4. 定义relationship和contains边类型
5. 插入人工智能相关实体关系示例数据

### test_nebula_console.sh

Nebula控制台测试脚本，包含：

1. 连接测试
2. 创建空间/标签/边类型
3. 插入测试数据
4. 实体关系查询
5. 关键词搜索功能

## 自定义配置

### 修改Nebula Graph配置

编辑`docker-compose.yml`文件中的Nebula Graph服务配置：

```yaml
services:
  nebula-metad:
    image: vesoft/nebula-metad:v3.6.0
    environment:
      USER: root
      # 添加自定义配置
  nebula-storaged:
    image: vesoft/nebula-storaged:v3.6.0
    environment:
      USER: root
      # 添加自定义配置
  nebula-graphd:
    image: vesoft/nebula-graphd:v3.6.0
    environment:
      USER: root
      # 添加自定义配置
```

### 修改Web API配置

编辑`web_api.py`文件中的配置：

```python
# Nebula Graph连接配置
NEBULA_CONFIG = {
    "hosts": [
        ("127.0.0.1", 9669),
    ],
    "username": "root",
    "password": "nebula",
    "space_name": "kg_agent_space"
}

# API服务配置
API_CONFIG = {
    "host": "0.0.0.0",
    "port": 5000,
    "debug": True
}
```

## 故障排除

### 1. Nebula Graph服务启动失败

检查Docker日志：

```bash
docker-compose logs nebula-graphd
docker-compose logs nebula-metad
docker-compose logs nebula-storaged
```

### 2. 连接Nebula Graph失败

检查网络连接和端口：

```bash
netstat -tlnp | grep 9669
```

### 3. Web API服务启动失败

检查Python依赖：

```bash
pip install -r requirements.txt
```

### 4. 测试失败

检查Nebula Graph服务状态和数据初始化：

```bash
./start.sh status
./start.sh init
```

## 扩展开发

### 1. 添加新的图操作

在`advanced_graph_operations.py`中添加新的图算法：

```python
def new_graph_algorithm(params):
    """
    新的图算法实现
    """
    # 实现算法逻辑
    pass
```

### 2. 添加新的API接口

在`web_api.py`中添加新的API端点：

```python
@app.route('/api/new_endpoint', methods=['GET'])
def new_endpoint():
    """
    新的API端点
    """
    # 实现API逻辑
    pass
```

### 3. 扩展Web界面

在`web/`目录下添加新的HTML和JavaScript文件，并在`index.html`中引用。

## 参考资料

- [Nebula Graph官方文档](https://docs.nebula-graph.io/)
- [Nebula Graph Python客户端](https://github.com/vesoft-inc/nebula-python)
- [D3.js图形可视化](https://d3js.org/)

## 许可证

本Demo遵循MIT许可证。