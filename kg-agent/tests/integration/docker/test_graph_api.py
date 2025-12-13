#!/usr/bin/env python
"""
Test script for kg-agent graph query functionality.
"""
import requests
import json

def test_graph_query():
    """Test the graph query API endpoint"""
    url = "http://localhost:5000/api/kg/explore"
    
    # Test data
    test_data = {
        "keywords": ["人工智能"],
        "depth": 2
    }
    
    try:
        response = requests.post(url, json=test_data)
        response.raise_for_status()
        
        result = response.json()
        print("Graph Query Result:")
        print(json.dumps(result, indent=2, ensure_ascii=False))
        
        # Check if we got any nodes
        if result.get("data", {}).get("nodes"):
            print(f"\nFound {len(result['data']['nodes'])} nodes and {len(result['data']['edges'])} edges")
            for node in result["data"]["nodes"][:3]:  # Show first 3 nodes
                print(f"Node: {node['name']} (Type: {node['type']})")
        else:
            print("No nodes found in the graph")
            
    except requests.exceptions.ConnectionError:
        print("Error: Could not connect to the API server. Make sure it's running on http://localhost:5000")
    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    test_graph_query()