from enum import Enum
from typing import List, Optional, Dict, Any
from pydantic import BaseModel, Field
from app.models.document import DocumentMetadata

class SearchMode(str, Enum):
    FULLTEXT = "fulltext"
    VECTOR = "vector"
    GRAPH = "graph"
    HYBRID = "hybrid"

class SearchQuery(BaseModel):
    """检索请求模型"""
    query: str = Field(..., min_length=1, description="搜索关键词或问题")
    mode: SearchMode = Field(default=SearchMode.HYBRID, description="检索模式")
    top_k: int = Field(default=10, ge=1, le=100, description="返回结果数量")
    threshold: float = Field(default=0.5, ge=0.0, le=1.0, description="相关性阈值")
    filters: Optional[Dict[str, Any]] = Field(default=None, description="过滤条件")
    rerank: bool = Field(default=True, description="是否启用重排序")

class SearchResultItem(BaseModel):
    """单条检索结果"""
    id: str = Field(..., description="分块ID或文档ID")
    content: str = Field(..., description="文本内容片段")
    score: float = Field(..., description="相关性得分")
    source: str = Field(..., description="来源 (es/milvus/nebula)")
    metadata: DocumentMetadata = Field(..., description="原始文档元数据")
    highlights: Optional[List[str]] = Field(None, description="高亮片段")

class SearchResponse(BaseModel):
    """检索响应模型"""
    total: int = Field(..., description="总命中数")
    items: List[SearchResultItem] = Field(default_factory=list, description="结果列表")
    took: float = Field(..., description="耗时(ms)")
    query_expansion: Optional[List[str]] = Field(None, description="查询扩展词")
