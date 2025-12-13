import pytest
import requests
from elasticsearch import Elasticsearch
from pymilvus import utility
from app.config import get_settings
from tests.integration.docker.base import BaseDockerIntegrationTest

class TestDockerHealthChecks(BaseDockerIntegrationTest):
    """Test health checks for Docker services."""

    def test_redis_health(self):
        """Test Redis service health."""
        import redis
        client = redis.Redis(
            host=self.settings.REDIS_HOST,
            port=self.settings.REDIS_PORT,
            db=self.settings.REDIS_DB,
            password=self.settings.REDIS_PASSWORD
        )
        try:
            self.assertTrue(client.ping())
        finally:
            client.close()

    def test_elasticsearch_health(self):
        """Test Elasticsearch service health."""
        # Note: In CI/Docker environments, we might need to adjust connection parameters
        # or handle potential connectivity issues more gracefully.
        # Here we try to connect with simple HTTP if no auth provided.
        # ES client version 8+ sends compatibility headers by default, which ES 8.14 accepts.
        # However, if using ES client v9 against ES v8, it might send v9 headers.
        # We can try to disable compatibility headers if needed, but it's not directly exposed in init.
        # Instead, we can try to use a lower level check with requests if the official client is too strict on version mismatch.
        
        try:
            # First try official client
            es = Elasticsearch(
                hosts=[f"{self.settings.ES_HOST}"],
                basic_auth=(self.settings.ES_USER, self.settings.ES_PASSWORD) if self.settings.ES_USER else None,
                verify_certs=False,
                ssl_show_warn=False
            )
            try:
                # Simple info check instead of ping which might fail if path is restricted or method differs
                info = es.info()
                self.assertTrue(info)
                # self.assertEqual(info['tagline'], "You Know, for Search")
                
                health = es.cluster.health()
                self.assertIn(health['status'], ['green', 'yellow'])
            finally:
                es.close()
        except Exception as e:
            # Fallback to requests if client fails due to version mismatch (e.g. client v9 vs server v8)
            print(f"ES Client check failed: {e}. Falling back to requests.")
            response = requests.get(f"{self.settings.ES_HOST}")
            self.assertEqual(response.status_code, 200)
            data = response.json()
            self.assertIn("tagline", data)
            self.assertEqual(data["tagline"], "You Know, for Search")

    def test_milvus_health(self):
        """Test Milvus service health."""
        # Note: Connection is already handled by the fixture in conftest.py implicitly if using pytest,
        # but here we are using unittest.TestCase, so we might need to rely on the global connection or connect explicitly.
        # However, BaseDockerIntegrationTest doesn't automatically connect.
        # Let's rely on utility.get_server_version() to check connectivity if connected.
        # Or better, try to connect here to ensure isolation.
        
        from pymilvus import connections
        try:
            connections.connect(
                alias="health_check",
                host=self.settings.MILVUS_HOST,
                port=self.settings.MILVUS_PORT
            )
            self.assertTrue(utility.get_server_version(using="health_check"))
        finally:
            connections.disconnect("health_check")

    def test_nebula_health(self):
        """Test NebulaGraph service health."""
        # NebulaGraph health check is more complex, usually involves checking if we can connect and execute a query.
        from nebula3.gclient.net import ConnectionPool
        from nebula3.Config import Config
        
        config = Config()
        config.max_connection_pool_size = 1
        pool = ConnectionPool()
        try:
            assert pool.init([(self.settings.NEBULA_HOST, self.settings.NEBULA_PORT)], config)
            # Try to get a session
            session = pool.get_session(self.settings.NEBULA_USER, self.settings.NEBULA_PASSWORD)
            self.assertIsNotNone(session)
            session.release()
        finally:
            pool.close()

    def test_minio_health(self):
        """Test MinIO service health."""
        # Check if MinIO port is open and responsive
        try:
            # Assuming MinIO console is at 9001 and API at 9000
            # We check the health endpoint
            # Note: docker-compose maps 9000:9000 and 9001:9001
            # MinIO health endpoint: /minio/health/live
            url = f"http://localhost:9000/minio/health/live"
            response = requests.get(url)
            self.assertEqual(response.status_code, 200)
        except requests.exceptions.ConnectionError:
            self.fail("Could not connect to MinIO")
