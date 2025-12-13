from nebula3.gclient.net import ConnectionPool
from nebula3.Config import Config

config = Config()
pool = ConnectionPool()
if not pool.init([('localhost', 9669)], config):
    print("Failed to connect")
    exit(1)

with pool.session_context('root', 'nebula') as session:
    session.execute('USE kg_agent_space')
    
    # Test CONTAINS
    query = 'LOOKUP ON entity WHERE entity.name CONTAINS "智能" YIELD vertex AS v'
    print(f"Executing: {query}")
    resp = session.execute(query)
    if resp.is_succeeded():
        print(f"Success! Found {len(resp.rows() or [])} rows")
        for row in (resp.rows() or []):
            print(row)
    else:
        print(f"Failed: {resp.error_msg()}")

    # Test PREFIX (which should definitely work with index)
    query = 'LOOKUP ON entity WHERE entity.name STARTS WITH "人工" YIELD vertex AS v'
    print(f"Executing: {query}")
    resp = session.execute(query)
    if resp.is_succeeded():
        print(f"Success! Found {len(resp.rows() or [])} rows")
    else:
        print(f"Failed: {resp.error_msg()}")
