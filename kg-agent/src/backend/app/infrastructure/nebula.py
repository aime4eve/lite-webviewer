from nebula3.gclient.net import ConnectionPool
from nebula3.Config import Config
from app.config import get_settings
from app.utils.logger import logger
from typing import List, Dict, Any

settings = get_settings()

class NebulaClient:
    _instance = None

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(NebulaClient, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        self.config = Config()
        self.config.max_connection_pool_size = 10
        self.pool = ConnectionPool()
        self._connect()
        self._init_schema()
        self.space_name = settings.NEBULA_SPACE

    def _connect(self):
        try:
            if not self.pool.init([(settings.NEBULA_HOST, settings.NEBULA_PORT)], self.config):
                logger.error("Failed to initialize Nebula connection pool")
            else:
                logger.info(f"Connected to NebulaGraph at {settings.NEBULA_HOST}:{settings.NEBULA_PORT}")
        except Exception as e:
            logger.error(f"Nebula connection error: {e}")

    def _execute(self, query: str):
        with self.pool.session_context(settings.NEBULA_USER, settings.NEBULA_PASSWORD) as session:
            resp = session.execute(query)
            if not resp.is_succeeded():
                logger.error(f"Nebula query failed: {resp.error_msg()}")
                raise Exception(resp.error_msg())
            return resp

    def _init_schema(self):
        """Initialize Space, Tags and EdgeTypes"""
        try:
            # Create Space
            self._execute(f"CREATE SPACE IF NOT EXISTS {settings.NEBULA_SPACE} (partition_num=10, replica_factor=1, vid_type=FIXED_STRING(256));")
            
            # Switch Space
            use_space_query = f"USE {settings.NEBULA_SPACE};"
            
            # Define Schema
            schema_queries = [
                "CREATE TAG IF NOT EXISTS entity(name string, type string);",
                "CREATE EDGE IF NOT EXISTS relationship(relation string);",
                "CREATE TAG IF NOT EXISTS Document(filename string, type string);",
                "CREATE TAG IF NOT EXISTS Chunk(index int);",
                "CREATE EDGE IF NOT EXISTS HAS_CHUNK();", # Document -> Chunk
                "CREATE TAG INDEX IF NOT EXISTS doc_index ON Document(filename(20));"
            ]
            
            # Execute schema queries
            for query in schema_queries:
                try:
                    self._execute(query)
                except Exception as e:
                    logger.warning(f"Schema query failed (might already exist): {query}, error: {e}")
            
            logger.info("Nebula Schema initialization completed")
            
        except Exception as e:
            logger.error(f"Nebula schema init failed: {e}")

    def insert_structure(self, doc_id: str, chunks: List[Dict[str, Any]]):
        """
        Insert Document -> Chunk structure
        """
        try:
            with self.pool.session_context(settings.NEBULA_USER, settings.NEBULA_PASSWORD) as session:
                session.execute(f"USE {settings.NEBULA_SPACE};")
                
                # Insert Document Vertex
                # VERTEX Document VALUES "doc_id":("filename", "type")
                # TODO: Pass filename/type
                session.execute(f'INSERT VERTEX Document(filename, type) VALUES "{doc_id}":("unknown", "txt");')
                
                # Insert Chunk Vertices and Edges
                # INSERT VERTEX Chunk(index) VALUES "chunk_id":(0)
                # INSERT EDGE HAS_CHUNK() VALUES "doc_id"->"chunk_id":()
                
                for chunk in chunks:
                    chunk_id = f"{doc_id}_c{chunk['index']}"
                    session.execute(f'INSERT VERTEX Chunk(index) VALUES "{chunk_id}":({chunk["index"]});')
                    session.execute(f'INSERT EDGE HAS_CHUNK() VALUES "{doc_id}"->"{chunk_id}":();')
                    
            logger.info(f"Inserted graph structure for {doc_id}")
        except Exception as e:
            logger.error(f"Failed to insert graph structure: {e}")
    
    def query_entities(self, keywords: List[str], depth: int = 2, limit: int = 50):
        """
        Query entities and their relationships based on keywords
        """
        try:
            # 使用USE语句切换到正确的空间
            self._execute(f"USE {settings.NEBULA_SPACE};")
            
            # 构建关键词条件
            keyword_conditions = []
            for keyword in keywords:
                keyword_conditions.append(f'entity.name CONTAINS "{keyword}"')
            
            where_clause = " OR ".join(keyword_conditions) if keyword_conditions else "true"
            
            # 构建查询语句 - 使用GO语句而不是MATCH
            nql = f'LOOKUP ON entity WHERE {where_clause} YIELD vertex AS v;'
            
            result = self._execute(nql)
            
            # 处理结果
            nodes = {}
            edges = []
            
            if result.is_succeeded():
                for row in result:
                    # 处理节点
                    if row[0]:
                        node_id = row[0].get_id()
                        if node_id not in nodes:
                            props = row[0].get_properties()
                            nodes[node_id] = {
                                "id": str(node_id),
                                "name": props.get("name", ""),
                                "type": props.get("type", "")
                            }
                            
                            # 查询相关节点和边
                            for d in range(1, depth + 1):
                                # 查询出边
                                go_out_query = f'GO FROM "{node_id}" OVER relationship YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation LIMIT {limit};'
                                go_out_result = self._execute(go_out_query)
                                
                                if go_out_result.is_succeeded():
                                    for go_row in go_out_result:
                                        src_id = str(go_row[0])
                                        dst_id = str(go_row[1])
                                        relation = go_row[2] if go_row[2] else "RELATED_TO"
                                        
                                        # 添加边
                                        edges.append({
                                            "source": src_id,
                                            "target": dst_id,
                                            "type": relation
                                        })
                                        
                                        # 添加目标节点（如果不存在）
                                        if dst_id not in nodes:
                                            fetch_query = f'FETCH PROP ON entity "{dst_id}" YIELD properties(vertex) AS props;'
                                            fetch_result = self._execute(fetch_query)
                                            
                                            if fetch_result.is_succeeded() and len(fetch_result) > 0:
                                                props = fetch_result[0][0]
                                                nodes[dst_id] = {
                                                    "id": dst_id,
                                                    "name": props.get("name", ""),
                                                    "type": props.get("type", "")
                                                }
                                
                                # 查询入边
                                go_in_query = f'GO FROM "{node_id}" OVER relationship REVERSELY YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation LIMIT {limit};'
                                go_in_result = self._execute(go_in_query)
                                
                                if go_in_result.is_succeeded():
                                    for go_row in go_in_result:
                                        src_id = str(go_row[0])
                                        dst_id = str(go_row[1])
                                        relation = go_row[2] if go_row[2] else "RELATED_TO"
                                        
                                        # 添加边
                                        edges.append({
                                            "source": src_id,
                                            "target": dst_id,
                                            "type": relation
                                        })
                                        
                                        # 添加源节点（如果不存在）
                                        if src_id not in nodes:
                                            fetch_query = f'FETCH PROP ON entity "{src_id}" YIELD properties(vertex) AS props;'
                                            fetch_result = self._execute(fetch_query)
                                            
                                            if fetch_result.is_succeeded() and len(fetch_result) > 0:
                                                props = fetch_result[0][0]
                                                nodes[src_id] = {
                                                    "id": src_id,
                                                    "name": props.get("name", ""),
                                                    "type": props.get("type", "")
                                                }
            
            return {
                "nodes": list(nodes.values()),
                "edges": edges
            }
            
        except Exception as e:
            logger.error(f"Entity query failed: {e}")
            return {"nodes": [], "edges": []}

nebula_client = NebulaClient()
