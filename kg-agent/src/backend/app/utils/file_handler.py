"""
文件处理工具类

提供统一的文件存储接口，支持本地文件系统和MinIO/S3存储。
基于配置自动选择存储后端。
"""

import os
import shutil
import hashlib
import mimetypes
from abc import ABC, abstractmethod
from datetime import datetime
from pathlib import Path
from typing import BinaryIO, Dict, List, Optional, Tuple, Union
from urllib.parse import urljoin

from app.config import get_settings

settings = get_settings()


class FileInfo:
    """文件信息类"""

    def __init__(
        self,
        name: str,
        path: str,
        size: int,
        modified_at: datetime,
        content_type: Optional[str] = None,
        etag: Optional[str] = None,
        metadata: Optional[Dict[str, str]] = None
    ):
        self.name = name
        self.path = path
        self.size = size
        self.modified_at = modified_at
        self.content_type = content_type
        self.etag = etag
        self.metadata = metadata or {}


class StorageBackend(ABC):
    """存储后端抽象基类"""

    @abstractmethod
    def upload(self, file_obj: BinaryIO, file_path: str, content_type: Optional[str] = None) -> FileInfo:
        """上传文件

        Args:
            file_obj: 文件对象（二进制模式）
            file_path: 文件存储路径
            content_type: 文件内容类型

        Returns:
            FileInfo: 文件信息
        """
        pass

    @abstractmethod
    def download(self, file_path: str) -> Tuple[BinaryIO, FileInfo]:
        """下载文件

        Args:
            file_path: 文件路径

        Returns:
            Tuple[文件对象, 文件信息]
        """
        pass

    @abstractmethod
    def delete(self, file_path: str) -> bool:
        """删除文件

        Args:
            file_path: 文件路径

        Returns:
            bool: 是否删除成功
        """
        pass

    @abstractmethod
    def exists(self, file_path: str) -> bool:
        """检查文件是否存在

        Args:
            file_path: 文件路径

        Returns:
            bool: 是否存在
        """
        pass

    @abstractmethod
    def list_files(self, prefix: str = "") -> List[FileInfo]:
        """列出文件

        Args:
            prefix: 路径前缀

        Returns:
            List[FileInfo]: 文件列表
        """
        pass

    @abstractmethod
    def get_info(self, file_path: str) -> Optional[FileInfo]:
        """获取文件信息

        Args:
            file_path: 文件路径

        Returns:
            Optional[FileInfo]: 文件信息，不存在则返回None
        """
        pass


class LocalFileStorage(StorageBackend):
    """本地文件系统存储"""

    def __init__(self, base_path: str = "./data/files"):
        """初始化本地文件存储

        Args:
            base_path: 基础存储路径
        """
        self.base_path = Path(base_path).resolve()
        self.base_path.mkdir(parents=True, exist_ok=True)

    def _get_full_path(self, file_path: str) -> Path:
        """获取完整文件路径

        Args:
            file_path: 相对路径

        Returns:
            Path: 完整路径
        """
        # 防止路径遍历攻击
        full_path = (self.base_path / file_path).resolve()
        if not str(full_path).startswith(str(self.base_path)):
            raise ValueError(f"Invalid file path: {file_path}")
        return full_path

    def upload(self, file_obj: BinaryIO, file_path: str, content_type: Optional[str] = None) -> FileInfo:
        full_path = self._get_full_path(file_path)
        full_path.parent.mkdir(parents=True, exist_ok=True)

        # 计算文件哈希
        hasher = hashlib.md5()
        file_size = 0

        with open(full_path, 'wb') as f:
            while chunk := file_obj.read(8192):
                f.write(chunk)
                hasher.update(chunk)
                file_size += len(chunk)

        # 重置文件指针以便后续使用
        file_obj.seek(0)

        etag = hasher.hexdigest()
        modified_at = datetime.fromtimestamp(full_path.stat().st_mtime)

        # 猜测内容类型
        if content_type is None:
            content_type, _ = mimetypes.guess_type(file_path)

        return FileInfo(
            name=full_path.name,
            path=file_path,
            size=file_size,
            modified_at=modified_at,
            content_type=content_type,
            etag=etag
        )

    def download(self, file_path: str) -> Tuple[BinaryIO, FileInfo]:
        full_path = self._get_full_path(file_path)

        if not full_path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")

        file_obj = open(full_path, 'rb')
        file_info = self.get_info(file_path)

        if file_info is None:
            file_obj.close()
            raise FileNotFoundError(f"File info not found: {file_path}")

        return file_obj, file_info

    def delete(self, file_path: str) -> bool:
        full_path = self._get_full_path(file_path)

        if not full_path.exists():
            return False

        try:
            if full_path.is_file():
                full_path.unlink()
            else:
                shutil.rmtree(full_path)
            return True
        except Exception:
            return False

    def exists(self, file_path: str) -> bool:
        full_path = self._get_full_path(file_path)
        return full_path.exists()

    def list_files(self, prefix: str = "") -> List[FileInfo]:
        result = []
        search_path = self._get_full_path(prefix)

        if not search_path.exists():
            return result

        for item in search_path.rglob("*"):
            if item.is_file():
                rel_path = str(item.relative_to(self.base_path))
                file_info = self.get_info(rel_path)
                if file_info:
                    result.append(file_info)

        return result

    def get_info(self, file_path: str) -> Optional[FileInfo]:
        full_path = self._get_full_path(file_path)

        if not full_path.exists() or not full_path.is_file():
            return None

        stat = full_path.stat()

        # 计算文件哈希
        hasher = hashlib.md5()
        try:
            with open(full_path, 'rb') as f:
                while chunk := f.read(8192):
                    hasher.update(chunk)
        except Exception:
            etag = None
        else:
            etag = hasher.hexdigest()

        content_type, _ = mimetypes.guess_type(file_path)

        return FileInfo(
            name=full_path.name,
            path=file_path,
            size=stat.st_size,
            modified_at=datetime.fromtimestamp(stat.st_mtime),
            content_type=content_type,
            etag=etag
        )


class MinioStorage(StorageBackend):
    """MinIO/S3对象存储（占位实现）"""

    def __init__(self, endpoint: str, access_key: str, secret_key: str, bucket: str):
        """初始化MinIO存储

        Args:
            endpoint: MinIO端点
            access_key: 访问密钥
            secret_key: 秘密密钥
            bucket: 存储桶名称
        """
        self.endpoint = endpoint
        self.access_key = access_key
        self.secret_key = secret_key
        self.bucket = bucket
        # TODO: 初始化MinIO客户端

    def upload(self, file_obj: BinaryIO, file_path: str, content_type: Optional[str] = None) -> FileInfo:
        # TODO: 实现MinIO上传
        raise NotImplementedError("MinIO storage not implemented yet")

    def download(self, file_path: str) -> Tuple[BinaryIO, FileInfo]:
        # TODO: 实现MinIO下载
        raise NotImplementedError("MinIO storage not implemented yet")

    def delete(self, file_path: str) -> bool:
        # TODO: 实现MinIO删除
        raise NotImplementedError("MinIO storage not implemented yet")

    def exists(self, file_path: str) -> bool:
        # TODO: 实现MinIO存在检查
        raise NotImplementedError("MinIO storage not implemented yet")

    def list_files(self, prefix: str = "") -> List[FileInfo]:
        # TODO: 实现MinIO列表
        raise NotImplementedError("MinIO storage not implemented yet")

    def get_info(self, file_path: str) -> Optional[FileInfo]:
        # TODO: 实现MinIO信息获取
        raise NotImplementedError("MinIO storage not implemented yet")


class FileStorageManager:
    """文件存储管理器（工厂模式）"""

    _instance = None
    _storage: Optional[StorageBackend] = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        if self._storage is None:
            self._storage = self._create_storage()

    def _create_storage(self) -> StorageBackend:
        """根据配置创建存储后端"""
        # 从配置获取存储类型，默认为本地存储
        # TODO: 从配置读取存储类型和参数
        storage_type = "local"  # 默认本地存储

        if storage_type == "local":
            # TODO: 从配置读取基础路径
            base_path = "./data/files"
            return LocalFileStorage(base_path)
        elif storage_type == "minio":
            # TODO: 从配置读取MinIO参数
            endpoint = settings.minio_endpoint if hasattr(settings, 'minio_endpoint') else ""
            access_key = settings.minio_access_key if hasattr(settings, 'minio_access_key') else ""
            secret_key = settings.minio_secret_key if hasattr(settings, 'minio_secret_key') else ""
            bucket = settings.minio_bucket if hasattr(settings, 'minio_bucket') else ""

            if not all([endpoint, access_key, secret_key, bucket]):
                raise ValueError("MinIO configuration incomplete")

            return MinioStorage(endpoint, access_key, secret_key, bucket)
        else:
            raise ValueError(f"Unknown storage type: {storage_type}")

    def get_storage(self) -> StorageBackend:
        """获取存储实例"""
        return self._storage


# 全局存储管理器实例
storage_manager = FileStorageManager()


def get_file_storage() -> StorageBackend:
    """获取文件存储实例（快捷函数）"""
    return storage_manager.get_storage()


def calculate_file_hash(file_path: Union[str, Path], algorithm: str = "md5") -> str:
    """计算文件哈希值

    Args:
        file_path: 文件路径
        algorithm: 哈希算法（md5, sha1, sha256）

    Returns:
        str: 哈希值
    """
    if algorithm not in ["md5", "sha1", "sha256"]:
        raise ValueError(f"Unsupported algorithm: {algorithm}")

    hasher = getattr(hashlib, algorithm)()

    with open(file_path, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)

    return hasher.hexdigest()


def safe_filename(filename: str) -> str:
    """生成安全的文件名

    Args:
        filename: 原始文件名

    Returns:
        str: 安全文件名
    """
    # 移除路径分隔符和特殊字符
    filename = os.path.basename(filename)
    # 替换可能不安全的字符
    filename = "".join(c if c.isalnum() or c in "._- " else "_" for c in filename)
    # 限制长度
    if len(filename) > 255:
        name, ext = os.path.splitext(filename)
        filename = name[:255 - len(ext)] + ext

    return filename