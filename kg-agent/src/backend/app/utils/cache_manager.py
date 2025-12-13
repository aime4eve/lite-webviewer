"""
缓存管理工具类

提供多级缓存策略：内存LRU缓存 + Redis分布式缓存。
支持缓存装饰器和手动缓存操作。
"""

import json
import pickle
import hashlib
import logging
from abc import ABC, abstractmethod
from datetime import datetime, timedelta
from enum import Enum
from functools import wraps, lru_cache as lru_cache_decorator
from typing import Any, Callable, Dict, List, Optional, Tuple, TypeVar, Union
import redis

from app.config import get_settings

logger = logging.getLogger(__name__)
settings = get_settings()

# 类型变量用于泛型
T = TypeVar('T')


class CacheBackend(ABC):
    """缓存后端抽象基类"""

    @abstractmethod
    def get(self, key: str) -> Optional[Any]:
        """获取缓存值

        Args:
            key: 缓存键

        Returns:
            Optional[Any]: 缓存值，不存在返回None
        """
        pass

    @abstractmethod
    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> bool:
        """设置缓存值

        Args:
            key: 缓存键
            value: 缓存值
            ttl: 过期时间（秒），None表示永不过期

        Returns:
            bool: 是否设置成功
        """
        pass

    @abstractmethod
    def delete(self, key: str) -> bool:
        """删除缓存键

        Args:
            key: 缓存键

        Returns:
            bool: 是否删除成功
        """
        pass

    @abstractmethod
    def exists(self, key: str) -> bool:
        """检查键是否存在

        Args:
            key: 缓存键

        Returns:
            bool: 是否存在
        """
        pass

    @abstractmethod
    def clear(self) -> bool:
        """清空所有缓存

        Returns:
            bool: 是否清空成功
        """
        pass

    @abstractmethod
    def keys(self, pattern: str = "*") -> List[str]:
        """获取匹配的键列表

        Args:
            pattern: 键模式

        Returns:
            List[str]: 键列表
        """
        pass


class MemoryCacheBackend(CacheBackend):
    """内存LRU缓存后端"""

    def __init__(self, maxsize: int = 1000):
        """初始化内存缓存

        Args:
            maxsize: 最大缓存条目数
        """
        self.maxsize = maxsize
        self._cache: Dict[str, Tuple[Any, Optional[datetime]]] = {}
        self._lru: List[str] = []

    def get(self, key: str) -> Optional[Any]:
        if key not in self._cache:
            return None

        value, expire_time = self._cache[key]

        # 检查是否过期
        if expire_time and datetime.now() > expire_time:
            self.delete(key)
            return None

        # 更新LRU顺序
        if key in self._lru:
            self._lru.remove(key)
        self._lru.append(key)

        # 如果超出大小，移除最久未使用的
        if len(self._lru) > self.maxsize:
            old_key = self._lru.pop(0)
            self._cache.pop(old_key, None)

        return value

    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> bool:
        expire_time = None
        if ttl is not None:
            expire_time = datetime.now() + timedelta(seconds=ttl)

        self._cache[key] = (value, expire_time)

        # 更新LRU顺序
        if key in self._lru:
            self._lru.remove(key)
        self._lru.append(key)

        # 如果超出大小，移除最久未使用的
        if len(self._lru) > self.maxsize:
            old_key = self._lru.pop(0)
            self._cache.pop(old_key, None)

        return True

    def delete(self, key: str) -> bool:
        if key in self._cache:
            del self._cache[key]
            if key in self._lru:
                self._lru.remove(key)
            return True
        return False

    def exists(self, key: str) -> bool:
        if key not in self._cache:
            return False

        _, expire_time = self._cache[key]
        if expire_time and datetime.now() > expire_time:
            self.delete(key)
            return False

        return True

    def clear(self) -> bool:
        self._cache.clear()
        self._lru.clear()
        return True

    def keys(self, pattern: str = "*") -> List[str]:
        # 简单模式匹配（仅支持*通配符）
        if pattern == "*":
            # 检查过期时间
            valid_keys = []
            for key, (_, expire_time) in list(self._cache.items()):
                if expire_time and datetime.now() > expire_time:
                    self.delete(key)
                else:
                    valid_keys.append(key)
            return valid_keys

        # 简单前缀匹配
        if pattern.endswith("*"):
            prefix = pattern[:-1]
            return [key for key in self._cache.keys() if key.startswith(prefix)]

        return [key for key in self._cache.keys() if key == pattern]


class RedisCacheBackend(CacheBackend):
    """Redis缓存后端"""

    def __init__(self, host: str = "localhost", port: int = 6379, db: int = 0, password: Optional[str] = None):
        """初始化Redis缓存

        Args:
            host: Redis主机
            port: Redis端口
            db: Redis数据库
            password: Redis密码
        """
        self.client = redis.Redis(
            host=host,
            port=port,
            db=db,
            password=password,
            decode_responses=False,  # 不自动解码，以便存储二进制数据
            socket_connect_timeout=5,
            socket_timeout=5,
            retry_on_timeout=True
        )

    def get(self, key: str) -> Optional[Any]:
        try:
            data = self.client.get(key)
            if data is None:
                return None
            return pickle.loads(data)
        except (redis.RedisError, pickle.PickleError) as e:
            logger.error(f"Redis get error for key {key}: {e}")
            return None

    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> bool:
        try:
            data = pickle.dumps(value)
            if ttl is not None:
                result = self.client.setex(key, ttl, data)
            else:
                result = self.client.set(key, data)
            return bool(result)
        except (redis.RedisError, pickle.PickleError) as e:
            logger.error(f"Redis set error for key {key}: {e}")
            return False

    def delete(self, key: str) -> bool:
        try:
            result = self.client.delete(key)
            return bool(result)
        except redis.RedisError as e:
            logger.error(f"Redis delete error for key {key}: {e}")
            return False

    def exists(self, key: str) -> bool:
        try:
            return bool(self.client.exists(key))
        except redis.RedisError as e:
            logger.error(f"Redis exists error for key {key}: {e}")
            return False

    def clear(self) -> bool:
        try:
            self.client.flushdb()
            return True
        except redis.RedisError as e:
            logger.error(f"Redis clear error: {e}")
            return False

    def keys(self, pattern: str = "*") -> List[str]:
        try:
            return [key.decode('utf-8') if isinstance(key, bytes) else key
                    for key in self.client.keys(pattern)]
        except redis.RedisError as e:
            logger.error(f"Redis keys error for pattern {pattern}: {e}")
            return []


class MultiLevelCacheBackend(CacheBackend):
    """多级缓存后端（内存 + Redis）"""

    def __init__(
        self,
        memory_maxsize: int = 1000,
        redis_host: str = "localhost",
        redis_port: int = 6379,
        redis_db: int = 0,
        redis_password: Optional[str] = None
    ):
        """初始化多级缓存

        Args:
            memory_maxsize: 内存缓存最大大小
            redis_host: Redis主机
            redis_port: Redis端口
            redis_db: Redis数据库
            redis_password: Redis密码
        """
        self.memory_cache = MemoryCacheBackend(memory_maxsize)
        self.redis_cache = RedisCacheBackend(redis_host, redis_port, redis_db, redis_password)

    def get(self, key: str) -> Optional[Any]:
        # 首先检查内存缓存
        value = self.memory_cache.get(key)
        if value is not None:
            logger.debug(f"Cache hit (memory): {key}")
            return value

        # 内存未命中，检查Redis
        value = self.redis_cache.get(key)
        if value is not None:
            logger.debug(f"Cache hit (redis): {key}")
            # 写回到内存缓存（带较短TTL）
            self.memory_cache.set(key, value, ttl=60)  # 内存缓存1分钟
            return value

        logger.debug(f"Cache miss: {key}")
        return None

    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> bool:
        # 设置Redis缓存
        redis_success = self.redis_cache.set(key, value, ttl)

        # 设置内存缓存（使用较短的TTL或默认TTL）
        memory_ttl = min(ttl, 300) if ttl is not None else 300  # 内存最多缓存5分钟
        memory_success = self.memory_cache.set(key, value, memory_ttl)

        logger.debug(f"Cache set: {key}, redis: {redis_success}, memory: {memory_success}")
        return redis_success and memory_success

    def delete(self, key: str) -> bool:
        memory_success = self.memory_cache.delete(key)
        redis_success = self.redis_cache.delete(key)

        logger.debug(f"Cache delete: {key}, redis: {redis_success}, memory: {memory_success}")
        return memory_success and redis_success

    def exists(self, key: str) -> bool:
        if self.memory_cache.exists(key):
            return True
        return self.redis_cache.exists(key)

    def clear(self) -> bool:
        memory_success = self.memory_cache.clear()
        redis_success = self.redis_cache.clear()
        return memory_success and redis_success

    def keys(self, pattern: str = "*") -> List[str]:
        # 合并内存和Redis的键（去重）
        memory_keys = set(self.memory_cache.keys(pattern))
        redis_keys = set(self.redis_cache.keys(pattern))
        return list(memory_keys.union(redis_keys))


class CacheManager:
    """缓存管理器"""

    _instance = None
    _cache: Optional[CacheBackend] = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super().__new__(cls)
        return cls._instance

    def __init__(self):
        if self._cache is None:
            self._cache = self._create_cache()

    def _create_cache(self) -> CacheBackend:
        """根据配置创建缓存后端"""
        # TODO: 从配置读取缓存类型
        cache_type = "multilevel"  # 默认使用多级缓存

        if cache_type == "memory":
            return MemoryCacheBackend(maxsize=1000)
        elif cache_type == "redis":
            return RedisCacheBackend(
                host=settings.REDIS_HOST,
                port=settings.REDIS_PORT,
                db=settings.REDIS_DB,
                password=settings.REDIS_PASSWORD
            )
        elif cache_type == "multilevel":
            return MultiLevelCacheBackend(
                memory_maxsize=1000,
                redis_host=settings.REDIS_HOST,
                redis_port=settings.REDIS_PORT,
                redis_db=settings.REDIS_DB,
                redis_password=settings.REDIS_PASSWORD
            )
        else:
            raise ValueError(f"Unknown cache type: {cache_type}")

    def get_cache(self) -> CacheBackend:
        """获取缓存实例"""
        return self._cache


# 全局缓存管理器实例
cache_manager = CacheManager()


def get_cache() -> CacheBackend:
    """获取缓存实例（快捷函数）"""
    return cache_manager.get_cache()


def generate_cache_key(prefix: str, *args, **kwargs) -> str:
    """生成缓存键

    Args:
        prefix: 键前缀
        *args: 位置参数
        **kwargs: 关键字参数

    Returns:
        str: 缓存键
    """
    # 将参数转换为字符串表示
    args_str = json.dumps(args, sort_keys=True, default=str)
    kwargs_str = json.dumps(kwargs, sort_keys=True, default=str)

    # 计算哈希
    key_data = f"{prefix}:{args_str}:{kwargs_str}"
    key_hash = hashlib.md5(key_data.encode('utf-8')).hexdigest()

    return f"{prefix}:{key_hash}"


def cached(
    ttl: Optional[int] = None,
    key_prefix: Optional[str] = None,
    cache_backend: Optional[CacheBackend] = None
):
    """缓存装饰器

    Args:
        ttl: 缓存过期时间（秒）
        key_prefix: 键前缀，默认为函数名
        cache_backend: 缓存后端，默认为全局缓存

    Returns:
        Callable: 装饰器函数
    """
    def decorator(func: Callable) -> Callable:
        @wraps(func)
        def wrapper(*args, **kwargs):
            # 获取缓存实例
            cache = cache_backend or get_cache()

            # 生成缓存键
            prefix = key_prefix or f"{func.__module__}.{func.__name__}"
            cache_key = generate_cache_key(prefix, *args, **kwargs)

            # 尝试从缓存获取
            cached_result = cache.get(cache_key)
            if cached_result is not None:
                logger.debug(f"Cache hit for {cache_key}")
                return cached_result

            # 缓存未命中，执行函数
            logger.debug(f"Cache miss for {cache_key}")
            result = func(*args, **kwargs)

            # 缓存结果
            cache.set(cache_key, result, ttl)

            return result

        return wrapper
    return decorator


def async_cached(
    ttl: Optional[int] = None,
    key_prefix: Optional[str] = None,
    cache_backend: Optional[CacheBackend] = None
):
    """异步函数缓存装饰器

    Args:
        ttl: 缓存过期时间（秒）
        key_prefix: 键前缀，默认为函数名
        cache_backend: 缓存后端，默认为全局缓存

    Returns:
        Callable: 装饰器函数
    """
    def decorator(func: Callable) -> Callable:
        @wraps(func)
        async def wrapper(*args, **kwargs):
            # 获取缓存实例
            cache = cache_backend or get_cache()

            # 生成缓存键
            prefix = key_prefix or f"{func.__module__}.{func.__name__}"
            cache_key = generate_cache_key(prefix, *args, **kwargs)

            # 尝试从缓存获取
            cached_result = cache.get(cache_key)
            if cached_result is not None:
                logger.debug(f"Cache hit for {cache_key}")
                return cached_result

            # 缓存未命中，执行异步函数
            logger.debug(f"Cache miss for {cache_key}")
            result = await func(*args, **kwargs)

            # 缓存结果
            cache.set(cache_key, result, ttl)

            return result

        return wrapper
    return decorator


class CacheNamespace:
    """缓存命名空间，用于组织相关缓存键"""

    def __init__(self, prefix: str, cache_backend: Optional[CacheBackend] = None):
        """初始化缓存命名空间

        Args:
            prefix: 命名空间前缀
            cache_backend: 缓存后端，默认为全局缓存
        """
        self.prefix = prefix
        self.cache = cache_backend or get_cache()

    def get(self, key: str) -> Optional[Any]:
        """获取缓存值"""
        full_key = f"{self.prefix}:{key}"
        return self.cache.get(full_key)

    def set(self, key: str, value: Any, ttl: Optional[int] = None) -> bool:
        """设置缓存值"""
        full_key = f"{self.prefix}:{key}"
        return self.cache.set(full_key, value, ttl)

    def delete(self, key: str) -> bool:
        """删除缓存键"""
        full_key = f"{self.prefix}:{key}"
        return self.cache.delete(full_key)

    def exists(self, key: str) -> bool:
        """检查键是否存在"""
        full_key = f"{self.prefix}:{key}"
        return self.cache.exists(full_key)

    def clear_namespace(self) -> bool:
        """清空命名空间下的所有键"""
        keys = self.cache.keys(f"{self.prefix}:*")
        success = True
        for key in keys:
            if not self.cache.delete(key):
                success = False
        return success


# 预定义的缓存命名空间
DOCUMENT_CACHE = CacheNamespace("document")
SEARCH_CACHE = CacheNamespace("search")
GRAPH_CACHE = CacheNamespace("graph")
USER_CACHE = CacheNamespace("user")