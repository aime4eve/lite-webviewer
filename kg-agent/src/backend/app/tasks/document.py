from app.celery_app import celery_app
from app.utils.logger import logger
from app.utils.text_processor import text_processor
from app.models import Document, ProcessingStatus, DocumentType
from celery import chain
from typing import List, Dict, Any
import time

@celery_app.task(bind=True)
def process_document_pipeline(self, doc_id: str, file_path: str):
    """
    文档处理流水线入口
    """
    logger.info(f"Starting processing pipeline for document {doc_id}")
    
    try:
        # Determine doc type
        ext = file_path.lower().split('.')[-1]
        doc_type = DocumentType.TXT
        if ext == 'pdf': doc_type = DocumentType.PDF
        elif ext in ['doc', 'docx']: doc_type = DocumentType.DOCX
        elif ext == 'md': doc_type = DocumentType.MARKDOWN
        elif ext == 'html': doc_type = DocumentType.HTML

        # 1. 文本提取
        text = extract_text.delay(file_path, doc_type).get()
        
        # 2. 文本分块
        chunks = chunk_text.delay(text).get()
        
        # 3. 触发索引构建 (异步)
        from app.tasks.index import index_chunks
        index_chunks.delay(doc_id, chunks)
        
        return {"status": "processing_started", "doc_id": doc_id}
        
    except Exception as e:
        logger.error(f"Pipeline failed for {doc_id}: {str(e)}")
        # TODO: Update document status to FAILED
        raise e

@celery_app.task
def extract_text(file_path: str, doc_type: str) -> str:
    """
    真实文本提取任务 (基于 LangChain)
    """
    logger.info(f"Extracting text from {file_path} ({doc_type})")
    try:
        return text_processor.load_document(file_path, DocumentType(doc_type))
    except Exception as e:
        logger.error(f"Extraction failed: {str(e)}")
        raise e

@celery_app.task
def chunk_text(text: str) -> List[Dict[str, Any]]:
    """
    真实文本分块任务 (基于 LangChain RecursiveCharacterTextSplitter)
    """
    logger.info(f"Chunking text length: {len(text)}")
    try:
        return text_processor.split_text(text)
    except Exception as e:
        logger.error(f"Chunking failed: {str(e)}")
        raise e
