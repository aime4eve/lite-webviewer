# 全文检索系统演示

这是一个基于Elasticsearch和IK中文分词器的全文检索系统演示，支持中文文档的高效索引和搜索。

## 功能特点

- 支持中文分词（IK分词器）
- 全文检索和高亮显示
- Web界面搜索
- RESTful API接口
- 自动文件索引

## 目录结构

```
demo/
├── docker/                 # Docker相关文件
│   ├── docker-compose.yml  # Docker Compose配置
│   └── Dockerfile.elasticsearch  # Elasticsearch镜像配置
├── scripts/                # 脚本文件
│   ├── init-ik.sh         # IK分词器初始化脚本
│   └── start_elasticsearch_with_ik.sh  # Elasticsearch启动脚本
├── web/                   # Web界面
│   ├── search_web_ui.py   # Flask Web界面
│   └── search_web.py      # 简单Web界面
├── tests/                 # 测试脚本
│   ├── test_elasticsearch.py  # Elasticsearch连接测试
│   ├── test_ik_analyzer.py    # IK分词器测试
│   └── test_full_text_search.py  # 全文搜索测试
├── full_text_search.py    # 全文搜索核心模块
├── start.sh              # 统一启动脚本
└── README.md             # 本文档
```

## 快速开始

### 1. 启动系统

使用统一启动脚本一键启动所有服务：

```bash
./start.sh
```

或者分步启动：

```bash
# 仅启动Elasticsearch
./start.sh elastic

# 初始化IK分词器
./start.sh ik

# 索引文件
./start.sh index

# 启动Web界面
./start.sh web
```

### 2. 访问Web界面

启动完成后，可以通过以下地址访问Web界面：

- http://localhost:5001 - 主搜索界面
- http://localhost:5000 - 简单搜索界面

### 3. 使用API接口

系统提供以下API接口：

- `GET /search?q=关键词` - 搜索文档
- `GET /index` - 重新索引文件

## 详细说明

### Docker环境

系统使用Docker Compose管理服务，主要包括：

- Elasticsearch 9.2.2
- IK中文分词器插件

### IK分词器

IK分词器是一个优秀的中文分词器，支持两种模式：

- `ik_max_word`：最细粒度分词
- `ik_smart`：智能分词

### 全文搜索

全文搜索功能包括：

- 自动文件索引
- 中文分词搜索
- 结果高亮显示
- 相关度评分

## 测试

运行测试脚本验证系统功能：

```bash
# 运行所有测试
./start.sh test

# 或者单独运行测试
python3 tests/test_elasticsearch.py
python3 tests/test_ik_analyzer.py
python3 tests/test_full_text_search.py
```

## 常用命令

```bash
# 查看服务状态
./start.sh status

# 停止所有服务
./start.sh stop

# 重新启动
./start.sh stop && ./start.sh start
```

## 故障排除

### Elasticsearch无法启动

1. 检查Docker是否正常运行
2. 确保端口9200未被占用
3. 检查Docker Compose配置

### IK分词器初始化失败

1. 确保Elasticsearch已完全启动
2. 检查网络连接
3. 查看初始化脚本日志

### 搜索无结果

1. 确保文件已正确索引
2. 检查搜索关键词是否正确
3. 查看Elasticsearch索引状态

## 扩展开发

### 添加新的文档类型

在`full_text_search.py`中的`is_text_file`方法中添加新的文件类型判断：

```python
def is_text_file(self, file_path):
    # 添加新的文件类型
    text_extensions = ['.txt', '.md', '.py', '.json', '.html', '.你的新类型']
    return any(file_path.lower().endswith(ext) for ext in text_extensions)
```

### 自定义分词器

可以修改Elasticsearch索引配置，添加自定义分词器：

```python
index_mapping = {
    "settings": {
        "analysis": {
            "analyzer": {
                "my_analyzer": {
                    "type": "custom",
                    "tokenizer": "ik_max_word",
                    "filter": ["lowercase", "stop", "你的自定义过滤器"]
                }
            }
        }
    },
    "mappings": {
        # 映射配置
    }
}
```

## 许可证

本项目采用MIT许可证。

## 联系方式

如有问题或建议，请提交Issue或Pull Request。