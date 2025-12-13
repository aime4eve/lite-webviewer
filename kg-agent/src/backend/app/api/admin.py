from flask import Blueprint, request, jsonify
from app.services import document_service
from app.exceptions import ValidationError

admin_bp = Blueprint('admin', __name__, url_prefix='/api/admin')

@admin_bp.route('/upload', methods=['POST'])
def upload_document():
    """
    上传文档
    ---
    tags:
      - Admin
    consumes:
      - multipart/form-data
    parameters:
      - in: formData
        name: file
        type: file
        required: true
        description: 要上传的文档文件
    responses:
      200:
        description: 上传成功
        schema:
          $ref: '#/definitions/Document'
    """
    if 'file' not in request.files:
        raise ValidationError(
            message="请求中未包含文件部分",
            details={"field": "file", "expected": "multipart/form-data with file field"}
        )

    file = request.files['file']
    if file.filename == '':
        raise ValidationError(
            message="未选择文件",
            details={"field": "file", "filename": ""}
        )

    content = file.read()
    doc = document_service.upload_document(content, file.filename)
    return jsonify(doc.model_dump())

@admin_bp.route('/status', methods=['GET'])
def system_status():
    """
    获取系统状态
    ---
    tags:
      - Admin
    responses:
      200:
        description: 系统状态信息
    """
    # Mock status
    return jsonify({
        "celery": "connected",
        "redis": "connected",
        "elasticsearch": "green",
        "nebula": "connected",
        "milvus": "connected"
    })
