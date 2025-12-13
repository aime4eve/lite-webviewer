from datetime import datetime
from enum import Enum
from typing import Optional, Dict, Any, List
from pydantic import BaseModel, Field

class DocumentType(str, Enum):
    PDF = "pdf"
    DOCX = "docx"
    TXT = "txt"
    MARKDOWN = "md"
    HTML = "html"

class ProcessingStatus(str, Enum):
    PENDING = "pending"
    PROCESSING = "processing"
    COMPLETED = "completed"
    FAILED = "failed"

class DocumentMetadata(BaseModel):
    """文档元数据模型"""
    title: str = Field(..., description="文档标题")
    author: Optional[str] = Field(None, description="文档作者")
    source: Optional[str] = Field(None, description="来源")
    created_at: datetime = Field(default_factory=datetime.utcnow, description="创建时间")
    file_size: int = Field(..., description="文件大小(bytes)")
    page_count: Optional[int] = Field(None, description="页数")
    extra: Dict[str, Any] = Field(default_factory=dict, description="扩展元数据")

class Chunk(BaseModel):
    """文档分块模型"""
    id: str = Field(..., description="分块唯一ID")
    doc_id: str = Field(..., description="所属文档ID")
    content: str = Field(..., description="分块文本内容")
    index: int = Field(..., description="在文档中的顺序索引")
    embedding: Optional[List[float]] = Field(None, description="向量嵌入")
    page_number: Optional[int] = Field(None, description="所在页码")
    token_count: Optional[int] = Field(None, description="Token数量")

class Document(BaseModel):
    """文档领域模型"""
    id: str = Field(..., description="文档唯一ID")
    filename: str = Field(..., description="原始文件名")
    file_path: str = Field(..., description="文件存储路径")
    type: DocumentType = Field(..., description="文档类型")
    metadata: DocumentMetadata = Field(..., description="文档元数据")
    status: ProcessingStatus = Field(default=ProcessingStatus.PENDING, description="处理状态")
    chunks: List[Chunk] = Field(default_factory=list, description="文档分块列表")
    error_message: Optional[str] = Field(None, description="错误信息")
    processed_at: Optional[datetime] = Field(None, description="处理完成时间")
