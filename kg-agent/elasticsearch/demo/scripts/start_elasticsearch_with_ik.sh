#!/bin/bash

echo "重新构建和启动带有IK分词器的Elasticsearch..."

# 停止现有服务
echo "停止现有服务..."
docker-compose down

# 清理旧的容器和卷
echo "清理旧的容器和卷..."
docker system prune -f

# 创建必要的目录
echo "创建必要的目录..."
mkdir -p elasticsearch/plugins

# 确保IK分词器安装包存在
if [ ! -f "elasticsearch/plugins/elasticsearch-analysis-ik-9.2.2.zip" ]; then
    echo "错误: IK分词器安装包不存在于 elasticsearch/plugins/elasticsearch-analysis-ik-9.2.2.zip"
    echo "请确保已将安装包复制到该位置"
    exit 1
fi

# 启动服务
echo "启动服务..."
docker-compose up -d elasticsearch

# 等待Elasticsearch启动
echo "等待Elasticsearch启动..."
sleep 30

# 检查Elasticsearch状态
echo "检查Elasticsearch状态..."
curl -X GET "localhost:9200/_cluster/health?pretty"

echo ""
echo "服务启动完成！"
echo "Elasticsearch: http://localhost:9200"
echo "运行以下命令测试IK分词器:"
echo "python3 test_ik_analyzer.py"