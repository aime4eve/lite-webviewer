"""
kg-agent Flask application entry point.

Application factory pattern with blueprint registration.
"""
import logging
import sys
from typing import Dict, Any

import structlog
from flask import Flask, jsonify
from flask_cors import CORS

from .config import settings


def configure_logging() -> None:
    """Configure structured logging with structlog."""
    logging.basicConfig(
        format="%(message)s",
        stream=sys.stdout,
        level=getattr(logging, settings.log_level.upper()),
    )

    structlog.configure(
        processors=[
            structlog.stdlib.filter_by_level,
            structlog.stdlib.add_logger_name,
            structlog.stdlib.add_log_level,
            structlog.stdlib.PositionalArgumentsFormatter(),
            structlog.processors.TimeStamper(fmt="iso"),
            structlog.processors.StackInfoRenderer(),
            structlog.processors.format_exc_info,
            structlog.processors.UnicodeDecoder(),
            structlog.processors.JSONRenderer(),
        ],
        context_class=dict,
        logger_factory=structlog.stdlib.LoggerFactory(),
        wrapper_class=structlog.stdlib.BoundLogger,
        cache_logger_on_first_use=True,
    )


def create_app() -> Flask:
    """Create and configure the Flask application."""
    configure_logging()
    logger = structlog.get_logger(__name__)
    logger.info("Creating Flask application", environment=settings.environment)

    app = Flask(__name__)
    app.config.update(
        {
            "SECRET_KEY": settings.secret_key,
            "DEBUG": settings.debug,
            "ENV": settings.environment,
        }
    )

    # Configure CORS
    CORS(app, origins=settings.cors_origins)

    # Register blueprints
    register_blueprints(app)

    # Register error handlers
    register_error_handlers(app)

    # Register health check endpoint
    register_health_check(app)

    logger.info("Flask application created successfully")
    return app


def register_blueprints(app: Flask) -> None:
    """Register API blueprints."""
    # Import blueprints here to avoid circular imports
    # TODO: Import and register actual blueprints
    # from .app.api.search import search_blueprint
    # from .app.api.kg import kg_blueprint
    # from .app.api.admin import admin_blueprint

    # app.register_blueprint(search_blueprint, url_prefix=f"{settings.api_prefix}/search")
    # app.register_blueprint(kg_blueprint, url_prefix=f"{settings.api_prefix}/kg")
    # app.register_blueprint(admin_blueprint, url_prefix=f"{settings.api_prefix}/admin")

    # Temporary placeholder blueprint
    from flask import Blueprint

    placeholder = Blueprint("placeholder", __name__)

    @placeholder.route("/")
    def index() -> Dict[str, str]:
        return {"message": "kg-agent API", "version": settings.app_version}

    app.register_blueprint(placeholder, url_prefix=settings.api_prefix)


def register_error_handlers(app: Flask) -> None:
    """Register error handlers."""

    @app.errorhandler(404)
    def not_found(error) -> Dict[str, Any]:
        return jsonify({"error": "Not found", "message": str(error)}), 404

    @app.errorhandler(500)
    def internal_error(error) -> Dict[str, Any]:
        return jsonify({"error": "Internal server error", "message": str(error)}), 500


def register_health_check(app: Flask) -> None:
    """Register health check endpoints."""

    @app.route("/health")
    def health() -> Dict[str, str]:
        """Simple health check endpoint."""
        return {"status": "healthy", "service": settings.app_name}

    @app.route("/health/ready")
    def ready() -> Dict[str, Any]:
        """Readiness probe with dependency checks."""
        # TODO: Add dependency checks (Redis, ES, Nebula, etc.)
        dependencies = {
            "redis": False,
            "elasticsearch": False,
            "nebula": False,
            "milvus": False,
        }
        all_healthy = all(dependencies.values())
        return {
            "status": "ready" if all_healthy else "not ready",
            "dependencies": dependencies,
        }

    @app.route("/health/live")
    def live() -> Dict[str, str]:
        """Liveness probe."""
        return {"status": "alive", "service": settings.app_name}


# Create the application instance
app = create_app()

if __name__ == "__main__":
    structlog.get_logger(__name__).info(
        "Starting kg-agent server",
        host=settings.host,
        port=settings.port,
        environment=settings.environment,
    )
    app.run(host=settings.host, port=settings.port, debug=settings.debug)