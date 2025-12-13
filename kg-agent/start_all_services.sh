#!/bin/bash
# 全文搜索系统一键启动脚本

echo "=========================================="
echo "全文搜索系统启动脚本"
echo "=========================================="

# 检查Elasticsearch是否运行
echo "检查Elasticsearch状态..."
if ! curl -s http://localhost:9200/_cluster/health > /dev/null; then
    echo "Elasticsearch未运行，正在启动..."
    cd /home/agentic/lite-webviewer/kg-agent
    docker-compose up -d elasticsearch
    
    # 等待Elasticsearch启动
    echo "等待Elasticsearch启动..."
    while ! curl -s http://localhost:9200/_cluster/health > /dev/null; do
        sleep 5
        echo "等待中..."
    done
    
    echo "Elasticsearch已启动"
else
    echo "Elasticsearch已运行"
fi

# 索引文件
echo "检查索引状态..."
cd /home/agentic/lite-webviewer/kg-agent
python3 -c "
from full_text_search import FullTextSearchService
import sys
try:
    service = FullTextSearchService()
    # 检查是否有文档
    result = service.es.count(index='documents')
    if result['count'] == 0:
        print('索引为空，开始索引文件...')
        service.index_files()
    else:
        print(f'索引已存在，包含 {result[\"count\"]} 个文档')
except Exception as e:
    print(f'检查索引时出错: {e}')
    sys.exit(1)
"

# 启动Web界面
echo "启动Web界面..."
if ! pgrep -f "search_web_ui.py" > /dev/null; then
    nohup python3 search_web_ui.py > /tmp/search_web_ui.log 2>&1 &
    echo "Web界面已启动，访问地址: http://localhost:5001"
else
    echo "Web界面已在运行"
fi

# 启动API服务
echo "启动API服务..."
if ! pgrep -f "kg_api_server.py" > /dev/null; then
    nohup python3 kg_api_server.py > /tmp/kg_api_server.log 2>&1 &
    echo "API服务已启动，访问地址: http://localhost:5004"
else
    echo "API服务已在运行"
fi

echo ""
echo "=========================================="
echo "全文搜索系统启动完成！"
echo "=========================================="
echo ""
echo "服务地址："
echo "- Web界面: http://localhost:5001"
echo "- API服务: http://localhost:5004"
echo ""
echo "使用方法："
echo "1. 在浏览器中打开 http://localhost:5001 进行搜索"
echo "2. 使用API: curl http://localhost:5004/api/search?query=关键词"
echo "3. 命令行搜索: python3 full_text_search.py --search \"关键词\""
echo ""
echo "测试工具："
echo "- 测试IK分词器: python3 test_ik_analyzer.py"
echo "- 测试全文搜索: python3 test_full_text_search.py"
echo ""
echo "日志文件："
echo "- Web界面日志: /tmp/search_web_ui.log"
echo "- API服务日志: /tmp/kg_api_server.log"