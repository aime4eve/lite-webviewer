from typing import List, Optional
from app.models import Document, DocumentType, ProcessingStatus, DocumentMetadata
from app.utils.logger import logger
from app.config import get_settings
import uuid
import os

settings = get_settings()

class DocumentService:
    def upload_document(self, file_content: bytes, filename: str) -> Document:
        """
        上传文档并触发处理流程
        """
        doc_id = str(uuid.uuid4())
        logger.info(f"Uploading document: {filename} (ID: {doc_id})")
        
        # Ensure upload directory exists
        os.makedirs(settings.UPLOAD_FOLDER, exist_ok=True)
        
        file_path = os.path.join(settings.UPLOAD_FOLDER, f"{doc_id}_{filename}")
        
        # Save file synchronously
        with open(file_path, 'wb') as f:
            f.write(file_content)
            
        # Trigger Celery task
        from app.tasks.document import process_document_pipeline
        process_document_pipeline.delay(doc_id, os.path.abspath(file_path))
        
        return Document(
            id=doc_id,
            filename=filename,
            file_path=file_path,
            type=self._detect_file_type(filename),
            metadata=DocumentMetadata(
                title=filename,
                file_size=len(file_content)
            ),
            status=ProcessingStatus.PENDING
        )

    def _detect_file_type(self, filename: str) -> DocumentType:
        ext = filename.lower().split('.')[-1]
        if ext == 'pdf': return DocumentType.PDF
        if ext in ['doc', 'docx']: return DocumentType.DOCX
        if ext == 'md': return DocumentType.MARKDOWN
        if ext == 'html': return DocumentType.HTML
        return DocumentType.TXT

    def get_status(self, doc_id: str) -> Document:
        """
        获取文档处理状态
        """
        # TODO: 从数据库获取文档状态
        # 临时返回模拟数据
        return Document(
            id=doc_id,
            filename="test.txt",
            file_path="/tmp/test.txt",
            type=DocumentType.TXT,
            metadata=DocumentMetadata(
                title="test.txt",
                file_size=1024
            ),
            status=ProcessingStatus.COMPLETED
        )

document_service = DocumentService()
