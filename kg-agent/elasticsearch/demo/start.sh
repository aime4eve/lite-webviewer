#!/bin/bash
# 全文检索系统启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker 未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose 未安装，请先安装Docker Compose"
        exit 1
    fi
    
    print_info "Docker 环境检查通过"
}

# 启动Elasticsearch服务
start_elasticsearch() {
    print_info "启动Elasticsearch服务..."
    
    # 进入docker目录
    cd docker
    
    # 停止现有容器
    docker-compose down
    
    # 启动服务
    docker-compose up -d
    
    # 等待服务启动
    print_info "等待Elasticsearch启动..."
    sleep 30
    
    # 检查服务状态
    if curl -s http://localhost:9200 > /dev/null; then
        print_info "Elasticsearch启动成功"
    else
        print_error "Elasticsearch启动失败"
        exit 1
    fi
    
    # 返回上一级目录
    cd ..
}

# 初始化IK分词器
init_ik_analyzer() {
    print_info "初始化IK分词器..."
    
    # 执行初始化脚本
    bash scripts/init-ik.sh
    
    print_info "IK分词器初始化完成"
}

# 索引文件
index_files() {
    print_info "开始索引文件..."
    
    # 执行全文搜索索引
    python3 full_text_search.py --index
    
    print_info "文件索引完成"
}

# 启动Web界面
start_web_ui() {
    print_info "启动Web界面..."
    
    # 在后台启动Web界面
    nohup python3 web/search_web_ui.py > web_ui.log 2>&1 &
    WEB_PID=$!
    echo $WEB_PID > web_ui.pid
    
    print_info "Web界面已启动，PID: $WEB_PID"
    print_info "访问 http://localhost:5001 使用搜索功能"
}

# 显示帮助信息
show_help() {
    echo "全文检索系统启动脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  start     启动完整系统 (默认)"
    echo "  elastic   仅启动Elasticsearch"
    echo "  ik        仅初始化IK分词器"
    echo "  index     仅索引文件"
    echo "  web       仅启动Web界面"
    echo "  stop      停止所有服务"
    echo "  status    查看服务状态"
    echo "  test      运行测试"
    echo "  help      显示此帮助信息"
}

# 停止所有服务
stop_services() {
    print_info "停止所有服务..."
    
    # 停止Web界面
    if [ -f web_ui.pid ]; then
        WEB_PID=$(cat web_ui.pid)
        if kill -0 $WEB_PID 2>/dev/null; then
            kill $WEB_PID
            print_info "Web界面已停止"
        fi
        rm web_ui.pid
    fi
    
    # 停止Elasticsearch
    cd docker
    docker-compose down
    cd ..
    
    print_info "所有服务已停止"
}

# 查看服务状态
check_status() {
    print_info "检查服务状态..."
    
    # 检查Elasticsearch
    if curl -s http://localhost:9200 > /dev/null; then
        print_info "Elasticsearch: 运行中"
    else
        print_warning "Elasticsearch: 未运行"
    fi
    
    # 检查Web界面
    if [ -f web_ui.pid ]; then
        WEB_PID=$(cat web_ui.pid)
        if kill -0 $WEB_PID 2>/dev/null; then
            print_info "Web界面: 运行中 (PID: $WEB_PID)"
        else
            print_warning "Web界面: 未运行 (PID文件存在但进程不存在)"
        fi
    else
        print_warning "Web界面: 未运行"
    fi
}

# 运行测试
run_tests() {
    print_info "运行测试..."
    
    # 检查Elasticsearch是否运行
    if ! curl -s http://localhost:9200 > /dev/null; then
        print_error "Elasticsearch未运行，请先启动服务"
        exit 1
    fi
    
    # 运行测试脚本
    python3 tests/test_elasticsearch.py
    python3 tests/test_ik_analyzer.py
    python3 tests/test_full_text_search.py
    
    print_info "测试完成"
}

# 主函数
main() {
    # 检查当前目录
    if [ ! -f "full_text_search.py" ]; then
        print_error "请在demo目录下运行此脚本"
        exit 1
    fi
    
    # 处理命令行参数
    case "${1:-start}" in
        start)
            check_docker
            start_elasticsearch
            init_ik_analyzer
            index_files
            start_web_ui
            print_info "全文检索系统启动完成"
            print_info "访问 http://localhost:5001 使用搜索功能"
            ;;
        elastic)
            check_docker
            start_elasticsearch
            ;;
        ik)
            init_ik_analyzer
            ;;
        index)
            index_files
            ;;
        web)
            start_web_ui
            ;;
        stop)
            stop_services
            ;;
        status)
            check_status
            ;;
        test)
            run_tests
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知选项: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@"