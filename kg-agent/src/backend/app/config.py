import os
from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache
from typing import Optional

class Settings(BaseSettings):
    # App Config
    APP_NAME: str = "kg-agent"
    APP_ENV: str = "development"
    DEBUG: bool = True
    API_V1_STR: str = "/api"
    SECRET_KEY: str = "insecure-dev-key-please-change"
    
    # Redis Config
    REDIS_HOST: str = "localhost"
    REDIS_PORT: int = 6379
    REDIS_DB: int = 0
    REDIS_PASSWORD: Optional[str] = None
    
    # Elasticsearch Config
    ES_HOST: str = "http://localhost:9200"
    ES_USER: Optional[str] = None
    ES_PASSWORD: Optional[str] = None
    ES_INDEX_PREFIX: str = "kg"
    
    # Milvus Config
    MILVUS_HOST: str = "localhost"
    MILVUS_PORT: int = 19530
    MILVUS_COLLECTION: str = "kg_documents"
    MILVUS_DIMENSION: int = 768  # Depends on embedding model
    
    # NebulaGraph Config
    NEBULA_HOST: str = "localhost"
    NEBULA_PORT: int = 9669
    NEBULA_USER: str = "root"
    NEBULA_PASSWORD: str = "nebula"
    NEBULA_SPACE: str = "kg_agent_space"
    
    # Celery Config
    CELERY_BROKER_URL: str = "redis://localhost:6379/0"
    CELERY_RESULT_BACKEND: str = "redis://localhost:6379/0"
    
    # File Storage Config
    STORAGE_TYPE: str = "local"  # local or minio
    UPLOAD_FOLDER: str = "./data/files"
    MAX_CONTENT_LENGTH: int = 16 * 1024 * 1024  # 16MB
    
    # MinIO Config (Optional)
    MINIO_ENDPOINT: Optional[str] = None
    MINIO_ACCESS_KEY: Optional[str] = None
    MINIO_SECRET_KEY: Optional[str] = None
    MINIO_BUCKET: str = "kg-agent"
    
    # Model Config
    EMBEDDING_MODEL_PATH: str = "all-MiniLM-L6-v2"  # or local path

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=True, extra="ignore")

@lru_cache
def get_settings() -> Settings:
    return Settings()
