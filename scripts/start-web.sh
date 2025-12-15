#!/bin/bash
# Nexus-Lite Web Service Start Script
APP_HOME=$(dirname $(readlink -f $0))
cd "$APP_HOME"

SERVER_PORT=8090

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

check_and_kill_port $SERVER_PORT

# Ensure logs directory exists
if [ ! -d "../logs" ]; then
    mkdir -p "../logs"
fi

# Check if jar exists
if [ ! -f "nexus-lite.jar" ]; then
    echo "Error: nexus-lite.jar not found in $APP_HOME"
    exit 1
fi

echo "Starting Nexus-Lite Web Service..."
nohup java -jar nexus-lite.jar > ../logs/web.log 2>&1 &
echo "Service started. PID: $!"
echo "Logs: ../logs/web.log"
