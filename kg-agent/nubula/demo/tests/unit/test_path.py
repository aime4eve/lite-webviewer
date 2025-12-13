from nebula3.gclient.net import ConnectionPool
from nebula3.Config import Config
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

config = Config()
config.max_connection_pool_size = 10
pool = ConnectionPool()

if pool.init([('localhost', 9669)], config):
    with pool.session_context('root', 'nebula') as session:
        session.execute('USE kg_agent_space;')
        
        # Define source and destination (using known entities from previous output)
        src_id = "人工智能"
        dst_id = "自然语言处理" # or "机器学习"
        
        # Test FIND SHORTEST PATH
        query = f'FIND SHORTEST PATH FROM "{src_id}" TO "{dst_id}" OVER * YIELD path AS p'
        print(f"Executing: {query}")
        resp = session.execute(query)
        
        if resp.is_succeeded():
            print(f"Success! Found {len(resp.rows() or [])} rows")
            rows = resp.rows() or []
            if rows:
                row = rows[0]
                val = row.values[0] # This should be the Value wrapper
                print(f"Value type: {type(val)}")
                
                # Check if it's a path
                if hasattr(val, 'get_pVal'):
                    path = val.get_pVal()
                    print(f"Path object: {type(path)}")
                    print(f"Path attributes: {dir(path)}")
                    
                    # Inspect nodes
                    if hasattr(path, 'nodes'):
                        print(f"path.nodes type: {type(path.nodes)}")
                        nodes = path.nodes
                        if nodes:
                            node = nodes[0]
                            print(f"Node object: {type(node)}")
                            print(f"Node attributes: {dir(node)}")
                            
                            # Check for properties/id
                            if hasattr(node, 'get_id'):
                                print(f"node.get_id(): {node.get_id()}") # Likely returns Value
                            if hasattr(node, 'vid'):
                                print(f"node.vid: {node.vid}")
                                
                            # Check tags/properties
                            if hasattr(node, 'tags'):
                                print(f"node.tags: {node.tags}")
                                if node.tags:
                                    tag = node.tags[0]
                                    print(f"tag.props: {tag.props}")

                    # Inspect steps/relationships if nodes are not populated as expected
                    if hasattr(path, 'steps'):
                         print(f"path.steps: {path.steps}")

        else:
            print(f"Failed: {resp.error_msg()}")
