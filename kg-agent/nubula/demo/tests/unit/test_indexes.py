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
        
        query = 'SHOW TAG INDEXES;'
        print(f"Executing: {query}")
        resp = session.execute(query)
        if resp.is_succeeded():
            print("Indexes:")
            for row in (resp.rows() or []):
                print(row.values)
        else:
            print(f"Failed: {resp.error_msg()}")

        query = 'SHOW TAGS;'
        print(f"Executing: {query}")
        resp = session.execute(query)
        if resp.is_succeeded():
            print("Tags:")
            for row in (resp.rows() or []):
                print(row.values)
        else:
             print(f"Failed: {resp.error_msg()}")
