import os
from app.config import get_settings
from app.utils.logger import logger
from sentence_transformers import SentenceTransformer
from typing import List

settings = get_settings()

class EmbeddingService:
    _instance = None
    _model = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(EmbeddingService, cls).__new__(cls)
        return cls._instance

    def _get_model(self):
        if self._model is None:
            logger.info(f"Loading embedding model: {settings.EMBEDDING_MODEL_PATH}")
            try:
                self._model = SentenceTransformer(settings.EMBEDDING_MODEL_PATH)
            except Exception as e:
                logger.error(f"Failed to load embedding model: {e}")
                # Fallback for development if model not found
                logger.warning("Using mock embedding model (random)")
                class MockModel:
                    def encode(self, texts):
                        import numpy as np
                        return np.random.rand(len(texts), 768).tolist()
                self._model = MockModel()
        return self._model

    def encode(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for a list of texts
        """
        model = self._get_model()
        embeddings = model.encode(texts)
        if hasattr(embeddings, "tolist"):
            return embeddings.tolist()
        return embeddings

embedding_service = EmbeddingService()
