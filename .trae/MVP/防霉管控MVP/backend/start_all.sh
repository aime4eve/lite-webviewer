#!/bin/bash

# Load environment variables
if [ -f ../.env ]; then
  export $(grep -v '^#' ../.env | xargs)
fi

# Function to start a service
start_service() {
    service_name=$1
    port=$2
    echo "Starting $service_name on port $port..."
    nohup java -Dserver.port=$port -jar $service_name/target/$service_name-0.0.1-SNAPSHOT.jar > logs/$service_name.log 2>&1 &
    echo "$service_name pid: $!"
}

mkdir -p logs

# Start Services with correct ports from .env
start_service "device-service" ${DEVICE_SERVICE_PORT:-8081}
start_service "control-service" ${CONTROL_SERVICE_PORT:-8084}
start_service "ai-service" ${AI_SERVICE_PORT:-8083}
start_service "subscription-service" ${SUBSCRIPTION_SERVICE_PORT:-8085}
start_service "report-service" ${REPORT_SERVICE_PORT:-8087}

echo "All services started. Check logs/ directory for output."
