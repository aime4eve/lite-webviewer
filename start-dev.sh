#!/bin/bash

# Nexus-Lite 知识预览系统 - 开发环境一键启动脚本
# 用于同时启动前端 React 开发服务器和后端 Spring Boot 应用

set -e

echo "========================================"
echo "Nexus-Lite 知识预览系统开发环境启动脚本"
echo "========================================"

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

echo "正在启动开发环境..."

# 创建临时文件存储进程ID
temp_dir=$(mktemp -d)
frontend_pid_file="$temp_dir/frontend.pid"
backend_pid_file="$temp_dir/backend.pid"

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
    
    # 清理临时文件
    rm -rf "$temp_dir"
    
    echo "清理完成！"
    echo "========================================"
}

# 注册清理函数，当脚本被中断时执行
trap cleanup INT TERM EXIT

# 启动前端开发服务器
start_frontend() {
    echo "\n启动前端开发服务器..."
    ( 
        cd frontend
        # 检查是否需要安装依赖
        if [ ! -d "node_modules" ]; then
            echo "前端依赖未安装，正在安装..."
            npm install
        fi
        echo "正在启动 React 开发服务器 (Vite)..."
        npm run dev
    ) > >(tee -a frontend.log) 2>&1 &
    
    frontend_pid=$!
    echo $frontend_pid > "$frontend_pid_file"
    echo "前端服务已启动，PID: $frontend_pid"
    echo "前端日志: frontend.log"
}

# 启动后端 Spring Boot 应用
start_backend() {
    echo "\n启动后端 Spring Boot 应用..."
    ( 
        echo "正在启动 Spring Boot 应用..."
        mvn spring-boot:run
    ) > >(tee -a backend.log) 2>&1 &
    
    backend_pid=$!
    echo $backend_pid > "$backend_pid_file"
    echo "后端服务已启动，PID: $backend_pid"
    echo "后端日志: backend.log"
}

# 启动服务
start_frontend
start_backend

echo "\n========================================"
echo "开发环境服务已全部启动！"
echo "前端开发服务器: http://localhost:5173"
echo "后端 Spring Boot 应用: http://localhost:8080"
echo "\n注意事项:"
echo "1. 按 Ctrl+C 可以停止所有服务"
echo "2. 前端日志在: frontend.log"
echo "3. 后端日志在: backend.log"
echo "========================================"

# 等待任一服务结束
wait -n

echo "\n检测到服务已退出，正在停止其他服务..."
