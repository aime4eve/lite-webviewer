import random
import string
import uuid
from datetime import datetime
from typing import Dict, Any, List

class TestDataGenerator:
    """Generator for test data."""

    @staticmethod
    def generate_random_string(length: int = 10) -> str:
        """Generate a random string of fixed length."""
        letters = string.ascii_lowercase
        return ''.join(random.choice(letters) for i in range(length))

    @staticmethod
    def generate_document_metadata() -> Dict[str, Any]:
        """Generate random document metadata."""
        return {
            "title": f"Test Document {TestDataGenerator.generate_random_string(5)}",
            "author": f"Author {TestDataGenerator.generate_random_string(5)}",
            "created_at": datetime.now().isoformat(),
            "tags": [TestDataGenerator.generate_random_string(3) for _ in range(3)]
        }

    @staticmethod
    def generate_es_document() -> Dict[str, Any]:
        """Generate a document for Elasticsearch."""
        return {
            "id": str(uuid.uuid4()),
            "content": f"This is a test content with random string {TestDataGenerator.generate_random_string(20)}",
            "metadata": TestDataGenerator.generate_document_metadata()
        }

    @staticmethod
    def generate_milvus_vector(dim: int = 768) -> List[float]:
        """Generate a random vector for Milvus."""
        return [random.random() for _ in range(dim)]

    @staticmethod
    def generate_graph_node() -> Dict[str, Any]:
        """Generate a graph node."""
        return {
            "vid": f"node_{TestDataGenerator.generate_random_string(5)}",
            "properties": {
                "name": TestDataGenerator.generate_random_string(8),
                "type": "test_node"
            }
        }

    @staticmethod
    def generate_graph_edge(src: str, dst: str) -> Dict[str, Any]:
        """Generate a graph edge."""
        return {
            "src": src,
            "dst": dst,
            "type": "test_edge",
            "properties": {
                "weight": random.random()
            }
        }
