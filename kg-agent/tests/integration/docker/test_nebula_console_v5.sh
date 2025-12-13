#!/bin/bash

# 测试Nebula Graph查询功能的脚本

echo "=== 测试Nebula Graph连接和查询功能 ==="

# 1. 测试连接
echo "1. 测试连接..."
echo "SHOW SPACES;" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 2. 创建或切换到kg_agent_space
echo -e "\n2. 创建或切换到kg_agent_space..."
echo "CREATE SPACE IF NOT EXISTS kg_agent_space(vid_type=FIXED_STRING(256));" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 3. 创建标签和边类型
echo -e "\n3. 创建标签和边类型..."
echo "USE kg_agent_space;
CREATE TAG IF NOT EXISTS entity(name string, type string);
CREATE EDGE IF NOT EXISTS relationship(relation string);" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 4. 创建索引
echo -e "\n4. 创建索引..."
echo "USE kg_agent_space;
CREATE TAG INDEX IF NOT EXISTS entity_name_index ON entity(name(64));
CREATE EDGE INDEX IF NOT EXISTS relationship_relation_index ON relationship(relation(64));" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 5. 等待索引生效
echo -e "\n5. 等待索引生效..."
sleep 10

# 6. 插入测试数据
echo -e "\n6. 插入测试数据..."
echo "USE kg_agent_space;
INSERT VERTEX entity(name, type) VALUES \"人工智能\":(\"人工智能\", \"概念\");
INSERT VERTEX entity(name, type) VALUES \"机器学习\":(\"机器学习\", \"技术\");
INSERT VERTEX entity(name, type) VALUES \"深度学习\":(\"深度学习\", \"技术\");
INSERT EDGE relationship(relation) VALUES \"人工智能\"->\"机器学习\":(\"包含\");
INSERT EDGE relationship(relation) VALUES \"机器学习\"->\"深度学习\":(\"属于\");" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 7. 查询测试数据
echo -e "\n7. 查询测试数据..."
echo "USE kg_agent_space;
GO FROM \"机器学习\" OVER relationship YIELD dst(edge) AS id;" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 8. 测试基于关键词的查询
echo -e "\n8. 测试基于关键词的查询..."
echo "USE kg_agent_space;
LOOKUP ON entity WHERE entity.name == \"机器学习\" YIELD vertex as v;" | nebula-console -addr localhost -port 9669 -u root -p nebula

# 9. 获取实体属性
echo -e "\n9. 获取实体属性..."
echo "USE kg_agent_space;
FETCH PROP ON entity \"机器学习\" YIELD properties(vertex);" | nebula-console -addr localhost -port 9669 -u root -p nebula

echo -e "\n=== 测试完成 ==="