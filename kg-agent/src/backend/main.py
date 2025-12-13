from flask import Flask, jsonify
from flasgger import Swagger
from app.config import get_settings
from app.api.search import search_bp
from app.api.kg import kg_bp
from app.api.admin import admin_bp
from app.models import * # Import all models for Swagger definitions
from app.error_handlers import register_error_handlers

settings = get_settings()

def create_app():
    app = Flask(__name__)
    
    # Register Error Handlers
    register_error_handlers(app)
    
    # Configure Swagger
    app.config['SWAGGER'] = {
        'title': 'KG-Agent API',
        'uiversion': 3,
        'version': '1.0.0',
        'description': 'Knowledge Graph Enhanced Retrieval-Augmented Generation API',
        'specs': [
            {
                "endpoint": 'apispec_1',
                "route": '/apispec_1.json',
                "rule_filter": lambda rule: True,  # all in
                "model_filter": lambda tag: True,  # all in
            }
        ],
    }
    
    # Register Blueprints
    app.register_blueprint(search_bp)
    app.register_blueprint(kg_bp)
    app.register_blueprint(admin_bp)

    # Initialize Swagger after registering blueprints and models
    Swagger(app, template={
        "definitions": {
            "SearchQuery": SearchQuery.model_json_schema(),
            "SearchResponse": SearchResponse.model_json_schema(),
            "SearchResultItem": SearchResultItem.model_json_schema(),
            "GraphQuery": GraphQuery.model_json_schema(),
            "GraphResult": GraphResult.model_json_schema(),
            "GraphData": GraphData.model_json_schema(),
            "Document": Document.model_json_schema(),
        }
    })

    @app.route("/health")
    def health_check():
        """
        Health Check Endpoint
        ---
        responses:
          200:
            description: Service is healthy
            schema:
              type: object
              properties:
                status:
                  type: string
                  example: ok
                env:
                  type: string
                  example: development
        """
        return jsonify({"status": "ok", "env": settings.APP_ENV})

    return app

if __name__ == "__main__":
    app = create_app()
    app.run(host="0.0.0.0", port=5000, debug=settings.DEBUG)
