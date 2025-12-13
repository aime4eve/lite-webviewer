#!/usr/bin/env python3
"""
简单的Flask应用，用于测试知识图谱查询API
"""
import sys
import os
from flask import Flask, request, jsonify, send_from_directory

# 添加项目路径到sys.path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src', 'backend'))

app = Flask(__name__, static_folder='static', static_url_path='/static')

@app.route('/api/kg/explore', methods=['POST'])
def explore_knowledge_graph():
    """探索知识图谱API"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data provided"}), 400
            
        keywords = data.get('keywords', [])
        depth = data.get('depth', 2)
        
        if not keywords:
            return jsonify({"error": "Keywords are required"}), 400
            
        # 模拟查询结果
        result = {
            "nodes": [
                {"id": "人工智能", "name": "人工智能", "type": "概念"},
                {"id": "机器学习", "name": "机器学习", "type": "技术"},
                {"id": "深度学习", "name": "深度学习", "type": "技术"}
            ],
            "edges": [
                {"source": "人工智能", "target": "机器学习", "type": "包含"},
                {"source": "机器学习", "target": "深度学习", "type": "属于"}
            ]
        }
        
        # 根据关键词过滤结果
        if keywords:
            filtered_nodes = [
                node for node in result["nodes"] 
                if any(keyword in node["name"] for keyword in keywords)
            ]
            
            # 获取过滤后的节点ID
            node_ids = {node["id"] for node in filtered_nodes}
            
            # 过滤边，只保留连接到过滤后节点的边
            filtered_edges = [
                edge for edge in result["edges"]
                if edge["source"] in node_ids or edge["target"] in node_ids
            ]
            
            result = {
                "nodes": filtered_nodes,
                "edges": filtered_edges
            }
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/')
def index():
    """主页"""
    return send_from_directory('static', 'graph_visualization.html')

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({"status": "healthy"})

if __name__ == '__main__':
    print("启动知识图谱查询API服务...")
    print("API端点:")
    print("  GET / - 知识图谱可视化页面")
    print("  POST /api/kg/explore - 探索知识图谱")
    print("  GET /health - 健康检查")
    print("\n测试命令:")
    print('  curl -X POST http://localhost:5001/api/kg/explore -H "Content-Type: application/json" -d \'{"keywords": ["人工智能"], "depth": 2}\'')
    
    app.run(host='0.0.0.0', port=5001, debug=True)