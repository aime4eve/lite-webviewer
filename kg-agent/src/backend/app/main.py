from flask import Flask
from app.api import blueprint as api_blueprint
from app.config import get_settings
from app.error_handlers import register_error_handlers
from app.services.storage_manager import init_storage

def create_app():
    app = Flask(__name__)
    settings = get_settings()
    app.config.from_object(settings)
    
    # Register blueprints
    app.register_blueprint(api_blueprint)
    
    # Register error handlers
    register_error_handlers(app)
    
    # Initialize storage connections
    init_storage()
    
    return app

if __name__ == "__main__":
    app = create_app()
    app.run(host="0.0.0.0", port=5000, debug=True)
