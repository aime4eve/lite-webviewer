#!/bin/bash

# - 如果要在 Docker 中运行全套环境：直接运行 ./start_docker.sh 。
# - 如果在 本地调试 代码：先运行 docker-compose up -d nacos smg-postgres ... 启动基础设施，
#   然后运行 ./backend/nacos-config.sh --local 更新配置，最后运行 ./backend/start_all.sh 启动服务。

# Load environment variables
if [ -f .env ]; then
  # Use grep to exclude comments and empty lines
  export $(grep -v '^#' .env | xargs)
fi

# Define ports to check (using env vars with defaults)
PORTS=(
  ${FRONTEND_PORT:-6690}             # Frontend
  ${DEVICE_SERVICE_PORT:-8081}       # Device Service
  ${AI_SERVICE_PORT:-8083}           # AI Service
  ${SUBSCRIPTION_SERVICE_PORT:-8085} # Subscription Service
  ${REPORT_SERVICE_PORT:-8087}       # Report Service
  ${CONTROL_SERVICE_PORT:-8084}      # Control Service
  ${NACOS_PORT:-8848}                # Nacos
  ${NACOS_GRPC_PORT:-9848}           # Nacos gRPC
  ${GATEWAY_SERVICE_PORT:-9999}      # Gateway Service
  ${POSTGRES_PORT:-5432}             # Postgres
  ${REDIS_PORT:-16379}               # Redis
  ${INFLUXDB_PORT:-8086}             # InfluxDB
  ${ZOOKEEPER_PORT:-2181}            # Zookeeper
  ${KAFKA_PORT:-19092}               # Kafka
)

# Function to show help
show_help() {
    echo "Usage: ./start_docker.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  -h, --help       Show this help message"
    echo "  --force          Force kill processes on occupied ports without prompting"
    echo ""
    echo "Description:"
    echo "  This script builds and starts the Docker environment for the SmartMoldGuard project."
    echo "  It automatically checks for occupied ports and stops conflicting processes."
    echo ""
}

# Function to check command existence
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to check and free ports
check_and_free_ports() {
    local force=$1
    echo "Checking for occupied ports..."
    
    for port in "${PORTS[@]}"; do
        if lsof -i :$port >/dev/null 2>&1; then
            echo "⚠️  Port $port is occupied."
            pid=$(lsof -t -i :$port)
            process_name=$(ps -p $pid -o comm=)
            
            if [ "$force" == "true" ]; then
                echo "Force killing process $process_name (PID: $pid) on port $port..."
                kill -9 $pid
            else
                read -p "Do you want to kill process $process_name (PID: $pid) on port $port? (y/N) " -n 1 -r
                echo
                if [[ $REPLY =~ ^[Yy]$ ]]; then
                    kill -9 $pid
                    echo "Process killed."
                else
                    echo "Skipping port $port. Docker start might fail."
                fi
            fi
        fi
    done
}

# Parse arguments
FORCE="false"
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            exit 0
            ;;
        --force)
            FORCE="true"
            shift
            ;;
        *)
            echo "Unknown option: $1"
            show_help
            exit 1
            ;;
    esac
    shift
done

# Stop local Java services
echo "Stopping local backend services..."
pkill -f "uni-app-ui-demo/backend/.*/target/.*.jar" || echo "No local backend services found."

# Stop local Frontend
echo "Stopping local frontend..."
pkill -f "vite" || echo "No local frontend found."

# Check and free ports
check_and_free_ports "$FORCE"

# Start Docker Compose
echo "Building and starting Docker services..."
if command_exists docker-compose; then
    docker-compose up --build -d
    LOG_CMD="docker-compose logs -f"
elif command_exists docker; then
    docker compose up --build -d
    LOG_CMD="docker compose logs -f"
else
    echo "Error: docker-compose or docker compose not found."
    exit 1
fi

# Wait for Nacos to be ready
echo "Waiting for Nacos to start..."
MAX_RETRIES=30
COUNT=0
NACOS_URL="http://localhost:${NACOS_PORT:-8848}/nacos"

while ! curl -s "$NACOS_URL" > /dev/null; do
    sleep 5
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "Error: Nacos failed to start within $((MAX_RETRIES*5)) seconds."
        echo "Please check logs with: $LOG_CMD"
        break
    fi
    echo "Waiting for Nacos... ($COUNT/$MAX_RETRIES)"
done

# Initialize Nacos Configuration
if [ -f ./backend/nacos-config.sh ]; then
    echo "Initializing Nacos configuration..."
    chmod +x ./backend/nacos-config.sh
    ./backend/nacos-config.sh --docker
else
    echo "Warning: ./backend/nacos-config.sh not found. Skipping Nacos configuration."
fi

echo "----------------------------------------------------------------"
echo "✅ All services started in Docker!"
echo "----------------------------------------------------------------"
echo "Frontend: http://localhost:6690"
echo "Device Service: http://localhost:8081"
echo "AI Service: http://localhost:8083"
echo "Subscription Service: http://localhost:8085"
echo "Report Service: http://localhost:${REPORT_SERVICE_PORT:-8087}"
echo "Control Service: http://localhost:${CONTROL_SERVICE_PORT:-8084}"
echo "Nacos Console: http://localhost:${NACOS_PORT:-8848}/nacos/index.html"
echo "----------------------------------------------------------------"
echo "To view logs: $LOG_CMD"
echo ""
echo "NOTE: If accessing from a remote IDE, ensure port ${NACOS_PORT:-8848} is forwarded."
echo "      If 'Connection Refused', try waiting a moment or checking the Ports tab."
