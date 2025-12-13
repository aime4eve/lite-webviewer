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
        query = 'USE kg_agent_space; LOOKUP ON entity YIELD vertex AS v LIMIT 10;'
        print(f"Executing: {query}")
        resp = session.execute(query)
        
        if resp.is_succeeded():
            print(f"Success! Found {len(resp.rows() or [])} rows")
        else:
            print(f"Failed: {resp.error_msg()}")
