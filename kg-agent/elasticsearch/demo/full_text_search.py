#!/usr/bin/env python3
"""
全文检索服务
用于索引和搜索 /home/hkt/cold-kg/test 目录中的文件
"""

import os
import sys
import json
import argparse
import subprocess
from pathlib import Path
from elasticsearch import Elasticsearch
from elasticsearch.exceptions import ConnectionError, RequestError
import mimetypes
import chardet

class FullTextSearchService:
    def __init__(self, elasticsearch_url="http://localhost:9200", index_name="documents"):
        """初始化全文检索服务"""
        self.es = Elasticsearch(elasticsearch_url)
        self.index_name = index_name
        # self.data_dir = "/home/hkt/cold-kg/test"
        self.data_dir  = "/home/hkt/cold-kg/档案管理"
        
        # 创建索引（如果不存在）
        self._create_index_if_not_exists()
    
    def _create_index_if_not_exists(self):
        """创建索引（如果不存在）"""
        try:
            if not self.es.indices.exists(index=self.index_name):
                # 创建索引，配置IK中文分词器
                index_mapping = {
                    "settings": {
                        "analysis": {
                            "analyzer": {
                                "ik_smart_analyzer": {
                                    "type": "ik_smart"
                                },
                                "ik_max_word_analyzer": {
                                    "type": "ik_max_word"
                                },
                                "my_analyzer": {
                                    "type": "custom",
                                    "tokenizer": "ik_max_word",
                                    "filter": ["lowercase", "stop"]
                                }
                            }
                        }
                    },
                    "mappings": {
                        "properties": {
                            "title": {
                                "type": "text",
                                "analyzer": "ik_max_word_analyzer",
                                "search_analyzer": "ik_smart_analyzer",
                                "fields": {
                                    "keyword": {
                                        "type": "keyword"
                                    }
                                }
                            },
                            "content": {
                                "type": "text",
                                "analyzer": "ik_max_word_analyzer",
                                "search_analyzer": "ik_smart_analyzer"
                            },
                            "file_path": {
                                "type": "keyword"
                            },
                            "file_type": {
                                "type": "keyword"
                            },
                            "file_size": {
                                "type": "long"
                            },
                            "last_modified": {
                                "type": "date"
                            }
                        }
                    }
                }
                
                # 尝试创建带IK分词器的索引
                try:
                    self.es.indices.create(index=self.index_name, body=index_mapping)
                    print(f"索引 '{self.index_name}' 创建成功，使用IK中文分词器")
                except Exception as ik_error:
                    print(f"使用IK分词器创建索引失败: {ik_error}")
                    print("尝试使用标准分词器创建索引...")
                    # 回退到标准分词器
                    fallback_mapping = {
                        "mappings": {
                            "properties": {
                                "title": {
                                    "type": "text",
                                    "fields": {
                                        "keyword": {
                                            "type": "keyword"
                                        }
                                    }
                                },
                                "content": {
                                    "type": "text"
                                },
                                "file_path": {
                                    "type": "keyword"
                                },
                                "file_type": {
                                    "type": "keyword"
                                },
                                "file_size": {
                                    "type": "long"
                                },
                                "last_modified": {
                                    "type": "date"
                                }
                            }
                        }
                    }
                    self.es.indices.create(index=self.index_name, body=fallback_mapping)
                    print(f"索引 '{self.index_name}' 创建成功，使用标准分词器")
            else:
                print(f"索引 '{self.index_name}' 已存在")
        except Exception as e:
            print(f"创建索引时出错: {e}")
            sys.exit(1)
    
    def _detect_file_encoding(self, file_path):
        """检测文件编码"""
        with open(file_path, 'rb') as f:
            raw_data = f.read()
            result = chardet.detect(raw_data)
            return result['encoding']
    
    def _read_file_content(self, file_path):
        """读取文件内容并检测编码"""
        try:
            # 使用 sudo cat 命令读取文件
            result = subprocess.run(
                f"sudo cat \"{file_path}\"",
                shell=True,
                capture_output=True,
                text=True
            )
            
            if result.returncode != 0:
                print(f"无法读取文件 {file_path}: {result.stderr}")
                return None
                
            content = result.stdout
            
            # 检测编码
            try:
                raw_data = content.encode('utf-8')
                encoding = chardet.detect(raw_data)['encoding'] or 'utf-8'
                if encoding != 'utf-8':
                    content = raw_data.decode(encoding, errors='replace')
            except:
                pass
                
            return content
        except Exception as e:
            print(f"读取文件 {file_path} 时出错: {str(e)}")
            return None
    
    def _is_text_file(self, file_path):
        """判断是否为文本文件或支持索引的文件"""
        # 总是索引PDF、DOC、DOCX、JPG、PNG等常见文件类型，即使不提取内容也要索引文件名
        supported_extensions = [
            # 文本文件
            '.txt', '.md', '.py', '.js', '.html', '.css', '.json', '.xml', '.csv', '.log',
            # 办公文档
            '.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx',
            # 图片
            '.jpg', '.jpeg', '.png', '.gif', '.bmp'
        ]
        
        if any(file_path.lower().endswith(ext) for ext in supported_extensions):
            return True
            
        mime_type, _ = mimetypes.guess_type(file_path)
        if mime_type and mime_type.startswith('text/'):
            return True
            
        return False
    
    def index_files(self):
        """索引目录中的所有文本文件"""
        # 使用 sg hkt 命令列出文件
        # 注意：由于 /home/hkt/cold-kg/档案管理 权限为 700，属于 hkt1334 用户
        # 即使切换到 hkt 组也可能无法访问，需要使用 sudo
        result = subprocess.run(
            f"sudo find \"{self.data_dir}\" -type f",
            shell=True,
            capture_output=True,
            text=True
        )
        
        if result.returncode != 0:
            print(f"无法列出目录 {self.data_dir} 中的文件: {result.stderr}")
            return
            
        file_paths = result.stdout.strip().split('\n')
        indexed_count = 0
        
        for file_path in file_paths:
            if not file_path.strip():
                continue
                
            if self._is_text_file(file_path):
                print(f"索引文件: {file_path}")
                
                # 尝试读取内容，对于二进制文件可能无法读取文本内容
                content = None
                is_binary = False
                
                # 检查是否为二进制文件扩展名
                binary_extensions = ['.pdf', '.doc', '.docx', '.xls', '.xlsx', '.ppt', '.pptx', '.jpg', '.jpeg', '.png', '.gif', '.bmp']
                if any(file_path.lower().endswith(ext) for ext in binary_extensions):
                    is_binary = True
                    content = ""  # 二进制文件暂不提取内容，只索引文件名
                else:
                    content = self._read_file_content(file_path)
                
                if content is not None:
                    # 使用 sg hkt 命令获取文件信息
                    stat_result = subprocess.run(
                        f"sg hkt -c 'stat \"{file_path}\"'",
                        shell=True,
                        capture_output=True,
                        text=True
                    )
                    
                    file_size = 0
                    last_modified = 0
                    
                    if stat_result.returncode == 0:
                        # 解析 stat 输出获取文件大小和修改时间
                        for line in stat_result.stdout.split('\n'):
                            if 'Size:' in line:
                                parts = line.split()
                                if len(parts) >= 2:
                                    file_size = int(parts[1])
                            elif 'Modify:' in line:
                                parts = line.split()
                                if len(parts) >= 2:
                                    # 获取日期部分并转换为时间戳
                                    date_str = ' '.join(parts[1:3])
                                    try:
                                        import datetime
                                        dt = datetime.datetime.strptime(date_str, '%Y-%m-%d %H:%M:%S')
                                        last_modified = dt.timestamp()
                                    except:
                                        pass
                    
                    # 准备文档
                    doc = {
                        "title": os.path.basename(file_path),
                        "content": content,
                        "file_path": file_path,
                        "file_type": os.path.splitext(file_path)[1],
                        "file_size": file_size,
                        "last_modified": last_modified
                    }
                    
                    # 索引文档
                    self.es.index(index=self.index_name, body=doc)
                    indexed_count += 1
                else:
                    print(f"无法读取文件内容: {file_path}")
            else:
                print(f"跳过非文本文件: {file_path}")
                
        print(f"索引完成，共索引 {indexed_count} 个文件")
    
    def search(self, query, size=10, file_types=None, sort_by="score"):
        """
        搜索文档
        :param query: 搜索关键词
        :param size: 返回结果数量
        :param file_types: 文件类型列表，如 ['.txt', '.pdf']
        :param sort_by: 排序方式，可选 values: 'score', 'date_desc', 'date_asc', 'size_desc', 'size_asc', 'name_len'
        """
        try:
            # 构建基础查询
            must_clauses = []
            
            # 文件类型过滤
            if file_types:
                must_clauses.append({
                    "terms": {
                        "file_type": file_types
                    }
                })

            # 构建主查询
            should_clauses = []
            
            # 1. 文件名检索 (支持通配符和模糊匹配)
            if '*' in query or '?' in query:
                # 通配符搜索
                should_clauses.append({
                    "wildcard": {
                        "title.keyword": {
                            "value": query,
                            "boost": 5.0
                        }
                    }
                })
            else:
                # 模糊搜索和精确匹配
                should_clauses.extend([
                    # 文件名精确匹配
                    {
                        "match_phrase": {
                            "title": {
                                "query": query,
                                "boost": 10.0
                            }
                        }
                    },
                    # 文件名包含匹配 (支持部分匹配)
                    {
                        "wildcard": {
                            "title.keyword": {
                                "value": f"*{query}*",
                                "boost": 5.0
                            }
                        }
                    },
                    # 模糊匹配
                    {
                        "fuzzy": {
                            "title.keyword": {
                                "value": query,
                                "fuzziness": "AUTO",
                                "boost": 3.0
                            }
                        }
                    }
                ])

            # 2. 文件内容检索 (全文索引，TF-IDF/BM25)
            should_clauses.extend([
                # 内容短语匹配
                {
                    "match_phrase": {
                        "content": {
                            "query": query,
                            "boost": 2.0
                        }
                    }
                },
                # 内容多关键词匹配
                {
                    "match": {
                        "content": {
                            "query": query,
                            "operator": "or",
                            "boost": 1.0
                        }
                    }
                }
            ])
            
            # 组合查询
            search_query = {
                "bool": {
                    "must": must_clauses,
                    "should": should_clauses,
                    "minimum_should_match": 1
                }
            }
            
            # 排序设置
            sort_config = []
            if sort_by == "date_desc":
                sort_config.append({"last_modified": {"order": "desc"}})
            elif sort_by == "date_asc":
                sort_config.append({"last_modified": {"order": "asc"}})
            elif sort_by == "size_desc":
                sort_config.append({"file_size": {"order": "desc"}})
            elif sort_by == "size_asc":
                sort_config.append({"file_size": {"order": "asc"}})
            elif sort_by == "name_len":
                # Elasticsearch 脚本排序：按文件名长度
                sort_config.append({
                    "_script": {
                        "type": "number",
                        "script": {
                            "lang": "painless",
                            "source": "doc['title.keyword'].value.length()"
                        },
                        "order": "asc"
                    }
                })
            
            # 始终包含相关度得分排序作为次级排序
            sort_config.append({"_score": {"order": "desc"}})

            # 执行搜索
            response = self.es.search(
                index=self.index_name,
                body={
                    "query": search_query,
                    "sort": sort_config,
                    "highlight": {
                        "pre_tags": ["<em>"],
                        "post_tags": ["</em>"],
                        "fields": {
                            "title": {"number_of_fragments": 0},
                            "content": {
                                "fragment_size": 150,
                                "number_of_fragments": 3
                            }
                        }
                    }
                },
                size=size
            )
            
            # 处理结果
            results = []
            for hit in response['hits']['hits']:
                source = hit['_source']
                score = hit['_score']
                
                # 获取高亮内容
                highlights = []
                if 'highlight' in hit:
                    if 'title' in hit['highlight']:
                        highlights.extend(hit['highlight']['title'])
                    if 'content' in hit['highlight']:
                        highlights.extend(hit['highlight']['content'])
                
                results.append({
                    "title": source.get('title'),
                    "file_path": source.get('file_path'),
                    "file_type": source.get('file_type'),
                    "file_size": source.get('file_size'),
                    "last_modified": source.get('last_modified'),
                    "score": score,
                    "highlights": highlights
                })
                
            return results
            
        except Exception as e:
            print(f"搜索出错: {e}")
            return []
    
    def print_search_results(self, results, query):
        """打印搜索结果"""
        if not results:
            print(f"没有找到与 '{query}' 相关的文档")
            return
        
        print(f"\n找到 {len(results)} 个与 '{query}' 相关的文档:")
        print("=" * 80)
        
        for i, result in enumerate(results, 1):
            print(f"\n{i}. {result['title']}")
            print(f"   路径: {result['file_path']}")
            print(f"   类型: {result['file_type']}")
            print(f"   相关度: {result['score']:.2f}")
            
            if 'highlights' in result:
                print("   相关片段:")
                for highlight in result['highlights'][:3]:  # 只显示前3个片段
                    print(f"   - {highlight.replace('...', '...').replace('<em>', '「').replace('</em>', '」')}")
        
        print("\n" + "=" * 80)


def main():
    parser = argparse.ArgumentParser(description='全文检索服务')
    parser.add_argument('--index', action='store_true', help='索引文件')
    parser.add_argument('--search', type=str, help='搜索关键词')
    parser.add_argument('--size', type=int, default=10, help='返回结果数量')
    
    args = parser.parse_args()
    
    # 创建全文检索服务
    search_service = FullTextSearchService()
    
    if args.index:
        # 索引文件
        search_service.index_files()
    elif args.search:
        # 搜索文件
        results = search_service.search(args.search, args.size)
        search_service.print_search_results(results, args.search)
    else:
        # 默认搜索"认证"
        results = search_service.search("认证", args.size)
        search_service.print_search_results(results, "认证")


if __name__ == "__main__":
    main()