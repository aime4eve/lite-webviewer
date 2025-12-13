#!/usr/bin/env python3
"""
Nebula Graph Advanced Operations
"""
import sys
import os
import json
from typing import List, Dict, Any, Optional

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.infrastructure.nebula_client import nebula_client

def find_shortest_path(src_id: str, dst_id: str):
    """Find shortest path between two nodes"""
    print(f"Finding shortest path between {src_id} and {dst_id}...")
    
    query = f"""
    FIND SHORTEST PATH FROM "{src_id}" TO "{dst_id}" OVER * YIELD path AS p
    """
    
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + query)
    
    if not result.is_succeeded():
        print(f"Query failed: {result.error_msg()}")
        return None
    
    paths = []
    for row in result:
        if row[0]:
            path = row[0]
            nodes = []
            edges = []
            
            # Get nodes in path
            for node in path.nodes:
                props = node.get_properties()
                nodes.append({
                    "id": str(node.get_id()),
                    "name": props.get("name", ""),
                    "type": props.get("type", ""),
                    "description": props.get("description", "")
                })
            
            # Get edges in path
            for edge in path.relationships:
                props = edge.get_properties()
                edges.append({
                    "source": str(edge.src()),
                    "target": str(edge.dst()),
                    "type": props.get("relation", "RELATED_TO"),
                    "weight": props.get("weight", 1.0),
                    "description": props.get("description", "")
                })
            
            paths.append({
                "nodes": nodes,
                "edges": edges,
                "length": len(edges)
            })
    
    print(f"Found {len(paths)} paths")
    for i, path in enumerate(paths):
        print(f"Path {i+1} (Length: {path['length']}):")
        for j, node in enumerate(path["nodes"]):
            if j > 0:
                edge = path["edges"][j-1]
                print(f"  -> {edge['type']} ->")
            print(f"  {node['name']} ({node['type']})")
    
    return paths

def find_related_entities(entity_id: str, relation_type: Optional[str] = None, max_depth: int = 2):
    """Find all entities related to a specific entity"""
    print(f"Finding entities related to {entity_id}...")
    
    if relation_type:
        query = f"""
        MATCH (v:entity)-[e:relationship*1..{max_depth}]->(u:entity) 
        WHERE id(v) == "{entity_id}" AND ALL(r IN e WHERE r.relation == "{relation_type}")
        RETURN DISTINCT id(u) AS related_id, u.name AS name, u.type AS type, u.description AS description
        """
    else:
        query = f"""
        MATCH (v:entity)-[e:relationship*1..{max_depth}]->(u:entity) 
        WHERE id(v) == "{entity_id}"
        RETURN DISTINCT id(u) AS related_id, u.name AS name, u.type AS type, u.description AS description
        """
    
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + query)
    
    if not result.is_succeeded():
        print(f"Query failed: {result.error_msg()}")
        return []
    
    related_entities = []
    for row in result:
        if row[0] and row[1] and row[2]:
            related_entities.append({
                "id": str(row[0]),
                "name": str(row[1]),
                "type": str(row[2]),
                "description": str(row[3]) if row[3] else ""
            })
    
    print(f"Found {len(related_entities)} related entities:")
    for entity in related_entities:
        print(f"  {entity['name']} ({entity['type']}) - {entity['description']}")
    
    return related_entities

def find_entities_by_type(entity_type: str):
    """Find all entities of a specific type"""
    print(f"Finding entities of type {entity_type}...")
    
    query = f"""
    MATCH (v:entity) 
    WHERE v.type == "{entity_type}"
    RETURN id(v) AS entity_id, v.name AS name, v.description AS description
    """
    
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + query)
    
    if not result.is_succeeded():
        print(f"Query failed: {result.error_msg()}")
        return []
    
    entities = []
    for row in result:
        if row[0] and row[1]:
            entities.append({
                "id": str(row[0]),
                "name": str(row[1]),
                "description": str(row[2]) if row[2] else ""
            })
    
    print(f"Found {len(entities)} entities of type {entity_type}:")
    for entity in entities:
        print(f"  {entity['name']} - {entity['description']}")
    
    return entities

def find_entities_by_keyword(keyword: str):
    """Find entities containing specific keyword"""
    print(f"Finding entities containing keyword '{keyword}'...")
    
    query = f"""
    MATCH (v:entity) 
    WHERE v.name CONTAINS "{keyword}" OR v.description CONTAINS "{keyword}"
    RETURN id(v) AS entity_id, v.name AS name, v.type AS type, v.description AS description
    """
    
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + query)
    
    if not result.is_succeeded():
        print(f"Query failed: {result.error_msg()}")
        return []
    
    entities = []
    for row in result:
        if row[0] and row[1]:
            entities.append({
                "id": str(row[0]),
                "name": str(row[1]),
                "type": str(row[2]),
                "description": str(row[3]) if row[3] else ""
            })
    
    print(f"Found {len(entities)} entities containing keyword '{keyword}':")
    for entity in entities:
        print(f"  {entity['name']} ({entity['type']}) - {entity['description']}")
    
    return entities

def calculate_centrality():
    """Calculate node centrality metrics"""
    print("Calculating node centrality metrics...")
    
    # Calculate degree centrality
    query = """
    MATCH (v:entity)
    OPTIONAL MATCH (v)-[e:relationship]->()
    WITH v, count(e) AS out_degree
    OPTIONAL MATCH (v)<-[e:relationship]-()
    WITH v, out_degree, count(e) AS in_degree
    RETURN id(v) AS entity_id, v.name AS name, out_degree, in_degree, out_degree + in_degree AS degree
    ORDER BY degree DESC
    """
    
    result = nebula_client._execute(f"USE {nebula_client.space_name}; " + query)
    
    if not result.is_succeeded():
        print(f"Query failed: {result.error_msg()}")
        return []
    
    centralities = []
    for row in result:
        if row[0] and row[1]:
            centralities.append({
                "id": str(row[0]),
                "name": str(row[1]),
                "out_degree": row[2] if row[2] is not None else 0,
                "in_degree": row[3] if row[3] is not None else 0,
                "degree": row[4] if row[4] is not None else 0
            })
    
    print("Top 10 nodes by degree centrality:")
    for i, node in enumerate(centralities[:10]):
        print(f"  {i+1}. {node['name']} (Degree: {node['degree']}, In: {node['in_degree']}, Out: {node['out_degree']})")
    
    return centralities
