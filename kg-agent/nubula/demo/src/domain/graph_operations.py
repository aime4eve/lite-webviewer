#!/usr/bin/env python3
"""
Nebula Graph Graph Operations Example
"""
import sys
import os
import json
from typing import List, Dict, Any

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.infrastructure.nebula_client import nebula_client

def create_sample_entities():
    """Create sample entities"""
    print("Creating sample entities...")
    
    entities = [
        ("人工智能", "概念", "模拟人类智能的技术", "{category: '技术', importance: 'high'}"),
        ("机器学习", "技术", "使计算机能够学习的技术", "{category: '技术', importance: 'high'}"),
        ("深度学习", "技术", "基于神经网络的学习方法", "{category: '技术', importance: 'medium'}"),
        ("神经网络", "技术", "模拟生物神经网络的计算模型", "{category: '技术', importance: 'medium'}"),
        ("自然语言处理", "技术", "使计算机能够理解人类语言的技术", "{category: '技术', importance: 'medium'}"),
        ("计算机视觉", "技术", "使计算机能够理解图像和视频的技术", "{category: '技术', importance: 'medium'}"),
        ("强化学习", "技术", "通过试错学习的技术", "{category: '技术', importance: 'medium'}"),
        ("监督学习", "技术", "使用标记数据学习的技术", "{category: '技术', importance: 'medium'}"),
        ("无监督学习", "技术", "使用未标记数据学习的技术", "{category: '技术', importance: 'medium'}"),
        ("知识图谱", "技术", "表示知识的图形结构", "{category: '技术', importance: 'high'}"),
        ("图神经网络", "技术", "处理图数据的神经网络", "{category: '技术', importance: 'medium'}"),
        ("大语言模型", "技术", "大规模预训练语言模型", "{category: '技术', importance: 'high'}"),
        ("Transformer", "技术", "基于自注意力机制的模型", "{category: '技术', importance: 'medium'}"),
        ("BERT", "模型", "双向Transformer编码器", "{category: '模型', importance: 'high'}"),
        ("GPT", "模型", "生成式预训练Transformer", "{category: '模型', importance: 'high'}")
    ]
    
    for entity_id, name, entity_type, description, properties in entities:
        nebula_client.insert_entity(entity_id, name, entity_type, description, properties)
    
    print(f"Created {len(entities)} entities")

def create_sample_relationships():
    """Create sample relationships"""
    print("Creating sample relationships...")
    
    relationships = [
        ("人工智能", "机器学习", "包含", 0.9, "人工智能包含机器学习"),
        ("机器学习", "深度学习", "包含", 0.8, "机器学习包含深度学习"),
        ("深度学习", "神经网络", "基于", 0.9, "深度学习基于神经网络"),
        ("人工智能", "自然语言处理", "包含", 0.7, "人工智能包含自然语言处理"),
        ("机器学习", "自然语言处理", "应用", 0.6, "机器学习应用于自然语言处理"),
        ("人工智能", "计算机视觉", "包含", 0.7, "人工智能包含计算机视觉"),
        ("机器学习", "计算机视觉", "应用", 0.6, "机器学习应用于计算机视觉"),
        ("机器学习", "强化学习", "包含", 0.7, "机器学习包含强化学习"),
        ("机器学习", "监督学习", "包含", 0.8, "机器学习包含监督学习"),
        ("机器学习", "无监督学习", "包含", 0.8, "机器学习包含无监督学习"),
        ("人工智能", "知识图谱", "包含", 0.6, "人工智能包含知识图谱"),
        ("深度学习", "图神经网络", "包含", 0.7, "深度学习包含图神经网络"),
        ("自然语言处理", "大语言模型", "包含", 0.9, "自然语言处理包含大语言模型"),
        ("大语言模型", "Transformer", "基于", 0.9, "大语言模型基于Transformer"),
        ("Transformer", "BERT", "包含", 0.8, "Transformer包含BERT"),
        ("Transformer", "GPT", "包含", 0.8, "Transformer包含GPT"),
        ("知识图谱", "图神经网络", "应用", 0.7, "知识图谱应用图神经网络"),
        ("监督学习", "神经网络", "使用", 0.7, "监督学习使用神经网络"),
        ("无监督学习", "神经网络", "使用", 0.7, "无监督学习使用神经网络"),
        ("强化学习", "神经网络", "使用", 0.7, "强化学习使用神经网络")
    ]
    
    for src_id, dst_id, relation_type, weight, description in relationships:
        nebula_client.insert_relationship(src_id, dst_id, relation_type, weight, description)
    
    print(f"Created {len(relationships)} relationships")

def query_graph(keywords: List[str], depth: int = 2):
    """Query graph"""
    print(f"Querying keywords: {keywords}, depth: {depth}")
    
    result = nebula_client.query_entities(keywords, depth)
    
    print(f"Found {len(result['nodes'])} nodes and {len(result['edges'])} edges:")
    
    for node in result['nodes']:
        print(f"  Node: {node['id']} ({node['type']}) - {node.get('name', '')}")
        if node.get('description'):
            print(f"    Description: {node['description']}")
        
    for edge in result['edges']:
        print(f"  Edge: {edge['source']} -> {edge['target']} ({edge.get('type', '')}) Weight: {edge.get('weight', 1.0)}")
        if edge.get('description'):
            print(f"    Description: {edge['description']}")
    
    return result

def export_graph_to_json(filename: str):
    """Export graph to JSON file"""
    print(f"Exporting graph to {filename}...")
    
    # Query all nodes
    all_nodes_query = "MATCH (v:entity) RETURN v;"
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + all_nodes_query)
    
    nodes = []
    if result.is_succeeded():
        for row in result:
            if row[0]:
                node_id = row[0].get_id()
                props = row[0].get_properties()
                nodes.append({
                    "id": str(node_id),
                    "name": props.get("name", ""),
                    "type": props.get("type", ""),
                    "description": props.get("description", ""),
                    "properties": props.get("properties", "")
                })
    
    # Query all edges
    all_edges_query = "MATCH (v1:entity)-[e:relationship]->(v2:entity) RETURN v1, e, v2;"
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + all_edges_query)
    
    edges = []
    if result.is_succeeded():
        for row in result:
            if row[0] and row[1] and row[2]:
                src_id = str(row[0].get_id())
                dst_id = str(row[2].get_id())
                edge_props = row[1].get_properties()
                edges.append({
                    "source": src_id,
                    "target": dst_id,
                    "type": edge_props.get("relation", "RELATED_TO"),
                    "weight": edge_props.get("weight", 1.0),
                    "description": edge_props.get("description", "")
                })
    
    graph_data = {
        "nodes": nodes,
        "edges": edges
    }
    
    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(graph_data, f, ensure_ascii=False, indent=2)
    
    print(f"Graph exported to {filename}, containing {len(nodes)} nodes and {len(edges)} edges")

def main():
    print("=== Nebula Graph Operations Example ===\n")
    
    # Create sample data
    create_sample_entities()
    create_sample_relationships()
    
    # Query example
    print("\n=== Query Example ===")
    query_graph(["人工智能", "机器学习"], 2)
    
    # Export graph
    print("\n=== Export Graph ===")
    export_graph_to_json("knowledge_graph.json")
    
    print("\n=== Example Completed ===")

if __name__ == "__main__":
    main()