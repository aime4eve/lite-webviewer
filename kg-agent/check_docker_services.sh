#!/bin/bash

# Docker服务测试脚本
# 作者：伍志勇
# 用于测试kg-agent项目中的所有Docker服务是否正常运行
# Docker服务测试脚本使用说明
# ## 概述

# `check_docker_services.sh` 是一个用于测试kg-agent项目中所有Docker服务是否正常运行的Shell脚本。

# ## 功能

# 该脚本会执行以下测试：

# 1. **基础环境检查**
#    - 检查Docker服务是否运行
#    - 检查docker-compose.yml文件是否存在

# 2. **容器状态检查**
#    - 检查所有容器是否正在运行
#    - 检查容器健康状态（如果有健康检查）

# 3. **服务连接性测试**
#    - Redis服务测试
#    - Elasticsearch HTTP服务测试
#    - etcd HTTP服务测试
#    - MinIO API HTTP服务测试
#    - MinIO Console HTTP服务测试
#    - Milvus TCP端口测试
#    - Nebula Graph TCP端口测试
#    - Nebula Meta TCP端口测试
#    - Nebula Storage TCP端口测试

# 4. **数据目录检查**
#    - 检查主数据目录是否存在
#    - 检查各服务数据目录是否存在

# ## 使用方法

# 1. 确保脚本具有执行权限：
#    ```bash
#    chmod +x test_docker_services.sh
#    ```

# 2. 运行脚本：
#    ```bash
#    ./test_docker_services.sh
#    ```

# ## 输出说明

# 脚本会输出详细的测试过程，包括：

# - 测试进度信息（黄色）
# - 成功信息（绿色）
# - 错误信息（红色）

# 最后会显示测试结果汇总，包括：
# - 总测试数
# - 通过测试数
# - 失败测试数

# 如果所有测试通过，脚本会以退出码0退出；如果有测试失败，会以退出码1退出。

# ## 故障排除

# 如果测试失败，可以：

# 1. 检查相关容器是否正在运行：
#    ```bash
#    docker-compose ps
#    ```

# 2. 查看容器日志：
#    ```bash
#    docker-compose logs [服务名]
#    ```

# 3. 重启服务：
#    ```bash
#    docker-compose restart [服务名]
#    ```

# 4. 如果Nebula Graph测试失败，可能需要单独启动该服务：
#    ```bash
#    docker-compose up -d nebula-graphd
#    ```

# ## 注意事项

# - 脚本需要在包含docker-compose.yml文件的目录中运行
# - 需要具有执行Docker命令的权限
# - 某些服务可能需要一段时间才能完全启动，如果测试失败，可以稍后再次运行脚本
# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 日志函数
log_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
    ((PASSED_TESTS++))
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    ((FAILED_TESTS++))
}

# 测试函数
# 测试函数
test_service() {
    local service_name=$1
    local test_command=$2
    local expected_result=$3
    
    log_info "测试 $service_name 服务..."
    
    if eval "$test_command" | grep -q "$expected_result"; then
        log_success "$service_name 服务正常"
        ((TOTAL_TESTS++))
        return 0
    else
        log_error "$service_name 服务异常"
        ((TOTAL_TESTS++))
        return 1
    fi
}

# 测试HTTP服务
test_http_service() {
    local service_name=$1
    local url=$2
    local expected_code=${3:-200}
    
    log_info "测试 $service_name HTTP服务 ($url)..."
    
    local status_code=$(curl -s -o /dev/null -w "%{http_code}" "$url" || echo "000")
    
    if [ "$status_code" = "$expected_code" ]; then
        log_success "$service_name HTTP服务正常 (状态码: $status_code)"
        ((TOTAL_TESTS++))
        return 0
    else
        log_error "$service_name HTTP服务异常 (状态码: $status_code)"
        ((TOTAL_TESTS++))
        return 1
    fi
}

# 测试TCP端口
test_tcp_port() {
    local service_name=$1
    local host=$2
    local port=$3
    
    log_info "测试 $service_name TCP端口 ($host:$port)..."
    
    if timeout 3 bash -c "</dev/tcp/$host/$port" 2>/dev/null; then
        log_success "$service_name TCP端口正常 ($host:$port)"
        ((TOTAL_TESTS++))
        return 0
    else
        log_error "$service_name TCP端口异常 ($host:$port)"
        ((TOTAL_TESTS++))
        return 1
    fi
}

# 主函数
main() {
    echo "=========================================="
    echo "Docker服务测试脚本"
    echo "测试时间: $(date)"
    echo "=========================================="
    
    # 检查Docker是否运行
    if ! docker info > /dev/null 2>&1; then
        log_error "Docker服务未运行"
        exit 1
    fi
    log_success "Docker服务运行正常"
    ((TOTAL_TESTS++))
    
    # 检查Docker Compose文件是否存在
    if [ ! -f "docker-compose.yml" ]; then
        log_error "docker-compose.yml 文件不存在"
        exit 1
    fi
    log_success "找到docker-compose.yml文件"
    ((TOTAL_TESTS++))
    
    # 检查容器状态
    echo ""
    echo "=========================================="
    echo "检查容器状态"
    echo "=========================================="
    
    # 获取所有容器状态
    containers=$(docker-compose ps -q)
    if [ -z "$containers" ]; then
        log_error "没有找到运行的容器"
        exit 1
    fi
    
    # 检查每个容器状态
    while IFS= read -r container; do
        if [ -n "$container" ]; then
            name=$(docker inspect -f '{{.Name}}' "$container" | sed 's/\///')
            status=$(docker inspect -f '{{.State.Status}}' "$container")
            health=$(docker inspect -f '{{.State.Health.Status}}' "$container" 2>/dev/null)
            
            if [ "$status" = "running" ]; then
                if [ "$health" = "healthy" ] || [ -z "$health" ]; then
                    log_success "容器 $name 运行正常 ($status)"
                else
                    log_error "容器 $name 状态异常 ($status, 健康检查: $health)"
                fi
            else
                log_error "容器 $name 未运行 ($status)"
            fi
            ((TOTAL_TESTS++))
        fi
    done <<< "$containers"
    
    # 测试各个服务
    echo ""
    echo "=========================================="
    echo "测试服务连接性"
    echo "=========================================="
    
    # Redis测试
    test_service "Redis" "docker exec kg-agent-redis-1 redis-cli ping" "PONG"
    
    # Elasticsearch测试
    test_http_service "Elasticsearch" "http://localhost:9200"
    
    # etcd测试
    test_http_service "etcd" "http://localhost:2379/health"
    
    # MinIO API测试
    test_http_service "MinIO API" "http://localhost:9000/minio/health/live"
    
    # MinIO控制台测试
    test_http_service "MinIO Console" "http://localhost:9001"
    
    # Milvus测试
    test_tcp_port "Milvus" "localhost" "19530"
    
    # Nebula Graph测试 - 增加等待时间
    log_info "测试 Nebula Graph TCP端口 (localhost:9669)..."
    if timeout 5 bash -c "</dev/tcp/localhost/9669" 2>/dev/null; then
        log_success "Nebula Graph TCP端口正常 (localhost:9669)"
    else
        log_error "Nebula Graph TCP端口异常 (localhost:9669)"
    fi
    ((TOTAL_TESTS++))
    
    test_tcp_port "Nebula Meta" "localhost" "9559"
    test_tcp_port "Nebula Storage" "localhost" "9779"
    
    # 数据目录检查
    echo ""
    echo "=========================================="
    echo "检查数据目录"
    echo "=========================================="
    
    data_dir="/data/kg-agent-docker-data"
    if [ -d "$data_dir" ]; then
        log_success "数据目录存在: $data_dir"
        ((TOTAL_TESTS++))
        
        # 检查各服务数据目录
        services=("redis" "elasticsearch" "etcd" "minio" "milvus" "nebula-graphd" "nebula-metad" "nebula-storaged")
        for service in "${services[@]}"; do
            if [ -d "$data_dir/$service" ]; then
                log_success "服务数据目录存在: $service"
            else
                log_error "服务数据目录不存在: $service"
            fi
            ((TOTAL_TESTS++))
        done
    else
        log_error "数据目录不存在: $data_dir"
        ((TOTAL_TESTS++))
    fi
    
    # 显示测试结果
    echo ""
    echo "=========================================="
    echo "测试结果汇总"
    echo "=========================================="
    echo "总测试数: $TOTAL_TESTS"
    echo -e "通过测试: ${GREEN}$PASSED_TESTS${NC}"
    echo -e "失败测试: ${RED}$FAILED_TESTS${NC}"
    
    if [ $FAILED_TESTS -eq 0 ]; then
        echo -e "\n${GREEN}所有测试通过！Docker服务运行正常。${NC}"
        exit 0
    else
        echo -e "\n${RED}有 $FAILED_TESTS 个测试失败，请检查相关服务。${NC}"
        exit 1
    fi
}

# 执行主函数
main "$@"