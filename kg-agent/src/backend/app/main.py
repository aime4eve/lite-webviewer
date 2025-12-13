from flask import Flask
from app.api import blueprint as api_blueprint
from app.config import get_settings

def create_app():
    app = Flask(__name__)
    settings = get_settings()
    app.config.from_object(settings)
    
    app.register_blueprint(api_blueprint)
    
    return app

if __name__ == "__main__":
    app = create_app()
    app.run(host="0.0.0.0", port=5000, debug=True)
