from typing import List, Dict, Any, Optional
from pymilvus import (
    connections,
    utility,
    FieldSchema, 
    CollectionSchema, 
    DataType, 
    Collection,
)
from app.config import get_settings
from app.utils.logger import logger

settings = get_settings()

class MilvusClient:
    _instance = None
    
    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(MilvusClient, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        self.alias = "default"
        self.collection_name = settings.MILVUS_COLLECTION
        if self._connect():
            try:
                self._ensure_collection()
            except Exception as e:
                logger.error(f"Failed to ensure Milvus collection: {e}")

    def _connect(self):
        try:
            connections.connect(
                alias=self.alias, 
                host=settings.MILVUS_HOST, 
                port=settings.MILVUS_PORT
            )
            logger.info(f"Connected to Milvus at {settings.MILVUS_HOST}:{settings.MILVUS_PORT}")
            return True
        except Exception as e:
            logger.error(f"Failed to connect to Milvus: {e}")
            return False

    def _ensure_collection(self):
        if not utility.has_collection(self.collection_name):
            logger.info(f"Creating Milvus collection: {self.collection_name}")
            fields = [
                FieldSchema(name="id", dtype=DataType.INT64, is_primary=True, auto_id=True),
                FieldSchema(name="doc_id", dtype=DataType.VARCHAR, max_length=64),
                FieldSchema(name="chunk_index", dtype=DataType.INT64),
                FieldSchema(name="content", dtype=DataType.VARCHAR, max_length=4096), # Limit content stored in vector DB
                FieldSchema(name="embedding", dtype=DataType.FLOAT_VECTOR, dim=settings.MILVUS_DIMENSION)
            ]
            schema = CollectionSchema(fields, "KG Document Chunks")
            collection = Collection(self.collection_name, schema)
            
            # Create Index
            index_params = {
                "metric_type": "COSINE",
                "index_type": "IVF_FLAT",
                "params": {"nlist": 128}
            }
            collection.create_index("embedding", index_params)
            logger.info("Milvus collection created and indexed.")
        else:
            logger.info(f"Milvus collection {self.collection_name} already exists.")

    def insert_chunks(self, doc_id: str, chunks: List[Dict[str, Any]], embeddings: List[List[float]]):
        """
        Insert chunks and embeddings into Milvus
        """
        if not utility.has_collection(self.collection_name):
            logger.warning("Milvus collection not found, skipping insertion")
            return []

        collection = Collection(self.collection_name)
        
        data = [
            [doc_id] * len(chunks),  # doc_id
            [c['index'] for c in chunks], # chunk_index
            [c['content'][:4000] for c in chunks], # content (truncated)
            embeddings # embedding
        ]
        
        res = collection.insert(data)
        collection.flush()
        logger.info(f"Inserted {len(chunks)} chunks into Milvus. IDs: {res.primary_keys}")
        return res.primary_keys

    def search(self, query_embedding: List[float], top_k: int = 5):
        if not utility.has_collection(self.collection_name):
            logger.warning("Milvus collection not found, returning empty results")
            return []
            
        collection = Collection(self.collection_name)
        collection.load()
        
        search_params = {"metric_type": "COSINE", "params": {"nprobe": 10}}
        results = collection.search(
            data=[query_embedding], 
            anns_field="embedding", 
            param=search_params, 
            limit=top_k, 
            output_fields=["doc_id", "chunk_index", "content"]
        )
        return results

milvus_client = MilvusClient()
