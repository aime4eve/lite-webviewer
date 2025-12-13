#!/usr/bin/env python3
"""
测试全文搜索功能
"""

import sys
import os
sys.path.append('/home/agentic/lite-webviewer/kg-agent')

from full_text_search import FullTextSearchService

def test_full_text_search():
    """测试全文搜索功能"""
    print("创建全文搜索服务...")
    search_service = FullTextSearchService()
    
    # 测试搜索功能
    test_queries = ["认证", "体系", "汇总", "说明"]
    
    for query in test_queries:
        print(f"\n搜索关键词: '{query}'")
        print("-" * 50)
        
        results = search_service.search(query, size=5)
        
        if not results:
            print(f"没有找到与 '{query}' 相关的文档")
        else:
            print(f"找到 {len(results)} 个相关文档:")
            for i, result in enumerate(results, 1):
                print(f"\n{i}. {result['title']}")
                print(f"   路径: {result['file_path']}")
                print(f"   相关度: {result['score']:.2f}")
                
                if 'highlights' in result:
                    print("   相关片段:")
                    for highlight in result['highlights'][:2]:  # 只显示前2个片段
                        print(f"   - {highlight.replace('...', '...').replace('<em>', '「').replace('</em>', '」')}")

if __name__ == "__main__":
    test_full_text_search()