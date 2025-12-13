"""
知识图谱服务，负责实体识别和关系抽取
"""
from typing import List, Dict, Any
from app.utils.logger import logger
from app.infrastructure.nebula import nebula_client
import re

class KGService:
    def __init__(self):
        self.nebula_client = nebula_client
    
    def extract_entities(self, text: str) -> List[Dict[str, Any]]:
        """
        从文本中提取实体
        简单实现：基于规则的实体识别
        """
        entities = []
        
        # 简单的规则：提取大写字母开头的连续单词（可能是实体）
        # 实际项目中应使用NLP模型（如LTP、BERT等）
        entity_pattern = r'\b[A-Z][a-zA-Z0-9]*\b'
        matches = re.findall(entity_pattern, text)
        
        for match in matches:
            entities.append({
                "name": match,
                "type": "entity",
                "score": 0.8  # 模拟置信度
            })
        
        return entities
    
    def extract_relations(self, text: str, entities: List[Dict[str, Any]]) -> List[Dict[str, Any]]:
        """
        从文本中提取实体之间的关系
        简单实现：基于规则的关系抽取
        """
        relations = []
        
        # 简单的规则：提取实体之间的关系
        # 实际项目中应使用NLP模型
        entity_names = [e["name"] for e in entities]
        
        # 检查实体对之间是否存在常见关系词汇
        relation_keywords = ["is", "are", "has", "have", "contains", "includes", "relates to", "associated with"]
        
        for i, entity1 in enumerate(entity_names):
            for j, entity2 in enumerate(entity_names):
                if i != j:
                    for keyword in relation_keywords:
                        if keyword in text and entity1 in text and entity2 in text:
                            # 检查实体1、关键词和实体2的顺序
                            entity1_pos = text.find(entity1)
                            entity2_pos = text.find(entity2)
                            keyword_pos = text.find(keyword)
                            
                            if entity1_pos < keyword_pos < entity2_pos:
                                relations.append({
                                    "source": entity1,
                                    "target": entity2,
                                    "relation": keyword,
                                    "score": 0.7  # 模拟置信度
                                })
        
        return relations
    
    def insert_entities_and_relations(self, doc_id: str, entities: List[Dict[str, Any]], relations: List[Dict[str, Any]]):
        """
        将实体和关系插入到图谱中
        """
        try:
            # 插入实体
            for entity in entities:
                # 使用唯一ID：实体名的哈希值
                entity_id = f"ent_{hash(entity['name'])}"
                self.nebula_client._execute(f"USE {self.nebula_client.space_name};")
                self.nebula_client._execute(
                    f'INSERT VERTEX IF NOT EXISTS entity(name, type) VALUES "{entity_id}":("{entity["name"]}", "{entity["type"]}");'
                )
                
                # 建立文档与实体的关系
                self.nebula_client._execute(
                    f'INSERT EDGE IF NOT EXISTS relationship(relation) VALUES "{doc_id}"->"{entity_id}":("MENTIONS");'
                )
            
            # 插入关系
            for relation in relations:
                source_id = f"ent_{hash(relation['source'])}"
                target_id = f"ent_{hash(relation['target'])}"
                self.nebula_client._execute(
                    f'INSERT EDGE IF NOT EXISTS relationship(relation) VALUES "{source_id}"->"{target_id}":("{relation["relation"]}");'
                )
            
            logger.info(f"Inserted {len(entities)} entities and {len(relations)} relations for document {doc_id}")
            
        except Exception as e:
            logger.error(f"Failed to insert entities and relations: {e}")
    
    def build_knowledge_graph(self, doc_id: str, chunks: List[Dict[str, Any]]):
        """
        构建知识图谱
        """
        logger.info(f"Building knowledge graph for document {doc_id}")
        
        for chunk in chunks:
            text = chunk["content"]
            
            # 提取实体
            entities = self.extract_entities(text)
            
            # 提取关系
            relations = self.extract_relations(text, entities)
            
            # 插入实体和关系
            self.insert_entities_and_relations(doc_id, entities, relations)

kg_service = KGService()
