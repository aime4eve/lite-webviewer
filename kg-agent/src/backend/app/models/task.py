from enum import Enum
from typing import Optional, Any, Dict
from datetime import datetime
from pydantic import BaseModel, Field

class TaskStatus(str, Enum):
    PENDING = "pending"
    STARTED = "started"
    SUCCESS = "success"
    FAILURE = "failure"
    RETRY = "retry"
    REVOKED = "revoked"

class TaskResult(BaseModel):
    """任务结果模型"""
    task_id: str = Field(..., description="任务ID")
    status: TaskStatus = Field(..., description="当前状态")
    result: Any = Field(None, description="任务返回值")
    error: Optional[str] = Field(None, description="错误信息")
    traceback: Optional[str] = Field(None, description="错误堆栈")
    created_at: datetime = Field(..., description="创建时间")
    completed_at: Optional[datetime] = Field(None, description="完成时间")
    meta: Dict[str, Any] = Field(default_factory=dict, description="任务元数据(进度等)")
