from app.celery_app import celery_app
from app.utils.logger import logger
from typing import List, Dict, Any

# Import Infrastructure Clients
from app.services.embedding_service import embedding_service
from app.infrastructure.milvus import milvus_client
from app.infrastructure.elasticsearch import es_client
from app.infrastructure.nebula import nebula_client

@celery_app.task
def index_chunks(doc_id: str, chunks: List[Dict[str, Any]]):
    """
    构建索引任务 (ES + Milvus + Nebula)
    """
    logger.info(f"Indexing {len(chunks)} chunks for document {doc_id}")
    
    try:
        # 1. Generate Embeddings
        logger.info("Generating embeddings...")
        texts = [c['content'] for c in chunks]
        embeddings = embedding_service.encode(texts)
        
        # 2. Index into Elasticsearch
        # Concat full content for ES
        full_content = "\n\n".join(texts)
        es_client.index_document(doc_id, full_content, metadata={"chunk_count": len(chunks)})
        
        # 3. Index into Milvus
        milvus_client.insert_chunks(doc_id, chunks, embeddings)
        
        # 4. Construct Graph & Index into Nebula
        nebula_client.insert_structure(doc_id, chunks)
        
        logger.info(f"Indexing completed for document {doc_id}")
        return {"status": "indexed", "doc_id": doc_id, "chunk_count": len(chunks)}
        
    except Exception as e:
        logger.error(f"Indexing failed for {doc_id}: {e}")
        raise e
