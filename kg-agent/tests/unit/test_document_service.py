"""
文档服务测试
"""
import unittest
import tempfile
import os
import sys

# 添加项目根目录到Python路径
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '../../src/backend'))

from app.services.document_service import document_service
from app.models import Document, DocumentType, ProcessingStatus

# 模拟Celery任务，避免真实的Redis连接
from unittest.mock import patch
from app.tasks.document import process_document_pipeline

# 在测试前模拟delay方法
process_document_pipeline.delay = lambda *args, **kwargs: None

class TestDocumentService(unittest.TestCase):
    def setUp(self):
        """
        设置测试环境
        """
        # 创建临时文件用于测试
        self.test_file = tempfile.NamedTemporaryFile(suffix=".txt", delete=False)
        self.test_file.write(b"Test document content")
        self.test_file.close()
        
    def tearDown(self):
        """
        清理测试环境
        """
        # 删除临时文件
        if os.path.exists(self.test_file.name):
            os.remove(self.test_file.name)
    
    def test_detect_file_type(self):
        """
        测试文件类型检测功能
        """
        # 测试不同文件类型
        self.assertEqual(document_service._detect_file_type("test.txt"), DocumentType.TXT)
        self.assertEqual(document_service._detect_file_type("test.pdf"), DocumentType.PDF)
        self.assertEqual(document_service._detect_file_type("test.docx"), DocumentType.DOCX)
        self.assertEqual(document_service._detect_file_type("test.md"), DocumentType.MARKDOWN)
        self.assertEqual(document_service._detect_file_type("test.html"), DocumentType.HTML)
    
    def test_upload_document(self):
        """
        测试文档上传功能
        """
        with open(self.test_file.name, 'rb') as f:
            content = f.read()
        
        doc = document_service.upload_document(content, "test.txt")
        
        # 检查返回的文档对象
        self.assertIsInstance(doc, Document)
        self.assertEqual(doc.filename, "test.txt")
        self.assertEqual(doc.type, DocumentType.TXT)
        self.assertEqual(doc.status, ProcessingStatus.PENDING)
        
        # 检查文件是否保存成功
        self.assertTrue(os.path.exists(doc.file_path))
        
        # 清理生成的文件
        if os.path.exists(doc.file_path):
            os.remove(doc.file_path)
    
    def test_get_status(self):
        """
        测试获取文档状态功能
        """
        doc = document_service.get_status("test_doc_123")
        
        # 检查返回的文档对象
        self.assertIsInstance(doc, Document)
        self.assertEqual(doc.id, "test_doc_123")
        self.assertEqual(doc.status, ProcessingStatus.COMPLETED)

if __name__ == "__main__":
    unittest.main()
