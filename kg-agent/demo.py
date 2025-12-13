#!/usr/bin/env python3
"""
知识图谱系统演示脚本
展示如何使用知识图谱API进行查询和可视化
"""

import requests
import json
import webbrowser
import time

# API基础URL
BASE_URL = "http://localhost:5004"

def demo_search_and_explore():
    """演示搜索和探索功能"""
    print("=== 知识图谱系统演示 ===\n")
    
    # 1. 搜索"人工智能"相关节点
    print("1. 搜索'人工智能'相关节点...")
    search_response = requests.post(
        f"{BASE_URL}/api/kg/search",
        headers={"Content-Type": "application/json"},
        json={"query": "人工智能"}
    )
    
    if search_response.status_code == 200:
        results = search_response.json().get("results", [])
        print(f"找到 {len(results)} 个相关节点:")
        for result in results:
            print(f"  - {result['name']} (类型: {result['type']}, 相关度: {result['relevance']:.2f})")
    else:
        print("搜索失败")
        return
    
    # 2. 探索"人工智能"的知识图谱
    print("\n2. 探索'人工智能'的知识图谱...")
    explore_response = requests.post(
        f"{BASE_URL}/api/kg/explore",
        headers={"Content-Type": "application/json"},
        json={"keywords": ["人工智能"], "depth": 2}
    )
    
    if explore_response.status_code == 200:
        data = explore_response.json()
        nodes = data.get("nodes", [])
        edges = data.get("edges", [])
        
        print(f"知识图谱包含 {len(nodes)} 个节点和 {len(edges)} 条边:")
        
        # 显示节点信息
        print("\n节点信息:")
        for node in nodes:
            print(f"  - {node['name']} (类型: {node['type']})")
            if 'description' in node:
                print(f"    描述: {node['description']}")
        
        # 显示边信息
        print("\n节点关系:")
        for edge in edges:
            print(f"  - {edge['source']} --[{edge['type']}]--> {edge['target']}")
    else:
        print("探索失败")
        return
    
    # 3. 获取"人工智能"节点的详细信息
    print("\n3. 获取'人工智能'节点的详细信息...")
    import urllib.parse
    node_id = urllib.parse.quote("人工智能")
    
    detail_response = requests.get(f"{BASE_URL}/api/kg/node/{node_id}")
    
    if detail_response.status_code == 200:
        node_detail = detail_response.json()
        print(f"节点名称: {node_detail['name']}")
        print(f"节点类型: {node_detail['type']}")
        print(f"节点描述: {node_detail['description']}")
        
        if 'properties' in node_detail:
            print("节点属性:")
            for key, value in node_detail['properties'].items():
                print(f"  - {key}: {value}")
    else:
        print("获取节点详情失败")
        return
    
    print("\n演示完成!")
    return True

def open_visualization():
    """打开可视化界面"""
    print("\n正在打开知识图谱可视化界面...")
    webbrowser.open(f"{BASE_URL}")
    print("请在浏览器中查看可视化界面")

def main():
    """运行演示"""
    # 检查API服务器是否运行
    try:
        response = requests.get(f"{BASE_URL}/health")
        if response.status_code != 200:
            print("API服务器未正常运行，请先启动服务器")
            return
    except requests.exceptions.ConnectionError:
        print("无法连接到API服务器，请确保服务器已启动")
        return
    
    # 运行演示
    if demo_search_and_explore():
        # 询问是否打开可视化界面
        try:
            choice = input("\n是否打开可视化界面? (y/n): ").lower()
            if choice == 'y' or choice == 'yes':
                open_visualization()
        except KeyboardInterrupt:
            print("\n演示结束")

if __name__ == "__main__":
    main()