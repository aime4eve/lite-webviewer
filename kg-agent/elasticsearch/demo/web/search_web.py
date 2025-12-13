#!/usr/bin/env python3
"""
全文检索 Web 界面
提供简单的 Web 界面来搜索和展示文档
"""

import os
import sys
from flask import Flask, render_template, request, jsonify
from full_text_search import FullTextSearchService

app = Flask(__name__)

# 初始化全文检索服务
search_service = FullTextSearchService()

@app.route('/')
def index():
    """主页"""
    return render_template('index.html')

@app.route('/search')
def search():
    """搜索接口"""
    query = request.args.get('q', '')
    # 处理URL编码的中文字符
    if query:
        try:
            from urllib.parse import unquote
            query = unquote(query)
        except:
            pass
    
    if not query:
        return jsonify({'error': '请输入搜索关键词'})
    
    try:
        results = search_service.search(query)
        return jsonify({'results': results, 'query': query})
    except Exception as e:
        return jsonify({'error': f'搜索出错: {str(e)}'})

@app.route('/index')
def index_files():
    """重新索引文件"""
    try:
        search_service.index_files()
        return jsonify({'success': True, 'message': '索引更新成功'})
    except Exception as e:
        return jsonify({'error': f'索引更新出错: {str(e)}'})

if __name__ == '__main__':
    # 确保模板目录存在
    os.makedirs('templates', exist_ok=True)
    
    # 创建简单的 HTML 模板
    with open('templates/index.html', 'w') as f:
        f.write('''<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>全文检索系统</title>
    <style>
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
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
            margin-bottom: 20px;
            text-align: center;
        }
        .search-box {
            display: flex;
            margin-bottom: 20px;
        }
        .search-input {
            flex: 1;
            padding: 12px;
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
        }
        .search-button:hover {
            background-color: #2980b9;
        }
        .index-button {
            padding: 10px 15px;
            background-color: #2ecc71;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            margin-left: 10px;
        }
        .index-button:hover {
            background-color: #27ae60;
        }
        .results {
            margin-top: 20px;
        }
        .result-item {
            border-bottom: 1px solid #eee;
            padding: 15px 0;
        }
        .result-item:last-child {
            border-bottom: none;
        }
        .result-title {
            font-size: 18px;
            font-weight: bold;
            color: #2c3e50;
            margin-bottom: 5px;
        }
        .result-path {
            font-size: 14px;
            color: #7f8c8d;
            margin-bottom: 8px;
        }
        .result-score {
            font-size: 12px;
            color: #95a5a6;
            margin-bottom: 10px;
        }
        .result-snippet {
            font-size: 14px;
            line-height: 1.5;
        }
        .highlight {
            background-color: #f39c12;
            padding: 1px 2px;
            border-radius: 2px;
        }
        /* 处理Elasticsearch返回的em标签 */
        .result-snippet em {
            background-color: #f39c12;
            padding: 1px 2px;
            border-radius: 2px;
            font-style: normal;
        }
        .loading {
            text-align: center;
            padding: 20px;
            color: #7f8c8d;
        }
        .error {
            color: #e74c3c;
            padding: 10px;
            background-color: #fadbd8;
            border-radius: 4px;
            margin-bottom: 20px;
        }
        .success {
            color: #27ae60;
            padding: 10px;
            background-color: #d5f4e6;
            border-radius: 4px;
            margin-bottom: 20px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>全文检索系统</h1>
        
        <div class="search-box">
            <input type="text" id="search-input" class="search-input" placeholder="请输入搜索关键词..." />
            <button id="search-button" class="search-button">搜索</button>
            <button id="index-button" class="index-button">更新索引</button>
        </div>
        
        <div id="message" class="message" style="display: none;"></div>
        
        <div id="results" class="results"></div>
    </div>

    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const searchInput = document.getElementById('search-input');
            const searchButton = document.getElementById('search-button');
            const indexButton = document.getElementById('index-button');
            const resultsDiv = document.getElementById('results');
            const messageDiv = document.getElementById('message');
            
            // 搜索功能
            function performSearch() {
                const query = searchInput.value.trim();
                if (!query) {
                    showMessage('请输入搜索关键词', 'error');
                    return;
                }
                
                resultsDiv.innerHTML = '<div class="loading">搜索中...</div>';
                
                fetch(`/search?q=${encodeURIComponent(query)}`)
                    .then(response => response.json())
                    .then(data => {
                        if (data.error) {
                            showMessage(data.error, 'error');
                            resultsDiv.innerHTML = '';
                        } else {
                            displayResults(data.results, data.query);
                            messageDiv.style.display = 'none';
                        }
                    })
                    .catch(error => {
                        showMessage(`搜索出错: ${error.message}`, 'error');
                        resultsDiv.innerHTML = '';
                    });
            }
            
            // 显示搜索结果
            function displayResults(results, query) {
                if (results.length === 0) {
                    resultsDiv.innerHTML = '<p>没有找到相关文档</p>';
                    return;
                }
                
                let html = `<h2>搜索结果 (共 ${results.length} 个文档)</h2>`;
                
                results.forEach(result => {
                      const title = highlightText(result.title, query);
                      let snippet = '';
                      if (result.highlights && result.highlights.length > 0) {
                          // 直接使用Elasticsearch返回的高亮片段，不再处理
                          snippet = result.highlights.join('...');
                      } else if (result.snippet) {
                          snippet = highlightText(result.snippet, query);
                      }
                      
                      html += `
                        <div class="result-item">
                            <div class="result-title">${title}</div>
                            <div class="result-path">${result.file_path}</div>
                            <div class="result-score">相关度: ${result.score.toFixed(2)}</div>
                            <div class="result-snippet">${snippet}</div>
                        </div>
                    `;
                });
                
                resultsDiv.innerHTML = html;
            }
            
            // 高亮关键词
            function highlightText(text, query) {
                if (!text || !query) return text;
                
                const regex = new RegExp(`(${query})`, 'gi');
                return text.replace(regex, '<span class="highlight">$1</span>');
            }
            
            // 显示消息
            function showMessage(message, type) {
                messageDiv.textContent = message;
                messageDiv.className = type;
                messageDiv.style.display = 'block';
                
                // 5秒后自动隐藏
                setTimeout(() => {
                    messageDiv.style.display = 'none';
                }, 5000);
            }
            
            // 更新索引
            function updateIndex() {
                resultsDiv.innerHTML = '<div class="loading">更新索引中...</div>';
                
                fetch('/index')
                    .then(response => response.json())
                    .then(data => {
                        if (data.error) {
                            showMessage(data.error, 'error');
                        } else {
                            showMessage(data.message, 'success');
                        }
                        resultsDiv.innerHTML = '';
                    })
                    .catch(error => {
                        showMessage(`更新索引出错: ${error.message}`, 'error');
                        resultsDiv.innerHTML = '';
                    });
            }
            
            // 事件监听
            searchButton.addEventListener('click', performSearch);
            indexButton.addEventListener('click', updateIndex);
            
            searchInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    performSearch();
                }
            });
            
            // 页面加载时自动搜索"认证"
            searchInput.value = '认证';
            performSearch();
        });
    </script>
</body>
</html>''')
    
    print("启动全文检索 Web 界面...")
    print("访问 http://localhost:5000 使用搜索功能")
    app.run(host='0.0.0.0', port=5000, debug=True)