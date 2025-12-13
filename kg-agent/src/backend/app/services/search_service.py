from typing import List, Optional
from app.models import SearchQuery, SearchResponse, SearchResultItem, SearchMode
from app.utils.logger import logger
from app.services.embedding_service import embedding_service
from app.infrastructure.milvus import milvus_client
from app.infrastructure.elasticsearch import es_client
import time
import asyncio

class SearchService:
    async def search(self, query: SearchQuery) -> SearchResponse:
        """
        执行混合检索
        """
        start_time = time.time()
        logger.info(f"Executing search query: {query.query} with mode {query.mode}")
        
        results = []
        
        # 1. Vector Search (Milvus)
        if query.mode in [SearchMode.VECTOR, SearchMode.HYBRID]:
            try:
                embedding = embedding_service.encode([query.query])[0]
                milvus_results = milvus_client.search(embedding, top_k=query.top_k)
                
                for hits in milvus_results:
                    for hit in hits:
                        results.append(SearchResultItem(
                            id=str(hit.entity.get("doc_id")),
                            content=hit.entity.get("content"),
                            score=hit.distance,
                            source="vector",
                            metadata={"chunk_index": hit.entity.get("chunk_index")}
                        ))
            except Exception as e:
                logger.error(f"Vector search failed: {e}")

        # 2. Fulltext Search (ES)
        if query.mode in [SearchMode.FULLTEXT, SearchMode.HYBRID]:
            try:
                es_hits = es_client.search(query.query, top_k=query.top_k)
                for hit in es_hits:
                    source = hit['_source']
                    results.append(SearchResultItem(
                        id=source.get('doc_id'),
                        content=source.get('content')[:200] + "...", # Snippet
                        score=hit['_score'],
                        source="fulltext",
                        metadata=source.get('metadata')
                    ))
            except Exception as e:
                logger.error(f"Fulltext search failed: {e}")

        # 3. Deduplicate and Sort
        # Simple strategy: prioritize vector scores if hybrid, or normalize scores
        # For now, just sort by score descending
        results.sort(key=lambda x: x.score, reverse=True)
        results = results[:query.top_k]
        
        took = (time.time() - start_time) * 1000
        
        return SearchResponse(
            total=len(results),
            items=results,
            took=took
        )

search_service = SearchService()
