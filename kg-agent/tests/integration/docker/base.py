import unittest
from app.config import get_settings

class BaseDockerIntegrationTest(unittest.TestCase):
    """Base class for Docker integration tests."""
    
    @classmethod
    def setUpClass(cls):
        """Set up test environment."""
        cls.settings = get_settings()
        
    def setUp(self):
        """Set up before each test."""
        pass
        
    def tearDown(self):
        """Clean up after each test."""
        pass
