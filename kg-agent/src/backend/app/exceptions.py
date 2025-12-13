"""
Application exceptions for kg-agent.

Provides unified exception handling with consistent error response format.
"""

from typing import Optional, Dict, Any, Union
from dataclasses import dataclass
from pydantic import ValidationError as PydanticValidationError


@dataclass
class ErrorDetail:
    """错误详情"""
    code: str
    message: str
    details: Optional[Dict[str, Any]] = None
    timestamp: Optional[str] = None

    def to_dict(self) -> Dict[str, Any]:
        """转换为字典格式"""
        import datetime

        result = {
            "code": self.code,
            "message": self.message,
        }

        if self.details:
            result["details"] = self.details

        if self.timestamp is None:
            result["timestamp"] = datetime.datetime.utcnow().isoformat()
        else:
            result["timestamp"] = self.timestamp

        return result


class AppException(Exception):
    """应用基础异常

    所有应用异常的基类，提供统一的错误响应格式。
    """

    def __init__(
        self,
        code: str,
        message: str,
        details: Optional[Dict[str, Any]] = None,
        http_status: int = 500
    ):
        """
        Args:
            code: 错误代码，如 "VALIDATION_ERROR"
            message: 错误消息，用户可读
            details: 错误详情，包含具体错误信息
            http_status: HTTP状态码，默认为500
        """
        self.code = code
        self.message = message
        self.details = details or {}
        self.http_status = http_status
        super().__init__(self.message)

    def to_error_detail(self) -> ErrorDetail:
        """转换为错误详情"""
        return ErrorDetail(
            code=self.code,
            message=self.message,
            details=self.details if self.details else None
        )

    def to_response_dict(self) -> Dict[str, Any]:
        """转换为API响应格式"""
        return {
            "error": self.to_error_detail().to_dict()
        }


# ===== 验证相关异常 =====

class ValidationError(AppException):
    """验证错误"""

    def __init__(
        self,
        message: str = "请求参数验证失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            code="VALIDATION_ERROR",
            message=message,
            details=details,
            http_status=400
        )


class PydanticValidationErrorWrapper(AppException):
    """Pydantic验证错误包装器"""

    def __init__(self, pydantic_error: PydanticValidationError):
        errors = []
        for error in pydantic_error.errors():
            errors.append({
                "loc": error.get("loc", []),
                "msg": error.get("msg", ""),
                "type": error.get("type", "")
            })

        super().__init__(
            code="VALIDATION_ERROR",
            message="请求参数验证失败",
            details={"errors": errors},
            http_status=400
        )


# ===== 存储相关异常 =====

class StorageError(AppException):
    """存储错误基类"""

    def __init__(
        self,
        message: str,
        storage_type: str,
        details: Optional[Dict[str, Any]] = None
    ):
        details = details or {}
        details["storage_type"] = storage_type

        super().__init__(
            code="STORAGE_ERROR",
            message=message,
            details=details,
            http_status=503  # 服务暂时不可用
        )


class ElasticsearchError(StorageError):
    """Elasticsearch错误"""

    def __init__(
        self,
        message: str = "Elasticsearch操作失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            message=message,
            storage_type="elasticsearch",
            details=details
        )


class MilvusError(StorageError):
    """Milvus错误"""

    def __init__(
        self,
        message: str = "Milvus操作失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            message=message,
            storage_type="milvus",
            details=details
        )


class NebulaGraphError(StorageError):
    """NebulaGraph错误"""

    def __init__(
        self,
        message: str = "NebulaGraph操作失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            message=message,
            storage_type="nebula_graph",
            details=details
        )


class RedisError(StorageError):
    """Redis错误"""

    def __init__(
        self,
        message: str = "Redis操作失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            message=message,
            storage_type="redis",
            details=details
        )


# ===== 业务逻辑异常 =====

class DocumentError(AppException):
    """文档处理错误"""

    def __init__(
        self,
        message: str = "文档处理失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            code="DOCUMENT_ERROR",
            message=message,
            details=details,
            http_status=400
        )


class SearchError(AppException):
    """检索错误"""

    def __init__(
        self,
        message: str = "检索操作失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            code="SEARCH_ERROR",
            message=message,
            details=details,
            http_status=400
        )


class GraphError(AppException):
    """图谱操作错误"""

    def __init__(
        self,
        message: str = "图谱操作失败",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            code="GRAPH_ERROR",
            message=message,
            details=details,
            http_status=400
        )


# ===== 系统异常 =====

class ConfigurationError(AppException):
    """配置错误"""

    def __init__(
        self,
        message: str = "配置错误",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            code="CONFIGURATION_ERROR",
            message=message,
            details=details,
            http_status=500
        )


class ServiceUnavailableError(AppException):
    """服务不可用错误"""

    def __init__(
        self,
        message: str = "服务暂时不可用",
        details: Optional[Dict[str, Any]] = None
    ):
        super().__init__(
            code="SERVICE_UNAVAILABLE",
            message=message,
            details=details,
            http_status=503
        )


class ResourceNotFoundError(AppException):
    """资源未找到错误"""

    def __init__(
        self,
        resource_type: str,
        resource_id: Optional[str] = None,
        details: Optional[Dict[str, Any]] = None
    ):
        message = f"{resource_type}未找到"
        if resource_id:
            message = f"{resource_type} '{resource_id}' 未找到"

        details = details or {}
        details["resource_type"] = resource_type
        if resource_id:
            details["resource_id"] = resource_id

        super().__init__(
            code="RESOURCE_NOT_FOUND",
            message=message,
            details=details,
            http_status=404
        )


# ===== 工具函数 =====

def wrap_exception(exception: Exception) -> AppException:
    """包装原生异常为应用异常

    Args:
        exception: 原始异常

    Returns:
        包装后的AppException
    """
    if isinstance(exception, AppException):
        return exception

    if isinstance(exception, PydanticValidationError):
        return PydanticValidationErrorWrapper(exception)

    # 默认转换为通用应用异常
    return AppException(
        code="INTERNAL_ERROR",
        message="内部服务器错误",
        details={
            "exception_type": exception.__class__.__name__,
            "exception_message": str(exception)
        },
        http_status=500
    )


def create_validation_error(
    field: str,
    message: str,
    value: Any = None
) -> ValidationError:
    """创建字段验证错误

    Args:
        field: 字段名
        message: 错误消息
        value: 字段值（可选）

    Returns:
        ValidationError实例
    """
    details = {
        "field": field,
        "message": message
    }
    if value is not None:
        details["value"] = value

    return ValidationError(
        message=f"字段 '{field}' 验证失败: {message}",
        details=details
    )