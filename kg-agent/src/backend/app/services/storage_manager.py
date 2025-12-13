"""
Storage manager for kg-agent.

Provides client connections to various storage backends:
- Elasticsearch (full-text search)
- Milvus (vector search)
- NebulaGraph (knowledge graph)
- Redis (cache and Celery broker)
"""

import logging
from typing import Optional, Dict, Any
from functools import lru_cache

from app.config import get_settings

# Optional imports with graceful fallback
try:
    from elasticsearch import Elasticsearch
    from elasticsearch.exceptions import ConnectionError as ESConnectionError
    ELASTICSEARCH_AVAILABLE = True
except ImportError:
    ELASTICSEARCH_AVAILABLE = False
    Elasticsearch = None
    ESConnectionError = None

try:
    from pymilvus import connections, utility, Collection, FieldSchema, CollectionSchema, DataType
    MILVUS_AVAILABLE = True
except ImportError:
    MILVUS_AVAILABLE = False
    connections = None
    utility = None
    Collection = None
    FieldSchema = None
    CollectionSchema = None
    DataType = None

try:
    from nebula3.gclient.net import ConnectionPool
    from nebula3.Config import Config
    NEBULA_GRAPH_AVAILABLE = True
except ImportError:
    NEBULA_GRAPH_AVAILABLE = False
    ConnectionPool = None
    Config = None

try:
    import redis
    REDIS_AVAILABLE = True
except ImportError:
    REDIS_AVAILABLE = False
    redis = None

logger = logging.getLogger(__name__)
settings = get_settings()


class StorageManager:
    """Manager for storage backend connections."""

    def __init__(self):
        """Initialize storage manager."""
        self._es_client: Optional[Elasticsearch] = None
        self._milvus_connected: bool = False
        self._nebula_pool: Optional[ConnectionPool] = None
        self._redis_client: Optional[redis.Redis] = None

    # ===== Elasticsearch =====

    @property
    def es_client(self) -> Optional[Elasticsearch]:
        """Get Elasticsearch client instance."""
        if not ELASTICSEARCH_AVAILABLE:
            logger.warning("Elasticsearch client not available. Install with 'pip install elasticsearch'")
            return None

        if self._es_client is None:
            try:
                # Build connection parameters
                es_params = {
                    "hosts": [settings.es_url],
                    "request_timeout": 30,
                }

                # Add authentication if provided
                if settings.es_username and settings.es_password:
                    es_params["basic_auth"] = (settings.es_username, settings.es_password)

                self._es_client = Elasticsearch(**es_params)

                # Test connection
                if self._es_client.ping():
                    logger.info(f"Elasticsearch connected to {settings.es_url}")
                else:
                    logger.error(f"Elasticsearch ping failed for {settings.es_url}")
                    self._es_client = None

            except Exception as e:
                logger.error(f"Failed to connect to Elasticsearch: {e}")
                self._es_client = None

        return self._es_client

    def es_health(self) -> Dict[str, Any]:
        """Check Elasticsearch health."""
        client = self.es_client
        if not client:
            return {"status": "unavailable", "error": "Elasticsearch client not initialized"}

        try:
            health_info = client.cluster.health()
            return {
                "status": health_info.get("status", "unknown"),
                "cluster_name": health_info.get("cluster_name"),
                "number_of_nodes": health_info.get("number_of_nodes"),
                "active_shards": health_info.get("active_shards"),
                "relocating_shards": health_info.get("relocating_shards"),
                "initializing_shards": health_info.get("initializing_shards"),
                "unassigned_shards": health_info.get("unassigned_shards"),
                "timed_out": health_info.get("timed_out"),
            }
        except Exception as e:
            return {"status": "error", "error": str(e)}

    def create_es_index(self, index_name: str, mappings: Optional[Dict] = None) -> bool:
        """Create Elasticsearch index if it doesn't exist."""
        client = self.es_client
        if not client:
            logger.error("Cannot create index: Elasticsearch client not available")
            return False

        try:
            # Use default mappings if not provided
            if mappings is None:
                mappings = {
                    "mappings": {
                        "properties": {
                            "content": {"type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart"},
                            "title": {"type": "text", "analyzer": "ik_max_word", "search_analyzer": "ik_smart"},
                            "metadata": {"type": "object"},
                            "embedding": {"type": "dense_vector", "dims": settings.milvus_dimension},
                            "created_at": {"type": "date"},
                            "updated_at": {"type": "date"},
                        }
                    },
                    "settings": {
                        "number_of_shards": 1,
                        "number_of_replicas": 0,
                    }
                }

            if not client.indices.exists(index=index_name):
                client.indices.create(index=index_name, body=mappings)
                logger.info(f"Created Elasticsearch index: {index_name}")
            else:
                logger.info(f"Elasticsearch index already exists: {index_name}")

            return True

        except Exception as e:
            logger.error(f"Failed to create Elasticsearch index {index_name}: {e}")
            return False

    # ===== Milvus =====

    def connect_milvus(self) -> bool:
        """Connect to Milvus server."""
        if not MILVUS_AVAILABLE:
            logger.warning("Milvus client not available. Install with 'pip install pymilvus'")
            return False

        if self._milvus_connected:
            return True

        try:
            connections.connect(
                alias="default",
                host=settings.milvus_host,
                port=settings.milvus_port
            )

            # Test connection
            if utility.has_collection(settings.milvus_collection):
                logger.info(f"Connected to Milvus, collection '{settings.milvus_collection}' exists")
            else:
                logger.info(f"Connected to Milvus, collection '{settings.milvus_collection}' does not exist")

            self._milvus_connected = True
            logger.info(f"Milvus connected to {settings.milvus_host}:{settings.milvus_port}")
            return True

        except Exception as e:
            logger.error(f"Failed to connect to Milvus: {e}")
            return False

    def create_milvus_collection(self) -> bool:
        """Create Milvus collection if it doesn't exist."""
        if not self.connect_milvus():
            return False

        try:
            if utility.has_collection(settings.milvus_collection):
                logger.info(f"Milvus collection '{settings.milvus_collection}' already exists")
                return True

            # Define schema
            fields = [
                FieldSchema(name="id", dtype=DataType.VARCHAR, is_primary=True, max_length=64),
                FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=settings.milvus_dimension),
                FieldSchema(name="doc_id", dtype=DataType.VARCHAR, max_length=64),
                FieldSchema(name="chunk_index", dtype=DataType.INT64),
                FieldSchema(name="content", dtype=DataType.VARCHAR, max_length=65535),
            ]

            schema = CollectionSchema(fields, description="Document chunks with embeddings")
            collection = Collection(name=settings.milvus_collection, schema=schema)

            # Create index for vector search
            index_params = {
                "metric_type": "COSINE",
                "index_type": "IVF_FLAT",
                "params": {"nlist": 128}
            }
            collection.create_index(field_name="embedding", index_params=index_params)

            logger.info(f"Created Milvus collection: {settings.milvus_collection}")
            return True

        except Exception as e:
            logger.error(f"Failed to create Milvus collection: {e}")
            return False

    # ===== NebulaGraph =====

    @property
    def nebula_pool(self) -> Optional[ConnectionPool]:
        """Get NebulaGraph connection pool."""
        if not NEBULA_GRAPH_AVAILABLE:
            logger.warning("NebulaGraph client not available. Install with 'pip install nebula3-python'")
            return None

        if self._nebula_pool is None:
            try:
                config = Config()
                config.max_connection_pool_size = 10

                self._nebula_pool = ConnectionPool()
                self._nebula_pool.init(
                    [(settings.nebula_host, settings.nebula_port)],
                    config
                )

                # Test connection
                conn = self._nebula_pool.get_connection()
                auth_result = conn.authenticate(settings.nebula_user, settings.nebula_password)
                self._nebula_pool.return_connection(conn)

                if auth_result.error_code == 0:
                    logger.info(f"NebulaGraph connected to {settings.nebula_host}:{settings.nebula_port}")
                else:
                    logger.error(f"NebulaGraph authentication failed: {auth_result.error_msg}")
                    self._nebula_pool = None

            except Exception as e:
                logger.error(f"Failed to connect to NebulaGraph: {e}")
                self._nebula_pool = None

        return self._nebula_pool

    def create_nebula_space(self) -> bool:
        """Create NebulaGraph space if it doesn't exist."""
        pool = self.nebula_pool
        if not pool:
            return False

        try:
            conn = pool.get_connection()

            # Check if space exists
            check_space_query = f"USE {settings.nebula_space}"
            result = conn.execute(check_space_query)

            if result.error_code != 0:
                # Space doesn't exist, create it
                create_space_query = f"""
                CREATE SPACE IF NOT EXISTS {settings.nebula_space} (
                    vid_type = FIXED_STRING(64),
                    partition_num = 10,
                    replica_factor = 1
                )
                """
                result = conn.execute(create_space_query)

                if result.error_code == 0:
                    logger.info(f"Created NebulaGraph space: {settings.nebula_space}")

                    # Wait for space to be ready
                    conn.execute(f"USE {settings.nebula_space}")

                    # Create tag schemas
                    tag_queries = [
                        """
                        CREATE TAG IF NOT EXISTS document(
                            title string,
                            type string,
                            created_at timestamp,
                            updated_at timestamp
                        )
                        """,
                        """
                        CREATE TAG IF NOT EXISTS entity(
                            name string,
                            type string,
                            confidence float
                        )
                        """,
                        """
                        CREATE TAG IF NOT EXISTS concept(
                            name string,
                            description string
                        )
                        """
                    ]

                    # Create edge schemas
                    edge_queries = [
                        """
                        CREATE EDGE IF NOT EXISTS contains(
                            chunk_index int,
                            weight float DEFAULT 1.0
                        )
                        """,
                        """
                        CREATE EDGE IF NOT EXISTS mentions(
                            confidence float,
                            context string
                        )
                        """,
                        """
                        CREATE EDGE IF NOT EXISTS related_to(
                            relationship string,
                            confidence float
                        )
                        """
                    ]

                    for query in tag_queries + edge_queries:
                        conn.execute(query)

                    logger.info(f"Created schemas in NebulaGraph space: {settings.nebula_space}")
                else:
                    logger.error(f"Failed to create NebulaGraph space: {result.error_msg}")
                    pool.return_connection(conn)
                    return False

            pool.return_connection(conn)
            return True

        except Exception as e:
            logger.error(f"Failed to create NebulaGraph space: {e}")
            return False

    # ===== Redis =====

    @property
    def redis_client(self) -> Optional[redis.Redis]:
        """Get Redis client instance."""
        if not REDIS_AVAILABLE:
            logger.warning("Redis client not available. Install with 'pip install redis'")
            return None

        if self._redis_client is None:
            try:
                self._redis_client = redis.Redis(
                    host=settings.redis_host,
                    port=settings.redis_port,
                    db=settings.redis_db,
                    password=settings.redis_password,
                    socket_connect_timeout=5,
                    socket_timeout=5,
                    decode_responses=True
                )

                # Test connection
                if self._redis_client.ping():
                    logger.info(f"Redis connected to {settings.redis_host}:{settings.redis_port}")
                else:
                    logger.error(f"Redis ping failed for {settings.redis_host}:{settings.redis_port}")
                    self._redis_client = None

            except Exception as e:
                logger.error(f"Failed to connect to Redis: {e}")
                self._redis_client = None

        return self._redis_client

    # ===== Health checks =====

    def check_all_connections(self) -> Dict[str, Dict[str, Any]]:
        """Check all storage connections."""
        results = {}

        # Elasticsearch
        es_status = {"available": ELASTICSEARCH_AVAILABLE}
        if ELASTICSEARCH_AVAILABLE and self.es_client:
            es_status["connected"] = True
            es_status["health"] = self.es_health()
        else:
            es_status["connected"] = False
        results["elasticsearch"] = es_status

        # Milvus
        milvus_status = {"available": MILVUS_AVAILABLE}
        if MILVUS_AVAILABLE:
            milvus_status["connected"] = self._milvus_connected
        results["milvus"] = milvus_status

        # NebulaGraph
        nebula_status = {"available": NEBULA_GRAPH_AVAILABLE}
        if NEBULA_GRAPH_AVAILABLE:
            nebula_status["connected"] = self.nebula_pool is not None
        results["nebula_graph"] = nebula_status

        # Redis
        redis_status = {"available": REDIS_AVAILABLE}
        if REDIS_AVAILABLE and self.redis_client:
            redis_status["connected"] = True
        else:
            redis_status["connected"] = False
        results["redis"] = redis_status

        return results

    def initialize_all(self) -> bool:
        """Initialize all storage connections and create necessary schemas."""
        logger.info("Initializing all storage backends...")

        # Initialize Elasticsearch
        es_ok = False
        if ELASTICSEARCH_AVAILABLE and self.es_client:
            index_name = f"{settings.es_index_prefix}_documents"
            es_ok = self.create_es_index(index_name)
            logger.info(f"Elasticsearch initialization: {'OK' if es_ok else 'FAILED'}")

        # Initialize Milvus
        milvus_ok = False
        if MILVUS_AVAILABLE:
            milvus_ok = self.create_milvus_collection()
            logger.info(f"Milvus initialization: {'OK' if milvus_ok else 'FAILED'}")

        # Initialize NebulaGraph
        nebula_ok = False
        if NEBULA_GRAPH_AVAILABLE and self.nebula_pool:
            nebula_ok = self.create_nebula_space()
            logger.info(f"NebulaGraph initialization: {'OK' if nebula_ok else 'FAILED'}")

        # Redis doesn't need schema initialization
        redis_ok = REDIS_AVAILABLE and self.redis_client is not None
        logger.info(f"Redis initialization: {'OK' if redis_ok else 'FAILED'}")

        overall_success = es_ok and milvus_ok and nebula_ok and redis_ok
        logger.info(f"Storage initialization overall: {'SUCCESS' if overall_success else 'PARTIAL/FAILED'}")

        return overall_success


# Global storage manager instance
_storage_manager: Optional[StorageManager] = None


def get_storage_manager() -> StorageManager:
    """Get storage manager instance."""
    global _storage_manager
    if _storage_manager is None:
        _storage_manager = StorageManager()
    return _storage_manager


def init_storage() -> bool:
    """Initialize storage connections. Call this at application startup."""
    manager = get_storage_manager()
    return manager.initialize_all()


def check_storage_health() -> Dict[str, Dict[str, Any]]:
    """Check health of all storage backends."""
    manager = get_storage_manager()
    return manager.check_all_connections()