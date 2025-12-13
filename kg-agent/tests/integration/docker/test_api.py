#!/usr/bin/env python3
"""
知识图谱API测试脚本
测试所有API端点的功能
"""

import requests
import json

# API基础URL
BASE_URL = "http://localhost:5004"

def test_health():
    """测试健康检查端点"""
    print("测试健康检查端点...")
    try:
        response = requests.get(f"{BASE_URL}/health")
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.json()}")
        print("健康检查测试通过\n")
        return True
    except Exception as e:
        print(f"健康检查测试失败: {e}\n")
        return False

def test_search():
    """测试搜索API"""
    print("测试搜索API...")
    try:
        response = requests.post(
            f"{BASE_URL}/api/kg/search",
            headers={"Content-Type": "application/json"},
            json={"query": "深度学习"}
        )
        print(f"状态码: {response.status_code}")
        print(f"响应: {json.dumps(response.json(), ensure_ascii=False, indent=2)}")
        print("搜索API测试通过\n")
        return True
    except Exception as e:
        print(f"搜索API测试失败: {e}\n")
        return False

def test_explore():
    """测试探索API"""
    print("测试探索API...")
    try:
        response = requests.post(
            f"{BASE_URL}/api/kg/explore",
            headers={"Content-Type": "application/json"},
            json={"keywords": ["深度学习"], "depth": 2}
        )
        print(f"状态码: {response.status_code}")
        data = response.json()
        print(f"节点数量: {len(data.get('nodes', []))}")
        print(f"边数量: {len(data.get('edges', []))}")
        print("探索API测试通过\n")
        return True
    except Exception as e:
        print(f"探索API测试失败: {e}\n")
        return False

def test_node_details():
    """测试节点详情API"""
    print("测试节点详情API...")
    try:
        # 对中文节点ID进行URL编码
        import urllib.parse
        node_id = urllib.parse.quote("人工智能")
        
        response = requests.get(f"{BASE_URL}/api/kg/node/{node_id}")
        print(f"状态码: {response.status_code}")
        print(f"响应: {json.dumps(response.json(), ensure_ascii=False, indent=2)}")
        print("节点详情API测试通过\n")
        return True
    except Exception as e:
        print(f"节点详情API测试失败: {e}\n")
        return False

def main():
    """运行所有测试"""
    print("开始测试知识图谱API...\n")
    
    tests = [
        test_health,
        test_search,
        test_explore,
        test_node_details
    ]
    
    passed = 0
    total = len(tests)
    
    for test in tests:
        if test():
            passed += 1
    
    print(f"测试完成: {passed}/{total} 通过")
    
    if passed == total:
        print("所有测试通过! 🎉")
    else:
        print("部分测试失败，请检查API服务器")

if __name__ == "__main__":
    main()