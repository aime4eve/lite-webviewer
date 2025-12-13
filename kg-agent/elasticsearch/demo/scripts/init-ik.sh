#!/bin/bash

# 等待Elasticsearch启动
echo "等待Elasticsearch启动..."
until curl -s http://localhost:9200/_cluster/health | grep -q '"status":"green\|yellow"'; do
    echo "Elasticsearch尚未就绪，等待中..."
    sleep 5
done

echo "Elasticsearch已启动，开始配置IK分词器..."

# 创建一个使用IK分词器的测试索引
curl -X PUT "localhost:9200/ik_test_index" -H 'Content-Type: application/json' -d'
{
  "settings": {
    "analysis": {
      "analyzer": {
        "ik_analyzer": {
          "type": "ik_max_word"
        },
        "ik_smart_analyzer": {
          "type": "ik_smart"
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "content": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      }
    }
  }
}'

# 测试分词效果
echo "测试IK分词器效果..."
curl -X POST "localhost:9200/ik_test_index/_analyze?pretty" -H 'Content-Type: application/json' -d'
{
  "analyzer": "ik_max_word",
  "text": "认证体系汇总说明表"
}'

echo ""
echo "IK分词器配置完成！"