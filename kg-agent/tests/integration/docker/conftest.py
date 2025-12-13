import pytest
import os
import time
from typing import Generator
import redis
from elasticsearch import Elasticsearch
from pymilvus import connections, utility
from nebula3.gclient.net import ConnectionPool
from nebula3.Config import Config

from app.config import get_settings

settings = get_settings()

@pytest.fixture(scope="session")
def redis_client():
    """Create a Redis client for testing."""
    client = redis.Redis(
        host=settings.REDIS_HOST,
        port=settings.REDIS_PORT,
        db=settings.REDIS_DB,
        password=settings.REDIS_PASSWORD,
        decode_responses=True
    )
    yield client
    client.close()

@pytest.fixture(scope="session")
def es_client():
    """Create an Elasticsearch client for testing."""
    client = Elasticsearch(
        hosts=[f"{settings.ES_HOST}"],
        basic_auth=(settings.ES_USER, settings.ES_PASSWORD) if settings.ES_USER else None
    )
    yield client
    client.close()

@pytest.fixture(scope="session")
def milvus_connection():
    """Create a Milvus connection for testing."""
    connections.connect(
        alias="default",
        host=settings.MILVUS_HOST,
        port=settings.MILVUS_PORT
    )
    yield
    connections.disconnect("default")

@pytest.fixture(scope="session")
def nebula_pool():
    """Create a NebulaGraph connection pool for testing."""
    config = Config()
    config.max_connection_pool_size = 10
    pool = ConnectionPool()
    
    # Wait for Nebula to be ready
    max_retries = 5
    for i in range(max_retries):
        try:
            if pool.init([(settings.NEBULA_HOST, settings.NEBULA_PORT)], config):
                break
        except Exception:
            if i == max_retries - 1:
                raise
            time.sleep(2)
            
    yield pool
    pool.close()

@pytest.fixture(scope="session")
def docker_services_ready(redis_client, es_client, milvus_connection, nebula_pool):
    """Wait for all Docker services to be ready."""
    # This fixture implicitly checks connections via the client fixtures
    return True
