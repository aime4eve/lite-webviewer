docker run -d --name elasticsearch -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" -e "xpack.security.enabled=false" -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" docker.elastic.co/elasticsearch/elasticsearch:8.11.0 


# 检查ES服务
curl -s http://localhost:9200 

curl -X PUT "http://localhost:9200/nexus-lite-docs" -H "Content-Type: application/json" -d '{
  "settings": {
    "index": {
      "highlight.max_analyzed_offset": 100000000,
      "mapping.total_fields.limit": 2000,
      "max_ngram_diff": 50,
      "max_result_window": 10000,
      "analysis": {
        "analyzer": {
          "chinese_analyzer": {
            "type": "custom",
            "tokenizer": "standard",
            "filter": ["lowercase", "stop", "length"]
          }
        }
      }
    }
  },
  "mappings": {
    "properties": {
      "id": {
        "type": "keyword"
      },
      "filePath": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "fileName": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "parentDir": {
        "type": "text",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "fileType": {
        "type": "keyword"
      },
      "title": {
        "type": "text",
        "analyzer": "chinese_analyzer"
      },
      "contentText": {
        "type": "text",
        "analyzer": "chinese_analyzer",
        "index_options": "offsets",
        "position_increment_gap": 100,
        "fielddata": true
      },
      "size": {
        "type": "long"
      },
      "modifiedAt": {
        "type": "long"
      }
    }
  }
}' 

# 检查搜索服务状态
curl -s http://localhost:8080/api/v1/search/status

# 触发索引重建
sleep 5 && curl -s -X POST "http://localhost:8080/api/v1/document/force-scan" 
curl -X POST "http://localhost:8080/api/v1/search/reindex?clear=true" 

# 编译项目
cd /root/lite-webviewer/frontend && npm install && npm run build 

cd /root/lite-webviewer && mvn compile 
# 启动项目
pkill -f "spring-boot:run" 
cd /root/lite-webviewer && mvn spring-boot:run 
cd /root/lite-webviewer/frontend && npm run dev 
# 全文检索含"新疆"的文档
cd /root/lite-webviewer && curl -s "http://localhost:8080/api/v1/search/advanced" -H "Content-Type: application/json" -d '{"keyword":"新疆"}' 
# 全文检索含"文档"的文档
curl -X POST "http://localhost:8080/api/v1/search/advanced" -H "Content-Type: application/json" -d '{"keyword":"文档"}' 
# 查看索引文档
cd /root/lite-webviewer && curl -s "http://localhost:8080/api/v1/index/json" 