#!/usr/bin/env python3
"""
Directly test Nebula Graph query function
"""
import sys
import os

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.infrastructure.nebula_client import NebulaClient, nebula_client

def test_nebula_connection():
    """Test Nebula Graph connection"""
    print(f"Connecting to Nebula Graph...")
    
    try:
        # Test connection
        if nebula_client.connected:
            print("Connected successfully!")
            result = nebula_client.execute_query("SHOW SPACES")
            if result.get("success"):
                print("Available spaces:")
                for row in result.get("data", []):
                    print(f"  - {row[0]}")
            return True
        else:
            print("Connection failed")
            return False
            
    except Exception as e:
        print(f"Connection failed: {str(e)}")
        return False

def test_entity_query():
    """Test entity query function"""
    print("\nTesting entity query function...")
    
    try:
        # Test query
        keywords = ["人工智能", "机器学习"]
        depth = 2
        
        result = nebula_client.query_entities(keywords, depth)
        
        print(f"Query keywords: {keywords}")
        print(f"Query depth: {depth}")
        print(f"Found {len(result['nodes'])} nodes and {len(result['edges'])} edges:")
        
        for node in result['nodes']:
            print(f"  Node: {node['id']} ({node['type']}) - {node.get('name', '')}")
            
        for edge in result['edges']:
            print(f"  Edge: {edge['source']} -> {edge['target']} ({edge.get('type', '')})")
            
        return True
    except Exception as e:
        print(f"Query failed: {str(e)}")
        return False

def main():
    print("=== Nebula Graph Direct Query Test ===\n")
    
    # Test connection
    if not test_nebula_connection():
        print("Connection test failed, exiting")
        return 1
        
    # Test query
    if not test_entity_query():
        print("Query test failed, exiting")
        return 1
        
    print("\nAll tests passed!")
    return 0

if __name__ == "__main__":
    sys.exit(main())