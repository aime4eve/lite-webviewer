#!/bin/bash

# Function to start a service
start_service() {
    service_name=$1
    port=$2
    echo "Starting $service_name on port $port..."
    nohup java -Dserver.port=$port -jar $service_name/target/$service_name-0.0.1-SNAPSHOT.jar > logs/$service_name.log 2>&1 &
    echo "$service_name pid: $!"
}

mkdir -p logs

# Start Services
start_service "device-service" 8081
start_service "control-service" 8089
start_service "ai-service" 8083
start_service "subscription-service" 8084
start_service "report-service" 8085

echo "All services started. Check logs/ directory for output."
