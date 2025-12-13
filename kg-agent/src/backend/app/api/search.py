from flask import Blueprint, request, jsonify
from app.models import SearchQuery, SearchResponse
from app.services import search_service

search_bp = Blueprint('search', __name__, url_prefix='/api/search')

@search_bp.route('/query', methods=['POST'])
def search():
    """
    执行混合检索
    ---
    tags:
      - Search
    parameters:
      - in: body
        name: body
        schema:
          id: SearchQuery
          required:
            - query
          properties:
            query:
              type: string
              description: 搜索关键词
            mode:
              type: string
              enum: [fulltext, vector, graph, hybrid]
              default: hybrid
            top_k:
              type: integer
              default: 10
    responses:
      200:
        description: 检索成功
        schema:
          id: SearchResponse
          properties:
            total:
              type: integer
            items:
              type: array
              items:
                $ref: '#/definitions/SearchResultItem'
      400:
        description: 请求参数错误
    """
    data = request.get_json()
    query = SearchQuery(**data)
    result = search_service.search(query)
    return jsonify(result.model_dump())
