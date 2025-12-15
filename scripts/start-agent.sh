#!/bin/bash
# Nexus-Lite KG-Agent Start Script
APP_HOME=$(dirname $(readlink -f $0))
# Assuming this script is in bin/, and kg-agent is in ../kg-agent
AGENT_DIR="$APP_HOME/../kg-agent"

if [ ! -d "$AGENT_DIR" ]; then
    echo "Error: KG-Agent directory not found at $AGENT_DIR"
    exit 1
fi

cd "$AGENT_DIR"

# Ensure logs directory exists
if [ ! -d "../logs" ]; then
    mkdir -p "../logs"
fi

echo "Starting KG-Agent..."

AGENT_PORT=5000

# Function to check and kill process on port
check_and_kill_port() {
    local port=$1
    echo "Checking port $port..."
    
    # Try using lsof
    if command -v lsof >/dev/null 2>&1; then
        pid=$(lsof -t -i:$port)
    # Try using netstat
    elif command -v netstat >/dev/null 2>&1; then
        pid=$(netstat -nlp | grep ":$port " | awk '{print $7}' | cut -d'/' -f1)
    fi

    if [ -n "$pid" ]; then
        echo "Port $port is occupied by PID $pid. Killing..."
        kill -9 $pid
        sleep 2
    fi
}

check_and_kill_port $AGENT_PORT

# Activate virtual environment if it exists
if [ -f "venv/bin/activate" ]; then
    source venv/bin/activate
fi

# Check if main.py exists
if [ ! -f "src/backend/main.py" ]; then
    echo "Error: src/backend/main.py not found in $AGENT_DIR"
    exit 1
fi

nohup python3 src/backend/main.py > ../logs/agent.log 2>&1 &
echo "Agent started. PID: $!"
echo "Logs: ../logs/agent.log"
