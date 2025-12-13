import logging
import sys
from app.config import get_settings

settings = get_settings()

class JSONFormatter(logging.Formatter):
    """
    简单模拟 JSON 日志格式
    实际生产环境建议使用 python-json-logger 库
    """
    def format(self, record):
        import json
        log_record = {
            "timestamp": self.formatTime(record),
            "level": record.levelname,
            "message": record.getMessage(),
            "module": record.module,
            "funcName": record.funcName,
            "lineNo": record.lineno,
            "process": record.process
        }
        if record.exc_info:
            log_record["exception"] = self.formatException(record.exc_info)
        return json.dumps(log_record)

def setup_logger(name: str) -> logging.Logger:
    logger = logging.getLogger(name)
    
    if not logger.handlers:
        handler = logging.StreamHandler(sys.stdout)
        
        if settings.APP_ENV == "production":
            formatter = JSONFormatter()
        else:
            formatter = logging.Formatter(
                '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
            )
            
        handler.setFormatter(formatter)
        logger.addHandler(handler)
        
        level = logging.DEBUG if settings.DEBUG else logging.INFO
        logger.setLevel(level)
        
    return logger

logger = setup_logger("kg-agent")
