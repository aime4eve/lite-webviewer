#!/usr/bin/env python3
"""
测试IK分词器功能
"""

from elasticsearch import Elasticsearch
import json

def test_ik_analyzer():
    """测试IK分词器"""
    es = Elasticsearch("http://localhost:9200")
    
    # 创建测试索引
    if not es.indices.exists(index="documents"):
        # 创建索引，配置IK中文分词器
        index_mapping = {
            "settings": {
                "analysis": {
                    "analyzer": {
                        "ik_smart_analyzer": {
                            "type": "ik_smart"
                        },
                        "ik_max_word_analyzer": {
                            "type": "ik_max_word"
                        },
                        "my_analyzer": {
                            "type": "custom",
                            "tokenizer": "ik_max_word",
                            "filter": ["lowercase", "stop"]
                        }
                    }
                }
            },
            "mappings": {
                "properties": {
                    "title": {
                        "type": "text",
                        "analyzer": "ik_max_word_analyzer",
                        "search_analyzer": "ik_smart_analyzer",
                        "fields": {
                            "keyword": {
                                "type": "keyword"
                            }
                        }
                    },
                    "content": {
                        "type": "text",
                        "analyzer": "ik_max_word_analyzer",
                        "search_analyzer": "ik_smart_analyzer"
                    },
                    "file_path": {
                        "type": "keyword"
                    },
                    "file_type": {
                        "type": "keyword"
                    },
                    "file_size": {
                        "type": "long"
                    },
                    "last_modified": {
                        "type": "date"
                    }
                }
            }
        }
        
        es.indices.create(index="documents", body=index_mapping)
        print("索引 'documents' 创建成功，使用IK中文分词器")
    
    # 测试文本
    test_text = "认证体系汇总说明表"
    
    # 测试ik_max_word分词器
    print("测试 ik_max_word 分词器:")
    response = es.indices.analyze(
        index="documents",
        body={
            "analyzer": "ik_max_word",
            "text": test_text
        }
    )
    
    tokens = [token["token"] for token in response["tokens"]]
    print(f"原文: {test_text}")
    print(f"分词结果: {tokens}")
    print()
    
    # 测试ik_smart分词器
    print("测试 ik_smart 分词器:")
    response = es.indices.analyze(
        index="documents",
        body={
            "analyzer": "ik_smart",
            "text": test_text
        }
    )
    
    tokens = [token["token"] for token in response["tokens"]]
    print(f"原文: {test_text}")
    print(f"分词结果: {tokens}")
    print()
    
    # 添加一些测试文档
    test_docs = [
        {
            "title": "认证体系汇总说明表.md",
            "content": "这是一个关于认证体系的详细说明文档，包含了各种认证标准和流程。",
            "file_path": "/test/认证体系汇总说明表.md",
            "file_type": ".md",
            "file_size": 1024,
            "last_modified": "2023-01-01T00:00:00"
        },
        {
            "title": "商业地产认证体系梳理报告.md",
            "content": "商业地产认证体系是评估商业地产质量和价值的重要工具。",
            "file_path": "/test/商业地产认证体系梳理报告.md",
            "file_type": ".md",
            "file_size": 2048,
            "last_modified": "2023-01-02T00:00:00"
        }
    ]
    
    # 索引测试文档
    for doc in test_docs:
        es.index(index="documents", body=doc)
    
    # 刷新索引
    es.indices.refresh(index="documents")
    
    # 测试搜索功能
    print("测试搜索功能:")
    search_response = es.search(
        index="documents",
        body={
            "query": {
                "match": {
                    "content": {
                        "query": "认证",
                        "analyzer": "ik_smart"
                    }
                }
            },
            "highlight": {
                "fields": {
                    "content": {}
                }
            }
        }
    )
    
    print(f"找到 {len(search_response['hits']['hits'])} 个结果")
    for hit in search_response['hits']['hits'][:3]:  # 只显示前3个结果
        print(f"标题: {hit['_source']['title']}")
        print(f"分数: {hit['_score']}")
        if 'highlight' in hit and 'content' in hit['highlight']:
            print(f"高亮: {hit['highlight']['content'][0]}")
        print()

if __name__ == "__main__":
    test_ik_analyzer()