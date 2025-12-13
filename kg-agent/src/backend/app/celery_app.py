from celery import Celery
from app.config import get_settings

settings = get_settings()

def make_celery(app_name=__name__):
    celery_app = Celery(
        app_name,
        broker=settings.CELERY_BROKER_URL,
        backend=settings.CELERY_RESULT_BACKEND,
        include=[
            "app.tasks.document",
            "app.tasks.index"
        ]
    )
    
    celery_app.conf.update(
        task_serializer="json",
        accept_content=["json"],
        result_serializer="json",
        timezone="UTC",
        enable_utc=True,
        task_track_started=True,
        # 任务路由配置 (可选)
        task_routes={
            'app.tasks.document.*': {'queue': 'document_processing'},
            'app.tasks.index.*': {'queue': 'indexing'},
        }
    )
    
    return celery_app

celery_app = make_celery("kg_agent")
