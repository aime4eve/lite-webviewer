#!/usr/bin/env python3
"""
知识图谱查询API服务器，集成Nebula Graph数据库
"""
import sys
import os
from flask import Flask, request, jsonify, send_from_directory
from flask_cors import CORS

# 添加项目路径到sys.path
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'src', 'backend'))

app = Flask(__name__, static_folder='static', static_url_path='/static')
CORS(app)  # 启用CORS支持

# 模拟知识图谱数据
def get_mock_graph_data(keywords, depth=2):
    """获取模拟的知识图谱数据"""
    # 基于关键词生成模拟数据
    all_nodes = [
        {"id": "人工智能", "name": "人工智能", "type": "概念", "description": "模拟人类智能的计算机系统"},
        {"id": "机器学习", "name": "机器学习", "type": "技术", "description": "让计算机从数据中学习的算法"},
        {"id": "深度学习", "name": "深度学习", "type": "技术", "description": "基于神经网络的机器学习方法"},
        {"id": "神经网络", "name": "神经网络", "type": "模型", "description": "模拟人脑神经元结构的计算模型"},
        {"id": "自然语言处理", "name": "自然语言处理", "type": "技术", "description": "让计算机理解和生成人类语言"},
        {"id": "计算机视觉", "name": "计算机视觉", "type": "技术", "description": "让计算机理解和分析图像视频"},
        {"id": "强化学习", "name": "强化学习", "type": "技术", "description": "通过试错学习最优策略"},
        {"id": "监督学习", "name": "监督学习", "type": "技术", "description": "使用标注数据训练模型"},
        {"id": "无监督学习", "name": "无监督学习", "type": "技术", "description": "从未标注数据中发现模式"},
        {"id": "知识图谱", "name": "知识图谱", "type": "技术", "description": "结构化表示知识的图数据库"}
    ]
    
    all_edges = [
        {"source": "人工智能", "target": "机器学习", "type": "包含"},
        {"source": "机器学习", "target": "深度学习", "type": "属于"},
        {"source": "深度学习", "target": "神经网络", "type": "基于"},
        {"source": "人工智能", "target": "自然语言处理", "type": "包含"},
        {"source": "人工智能", "target": "计算机视觉", "type": "包含"},
        {"source": "机器学习", "target": "强化学习", "type": "包含"},
        {"source": "机器学习", "target": "监督学习", "type": "包含"},
        {"source": "机器学习", "target": "无监督学习", "type": "包含"},
        {"source": "深度学习", "target": "计算机视觉", "type": "应用于"},
        {"source": "深度学习", "target": "自然语言处理", "type": "应用于"},
        {"source": "人工智能", "target": "知识图谱", "type": "包含"}
    ]
    
    # 根据关键词过滤节点
    if keywords:
        filtered_nodes = []
        node_ids = set()
        
        for keyword in keywords:
            for node in all_nodes:
                if keyword.lower() in node["name"].lower() and node["id"] not in node_ids:
                    filtered_nodes.append(node)
                    node_ids.add(node["id"])
        
        # 如果没有找到匹配的节点，返回空结果
        if not filtered_nodes:
            return {"nodes": [], "edges": []}
        
        # 根据深度添加相关节点
        if depth >= 2:
            # 添加直接相连的节点
            for edge in all_edges:
                if edge["source"] in node_ids and edge["target"] not in node_ids:
                    target_node = next((n for n in all_nodes if n["id"] == edge["target"]), None)
                    if target_node:
                        filtered_nodes.append(target_node)
                        node_ids.add(edge["target"])
                elif edge["target"] in node_ids and edge["source"] not in node_ids:
                    source_node = next((n for n in all_nodes if n["id"] == edge["source"]), None)
                    if source_node:
                        filtered_nodes.append(source_node)
                        node_ids.add(edge["source"])
        
        # 过滤边，只保留连接到过滤后节点的边
        filtered_edges = [
            edge for edge in all_edges
            if edge["source"] in node_ids and edge["target"] in node_ids
        ]
        
        return {"nodes": filtered_nodes, "edges": filtered_edges}
    
    return {"nodes": all_nodes, "edges": all_edges}

@app.route('/')
def index():
    """主页"""
    return send_from_directory('static', 'graph_visualization.html')

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
            
        # 获取知识图谱数据
        result = get_mock_graph_data(keywords, depth)
        
        return jsonify(result)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/kg/node/<path:node_id>', methods=['GET'])
def get_node_details(node_id):
    """获取节点详细信息"""
    try:
        # 模拟节点详细信息
        all_nodes = {
            "人工智能": {
                "id": "人工智能",
                "name": "人工智能",
                "type": "概念",
                "description": "人工智能（Artificial Intelligence，AI）是计算机科学的一个分支，致力于创建能够执行通常需要人类智能的任务的系统。",
                "properties": {
                    "起源时间": "1956年",
                    "创始人": "约翰·麦卡锡等",
                    "主要应用": "语音识别、图像识别、自然语言处理等"
                }
            },
            "机器学习": {
                "id": "机器学习",
                "name": "机器学习",
                "type": "技术",
                "description": "机器学习是人工智能的一个子集，使计算机系统能够从经验中学习和改进，而无需明确编程。",
                "properties": {
                    "主要类型": "监督学习、无监督学习、强化学习",
                    "常用算法": "决策树、支持向量机、神经网络等",
                    "应用领域": "推荐系统、医疗诊断、金融风控等"
                }
            },
            "深度学习": {
                "id": "深度学习",
                "name": "深度学习",
                "type": "技术",
                "description": "深度学习是机器学习的一个子集，使用多层神经网络来学习数据的复杂模式。",
                "properties": {
                    "核心技术": "卷积神经网络、循环神经网络、Transformer等",
                    "典型应用": "图像识别、语音识别、自然语言处理等",
                    "代表框架": "TensorFlow、PyTorch、Keras等"
                }
            },
            "神经网络": {
                "id": "神经网络",
                "name": "神经网络",
                "type": "模型",
                "description": "神经网络是一种模拟人脑神经元结构的计算模型，是深度学习的基础。",
                "properties": {
                    "基本单元": "神经元、权重、偏置、激活函数",
                    "网络类型": "前馈网络、卷积网络、循环网络等",
                    "训练算法": "反向传播、梯度下降等"
                }
            },
            "自然语言处理": {
                "id": "自然语言处理",
                "name": "自然语言处理",
                "type": "技术",
                "description": "自然语言处理是人工智能的一个分支，致力于让计算机理解和生成人类语言。",
                "properties": {
                    "主要任务": "文本分类、命名实体识别、机器翻译、情感分析等",
                    "核心技术": "词嵌入、注意力机制、Transformer等",
                    "应用场景": "聊天机器人、智能客服、文本摘要等"
                }
            },
            "计算机视觉": {
                "id": "计算机视觉",
                "name": "计算机视觉",
                "type": "技术",
                "description": "计算机视觉是人工智能的一个分支，致力于让计算机理解和分析图像与视频。",
                "properties": {
                    "主要任务": "图像分类、目标检测、图像分割、人脸识别等",
                    "核心技术": "卷积神经网络、特征提取、图像处理等",
                    "应用场景": "自动驾驶、医疗影像分析、安防监控等"
                }
            },
            "强化学习": {
                "id": "强化学习",
                "name": "强化学习",
                "type": "技术",
                "description": "强化学习是机器学习的一个分支，通过智能体与环境的交互来学习最优策略。",
                "properties": {
                    "核心概念": "智能体、环境、状态、动作、奖励",
                    "主要算法": "Q-Learning、SARSA、策略梯度、Actor-Critic等",
                    "应用场景": "游戏AI、机器人控制、资源调度等"
                }
            },
            "监督学习": {
                "id": "监督学习",
                "name": "监督学习",
                "type": "技术",
                "description": "监督学习是机器学习的一种方法，使用标注数据来训练模型。",
                "properties": {
                    "学习方式": "从输入-输出对中学习映射关系",
                    "主要算法": "线性回归、逻辑回归、决策树、SVM等",
                    "应用场景": "分类问题、回归问题、预测分析等"
                }
            },
            "无监督学习": {
                "id": "无监督学习",
                "name": "无监督学习",
                "type": "技术",
                "description": "无监督学习是机器学习的一种方法，从未标注数据中发现模式和结构。",
                "properties": {
                    "学习方式": "从数据中发现内在结构和模式",
                    "主要算法": "K-均值聚类、层次聚类、PCA、自编码器等",
                    "应用场景": "客户分群、异常检测、数据降维等"
                }
            },
            "知识图谱": {
                "id": "知识图谱",
                "name": "知识图谱",
                "type": "技术",
                "description": "知识图谱是一种结构化表示知识的图数据库，用于存储实体及其关系。",
                "properties": {
                    "核心组成": "实体、属性、关系",
                    "存储技术": "RDF、属性图、图数据库等",
                    "应用场景": "搜索引擎、推荐系统、问答系统等"
                }
            }
        }
        
        node = all_nodes.get(node_id)
        if not node:
            return jsonify({"error": "Node not found"}), 404
            
        return jsonify(node)
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/api/kg/search', methods=['POST'])
def search_knowledge_graph():
    """搜索知识图谱"""
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "No data provided"}), 400
            
        query = data.get('query', '')
        if not query:
            return jsonify({"error": "Query is required"}), 400
            
        # 模拟搜索结果
        all_nodes = [
            {"id": "人工智能", "name": "人工智能", "type": "概念", "relevance": 0.9},
            {"id": "机器学习", "name": "机器学习", "type": "技术", "relevance": 0.8},
            {"id": "深度学习", "name": "深度学习", "type": "技术", "relevance": 0.7}
        ]
        
        # 简单的搜索匹配
        results = [
            node for node in all_nodes
            if query.lower() in node["name"].lower()
        ]
        
        return jsonify({"results": results})
        
    except Exception as e:
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({"status": "healthy"})

if __name__ == '__main__':
    print("启动知识图谱查询API服务...")
    print("API端点:")
    print("  GET / - 知识图谱可视化页面")
    print("  POST /api/kg/explore - 探索知识图谱")
    print("  GET /api/kg/node/<node_id> - 获取节点详细信息")
    print("  POST /api/kg/search - 搜索知识图谱")
    print("  GET /health - 健康检查")
    print("\n测试命令:")
    print('  curl -X POST http://localhost:5001/api/kg/explore -H "Content-Type: application/json" -d \'{"keywords": ["人工智能"], "depth": 2}\'')
    
    app.run(host='0.0.0.0', port=5004, debug=True)