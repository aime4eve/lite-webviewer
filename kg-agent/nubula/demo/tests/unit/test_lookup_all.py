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
        
        # Test GO query for "人工智能"
        vid = "人工智能"
        # query = f'GO FROM "{vid}" OVER relationship YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation, properties(edge).weight AS weight LIMIT 10;'
        query = f'GO FROM "{vid}" OVER relationship YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation, properties(edge).weight AS weight;'
        print(f"Executing: {query}")
        resp = session.execute(query)
        if resp.is_succeeded():
            print(f"GO Success! Found {len(resp.rows() or [])} rows")
            for row in (resp.rows() or []):
                print(row.values)
        else:
            print(f"GO Failed: {resp.error_msg()}")

        # Test REVERSELY with pipe LIMIT
        query = f'GO FROM "{vid}" OVER relationship REVERSELY YIELD src(edge) AS src, dst(edge) AS dst, properties(edge).relation AS relation, properties(edge).weight AS weight | LIMIT 10;'
        print(f"Executing: {query}")
        resp = session.execute(query)
        if resp.is_succeeded():
            print(f"GO REVERSELY Success! Found {len(resp.rows() or [])} rows")
            for row in (resp.rows() or []):
                print(row.values)
        else:
            print(f"GO REVERSELY Failed: {resp.error_msg()}")
