#!/usr/bin/env python3
"""
全文搜索Web界面
提供简单的HTML界面用于搜索文档
"""

from flask import Flask, render_template_string, request
import sys
import os

# 添加上级目录到sys.path，以便导入full_text_search.py
current_dir = os.path.dirname(os.path.abspath(__file__))
parent_dir = os.path.dirname(current_dir)
sys.path.append(parent_dir)

from full_text_search import FullTextSearchService

app = Flask(__name__)

# HTML模板
HTML_TEMPLATE = """
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>全文搜索系统</title>
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, 'Open Sans', 'Helvetica Neue', sans-serif;
            line-height: 1.6;
            color: #333;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #f5f5f5;
        }
        .container {
            background-color: white;
            border-radius: 8px;
            padding: 30px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
        }
        h1 {
            color: #2c3e50;
            text-align: center;
            margin-bottom: 30px;
        }
        .search-form {
            display: flex;
            margin-bottom: 30px;
        }
        .search-input {
            flex: 1;
            padding: 12px 15px;
            border: 1px solid #ddd;
            border-radius: 4px 0 0 4px;
            font-size: 16px;
        }
        .search-button {
            padding: 12px 20px;
            background-color: #3498db;
            color: white;
            border: none;
            border-radius: 0 4px 4px 0;
            cursor: pointer;
            font-size: 16px;
            transition: background-color 0.3s;
        }
        .search-options {
            display: flex;
            gap: 20px;
            margin-bottom: 20px;
            padding: 15px;
            background-color: #f8f9fa;
            border-radius: 4px;
            align-items: center;
        }
        .option-group {
            display: flex;
            align-items: center;
            gap: 10px;
        }
        .option-label {
            font-weight: 600;
            color: #555;
        }
        select {
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            background-color: white;
        }
        .file-info {
            font-size: 13px;
            color: #95a5a6;
            margin-bottom: 8px;
            display: flex;
            gap: 15px;
        }

        .result-item {
            border-bottom: 1px solid #eee;
            padding: 20px 0;
        }
        .result-item:last-child {
            border-bottom: none;
        }
        .result-title {
            font-size: 18px;
            font-weight: 600;
            margin-bottom: 8px;
            color: #2c3e50;
        }
        .result-path {
            font-size: 14px;
            color: #7f8c8d;
            margin-bottom: 8px;
        }
        .result-score {
            font-size: 14px;
            color: #27ae60;
            margin-bottom: 10px;
        }
        .result-highlight {
            background-color: #f8f9fa;
            padding: 10px;
            border-radius: 4px;
            margin-top: 10px;
        }
        .highlight {
            background-color: #fff3cd;
            padding: 2px 4px;
            border-radius: 3px;
        }
        .no-results {
            text-align: center;
            padding: 40px 0;
            color: #7f8c8d;
        }
        .footer {
            margin-top: 40px;
            text-align: center;
            color: #7f8c8d;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>全文搜索系统</h1>
        
        <form class="search-form" method="GET" style="margin-bottom: 10px;">
            <input type="text" name="q" class="search-input" placeholder="输入搜索关键词 (支持 * ? 通配符)..." value="{{ query or '' }}" required>
            <button type="submit" class="search-button">搜索</button>
        </form>
        
        <div class="search-options">
            <div class="option-group">
                <span class="option-label">排序:</span>
                <select name="sort" onchange="updateSearch()">
                    <option value="score" {% if sort_by == 'score' %}selected{% endif %}>相关度</option>
                    <option value="date_desc" {% if sort_by == 'date_desc' %}selected{% endif %}>修改时间 (新→旧)</option>
                    <option value="date_asc" {% if sort_by == 'date_asc' %}selected{% endif %}>修改时间 (旧→新)</option>
                    <option value="size_desc" {% if sort_by == 'size_desc' %}selected{% endif %}>文件大小 (大→小)</option>
                    <option value="size_asc" {% if sort_by == 'size_asc' %}selected{% endif %}>文件大小 (小→大)</option>
                    <option value="name_len" {% if sort_by == 'name_len' %}selected{% endif %}>文件名长度</option>
                </select>
            </div>
            <div class="option-group">
                <span class="option-label">文件类型:</span>
                <select name="type" onchange="updateSearch()">
                    <option value="" {% if not file_type %}selected{% endif %}>全部类型</option>
                    <option value=".txt" {% if file_type == '.txt' %}selected{% endif %}>TXT</option>
                    <option value=".md" {% if file_type == '.md' %}selected{% endif %}>Markdown</option>
                    <option value=".pdf" {% if file_type == '.pdf' %}selected{% endif %}>PDF</option>
                    <option value=".doc" {% if file_type == '.doc' %}selected{% endif %}>DOC/DOCX</option>
                    <option value=".py" {% if file_type == '.py' %}selected{% endif %}>Python</option>
                    <option value=".js" {% if file_type == '.js' %}selected{% endif %}>JavaScript</option>
                    <option value=".html" {% if file_type == '.html' %}selected{% endif %}>HTML</option>
                </select>
            </div>
        </div>
        
        <script>
            function updateSearch() {
                const query = document.querySelector('input[name="q"]').value;
                if (query) {
                    const sort = document.querySelector('select[name="sort"]').value;
                    const type = document.querySelector('select[name="type"]').value;
                    window.location.href = `/?q=${encodeURIComponent(query)}&sort=${sort}&type=${type}`;
                }
            }
        </script>
        
        {% if query %}
        <div class="results-info">
            {% if results %}
                找到 {{ results|length }} 个与 "{{ query }}" 相关的文档
            {% else %}
                没有找到与 "{{ query }}" 相关的文档
            {% endif %}
        </div>
        
        {% if results %}
            {% for result in results %}
            <div class="result-item">
                <div class="result-title">{{ result.title }}</div>
                <div class="file-info">
                    <span>类型: {{ result.file_type }}</span>
                    <span>大小: {{ (result.file_size / 1024)|round(1) }} KB</span>
                    <span>修改时间: {{ result.last_modified|int|replace('.0', '')|timestamp_to_time }}</span>
                </div>
                <div class="result-path">{{ result.file_path }}</div>
                <div class="result-score">相关度: {{ "%.2f"|format(result.score) }}</div>
                
                {% if result.highlights %}
                <div class="result-highlight">
                    {% for highlight in result.highlights[:2] %}
                        <p>{{ highlight|replace('<em>', '<span class="highlight">')|replace('</em>', '</span>')|safe }}</p>
                    {% endfor %}
                </div>
                {% endif %}
            </div>
            {% endfor %}
        {% else %}
            <div class="no-results">
                <p>没有找到相关结果，请尝试其他关键词</p>
            </div>
        {% endif %}
        {% endif %}
        
        <div class="footer">
            <p>基于Elasticsearch和IK中文分词器的全文搜索系统</p>
        </div>
    </div>
</body>
</html>
"""

@app.template_filter('timestamp_to_time')
def timestamp_to_time(timestamp):
    try:
        import datetime
        dt = datetime.datetime.fromtimestamp(float(timestamp))
        return dt.strftime('%Y-%m-%d %H:%M:%S')
    except:
        return str(timestamp)

@app.route('/')
def index():
    query = request.args.get('q', '')
    sort_by = request.args.get('sort', 'score')
    file_type = request.args.get('type', '')
    
    results = []
    
    if query:
        search_service = FullTextSearchService()
        file_types = [file_type] if file_type else None
        results = search_service.search(query, size=20, file_types=file_types, sort_by=sort_by)
    
    return render_template_string(HTML_TEMPLATE, query=query, results=results, sort_by=sort_by, file_type=file_type)

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)