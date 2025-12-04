#!/bin/bash

# Nexus-Lite 知识预览系统 - 多功能脚本
# 支持开发环境一键启动和完整打包两种模式
# 
# 使用方式:
#   ./start-dev.sh                    # 启动开发环境
#   ./start-dev.sh --build 或 -b      # 完整打包项目
#   ./start-dev.sh --build --run-tests # 打包并运行测试

set -e

echo "========================================"
echo "Nexus-Lite 知识预览系统多功能脚本"
echo "========================================"

# 检查是否使用打包模式
if [ "$1" == "--build" ] || [ "$1" == "-b" ]; then
    BUILD_MODE=true
    echo "运行打包模式..."
else
    BUILD_MODE=false
    echo "运行开发模式..."
fi

# 检查必要的命令
check_command() {
    if ! command -v $1 &> /dev/null; then
        echo "错误: 未找到命令 $1，请先安装。"
        exit 1
    fi
}

check_command npm
check_command mvn
check_command java
check_command docker

echo "正在启动开发环境..."

# 创建临时文件存储进程ID
temp_dir=$(mktemp -d 2>/dev/null || echo "/tmp/nexus-lite-dev-$(date +%s)")
mkdir -p "$temp_dir"
frontend_pid_file="$temp_dir/frontend.pid"
backend_pid_file="$temp_dir/backend.pid"
es_container_name="es814"
#docker run -d --name elasticsearch -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" e29a1f876ffd
    
# 清理函数，用于停止服务和清理临时文件
cleanup() {
    echo "\n正在清理资源..."
    
    # 停止前端服务
    if [ -f "$frontend_pid_file" ]; then
        frontend_pid=$(cat "$frontend_pid_file")
        echo "停止前端开发服务器..."
        kill -15 $frontend_pid 2>/dev/null || true
        wait $frontend_pid 2>/dev/null || true
    fi
    
    # 停止后端服务
    if [ -f "$backend_pid_file" ]; then
        backend_pid=$(cat "$backend_pid_file")
        echo "停止后端 Spring Boot 应用..."
        kill -15 $backend_pid 2>/dev/null || true
        wait $backend_pid 2>/dev/null || true
    fi
    
    # 停止ES服务
    echo "停止 $es_container_name 容器..."
    docker stop $es_container_name 2>/dev/null || true
    
    # 清理临时文件
    rm -rf "$temp_dir"
    
    echo "清理完成！"
    echo "========================================"
}

# 注册清理函数，当脚本被中断时执行（仅在开发模式下）
if [ "$BUILD_MODE" = false ]; then
    trap cleanup INT TERM EXIT
fi

# 启动前端开发服务器
start_frontend() {
    echo "\n启动前端开发服务器..."
    
    # 创建临时文件来存储前端端口
    frontend_port_file="$temp_dir/frontend_port"
    
    ( 
        cd frontend
        # 检查是否需要安装依赖
        if [ ! -d "node_modules" ]; then
            echo "前端依赖未安装，正在安装..."
            npm install
        fi
        echo "正在启动 React 开发服务器 (Vite)..."
        # 使用Vite的自动端口选择功能
        npm run dev
    ) > >(tee -a frontend.log) 2>&1 &
    
    frontend_pid=$!
    echo $frontend_pid > "$frontend_pid_file"
    echo "前端服务已启动，PID: $frontend_pid"
    echo "前端日志: frontend.log"
    
    # 等待前端启动并获取实际端口
    echo "等待前端服务器启动..."
    sleep 5
    
    # 从日志中提取实际使用的端口
    if grep -q "Local:" frontend.log 2>/dev/null; then
        frontend_port=$(grep "Local:" frontend.log | head -1 | grep -oE ':[0-9]+' | cut -d: -f2)
        if [ -n "$frontend_port" ]; then
            echo "前端实际端口: $frontend_port"
            echo $frontend_port > "$frontend_port_file"
        else
            echo "警告: 无法获取前端端口，使用默认端口5173"
            echo "5173" > "$frontend_port_file"
        fi
    else
        echo "警告: 无法获取前端端口信息，使用默认端口5173"
        echo "5173" > "$frontend_port_file"
    fi
}

# 启动 Elasticsearch 容器
start_elasticsearch() {
    echo "\n启动 $es_container_name 容器..."
    # 检查容器是否存在
    if docker ps -a | grep -q "$es_container_name"; then
        echo "检测到已存在的 $es_container_name 容器，正在启动..."
        docker start $es_container_name
    else
        echo "未找到 $es_container_name 容器，正在尝试查找并启动..."
        # 尝试查找其他可能的ES容器
        es_container=$(docker ps -a | grep elasticsearch | head -1 | awk '{print $1}')
        if [ -n "$es_container" ]; then
            echo "找到 Elasticsearch 容器: $es_container，正在启动..."
            docker start $es_container
            es_container_name=$es_container
        else
            echo "警告: 未找到 $es_container_name 容器，请确保已使用正确的镜像创建容器"
            echo "提示: 可以使用以下命令创建ES容器:"
            echo "docker run -d --name $es_container_name -p 9200:9200 -p 9300:9300 -e 'discovery.type=single-node' docker.elastic.co/elasticsearch/elasticsearch:8.14.0"
        fi
    fi
    
    # 等待ES服务启动
    echo "等待 Elasticsearch 服务启动..."
    for i in {1..30}; do
        if curl -s http://localhost:9200 > /dev/null; then
            echo "Elasticsearch 服务已成功启动！"
            break
        fi
        if [ $i -eq 30 ]; then
            echo "警告: Elasticsearch 服务启动超时，请检查容器状态"
        else
            echo "Elasticsearch 服务正在启动中... ($i/30)"
            sleep 2
        fi
    done
}

# 清理被占用的端口
cleanup_ports() {
    echo "正在清理被占用的端口..."
    
    # 智能端口清理策略：优先使用非sudo方法，必要时请求sudo
    local can_sudo=false
    
    # 检查是否有sudo权限
    if sudo -n true 2>/dev/null; then
        can_sudo=true
        echo "检测到sudo权限，将进行深度端口清理"
    else
        echo "警告: 没有sudo权限，将进行基础端口清理（可能无法清理所有占用进程）"
    fi
    
    # 基础清理：使用非sudo方法清理已知的常见服务进程
    echo "基础端口清理..."
    
    # 清理已知的Java进程（Spring Boot应用）
    pkill -f "java.*server.port" 2>/dev/null || true
    
    # 清理Node.js/Vite进程
    pkill -f "vite" 2>/dev/null || true
    pkill -f "node.*5173" 2>/dev/null || true
    
    # 清理已知的bundle进程（Git相关）
    pkill -f "bundle" 2>/dev/null || true
    
    # 如果有sudo权限，进行深度清理
    if [ "$can_sudo" = true ]; then
        echo "深度端口清理..."
        
        # 清理后端常用端口范围 (8080-8090)
        for port in {8090..8099}; do
            if sudo lsof -i :$port > /dev/null 2>&1; then
                echo "清理端口 $port 上的进程..."
                # 只杀死LISTEN状态的进程，避免杀死非服务进程
                sudo lsof -i :$port | grep LISTEN | awk '{print $2}' | xargs -r sudo kill -9 2>/dev/null || true
            fi
        done
        
        # 清理前端常用端口范围 (5173-5200)
        for port in {5173..5200}; do
            if sudo lsof -i :$port > /dev/null 2>&1; then
                echo "清理端口 $port 上的进程..."
                # 只杀死LISTEN状态的进程，避免杀死非服务进程
                sudo lsof -i :$port | grep LISTEN | awk '{print $2}' | xargs -r sudo kill -9 2>/dev/null || true
            fi
        done
    else
        echo "跳过深度端口清理（需要sudo权限）"
    fi
    
    echo "端口清理完成！"
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if sudo lsof -i :$port > /dev/null 2>&1; then
        echo "端口 $port 已被占用，尝试使用备用端口..."
        return 1  # 端口被占用，返回1
    fi
    return 0  # 端口可用，返回0
}

# 获取可用端口
find_available_port() {
    local base_port=$1
    local max_attempts=20
    
    for attempt in $(seq 1 $max_attempts); do
        local test_port=$((base_port + attempt - 1))
        if check_port $test_port; then
            echo $test_port
            return 0
        fi
    done
    
    echo "错误: 在端口范围 $base_port-$((base_port + max_attempts - 1)) 内找不到可用端口"
    exit 1
}

# 完整打包功能
build_project() {
    echo "\n========================================"
    echo "开始完整打包 Nexus-Lite 知识预览系统"
    echo "========================================"
    
    # 1. 编译 React 前端
    echo "\n1. 编译 React 前端..."
    cd frontend
    # 检查是否需要安装依赖
    if [ ! -d "node_modules" ]; then
        echo "前端依赖未安装，正在安装..."
        npm install
    fi
    npm run build
    cd ..
    
    # 2. 复制前端构建文件到 Spring Boot 静态目录
    echo "\n2. 复制前端构建文件..."
    rm -rf backend/src/main/resources/static/*
    cp -r frontend/dist/* backend/src/main/resources/static/
    
    # 3. 使用 Maven 打包 Spring Boot 应用
    echo "\n3. 打包 Spring Boot 应用..."
    cd backend
    mvn clean package -DskipTests
    cd ..
    
    # 4. 运行测试套件（可选，默认跳过）
    if [ "$2" == "--run-tests" ]; then
        echo "\n4. 运行测试套件..."
        cd backend
        mvn test
        cd ..
    fi
    
    echo "\n========================================"
    echo "打包完成！"
    echo "最终 JAR 文件：backend/target/nexus-lite-1.0.0-SNAPSHOT.jar"
    echo "\n运行命令："
    echo "  java -jar backend/target/nexus-lite-1.0.0-SNAPSHOT.jar"
    echo "\n带自定义配置运行："
    echo "  java -jar backend/target/nexus-lite-1.0.0-SNAPSHOT.jar --app.scan.root-dirs=/custom/path"
    echo "========================================"
}

# 启动后端 Spring Boot 应用
start_backend() {
    echo "\n启动后端 Spring Boot 应用..."
    
    # 使用智能端口选择，避免冲突
    local backend_port=$(find_available_port 8090)
    echo "使用端口: $backend_port"
    
    ( 
        echo "正在启动 Spring Boot 应用，端口: $backend_port..."
        cd backend
        # 先打包生成jar文件，然后使用java命令启动Spring Boot应用
        mvn clean package -DskipTests
        java -Dserver.port=$backend_port -jar target/nexus-lite-1.0.0-SNAPSHOT.jar
    ) > >(tee -a backend.log) 2>&1 &
    
    backend_pid=$!
    echo $backend_pid > "$backend_pid_file"
    echo "后端服务已启动，PID: $backend_pid，端口: $backend_port"
    echo "后端日志: backend.log"
    
    # 返回后端端口号
    echo $backend_port > "$temp_dir/backend_port"
}

# 主逻辑
if [ "$BUILD_MODE" = true ]; then
    # 打包模式
    build_project "$@"
else
    # 开发模式 - 先清理端口，再启动服务
    cleanup_ports
    start_elasticsearch
    start_frontend
    start_backend
    
    # 读取后端端口号
    backend_port=$(cat "$temp_dir/backend_port" 2>/dev/null || echo "8080")
    # 读取前端端口号
    frontend_port=$(cat "$temp_dir/frontend_port" 2>/dev/null || echo "5173")
    
    echo "\n========================================"
    echo "开发环境服务已全部启动！"
    echo "前端开发服务器: http://localhost:$frontend_port"
    echo "后端 Spring Boot 应用: http://localhost:$backend_port"
    echo "Elasticsearch 服务: http://localhost:9200"
    echo "\n注意事项:"
    echo "1. 按 Ctrl+C 可以停止所有服务"
    echo "2. 前端日志在: frontend.log"
    echo "3. 后端日志在: backend.log"
    echo "4. Elasticsearch 日志可以通过 'docker logs $es_container_name' 查看"
    echo "========================================"
    
    # 等待任一服务结束
    wait -n
    
    echo "\n检测到服务已退出，正在停止其他服务..."
fi
