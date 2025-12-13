"""
健康检查工具类

提供系统健康检查功能，包括依赖服务连接状态、资源使用情况等。
"""

import logging
import platform
import sys
from datetime import datetime
from enum import Enum
from typing import Dict, List, Optional, Tuple, Any
from dataclasses import dataclass

from app.config import get_settings

# 可选依赖处理
try:
    import psutil
    PSUTIL_AVAILABLE = True
except ImportError:
    PSUTIL_AVAILABLE = False
    psutil = None

try:
    import redis
    REDIS_AVAILABLE = True
except ImportError:
    REDIS_AVAILABLE = False
    redis = None

try:
    import requests
    REQUESTS_AVAILABLE = True
except ImportError:
    REQUESTS_AVAILABLE = False
    requests = None

try:
    from pymilvus import connections, utility
    MILVUS_AVAILABLE = True
except ImportError:
    MILVUS_AVAILABLE = False
    connections = None
    utility = None

logger = logging.getLogger(__name__)
settings = get_settings()


class HealthStatus(str, Enum):
    """健康状态枚举"""
    HEALTHY = "healthy"
    DEGRADED = "degraded"  # 部分功能不可用，但核心功能正常
    UNHEALTHY = "unhealthy"  # 核心功能不可用
    UNKNOWN = "unknown"


@dataclass
class HealthCheckResult:
    """健康检查结果"""
    status: HealthStatus
    message: str
    details: Optional[Dict[str, Any]] = None
    timestamp: datetime = None

    def __post_init__(self):
        if self.timestamp is None:
            self.timestamp = datetime.utcnow()


@dataclass
class DependencyStatus:
    """依赖服务状态"""
    name: str
    status: HealthStatus
    response_time_ms: Optional[float] = None
    error: Optional[str] = None
    details: Optional[Dict[str, Any]] = None


class HealthChecker:
    """健康检查器"""

    def __init__(self):
        """初始化健康检查器"""
        self.dependencies: List[str] = []

    def check_redis(self) -> DependencyStatus:
        """检查Redis连接"""
        if not REDIS_AVAILABLE:
            return DependencyStatus(
                name="redis",
                status=HealthStatus.UNKNOWN,
                response_time_ms=0,
                error="Redis client not installed",
                details={"note": "Install redis package to enable Redis health checks"}
            )

        start_time = datetime.utcnow()
        try:
            client = redis.Redis(
                host=settings.redis_host,
                port=settings.redis_port,
                db=settings.redis_db,
                socket_connect_timeout=2,
                socket_timeout=2
            )
            # 测试连接
            result = client.ping()
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000

            if result:
                return DependencyStatus(
                    name="redis",
                    status=HealthStatus.HEALTHY,
                    response_time_ms=response_time,
                    details={
                        "host": settings.redis_host,
                        "port": settings.redis_port,
                        "db": settings.redis_db
                    }
                )
            else:
                return DependencyStatus(
                    name="redis",
                    status=HealthStatus.UNHEALTHY,
                    response_time_ms=response_time,
                    error="Redis ping returned False"
                )

        except Exception as e:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="redis",
                status=HealthStatus.UNHEALTHY,
                response_time_ms=response_time,
                error=str(e),
                details={
                    "host": settings.redis_host,
                    "port": settings.redis_port,
                    "db": settings.redis_db
                }
            )

    def check_elasticsearch(self) -> DependencyStatus:
        """检查Elasticsearch连接"""
        start_time = datetime.utcnow()
        try:
            # 解析ES主机URL
            es_url = settings.es_url
            if not es_url.startswith(('http://', 'https://')):
                es_url = f"http://{es_url}"

            # 设置认证
            auth = None
            if settings.es_username and settings.es_password:
                auth = (settings.es_username, settings.es_password)

            # 发送健康检查请求
            response = requests.get(
                f"{es_url}/_cluster/health",
                auth=auth,
                timeout=5
            )
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000

            if response.status_code == 200:
                data = response.json()
                status = data.get('status', 'unknown')

                # 映射ES状态到健康状态
                if status == 'green':
                    health_status = HealthStatus.HEALTHY
                elif status == 'yellow':
                    health_status = HealthStatus.DEGRADED
                else:  # red
                    health_status = HealthStatus.UNHEALTHY

                return DependencyStatus(
                    name="elasticsearch",
                    status=health_status,
                    response_time_ms=response_time,
                    details=data
                )
            else:
                return DependencyStatus(
                    name="elasticsearch",
                    status=HealthStatus.UNHEALTHY,
                    response_time_ms=response_time,
                    error=f"HTTP {response.status_code}: {response.text}"
                )

        except Exception as e:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="elasticsearch",
                status=HealthStatus.UNHEALTHY,
                response_time_ms=response_time,
                error=str(e),
                details={
                    "host": settings.es_host
                }
            )

    def check_milvus(self) -> DependencyStatus:
        """检查Milvus连接"""
        if not MILVUS_AVAILABLE:
            return DependencyStatus(
                name="milvus",
                status=HealthStatus.UNKNOWN,
                response_time_ms=0,
                error="Milvus client not installed",
                details={"note": "Install pymilvus package to enable Milvus health checks"}
            )

        start_time = datetime.utcnow()
        try:
            # 连接到Milvus
            connections.connect(
                alias="default",
                host=settings.milvus_host,
                port=settings.milvus_port
            )

            # 获取服务器版本
            version = utility.get_server_version()
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000

            # 断开连接（健康检查不需要保持连接）
            connections.disconnect("default")

            return DependencyStatus(
                name="milvus",
                status=HealthStatus.HEALTHY,
                response_time_ms=response_time,
                details={
                    "host": settings.milvus_host,
                    "port": settings.milvus_port,
                    "version": version,
                    "collection": settings.milvus_collection if hasattr(settings, 'milvus_collection') else "documents"
                }
            )

        except Exception as e:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="milvus",
                status=HealthStatus.UNHEALTHY,
                response_time_ms=response_time,
                error=str(e),
                details={
                    "host": settings.milvus_host,
                    "port": settings.milvus_port
                }
            )

    def check_nebula_graph(self) -> DependencyStatus:
        """检查NebulaGraph连接（可选）"""
        start_time = datetime.utcnow()
        try:
            # 尝试导入nebula3
            from nebula3.gclient.net import ConnectionPool
            from nebula3.Config import Config

            # 创建配置
            config = Config()
            config.max_connection_pool_size = 1

            # 创建连接池
            connection_pool = ConnectionPool()
            connection_pool.init(
                [(settings.nebula_host, settings.nebula_port)],
                config
            )

            # 获取连接
            connection = connection_pool.get_connection()

            # 认证
            auth_result = connection.authenticate(
                settings.nebula_user,
                settings.nebula_password
            )

            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000

            if auth_result.error_code == 0:
                # 释放连接
                connection_pool.return_connection(connection)

                return DependencyStatus(
                    name="nebula_graph",
                    status=HealthStatus.HEALTHY,
                    response_time_ms=response_time,
                    details={
                        "host": settings.nebula_host,
                        "port": settings.nebula_port,
                        "user": settings.nebula_user,
                        "space": settings.nebula_space if hasattr(settings, 'nebula_space') else "default"
                    }
                )
            else:
                return DependencyStatus(
                    name="nebula_graph",
                    status=HealthStatus.UNHEALTHY,
                    response_time_ms=response_time,
                    error=f"Authentication failed: {auth_result.error_msg}"
                )

        except ImportError:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="nebula_graph",
                status=HealthStatus.UNKNOWN,
                response_time_ms=response_time,
                error="NebulaGraph client not installed",
                details={"note": "This is an optional dependency"}
            )
        except Exception as e:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="nebula_graph",
                status=HealthStatus.UNHEALTHY,
                response_time_ms=response_time,
                error=str(e)
            )

    def check_filesystem(self, path: str = "./data") -> DependencyStatus:
        """检查文件系统访问"""
        import os
        import shutil

        start_time = datetime.utcnow()
        try:
            # 检查路径是否存在且可访问
            if not os.path.exists(path):
                os.makedirs(path, exist_ok=True)

            # 测试写入权限
            test_file = os.path.join(path, ".healthcheck")
            with open(test_file, 'w') as f:
                f.write(str(datetime.utcnow()))

            # 测试读取权限
            with open(test_file, 'r') as f:
                _ = f.read()

            # 清理测试文件
            os.remove(test_file)

            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000

            # 获取磁盘使用情况
            disk_usage = shutil.disk_usage(path)

            return DependencyStatus(
                name="filesystem",
                status=HealthStatus.HEALTHY,
                response_time_ms=response_time,
                details={
                    "path": os.path.abspath(path),
                    "total_gb": round(disk_usage.total / (1024**3), 2),
                    "used_gb": round(disk_usage.used / (1024**3), 2),
                    "free_gb": round(disk_usage.free / (1024**3), 2),
                    "free_percent": round(disk_usage.free / disk_usage.total * 100, 2)
                }
            )

        except Exception as e:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="filesystem",
                status=HealthStatus.UNHEALTHY,
                response_time_ms=response_time,
                error=str(e),
                details={"path": path}
            )

    def check_system_resources(self) -> DependencyStatus:
        """检查系统资源使用情况"""
        start_time = datetime.utcnow()
        try:
            # CPU使用率
            cpu_percent = psutil.cpu_percent(interval=0.1)

            # 内存使用情况
            memory = psutil.virtual_memory()

            # 磁盘使用情况（根目录）
            disk = psutil.disk_usage('/')

            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000

            # 判断健康状况
            status = HealthStatus.HEALTHY
            warnings = []

            if cpu_percent > 90:
                status = HealthStatus.DEGRADED
                warnings.append(f"High CPU usage: {cpu_percent}%")

            if memory.percent > 90:
                status = HealthStatus.DEGRADED
                warnings.append(f"High memory usage: {memory.percent}%")

            if disk.percent > 90:
                status = HealthStatus.DEGRADED
                warnings.append(f"High disk usage: {disk.percent}%")

            details = {
                "cpu_percent": cpu_percent,
                "memory_total_gb": round(memory.total / (1024**3), 2),
                "memory_used_gb": round(memory.used / (1024**3), 2),
                "memory_percent": memory.percent,
                "disk_total_gb": round(disk.total / (1024**3), 2),
                "disk_used_gb": round(disk.used / (1024**3), 2),
                "disk_percent": disk.percent,
                "system": platform.system(),
                "python_version": platform.python_version(),
                "warnings": warnings if warnings else None
            }

            return DependencyStatus(
                name="system_resources",
                status=status,
                response_time_ms=response_time,
                details=details
            )

        except Exception as e:
            response_time = (datetime.utcnow() - start_time).total_seconds() * 1000
            return DependencyStatus(
                name="system_resources",
                status=HealthStatus.UNKNOWN,
                response_time_ms=response_time,
                error=str(e)
            )

    def check_all_dependencies(self) -> List[DependencyStatus]:
        """检查所有依赖服务"""
        dependencies = []

        # 检查Redis
        dependencies.append(self.check_redis())

        # 检查Elasticsearch
        dependencies.append(self.check_elasticsearch())

        # 检查Milvus
        dependencies.append(self.check_milvus())

        # 检查NebulaGraph（可选）
        dependencies.append(self.check_nebula_graph())

        # 检查文件系统
        dependencies.append(self.check_filesystem())

        # 检查系统资源
        dependencies.append(self.check_system_resources())

        return dependencies

    def liveness_check(self) -> HealthCheckResult:
        """存活检查（Liveness Probe）

        检查应用是否正在运行，不检查外部依赖。
        """
        try:
            # 简单检查应用是否响应
            return HealthCheckResult(
                status=HealthStatus.HEALTHY,
                message="Application is alive",
                details={
                    "timestamp": datetime.utcnow().isoformat(),
                    "service": settings.app_name
                }
            )
        except Exception as e:
            return HealthCheckResult(
                status=HealthStatus.UNHEALTHY,
                message=f"Application error: {str(e)}",
                details={"error": str(e)}
            )

    def readiness_check(self, require_all: bool = False) -> HealthCheckResult:
        """就绪检查（Readiness Probe）

        检查应用是否准备好处理请求，包括依赖服务检查。

        Args:
            require_all: 是否要求所有依赖都健康

        Returns:
            HealthCheckResult: 就绪检查结果
        """
        dependencies = self.check_all_dependencies()

        # 统计状态
        status_counts = {}
        for dep in dependencies:
            status_counts[dep.status] = status_counts.get(dep.status, 0) + 1

        # 判断整体状态
        if HealthStatus.UNHEALTHY in status_counts:
            overall_status = HealthStatus.UNHEALTHY
        elif HealthStatus.DEGRADED in status_counts:
            overall_status = HealthStatus.DEGRADED
        elif HealthStatus.HEALTHY in status_counts:
            overall_status = HealthStatus.HEALTHY
        else:
            overall_status = HealthStatus.UNKNOWN

        # 如果要求所有依赖都健康，但存在非健康状态
        if require_all and overall_status != HealthStatus.HEALTHY:
            overall_status = HealthStatus.UNHEALTHY

        # 构建详细信息
        dependency_details = {}
        for dep in dependencies:
            dep_details = {
                "status": dep.status,
                "response_time_ms": dep.response_time_ms
            }
            if dep.error:
                dep_details["error"] = dep.error
            if dep.details:
                dep_details.update(dep.details)

            dependency_details[dep.name] = dep_details

        # 生成消息
        if overall_status == HealthStatus.HEALTHY:
            message = "All dependencies are healthy"
        elif overall_status == HealthStatus.DEGRADED:
            unhealthy_count = status_counts.get(HealthStatus.DEGRADED, 0) + status_counts.get(HealthStatus.UNHEALTHY, 0)
            message = f"{unhealthy_count} dependencies are degraded or unhealthy"
        else:
            message = "Critical dependencies are unhealthy"

        return HealthCheckResult(
            status=overall_status,
            message=message,
            details={
                "dependencies": dependency_details,
                "status_summary": status_counts,
                "require_all": require_all
            }
        )

    def comprehensive_check(self) -> HealthCheckResult:
        """综合健康检查

        包含存活检查和就绪检查。
        """
        # 存活检查
        liveness_result = self.liveness_check()

        # 如果应用本身不健康，直接返回
        if liveness_result.status != HealthStatus.HEALTHY:
            return liveness_result

        # 就绪检查
        readiness_result = self.readiness_check()

        # 合并结果
        if readiness_result.status == HealthStatus.HEALTHY:
            overall_status = HealthStatus.HEALTHY
            message = "Application is fully operational"
        else:
            overall_status = readiness_result.status
            message = readiness_result.message

        # 合并详情
        details = {
            "liveness": {
                "status": liveness_result.status,
                "message": liveness_result.message
            },
            "readiness": {
                "status": readiness_result.status,
                "message": readiness_result.message,
                "details": readiness_result.details
            }
        }

        return HealthCheckResult(
            status=overall_status,
            message=message,
            details=details
        )


# 全局健康检查器实例
_health_checker = HealthChecker()


def get_health_checker() -> HealthChecker:
    """获取健康检查器实例"""
    return _health_checker


# 快捷函数
def check_liveness() -> HealthCheckResult:
    """存活检查（快捷函数）"""
    return _health_checker.liveness_check()


def check_readiness(require_all: bool = False) -> HealthCheckResult:
    """就绪检查（快捷函数）"""
    return _health_checker.readiness_check(require_all)


def check_health() -> HealthCheckResult:
    """综合健康检查（快捷函数）"""
    return _health_checker.comprehensive_check()