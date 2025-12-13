from flask import Blueprint
from app.api.admin import admin_bp
from app.api.search import search_bp

blueprint = Blueprint('api', __name__)
blueprint.register_blueprint(admin_bp)
blueprint.register_blueprint(search_bp)
