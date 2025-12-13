#!/usr/bin/env python3
"""
测试 Elasticsearch 连接
"""

from elasticsearch import Elasticsearch

def test_elasticsearch():
    try:
        es = Elasticsearch("http://localhost:9200")
        
        # 测试连接
        info = es.info()
        print(f"Elasticsearch 版本: {info['version']['number']}")
        
        # 列出所有索引
        indices = es.cat.indices(format='json')
        print(f"现有索引: {indices}")
        
        # 尝试创建一个简单索引
        if not es.indices.exists(index="test"):
            es.indices.create(index="test")
            print("创建测试索引成功")
        
        return True
    except Exception as e:
        print(f"连接 Elasticsearch 时出错: {e}")
        return False

if __name__ == "__main__":
    test_elasticsearch()