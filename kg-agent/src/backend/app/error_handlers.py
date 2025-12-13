from flask import jsonify
from werkzeug.exceptions import HTTPException
from pydantic import ValidationError

def register_error_handlers(app):
    @app.errorhandler(HTTPException)
    def handle_http_exception(e):
        """处理 HTTP 异常"""
        response = {
            "code": e.code,
            "name": e.name,
            "description": e.description,
        }
        return jsonify(response), e.code

    @app.errorhandler(ValidationError)
    def handle_pydantic_validation_error(e):
        """处理 Pydantic 数据校验异常"""
        response = {
            "code": 400,
            "name": "Validation Error",
            "description": "Invalid request data",
            "details": e.errors()
        }
        return jsonify(response), 400

    @app.errorhandler(Exception)
    def handle_generic_exception(e):
        """处理未捕获的通用异常"""
        # 在生产环境中不要返回详细堆栈
        import traceback
        from app.utils.logger import logger
        
        logger.error(f"Unhandled exception: {str(e)}")
        logger.debug(traceback.format_exc())
        
        response = {
            "code": 500,
            "name": "Internal Server Error",
            "description": "An unexpected error occurred."
        }
        return jsonify(response), 500
