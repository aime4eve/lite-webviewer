"""
知识图谱服务测试
"""
import unittest
import sys
import os

# 添加项目根目录到Python路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src/backend'))

from app.services.kg_service import kg_service

class TestKGService(unittest.TestCase):
    def test_extract_entities(self):
        """
        测试实体识别功能
        """
        text = "Python is a programming language. Java is another popular language."
        entities = kg_service.extract_entities(text)
        
        # 检查实体数量
        self.assertGreater(len(entities), 0)
        
        # 检查是否提取到了预期的实体
        entity_names = [e["name"] for e in entities]
        self.assertIn("Python", entity_names)
        self.assertIn("Java", entity_names)
    
    def test_extract_relations(self):
        """
        测试关系抽取功能
        """
        text = "Python is a programming language. Java is another popular language."
        entities = kg_service.extract_entities(text)
        relations = kg_service.extract_relations(text, entities)
        
        # 检查关系数量
        self.assertGreater(len(relations), 0)
        
        # 检查关系是否正确
        relation_types = [r["relation"] for r in relations]
        self.assertIn("is", relation_types)
    
    def test_build_knowledge_graph(self):
        """
        测试知识图谱构建功能
        """
        # 简单测试，确保不会抛出异常
        doc_id = "test_doc_123"
        chunks = [{
            "content": "Python is a programming language. Java is another popular language.",
            "index": 0
        }]
        
        # 调用构建函数，确保不会抛出异常
        try:
            kg_service.build_knowledge_graph(doc_id, chunks)
            result = True
        except Exception as e:
            result = False
        
        self.assertTrue(result, "Knowledge graph building should not raise exceptions")

if __name__ == "__main__":
    unittest.main()
