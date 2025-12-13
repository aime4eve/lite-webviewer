#!/bin/bash

# Nebula Graph 初始化脚本

echo "=== 初始化 Nebula Graph ==="

# 等待Nebula Graph服务启动
echo "等待Nebula Graph服务启动..."
for i in {1..30}; do
    if nebula-console -addr localhost -port 9669 -u root -p nebula -e "SHOW HOSTS;" > /tmp/hosts.log 2>&1; then
        echo "Nebula Graph服务已启动"
        cat /tmp/hosts.log
        break
    fi
    echo "等待Nebula Graph服务启动... ($i/30)"
    sleep 5
done

# 添加 Storage Host (如果尚未添加)
echo "检查并添加 Storage Host..."
if ! nebula-console -addr localhost -port 9669 -u root -p nebula -e "SHOW HOSTS;" | grep -q "nebula-storaged"; then
    echo "添加 nebula-storaged:9779..."
    nebula-console -addr localhost -port 9669 -u root -p nebula -e "ADD HOSTS \"nebula-storaged\":9779;"
    sleep 5
fi

# 再次检查是否有在线的 Storage 节点
echo "检查 Storage 节点状态..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "SHOW HOSTS;"
sleep 5

# 创建空间
echo "创建kg_agent_space..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "CREATE SPACE IF NOT EXISTS kg_agent_space(vid_type=FIXED_STRING(256), partition_num=10, replica_factor=1);"

# 等待空间创建完成 (Meta服务同步需要时间)
echo "等待空间创建完成..."
sleep 10

# 使用空间
echo "使用kg_agent_space..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "USE kg_agent_space;"

# 创建标签
echo "创建标签..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "
USE kg_agent_space;
CREATE TAG IF NOT EXISTS entity(name string, type string, description string, properties string);
CREATE TAG IF NOT EXISTS document(title string, content string, url string);
CREATE TAG IF NOT EXISTS concept(name string, category string, definition string);
CREATE TAG IF NOT EXISTS person(name string, role string, organization string);
CREATE TAG IF NOT EXISTS technology(name string, category string, description string);
"

# 创建索引
echo "创建索引..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "
USE kg_agent_space;
CREATE TAG INDEX IF NOT EXISTS entity_name_index ON entity(name(20));
"
sleep 10

# 创建边类型
echo "创建边类型..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "
USE kg_agent_space;
CREATE EDGE IF NOT EXISTS relationship(relation string, weight double, description string);
CREATE EDGE IF NOT EXISTS contains(strength double);
CREATE EDGE IF NOT EXISTS belongs_to(confidence double);
CREATE EDGE IF NOT EXISTS related_to(similarity double);
CREATE EDGE IF NOT EXISTS mentions(count int);
CREATE EDGE IF NOT EXISTS authored(\`date\` string);
"

# 等待 Schema 同步
echo "等待 Schema 同步..."
sleep 20

# 插入示例数据
echo "插入示例数据..."
nebula-console -addr localhost -port 9669 -u root -p nebula -e "
USE kg_agent_space;
INSERT VERTEX entity(name, type, description, properties) VALUES \"人工智能\":(\"人工智能\", \"概念\", \"模拟人类智能的技术\", \"{category: '技术', importance: 'high'}\");
INSERT VERTEX entity(name, type, description, properties) VALUES \"机器学习\":(\"机器学习\", \"技术\", \"使计算机能够学习的技术\", \"{category: '技术', importance: 'high'}\");
INSERT VERTEX entity(name, type, description, properties) VALUES \"深度学习\":(\"深度学习\", \"技术\", \"基于神经网络的学习方法\", \"{category: '技术', importance: 'medium'}\");
INSERT VERTEX entity(name, type, description, properties) VALUES \"神经网络\":(\"神经网络\", \"技术\", \"模拟生物神经网络的计算模型\", \"{category: '技术', importance: 'medium'}\");
INSERT VERTEX entity(name, type, description, properties) VALUES \"自然语言处理\":(\"自然语言处理\", \"技术\", \"使计算机能够理解人类语言的技术\", \"{category: '技术', importance: 'medium'}\");

INSERT EDGE relationship(relation, weight, description) VALUES \"人工智能\"->\"机器学习\":(\"包含\", 0.9, \"人工智能包含机器学习\");
INSERT EDGE relationship(relation, weight, description) VALUES \"机器学习\"->\"深度学习\":(\"包含\", 0.8, \"机器学习包含深度学习\");
INSERT EDGE relationship(relation, weight, description) VALUES \"深度学习\"->\"神经网络\":(\"基于\", 0.9, \"深度学习基于神经网络\");
INSERT EDGE relationship(relation, weight, description) VALUES \"人工智能\"->\"自然语言处理\":(\"包含\", 0.7, \"人工智能包含自然语言处理\");
INSERT EDGE relationship(relation, weight, description) VALUES \"机器学习\"->\"自然语言处理\":(\"应用\", 0.6, \"机器学习应用于自然语言处理\");
"

echo "=== Nebula Graph 初始化完成 ==="