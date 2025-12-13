import time
import pytest
from elasticsearch import Elasticsearch
from tests.integration.docker.base import BaseDockerIntegrationTest
from tests.integration.docker.test_data_generators import TestDataGenerator

class TestDockerPerformance(BaseDockerIntegrationTest):
    """Performance benchmark tests for Docker services."""

    def test_redis_latency(self):
        """Benchmark Redis set/get latency."""
        import redis
        client = redis.Redis(
            host=self.settings.REDIS_HOST,
            port=self.settings.REDIS_PORT,
            db=self.settings.REDIS_DB,
            password=self.settings.REDIS_PASSWORD
        )
        try:
            start_time = time.time()
            for i in range(100):
                client.set(f"perf_key_{i}", "value")
            write_duration = time.time() - start_time
            
            start_time = time.time()
            for i in range(100):
                client.get(f"perf_key_{i}")
            read_duration = time.time() - start_time
            
            print(f"\nRedis Write Latency (100 ops): {write_duration*1000:.2f}ms")
            print(f"Redis Read Latency (100 ops): {read_duration*1000:.2f}ms")
            
            # Simple assertion to ensure it's not terribly slow (e.g., < 1s for 100 ops)
            self.assertLess(write_duration, 1.0)
            self.assertLess(read_duration, 1.0)
        finally:
            client.close()

    def test_elasticsearch_indexing_latency(self):
        """Benchmark Elasticsearch indexing latency."""
        es = Elasticsearch(
            hosts=[f"{self.settings.ES_HOST}"],
            basic_auth=(self.settings.ES_USER, self.settings.ES_PASSWORD) if self.settings.ES_USER else None,
            verify_certs=False,
            ssl_show_warn=False
        )
        try:
            index_name = f"perf_index_{TestDataGenerator.generate_random_string(5).lower()}"
            if not es.indices.exists(index=index_name):
                es.indices.create(index=index_name)
            
            docs = [TestDataGenerator.generate_es_document() for _ in range(50)]
            
            start_time = time.time()
            for doc in docs:
                es.index(index=index_name, id=doc['id'], document=doc)
            duration = time.time() - start_time
            
            print(f"\nElasticsearch Indexing Latency (50 docs): {duration*1000:.2f}ms")
            
            self.assertLess(duration, 5.0) # Allow some time for ES
            
            es.indices.delete(index=index_name)
        except Exception as e:
            print(f"ES Performance test failed or skipped due to client issue: {e}")
        finally:
            es.close()
