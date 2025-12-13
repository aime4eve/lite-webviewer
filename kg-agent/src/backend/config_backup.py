"""
Configuration management for kg-agent.

Uses Pydantic Settings for type-safe configuration with environment variable support.
"""
import os
from typing import Optional, Dict, Any, List
from pydantic import Field, PostgresDsn, RedisDsn, AnyHttpUrl, validator
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    """Application settings."""

    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
        env_prefix="KG_",
        extra="ignore",
    )

    # Application
    app_name: str = "kg-agent"
    app_version: str = "0.1.0"
    debug: bool = False
    environment: str = "development"
    log_level: str = "INFO"

    # Server
    host: str = "0.0.0.0"
    port: int = 5000
    workers: int = 1
    api_prefix: str = "/api"

    # CORS
    cors_origins: List[str] = ["http://localhost:5173", "http://localhost:8080"]

    # Security
    secret_key: str = Field(default="change-me-in-production-and-make-it-long-enough", min_length=32)

    # Storage: Elasticsearch
    es_host: str = "localhost"
    es_port: int = 9200
    es_scheme: str = "http"
    es_index_prefix: str = "kg-agent"
    es_username: Optional[str] = None
    es_password: Optional[str] = None

    @property
    def es_url(self) -> str:
        """Elasticsearch connection URL."""
        auth = ""
        if self.es_username and self.es_password:
            auth = f"{self.es_username}:{self.es_password}@"
        return f"{self.es_scheme}://{auth}{self.es_host}:{self.es_port}"

    # Storage: Milvus
    milvus_host: str = "localhost"
    milvus_port: int = 19530
    milvus_collection: str = "documents"
    milvus_dimension: int = 1024  # BGE-M3 vector dimension

    # Storage: NebulaGraph
    nebula_host: str = "localhost"
    nebula_port: int = 9669
    nebula_user: str = "root"
    nebula_password: str = "nebula"
    nebula_space: str = "kg_agent"

    # Storage: Redis (for Celery and cache)
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_db: int = 0
    redis_password: Optional[str] = None

    @property
    def redis_url(self) -> str:
        """Redis connection URL for Celery."""
        auth = ""
        if self.redis_password:
            auth = f":{self.redis_password}@"
        return f"redis://{auth}{self.redis_host}:{self.redis_port}/{self.redis_db}"

    # File storage
    file_storage_type: str = "local"  # "local" or "minio"
    file_storage_path: str = "./data/files"
    minio_endpoint: Optional[str] = None
    minio_access_key: Optional[str] = None
    minio_secret_key: Optional[str] = None
    minio_bucket: Optional[str] = None

    # Model paths
    embedding_model: str = "BAAI/bge-m3"
    embedding_device: str = "cpu"  # "cpu" or "cuda"
    ner_model: str = "bert-base-chinese"  # or paddle model
    ner_device: str = "cpu"

    # Processing
    chunk_size: int = 512
    chunk_overlap: int = 50
    max_file_size_mb: int = 100

    # Celery
    celery_broker_url: Optional[str] = None
    celery_result_backend: Optional[str] = None
    celery_task_serializer: str = "json"
    celery_accept_content: List[str] = ["json"]
    celery_result_serializer: str = "json"
    celery_timezone: str = "UTC"

    # External services
    spring_boot_url: str = "http://localhost:8080"

    @validator("secret_key")
    def validate_secret_key(cls, v: str) -> str:
        """Validate secret key length in production."""
        if len(v) < 32 and os.getenv("KG_ENVIRONMENT") == "production":
            raise ValueError("SECRET_KEY must be at least 32 characters in production")
        return v

    @validator("file_storage_path")
    def validate_storage_path(cls, v: str) -> str:
        """Ensure storage path exists."""
        os.makedirs(v, exist_ok=True)
        return os.path.abspath(v)

    @validator("celery_broker_url", "celery_result_backend", pre=True)
    def set_celery_urls(cls, v: Optional[str], values: Dict[str, Any]) -> str:
        """Set Celery URLs to Redis URL if not provided."""
        if v is not None:
            return v
        # Build Redis URL from values
        redis_host = values.get("redis_host", "localhost")
        redis_port = values.get("redis_port", 6379)
        redis_db = values.get("redis_db", 0)
        redis_password = values.get("redis_password")

        auth = ""
        if redis_password:
            auth = f":{redis_password}@"
        return f"redis://{auth}{redis_host}:{redis_port}/{redis_db}"


# Global settings instance
settings = Settings()