#!/bin/bash

# Define ports to check
PORTS=(
  6690  # Frontend
  8081  # Device Service
  8083  # AI Service
  8085  # Subscription Service
  8087  # Report Service
  8089  # Control Service
  5432  # Postgres
  16379 # Redis
  8086  # InfluxDB
  2181  # Zookeeper
  19092 # Kafka
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

echo "----------------------------------------------------------------"
echo "✅ All services started in Docker!"
echo "----------------------------------------------------------------"
echo "Frontend: http://localhost:6690"
echo "Device Service: http://localhost:8081"
echo "AI Service: http://localhost:8083"
echo "Subscription Service: http://localhost:8085"
echo "Report Service: http://localhost:8087"
echo "Control Service: http://localhost:8089"
echo "----------------------------------------------------------------"
echo "To view logs: $LOG_CMD"
