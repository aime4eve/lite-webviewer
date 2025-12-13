#!/usr/bin/env python3
# -*- coding: utf-8 -*-

"""
Nebula Graph Client Interface
Provides a unified Nebula Graph client interface, automatically selecting real or mock client.
"""

import os
import logging
import json
from typing import Dict, List, Any, Optional, Union

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def is_nebula3_available() -> bool:
    """
    Check if nebula3 package is available
    
    Returns:
        bool: whether nebula3 package is available
    """
    try:
        import nebula3
        logger.info("nebula3 package available, using real Nebula client")
        return True
    except ImportError:
        logger.warning("nebula3 package unavailable, using mock Nebula client")
        return False

# Import appropriate client based on availability
if is_nebula3_available():
    from nebula3.gclient.net import ConnectionPool
    from nebula3.Config import Config
    from nebula3.common import *
    
    class NebulaClient:
        """
        Real Nebula Graph Client
        """
        
        def __init__(self, config: Dict[str, Any] = None):
            """
            Initialize Nebula Client
            
            Args:
                config: Nebula connection configuration
            """
            if config is None:
                config = {}
            
            self.config = config
            self.space_name = config.get("space_name", "kg_agent_space")
            self.hosts = config.get("hosts", [("127.0.0.1", 9669)])
            self.username = config.get("username", "root")
            self.password = config.get("password", "nebula")
            self.pool = None
            self.session = None
            self.connected = False
            logger.info(f"Real Nebula Client initialized, space: {self.space_name}")
            
            # Auto connect
            self.connect()
            self._init_schema()
        
        def connect(self) -> bool:
            """
            Connect to Nebula Graph
            
            Returns:
                bool: success status
            """
            try:
                # Create pool config
                config = Config()
                config.max_connection_pool_size = 10
                # Create pool
                self.pool = ConnectionPool()
                # Init pool
                if not self.pool.init([(host, port) for host, port in self.hosts], config):
                    logger.error("Failed to initialize connection pool")
                    return False
                # Get session
                self.session = self.pool.get_session(self.username, self.password)
                self.connected = True
                logger.info("Nebula Graph connected successfully")
                return True
            except Exception as e:
                logger.error(f"Failed to connect to Nebula Graph: {e}")
                return False
        
        def disconnect(self) -> bool:
            """
            Disconnect from Nebula Graph
            
            Returns:
                bool: success status
            """
            try:
                if self.session:
                    self.session.release()
                if self.pool:
                    self.pool.close()
                self.connected = False
                logger.info("Nebula Graph disconnected")
                return True
            except Exception as e:
                logger.error(f"Failed to disconnect from Nebula Graph: {e}")
                return False
        
        def _execute(self, query: str):
            """Execute query"""
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                raise Exception("Not connected to Nebula Graph")
            
            with self.pool.session_context(self.username, self.password) as session:
                # Auto switch space if needed and not creating/dropping space
                if self.space_name and "CREATE SPACE" not in query.upper() and "DROP SPACE" not in query.upper():
                    try:
                        session.execute(f"USE {self.space_name};")
                    except:
                        pass
                        
                resp = session.execute(query)
                if not resp.is_succeeded():
                    logger.error(f"Nebula query failed: {resp.error_msg()}")
                    # raise Exception(resp.error_msg())
                    pass
                return resp
    
        def init_schema(self, space_name: str = None):
            """Initialize schema for a space"""
            if space_name:
                self.space_name = space_name
            self._init_schema()

        def _init_schema(self):
            """Initialize Space, Tags and EdgeTypes"""
            try:
                # Create Space
                self._execute(f"CREATE SPACE IF NOT EXISTS {self.space_name} (partition_num=10, replica_factor=1, vid_type=FIXED_STRING(256));")
                
                # Switch Space
                self._execute(f"USE {self.space_name};")
                
                # Define Schema
                schema_queries = [
                    "CREATE TAG IF NOT EXISTS entity(name string, type string, description string, properties string);",
                    "CREATE EDGE IF NOT EXISTS relationship(relation string, weight double, description string);",
                    "CREATE TAG IF NOT EXISTS document(title string, content string, url string);",
                    "CREATE TAG IF NOT EXISTS concept(name string, category string, definition string);",
                    "CREATE TAG IF NOT EXISTS person(name string, role string, organization string);",
                    "CREATE TAG IF NOT EXISTS technology(name string, category string, description string);",
                    "CREATE EDGE IF NOT EXISTS contains(strength double);",
                    "CREATE EDGE IF NOT EXISTS belongs_to(confidence double);",
                    "CREATE EDGE IF NOT EXISTS related_to(similarity double);",
                    "CREATE EDGE IF NOT EXISTS mentions(count int);",
                    "CREATE EDGE IF NOT EXISTS authored(`date` string);",
                    "CREATE TAG INDEX IF NOT EXISTS entity_name_index ON entity(name(20));",
                    "CREATE TAG INDEX IF NOT EXISTS entity_index ON entity();"
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
        
        def execute_query(self, query: str) -> Dict[str, Any]:
            """
            Execute Nebula Graph query
            
            Args:
                query: nGQL query string
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Ensure correct space
                self._execute(f"USE {self.space_name};")
                
                # Execute query
                result = self._execute(query)
                if not result.is_succeeded():
                    error_msg = result.error_msg()
                    logger.error(f"Query failed: {error_msg}")
                    return {"success": False, "error": error_msg}
                
                # Process result
                data = []
                columns = []
                
                # Get column names
                if result.col_size() > 0:
                    columns = result.keys()
                
                # Get data
                for record in result:
                    row = []
                    for i in range(record.size()):
                        val_list = record.values()
                        value = val_list[i]
                        if value.is_null():
                            row.append(None)
                        elif value.is_bool():
                            row.append(value.as_bool())
                        elif value.is_int():
                            row.append(value.as_int())
                        elif value.is_double():
                            row.append(value.as_double())
                        elif value.is_string():
                            row.append(value.as_string())
                        elif value.is_datetime():
                            row.append(str(value.as_datetime()))
                        else:
                            row.append(str(value))
                    data.append(row)
                
                return {
                    "success": True,
                    "data": data,
                    "columns": columns,
                    "latency": result.latency()
                }
            except Exception as e:
                logger.error(f"Execute query failed: {e}")
                return {"success": False, "error": str(e)}
        
        def insert_entity(self, entity_id: str, name: str, entity_type: str, description: str = "", properties: str = "") -> bool:
            """
            Insert entity node
            
            Args:
                entity_id: Entity ID
                name: Entity name
                entity_type: Entity type
                description: Description
                properties: Properties
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                query = f'INSERT VERTEX entity(name, type, description, properties) VALUES "{entity_id}":("{name}", "{entity_type}", "{description}", "{properties}");'
                self._execute(query)
                logger.info(f"Inserted entity: {entity_id}")
                return True
            except Exception as e:
                logger.error(f"Insert entity failed {entity_id}: {e}")
                return False
        
        def insert_relationship(self, src_id: str, dst_id: str, relation_type: str, weight: float = 1.0, description: str = "") -> bool:
            """
            Insert relationship edge
            
            Args:
                src_id: Source entity ID
                dst_id: Target entity ID
                relation_type: Relationship type
                weight: Weight
                description: Description
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                query = f'INSERT EDGE relationship(relation, weight, description) VALUES "{src_id}"->"{dst_id}":("{relation_type}", {weight}, "{description}");'
                self._execute(query)
                logger.info(f"Inserted relationship: {src_id} -> {dst_id} ({relation_type})")
                return True
            except Exception as e:
                logger.error(f"Insert relationship failed {src_id} -> {dst_id}: {e}")
                return False
        
        def query_entities(self, keywords: List[str], depth: int = 2, limit: int = 50) -> Dict[str, Any]:
            """
            Query entities and relationships by keywords
            
            Args:
                keywords: List of keywords
                depth: Query depth
                limit: Result limit
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"nodes": [], "edges": []}
            
            try:
                # Ensure correct space
                self._execute(f"USE {self.space_name};")
                
                # Build keyword conditions
                keyword_conditions = []
                for keyword in keywords:
                    # Use == instead of CONTAINS because native index only supports exact match or prefix
                    keyword_conditions.append(f'entity.name == "{keyword}"')
                
                if keyword_conditions:
                    where_clause = "WHERE " + " OR ".join(keyword_conditions)
                else:
                    # If no keywords, scan all entities (requires index)
                    where_clause = ""
                
                # Build query - use LOOKUP instead of MATCH
                # LIMIT might cause syntax error in some versions if placed after YIELD
                nql = f'LOOKUP ON entity {where_clause} YIELD vertex AS v | LIMIT {limit};'
                
                result = self._execute(nql)
                
                # Process result
                nodes = {}
                edges = []
                
                if result.is_succeeded():
                    for row in result:
                        # Process node
                        # row is a Record, use values() method
                        val_list = row.values()
                        if val_list and val_list[0].is_vertex():
                            node = val_list[0].as_node()
                            node_id = node.get_id()
                            if hasattr(node_id, 'is_string') and node_id.is_string():
                                node_id = node_id.as_string()
                            else:
                                node_id = str(node_id)
                                
                            if node_id not in nodes:
                                if callable(node.properties):
                                    props = node.properties()
                                else:
                                    props = node.properties
                                
                                nodes[node_id] = {
                                    "id": str(node_id),
                                    "name": props.get("name", "").as_string() if hasattr(props.get("name", ""), "as_string") else str(props.get("name", "")),
                                    "type": props.get("type", "").as_string() if hasattr(props.get("type", ""), "as_string") else str(props.get("type", "")),
                                    "description": props.get("description", "").as_string() if hasattr(props.get("description", ""), "as_string") else str(props.get("description", "")),
                                    "properties": props.get("properties", "").as_string() if hasattr(props.get("properties", ""), "as_string") else str(props.get("properties", ""))
                                }
                                
                                # Query related nodes and edges
                                for d in range(1, depth + 1):
                                    # Query out edges
                                    go_out_query = f'GO FROM "{node_id}" OVER relationship YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation, properties(edge).weight AS weight | LIMIT {limit};'
                                    go_out_result = self._execute(go_out_query)
                                    
                                    if go_out_result.is_succeeded():
                                        for go_row in go_out_result:
                                            go_vals = go_row.values()
                                            src_id = go_vals[0].as_string() if go_vals[0].is_string() else str(go_vals[0])
                                            dst_id = go_vals[1].as_string() if go_vals[1].is_string() else str(go_vals[1])
                                            relation = go_vals[2].as_string() if not go_vals[2].is_null() else "RELATED_TO"
                                            weight = go_vals[3].as_double() if not go_vals[3].is_null() else 1.0
                                            
                                            # Add edge
                                            edges.append({
                                                "source": src_id,
                                                "target": dst_id,
                                                "type": relation,
                                                "weight": weight
                                            })
                                            
                                            # Add target node (if not exists)
                                            if dst_id not in nodes:
                                                fetch_query = f'FETCH PROP ON entity "{dst_id}" YIELD properties(vertex) AS props;'
                                                fetch_result = self._execute(fetch_query)
                                                
                                                if fetch_result.is_succeeded() and not fetch_result.is_empty():
                                                    # Get first row
                                                    for fetch_row in fetch_result:
                                                        fetch_vals = fetch_row.values()
                                                        if fetch_vals and fetch_vals[0].is_map():
                                                            # props is a dict-like object from ValueWrapper
                                                            # Actually ValueWrapper as_map returns dict of ValueWrappers?
                                                            # Let's assume as_map returns a python dict of values, 
                                                            # but keys are strings and values are ValueWrappers?
                                                            # Wait, props from node.properties is a dict of ValueWrappers usually.
                                                            # Let's handle props safely.
                                                            # In FETCH YIELD properties(vertex), it returns a Map.
                                                            
                                                            # Actually, easier way:
                                                            # ValueWrapper.as_map() returns dict<string, ValueWrapper> ?
                                                            # Or dict<string, primitive>?
                                                            # Usually ValueWrapper returns primitive for simple types.
                                                            # But let's look at debug output: {'name': "...", ...}
                                                            # The values in the dict seem to be strings/primitives already?
                                                            # Wait, debug output showed:
                                                            # First value: {'name': "人工智能", ...}
                                                            # This looks like python dict with string values.
                                                            
                                                            # Let's assume as_map() returns python dict with primitives.
                                                            # But to be safe, we can treat them as primitives.
                                                            
                                                            props_map = fetch_vals[0].as_map()
                                                            nodes[dst_id] = {
                                                                "id": dst_id,
                                                                "name": str(props_map.get("name", "")),
                                                                "type": str(props_map.get("type", "")),
                                                                "description": str(props_map.get("description", "")),
                                                                "properties": str(props_map.get("properties", ""))
                                                            }
                                                        break
                                    
                                    # Query in edges
                                    go_in_query = f'GO FROM "{node_id}" OVER relationship REVERSELY YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation, properties(edge).weight AS weight | LIMIT {limit};'
                                    go_in_result = self._execute(go_in_query)
                                    
                                    if go_in_result.is_succeeded():
                                        for go_row in go_in_result:
                                            go_vals = go_row.values()
                                            src_id = go_vals[0].as_string() if go_vals[0].is_string() else str(go_vals[0])
                                            dst_id = go_vals[1].as_string() if go_vals[1].is_string() else str(go_vals[1])
                                            relation = go_vals[2].as_string() if not go_vals[2].is_null() else "RELATED_TO"
                                            weight = go_vals[3].as_double() if not go_vals[3].is_null() else 1.0
                                            
                                            # Add edge
                                            edges.append({
                                                "source": src_id,
                                                "target": dst_id,
                                                "type": relation,
                                                "weight": weight
                                            })
                                            
                                            # Add source node (if not exists)
                                            if src_id not in nodes:
                                                fetch_query = f'FETCH PROP ON entity "{src_id}" YIELD properties(vertex) AS props;'
                                                fetch_result = self._execute(fetch_query)
                                                
                                                if fetch_result.is_succeeded() and not fetch_result.is_empty():
                                                    for fetch_row in fetch_result:
                                                        fetch_vals = fetch_row.values()
                                                        if fetch_vals and fetch_vals[0].is_map():
                                                            props_map = fetch_vals[0].as_map()
                                                            nodes[src_id] = {
                                                                "id": src_id,
                                                                "name": str(props_map.get("name", "")),
                                                                "type": str(props_map.get("type", "")),
                                                                "description": str(props_map.get("description", "")),
                                                                "properties": str(props_map.get("properties", ""))
                                                            }
                                                        break
                
                return {
                    "nodes": list(nodes.values()),
                    "edges": edges
                }
                
            except Exception as e:
                logger.error(f"Entity query failed: {e}")
                return {"nodes": [], "edges": []}
        
        def insert_vertex(self, tag: str, vid: str, props: Dict[str, Any]) -> bool:
            """
            Insert vertex
            
            Args:
                tag: Tag name
                vid: Vertex ID
                props: Properties dictionary
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                # Build property values
                values = []
                for key, value in props.items():
                    if isinstance(value, str):
                        values.append(f'{key}: "{value}"')
                    elif isinstance(value, bool):
                        values.append(f'{key}: {str(value).lower()}')
                    elif isinstance(value, (int, float)):
                        values.append(f'{key}: {value}')
                    else:
                        values.append(f'{key}: "{str(value)}"')
                
                props_str = "{" + ", ".join(values) + "}"
                query = f'INSERT VERTEX {tag} ({", ".join(props.keys())}) VALUES "{vid}": {props_str}'
                
                # Execute insert
                self._execute(query)
                return True
            except Exception as e:
                logger.error(f"Insert vertex failed: {e}")
                return False
        
        def insert_edge(self, edge_type: str, src: str, dst: str, props: Dict[str, Any]) -> bool:
            """
            Insert edge
            
            Args:
                edge_type: Edge type
                src: Source vertex ID
                dst: Target vertex ID
                props: Properties dictionary
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                # Build property values
                values = []
                for key, value in props.items():
                    if isinstance(value, str):
                        values.append(f'{key}: "{value}"')
                    elif isinstance(value, bool):
                        values.append(f'{key}: {str(value).lower()}')
                    elif isinstance(value, (int, float)):
                        values.append(f'{key}: {value}')
                    else:
                        values.append(f'{key}: "{str(value)}"')
                
                props_str = "{" + ", ".join(values) + "}"
                query = f'INSERT EDGE {edge_type} ({", ".join(props.keys())}) VALUES "{src}"->"{dst}": {props_str}'
                
                # Execute insert
                self._execute(query)
                return True
            except Exception as e:
                logger.error(f"Insert edge failed: {e}")
                return False
        
        def get_space_info(self) -> Dict[str, Any]:
            """
            Get space info
            
            Returns:
                Dict: Space info
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Execute query
                query = f"DESC SPACE {self.space_name}"
                result = self.execute_query(query)
                
                if not result["success"]:
                    return result
                
                # Parse result
                data = result["data"]
                if not data:
                    return {"success": False, "error": "Space not found"}
                
                # Build space info
                space_info = {"name": self.space_name}
                for row in data:
                    field = row[0]
                    value = row[1]
                    space_info[field] = value
                
                return {"success": True, "data": space_info}
            except Exception as e:
                logger.error(f"Get space info failed: {e}")
                return {"success": False, "error": str(e)}
        
        def get_tags(self) -> Dict[str, Any]:
            """
            Get tags list
            
            Returns:
                Dict: Tags list
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Execute query
                query = "SHOW TAGS"
                result = self.execute_query(query)
                
                if not result["success"]:
                    return result
                
                # Parse result
                data = result["data"]
                tags = []
                
                for row in data:
                    tag_name = row[0]
                    # Get tag fields
                    fields_query = f"DESC TAG {tag_name}"
                    fields_result = self.execute_query(fields_query)
                    
                    fields = []
                    if fields_result["success"]:
                        for field_row in fields_result["data"]:
                            fields.append(field_row[0])  # Field name
                    
                    tags.append({"name": tag_name, "fields": fields})
                
                return {"success": True, "data": tags}
            except Exception as e:
                logger.error(f"Get tags list failed: {e}")
                return {"success": False, "error": str(e)}
        
        def get_edge_types(self) -> Dict[str, Any]:
            """
            Get edge types list
            
            Returns:
                Dict: Edge types list
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Execute query
                query = "SHOW EDGES"
                result = self.execute_query(query)
                
                if not result["success"]:
                    return result
                
                # Parse result
                data = result["data"]
                edge_types = []
                
                for row in data:
                    edge_name = row[0]
                    # Get edge fields
                    fields_query = f"DESC EDGE {edge_name}"
                    fields_result = self.execute_query(fields_query)
                    
                    fields = []
                    if fields_result["success"]:
                        for field_row in fields_result["data"]:
                            fields.append(field_row[0])  # Field name
                    
                    edge_types.append({"name": edge_name, "fields": fields})
                
                return {"success": True, "data": edge_types}
            except Exception as e:
                logger.error(f"Get edge types list failed: {e}")
                return {"success": False, "error": str(e)}
        
        def query_vertices(self, tag: str, props: Dict[str, Any] = None, limit: int = 100) -> Dict[str, Any]:
            """
            Query vertices
            
            Args:
                tag: Tag name
                props: Property filter
                limit: Limit
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Build query
                where_clause = ""
                if props:
                    conditions = []
                    for key, value in props.items():
                        if isinstance(value, str):
                            conditions.append(f'{key} == "{value}"')
                        else:
                            conditions.append(f'{key} == {value}')
                    where_clause = " WHERE " + " AND ".join(conditions)
                
                query = f'FETCH PROP ON {tag} *{where_clause} LIMIT {limit}'
                
                # Execute query
                result = self.execute_query(query)
                return result
            except Exception as e:
                logger.error(f"Query vertices failed: {e}")
                return {"success": False, "error": str(e)}
        
        def query_edges(self, edge_type: str, props: Dict[str, Any] = None, limit: int = 100) -> Dict[str, Any]:
            """
            Query edges
            
            Args:
                edge_type: Edge type
                props: Property filter
                limit: Limit
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Build query
                where_clause = ""
                if props:
                    conditions = []
                    for key, value in props.items():
                        if isinstance(value, str):
                            conditions.append(f'{key} == "{value}"')
                        else:
                            conditions.append(f'{key} == {value}')
                    where_clause = " WHERE " + " AND ".join(conditions)
                
                query = f'FETCH PROP ON {edge_type} *{where_clause} LIMIT {limit}'
                
                # Execute query
                result = self.execute_query(query)
                return result
            except Exception as e:
                logger.error(f"Query edges failed: {e}")
                return {"success": False, "error": str(e)}
else:
    # Use mock client if nebula3 not available
    class NebulaClient:
        """
        Mock Nebula Graph Client
        Provides basic functionality in environments where nebula3 package is unavailable
        """
        
        def __init__(self, config: Dict[str, Any] = None):
            """
            Initialize Nebula Client
            
            Args:
                config: Nebula connection configuration
            """
            if config is None:
                config = {}
                
            self.config = config
            self.space_name = config.get("space_name", "kg_agent_space")
            self.connected = False
            logger.info(f"Mock Nebula Client initialized, space: {self.space_name}")
            
            # Auto connect
            self.connect()
        
        def connect(self) -> bool:
            """
            Connect to Nebula Graph
            
            Returns:
                bool: success status
            """
            try:
                # Mock connection process
                self.connected = True
                logger.info("Mock Nebula Graph connected successfully")
                return True
            except Exception as e:
                logger.error(f"Failed to connect to Nebula Graph: {e}")
                return False
        
        def disconnect(self) -> bool:
            """
            Disconnect from Nebula Graph
            
            Returns:
                bool: success status
            """
            try:
                self.connected = False
                logger.info("Mock Nebula Graph disconnected")
                return True
            except Exception as e:
                logger.error(f"Failed to disconnect from Nebula Graph: {e}")
                return False
        
        def execute_query(self, query: str) -> Dict[str, Any]:
            """
            Execute Nebula Graph query
            
            Args:
                query: nGQL query string
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock query execution
                logger.info(f"Mock execute query: {query}")
                
                # Return mock result based on query type
                if "SHOW SPACES" in query:
                    return {
                        "success": True,
                        "data": [{"Name": self.space_name}],
                        "columns": ["Name"]
                    }
                elif "USE" in query:
                    return {"success": True}
                elif "FETCH PROP ON" in query and "entity" in query:
                    # Mock entity query result
                    return {
                        "success": True,
                        "data": [
                            ["人工智能", "概念", "模拟人类智能的技术", "{\"category\": \"技术\", \"importance\": \"high\"}"],
                            ["机器学习", "技术", "使计算机能够学习的技术", "{\"category\": \"技术\", \"importance\": \"high\"}"],
                            ["深度学习", "技术", "基于神经网络的学习方法", "{\"category\": \"技术\", \"importance\": \"medium\"}"],
                            ["神经网络", "技术", "模拟生物神经网络的计算模型", "{\"category\": \"技术\", \"importance\": \"medium\"}"],
                            ["自然语言处理", "技术", "使计算机能够理解人类语言的技术", "{\"category\": \"技术\", \"importance\": \"medium\"}"]
                        ],
                        "columns": ["name", "type", "description", "properties"]
                    }
                elif "FETCH PROP ON" in query and "relationship" in query:
                    # Mock relationship query result
                    return {
                        "success": True,
                        "data": [
                            ["人工智能", "机器学习", "包含", 0.9, "人工智能包含机器学习"],
                            ["机器学习", "深度学习", "包含", 0.8, "机器学习包含深度学习"],
                            ["深度学习", "神经网络", "基于", 0.9, "深度学习基于神经网络"],
                            ["人工智能", "自然语言处理", "包含", 0.7, "人工智能包含自然语言处理"],
                            ["机器学习", "自然语言处理", "应用", 0.6, "机器学习应用于自然语言处理"]
                        ],
                        "columns": ["src", "dst", "relation", "weight", "description"]
                    }
                elif "GO FROM" in query and "OVER relationship" in query:
                    # Mock path query result
                    return {
                        "success": True,
                        "data": [
                            ["机器学习"],
                            ["自然语言处理"]
                        ],
                        "columns": ["dst"]
                    }
                else:
                    # Default empty result
                    return {
                        "success": True,
                        "data": [],
                        "columns": []
                    }
            except Exception as e:
                logger.error(f"Execute query failed: {e}")
                return {"success": False, "error": str(e)}
        
        def insert_vertex(self, tag: str, vid: str, props: Dict[str, Any]) -> bool:
            """
            Insert vertex
            
            Args:
                tag: Tag name
                vid: Vertex ID
                props: Properties dictionary
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                # Mock insert vertex
                logger.info(f"Mock insert vertex: {tag} {vid} {props}")
                return True
            except Exception as e:
                logger.error(f"Insert vertex failed: {e}")
                return False
        
        def insert_edge(self, edge_type: str, src: str, dst: str, props: Dict[str, Any]) -> bool:
            """
            Insert edge
            
            Args:
                edge_type: Edge type
                src: Source vertex ID
                dst: Target vertex ID
                props: Properties dictionary
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                # Mock insert edge
                logger.info(f"Mock insert edge: {src} -> {dst} ({edge_type}) {props}")
                return True
            except Exception as e:
                logger.error(f"Insert edge failed: {e}")
                return False
        
        def get_space_info(self) -> Dict[str, Any]:
            """
            Get space info
            
            Returns:
                Dict: Space info
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock get space info
                return {
                    "success": True,
                    "data": {
                        "name": self.space_name,
                        "partition_num": 10,
                        "replica_factor": 1,
                        "vid_type": "FIXED_STRING(256)"
                    }
                }
            except Exception as e:
                logger.error(f"Get space info failed: {e}")
                return {"success": False, "error": str(e)}
        
        def get_tags(self) -> Dict[str, Any]:
            """
            Get tags list
            
            Returns:
                Dict: Tags list
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock get tags list
                return {
                    "success": True,
                    "data": [
                        {"name": "entity", "fields": ["name", "type", "description", "properties"]},
                        {"name": "document", "fields": ["title", "content", "url"]},
                        {"name": "concept", "fields": ["name", "category", "definition"]},
                        {"name": "person", "fields": ["name", "role", "organization"]},
                        {"name": "technology", "fields": ["name", "category", "description"]}
                    ]
                }
            except Exception as e:
                logger.error(f"Get tags list failed: {e}")
                return {"success": False, "error": str(e)}
        
        def get_edge_types(self) -> Dict[str, Any]:
            """
            Get edge types list
            
            Returns:
                Dict: Edge types list
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock get edge types list
                return {
                    "success": True,
                    "data": [
                        {"name": "relationship", "fields": ["relation", "weight", "description"]},
                        {"name": "contains", "fields": ["strength"]},
                        {"name": "belongs_to", "fields": ["confidence"]},
                        {"name": "related_to", "fields": ["similarity"]},
                        {"name": "mentions", "fields": ["count"]},
                        {"name": "authored", "fields": ["date"]}
                    ]
                }
            except Exception as e:
                logger.error(f"Get edge types list failed: {e}")
                return {"success": False, "error": str(e)}
        
        def query_entities(self, keywords: List[str], depth: int = 2, limit: int = 50) -> Dict[str, Any]:
            """
            Query entities
            
            Args:
                keywords: Keywords list
                depth: Query depth
                limit: Limit
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock query entities
                logger.info(f"Mock query entities: {keywords} depth={depth} limit={limit}")
                
                # Mock query result
                entities = []
                relationships = []
                
                # Match entities by keyword
                for keyword in keywords:
                    if "机器学习" in keyword:
                        entities.extend([
                            {"id": "ml_001", "name": "机器学习", "type": "技术", "description": "使计算机能够学习的技术", "properties": {"category": "技术", "importance": "high"}},
                            {"id": "dl_001", "name": "深度学习", "type": "技术", "description": "基于神经网络的学习方法", "properties": {"category": "技术", "importance": "medium"}},
                            {"id": "nn_001", "name": "神经网络", "type": "技术", "description": "模拟生物神经网络的计算模型", "properties": {"category": "技术", "importance": "medium"}}
                        ])
                        
                        relationships.extend([
                            {"src": "ml_001", "dst": "dl_001", "relation": "包含", "weight": 0.8, "description": "机器学习包含深度学习"},
                            {"src": "dl_001", "dst": "nn_001", "relation": "基于", "weight": 0.9, "description": "深度学习基于神经网络"}
                        ])
                    elif "人工智能" in keyword:
                        entities.extend([
                            {"id": "ai_001", "name": "人工智能", "type": "概念", "description": "模拟人类智能的技术", "properties": {"category": "概念", "importance": "high"}},
                            {"id": "ml_001", "name": "机器学习", "type": "技术", "description": "使计算机能够学习的技术", "properties": {"category": "技术", "importance": "high"}},
                            {"id": "nlp_001", "name": "自然语言处理", "type": "技术", "description": "使计算机能够理解人类语言的技术", "properties": {"category": "技术", "importance": "medium"}}
                        ])
                        
                        relationships.extend([
                            {"src": "ai_001", "dst": "ml_001", "relation": "包含", "weight": 0.9, "description": "人工智能包含机器学习"},
                            {"src": "ai_001", "dst": "nlp_001", "relation": "包含", "weight": 0.7, "description": "人工智能包含自然语言处理"}
                        ])
                    elif "测试实体" in keyword:
                        entities.append({"id": "test_001", "name": "测试实体", "type": "概念", "description": "这是一个测试实体", "properties": {"category": "测试", "importance": "high"}})
                        relationships.append({"src": "test_001", "dst": "ml_001", "relation": "related_to", "weight": 0.8, "description": "测试实体与机器学习相关"})
                
                return {
                    "success": True,
                    "data": {
                        "entities": entities[:limit],
                        "relationships": relationships[:limit]
                    }
                }
            except Exception as e:
                logger.error(f"Query entities failed: {e}")
                return {"success": False, "error": str(e)}
        
        def insert_entity(self, entity_id: str, name: str, entity_type: str, 
                         description: str = "", properties: str = "") -> bool:
            """
            Insert entity
            
            Args:
                entity_id: Entity ID
                name: Entity name
                entity_type: Entity type
                description: Description
                properties: Properties
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                # Mock insert entity
                logger.info(f"Mock insert entity: {entity_id} {name} {entity_type}")
                return True
            except Exception as e:
                logger.error(f"Insert entity failed: {e}")
                return False
        
        def insert_relationship(self, src_id: str, dst_id: str, relation_type: str, 
                               weight: float = 1.0, description: str = "") -> bool:
            """
            Insert relationship
            
            Args:
                src_id: Source entity ID
                dst_id: Target entity ID
                relation_type: Relationship type
                weight: Weight
                description: Description
                
            Returns:
                bool: Success status
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return False
            
            try:
                # Mock insert relationship
                logger.info(f"Mock insert relationship: {src_id} -> {dst_id} ({relation_type})")
                return True
            except Exception as e:
                logger.error(f"Insert relationship failed: {e}")
                return False
        
        def query_edges(self, edge_type: str, props: Dict[str, Any] = None, limit: int = 100) -> Dict[str, Any]:
            """
            Query edges
            
            Args:
                edge_type: Edge type
                props: Property filter
                limit: Limit
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock query edges
                logger.info(f"Mock query edges: {edge_type} {props} limit={limit}")
                
                # Return mock result based on edge type
                if edge_type == "relationship":
                    return {
                        "success": True,
                        "data": [
                            ["人工智能", "机器学习", "包含", 0.9, "人工智能包含机器学习"],
                            ["机器学习", "深度学习", "包含", 0.8, "机器学习包含深度学习"],
                            ["深度学习", "神经网络", "基于", 0.9, "深度学习基于神经网络"],
                            ["人工智能", "自然语言处理", "包含", 0.7, "人工智能包含自然语言处理"],
                            ["机器学习", "自然语言处理", "应用", 0.6, "机器学习应用于自然语言处理"]
                        ],
                        "columns": ["src", "dst", "relation", "weight", "description"]
                    }
                else:
                    return {
                        "success": True,
                        "data": [],
                        "columns": []
                    }
            except Exception as e:
                logger.error(f"Query edges failed: {e}")
                return {"success": False, "error": str(e)}

        def query_vertices(self, tag: str, props: Dict[str, Any] = None, limit: int = 100) -> Dict[str, Any]:
            """
            Query vertices
            
            Args:
                tag: Tag name
                props: Property filter
                limit: Limit
                
            Returns:
                Dict: Query result
            """
            if not self.connected:
                logger.error("Not connected to Nebula Graph")
                return {"success": False, "error": "Not connected to Nebula Graph"}
            
            try:
                # Mock query vertices
                logger.info(f"Mock query vertices: {tag} {props} limit={limit}")
                
                # Return mock result based on tag
                if tag == "entity":
                    return {
                        "success": True,
                        "data": [
                            ["人工智能", "概念", "模拟人类智能的技术", "{\"category\": \"技术\", \"importance\": \"high\"}"],
                            ["机器学习", "技术", "使计算机能够学习的技术", "{\"category\": \"技术\", \"importance\": \"high\"}"],
                            ["深度学习", "技术", "基于神经网络的学习方法", "{\"category\": \"技术\", \"importance\": \"medium\"}"],
                            ["神经网络", "技术", "模拟生物神经网络的计算模型", "{\"category\": \"技术\", \"importance\": \"medium\"}"],
                            ["自然语言处理", "技术", "使计算机能够理解人类语言的技术", "{\"category\": \"技术\", \"importance\": \"medium\"}"]
                        ],
                        "columns": ["name", "type", "description", "properties"]
                    }
                else:
                    return {
                        "success": True,
                        "data": [],
                        "columns": []
                    }
            except Exception as e:
                logger.error(f"Query vertices failed: {e}")
                return {"success": False, "error": str(e)}


# Create global client instance
nebula_client = None

def init_nebula_client(config: Dict[str, Any] = None) -> NebulaClient:
    """
    Initialize Nebula Client
    
    Args:
        config: Nebula connection configuration
        
    Returns:
        NebulaClient: Client instance
    """
    global nebula_client
    nebula_client = NebulaClient(config)
    return nebula_client

def get_nebula_client() -> NebulaClient:
    """
    Get Nebula Client instance
    
    Returns:
        NebulaClient: Client instance
    """
    global nebula_client
    if nebula_client is None:
        nebula_client = NebulaClient()
    return nebula_client

# Create default instance
nebula_client = NebulaClient()