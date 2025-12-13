from typing import List, Optional
from app.models import GraphQuery, GraphResult, GraphData, GraphEntity, GraphRelation
from app.utils.logger import logger
from app.exceptions import ValidationError
from app.infrastructure.nebula import nebula_client

class GraphService:
    def query_graph(self, query: GraphQuery) -> GraphResult:
        """
        执行图谱查询
        """
        logger.info(f"Executing graph query with keywords: {query.keywords}")

        # 参数验证
        if not query.keywords:
            raise ValidationError(
                message="图谱查询关键词不能为空",
                details={"field": "keywords", "value": query.keywords}
            )

        if query.depth is not None:
            if query.depth <= 0:
                raise ValidationError(
                    message="查询深度必须大于0",
                    details={"field": "depth", "value": query.depth}
                )
            if query.depth > 10:
                raise ValidationError(
                    message="查询深度不能超过10",
                    details={"field": "depth", "value": query.depth}
                )

        # 使用Nebula客户端查询实体和关系
        depth = query.depth if query.depth is not None else 2
        result = nebula_client.query_entities(query.keywords, depth)
        
        # 转换结果为模型对象
        nodes = [GraphEntity(**node) for node in result["nodes"]]
        edges = [GraphRelation(**edge) for edge in result["edges"]]
        
        # 构建返回结果
        graph_data = GraphData(nodes=nodes, edges=edges)
        return GraphResult(data=graph_data, paths=[])

graph_service = GraphService()
