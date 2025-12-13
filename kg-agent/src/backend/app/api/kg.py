from flask import Blueprint, request, jsonify
from app.models import GraphQuery
from app.services import graph_service

kg_bp = Blueprint('kg', __name__, url_prefix='/api/kg')

@kg_bp.route('/explore', methods=['POST'])
def explore_graph():
    """
    图谱探索与查询
    ---
    tags:
      - Knowledge Graph
    parameters:
      - in: body
        name: body
        schema:
          id: GraphQuery
          properties:
            keywords:
              type: array
              items:
                type: string
            depth:
              type: integer
              default: 2
    responses:
      200:
        description: 查询成功
        schema:
          id: GraphResult
          properties:
            data:
              $ref: '#/definitions/GraphData'
    """
    data = request.get_json()
    query = GraphQuery(**data)
    result = graph_service.query_graph(query)
    return jsonify(result.model_dump())
