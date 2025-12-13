from typing import List, Dict, Any, Optional
from pydantic import BaseModel, Field

class GraphEntity(BaseModel):
    """图谱实体模型"""
    id: str = Field(..., description="实体ID")
    name: str = Field(..., description="实体名称")
    type: str = Field(..., description="实体类型")
    properties: Dict[str, Any] = Field(default_factory=dict, description="实体属性")

class GraphRelation(BaseModel):
    """图谱关系模型"""
    source: str = Field(..., description="源实体ID")
    target: str = Field(..., description="目标实体ID")
    type: str = Field(..., description="关系类型")
    properties: Dict[str, Any] = Field(default_factory=dict, description="关系属性")

class GraphData(BaseModel):
    """图谱数据集合"""
    nodes: List[GraphEntity] = Field(default_factory=list, description="节点列表")
    edges: List[GraphRelation] = Field(default_factory=list, description="边列表")

class GraphQuery(BaseModel):
    """图谱查询请求"""
    entity_ids: Optional[List[str]] = Field(None, description="起始实体ID列表")
    keywords: Optional[List[str]] = Field(None, description="关键词列表")
    depth: int = Field(default=2, ge=1, le=5, description="遍历深度")
    relation_types: Optional[List[str]] = Field(None, description="限制关系类型")

class GraphResult(BaseModel):
    """图谱查询结果"""
    data: GraphData = Field(..., description="图数据")
    paths: Optional[List[List[str]]] = Field(None, description="发现的路径")
