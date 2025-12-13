#!/usr/bin/env python3
"""
Nebula Graph Web API Service
Provides RESTful API interfaces for frontend
"""
import sys
import os
import json
from typing import List, Dict, Any, Optional
from flask import Flask, request, jsonify, render_template, send_from_directory
from flask_cors import CORS

# Add project root to sys.path
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from src.infrastructure.nebula_client import nebula_client

# Static file directory
# Adjusted for new directory structure: src/interfaces/../../web -> web
STATIC_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "../../web"))

app = Flask(__name__, static_folder=STATIC_DIR, static_url_path="")
CORS(app)

@app.route('/')
def index():
    """Home Page"""
    return send_from_directory(STATIC_DIR, "index.html")

@app.route('/api/health')
def health_check():
    """Health Check"""
    try:
        # Try to connect to Nebula Graph (check if client is connected)
        connected = nebula_client.connected
        return jsonify({
            "status": "ok" if connected else "error",
            "message": "Nebula Graph connected" if connected else "Nebula Graph disconnected"
        })
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": f"Health check failed: {str(e)}"
        })

@app.route('/api/query', methods=['POST'])
def query_entities():
    """Query Entities"""
    try:
        data = request.get_json()
        keywords = data.get('keywords', [])
        depth = data.get('depth', 2)
        limit = data.get('limit', 50)
        
        if not keywords:
            # If no keywords, query all entities with a limit
            # Default limit 1000 if not specified
            limit = data.get('limit', 1000)
            
        result = nebula_client.query_entities(keywords, depth, limit)
        return jsonify({
            "status": "success",
            "data": result
        })
    except Exception as e:
        return jsonify({
            "status": "error",
            "message": str(e)
        }), 500

@app.route('/api/stats', methods=['GET'])
def get_stats():
    """Get Graph Statistics"""
    try:
        space_info = nebula_client.get_space_info()
        tags = nebula_client.get_tags()
        edge_types = nebula_client.get_edge_types()
        
        return jsonify({
            "space": space_info.get("data", {}),
            "tags": tags.get("data", []),
            "edge_types": edge_types.get("data", [])
        })
    except Exception as e:
        return jsonify({
            "error": str(e)
        }), 500

if __name__ == '__main__':
    port = int(os.environ.get('PORT', 5000))
    app.run(host='0.0.0.0', port=port, debug=True)
