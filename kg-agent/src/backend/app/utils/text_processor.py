from langchain_community.document_loaders import (
    TextLoader, 
    PyPDFLoader, 
    UnstructuredWordDocumentLoader,
    UnstructuredMarkdownLoader
)
from langchain_text_splitters import RecursiveCharacterTextSplitter
from typing import List, Dict, Any
import os
from app.models import DocumentType

class TextProcessor:
    def __init__(self):
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=512,
            chunk_overlap=64,
            length_function=len,
            is_separator_regex=False,
        )

    def load_document(self, file_path: str, doc_type: DocumentType) -> str:
        """
        根据文档类型加载并提取文本
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"File not found: {file_path}")

        try:
            if doc_type == DocumentType.PDF:
                loader = PyPDFLoader(file_path)
                pages = loader.load()
                return "\n\n".join([p.page_content for p in pages])
                
            elif doc_type == DocumentType.DOCX:
                loader = UnstructuredWordDocumentLoader(file_path)
                docs = loader.load()
                return "\n\n".join([d.page_content for d in docs])
                
            elif doc_type == DocumentType.MARKDOWN:
                loader = UnstructuredMarkdownLoader(file_path)
                docs = loader.load()
                return "\n\n".join([d.page_content for d in docs])
                
            else: # Default to TextLoader
                loader = TextLoader(file_path, encoding='utf-8')
                docs = loader.load()
                return "\n\n".join([d.page_content for d in docs])
                
        except Exception as e:
            raise ValueError(f"Failed to load document {file_path}: {str(e)}")

    def split_text(self, text: str) -> List[Dict[str, Any]]:
        """
        将文本切分为 Chunks
        """
        chunks = self.text_splitter.create_documents([text])
        
        return [
            {
                "content": chunk.page_content,
                "index": i,
                "token_count": len(chunk.page_content) # Simple char count for now
            }
            for i, chunk in enumerate(chunks)
        ]

text_processor = TextProcessor()
