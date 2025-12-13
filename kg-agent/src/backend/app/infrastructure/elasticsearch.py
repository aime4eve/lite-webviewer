from elasticsearch import Elasticsearch, helpers
from app.config import get_settings
from app.utils.logger import logger
from typing import List, Dict, Any

settings = get_settings()

class ESClient:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(ESClient, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        try:
            self.client = Elasticsearch(
                hosts=[settings.ES_HOST],
                basic_auth=(settings.ES_USER, settings.ES_PASSWORD) if settings.ES_USER else None
            )
            self.index_name = f"{settings.ES_INDEX_PREFIX}_docs"
            self._ensure_index()
        except Exception as e:
            logger.error(f"Failed to initialize ES Client: {e}")
            self.client = None

    def _ensure_index(self):
        if not self.client: return
        try:
            # Check connection first
            if not self.client.ping():
                logger.error("Could not connect to Elasticsearch")
                return

            if not self.client.indices.exists(index=self.index_name):
                logger.info(f"Creating ES index: {self.index_name}")
                mappings = {
                    "properties": {
                        "doc_id": {"type": "keyword"},
                        "filename": {"type": "keyword"},
                        "content": {"type": "text", "analyzer": "standard"}, # Use standard for now, switch to ik_max_word if plugin available
                        "metadata": {"type": "object"},
                        "created_at": {"type": "date"}
                    }
                }
                self.client.indices.create(index=self.index_name, mappings=mappings)
            else:
                logger.info(f"ES index {self.index_name} already exists.")
        except Exception as e:
            logger.error(f"ES index check failed: {e}")

    def index_document(self, doc_id: str, content: str, metadata: Dict[str, Any]):
        """Index full document content"""
        if not self.client: return
        try:
            doc = {
                "doc_id": doc_id,
                "content": content,
                "metadata": metadata
            }
            res = self.client.index(index=self.index_name, id=doc_id, document=doc)
            logger.info(f"Indexed document {doc_id} to ES: {res['result']}")
        except Exception as e:
            logger.error(f"Failed to index document to ES: {e}")

    def search(self, query: str, top_k: int = 10):
        try:
            res = self.client.search(
                index=self.index_name,
                query={"match": {"content": query}},
                size=top_k
            )
            return res['hits']['hits']
        except Exception as e:
            logger.error(f"ES search failed: {e}")
            return []

es_client = ESClient()
