#!/bin/bash
# Nebula Graph Demo 统一启动脚本

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 打印带颜色的消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEMO_DIR="$SCRIPT_DIR"

# 检查Docker是否安装
check_docker() {
    print_info "检查Docker安装状态..."
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! docker info &> /dev/null; then
        print_error "Docker未运行，请先启动Docker服务"
        exit 1
    fi
    
    print_success "Docker已安装并运行"
}

# 检查Docker Compose是否安装
check_docker_compose() {
    print_info "检查Docker Compose安装状态..."
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    print_success "Docker Compose已安装"
}

# 检查并释放端口
check_and_free_ports() {
    print_info "检查端口占用情况..."
    
    # 1. 按端口映射检查 (针对 bridge 模式)
    local ports=("2379" "9559" "9669" "9779" "19559" "19669" "19779")
    for port in "${ports[@]}"; do
        local container_ids=$(docker ps --format "{{.ID}} {{.Ports}}" | grep ":$port->" | awk '{print $1}')
        if [ ! -z "$container_ids" ]; then
            print_warning "端口 $port 被容器占用 (Bridge模式)，正在清理..."
            for pid in $container_ids; do
                local name=$(docker inspect --format '{{.Name}}' $pid | sed 's/\///')
                print_info "停止容器: $name ($pid)"
                docker stop $pid > /dev/null
                docker rm $pid > /dev/null
            done
        fi
    done

    # 2. 按关键词检查 (针对 Host 模式或无端口映射显示的容器)
    # 关键词: nebula-, etcd
    local keywords=("nebula-" "etcd")
    for keyword in "${keywords[@]}"; do
        # 查找所有运行中的包含 keyword 的容器
        local container_ids=$(docker ps --format "{{.ID}} {{.Names}}" | grep "$keyword" | awk '{print $1}')
        
        if [ ! -z "$container_ids" ]; then
             print_warning "发现可能冲突的容器 (匹配关键字 '$keyword')，正在清理..."
             for pid in $container_ids; do
                 local name=$(docker inspect --format '{{.Name}}' $pid | sed 's/\///')
                 print_info "停止容器: $name ($pid)"
                 docker stop $pid > /dev/null
                 docker rm $pid > /dev/null
             done
        fi
    done
    
    print_success "端口检查完成"
}

# 清理数据
clean_data() {
    print_info "清理旧数据..."
    # 检查数据目录是否存在
    if [ -d "/data/nebula-docker-data" ]; then
        # 使用 docker 容器来删除 root 拥有的数据文件 (因为当前用户可能没有权限)
        # 挂载父目录 /data/nebula-docker-data 到容器内的 /clean_dir
        docker run --rm -v /data/nebula-docker-data:/clean_dir alpine sh -c "rm -rf /clean_dir/*"
        print_success "旧数据已清理"
    else
        print_info "数据目录不存在，无需清理"
    fi
}

# 启动Nebula Graph服务
start_nebula() {
    print_info "启动Nebula Graph服务..."
    
    cd "$DEMO_DIR/docker"
    
    # 停止可能已存在的容器 (当前项目的)
    docker-compose down
    
    # 检查并释放端口 (清理其他项目的冲突容器)
    check_and_free_ports

    # 清理数据 (确保环境纯净，避免因 IP 变动导致的 Leader 选举失败)
    clean_data
    
    # 启动核心服务
    docker-compose up -d nebula-metad nebula-storaged nebula-graphd
    
    # 尝试启动 Nebula Studio (允许失败)
    print_info "尝试启动 Nebula Studio..."
    if ! docker-compose up -d nebula-studio; then
        print_warning "Nebula Studio 启动失败 (可能是镜像拉取失败)，但不影响核心服务运行"
    fi
    
    print_info "等待Nebula Graph服务启动..."
    sleep 30
    
    # 检查服务状态
    if docker-compose ps | grep -q "Up"; then
        print_success "Nebula Graph服务已启动"
    else
        print_error "Nebula Graph服务启动失败"
        docker-compose logs
        exit 1
    fi
}

# 初始化Nebula Graph数据
init_nebula() {
    print_info "初始化Nebula Graph数据..."
    
    # 等待服务完全启动
    sleep 20
    
    # 执行初始化脚本
    if [ -f "$DEMO_DIR/scripts/init_nebula.sh" ]; then
        chmod +x "$DEMO_DIR/scripts/init_nebula.sh"
        if "$DEMO_DIR/scripts/init_nebula.sh"; then
            print_success "Nebula Graph数据初始化完成"
        else
            print_error "Nebula Graph数据初始化失败"
            print_info "诊断信息: 查看 metad 日志..."
            docker logs nebula-metad 2>&1 | tail -n 20
            print_info "诊断信息: 查看 graphd 日志..."
            docker logs nebula-graphd 2>&1 | tail -n 20
            exit 1
        fi
    else
        print_error "初始化脚本不存在: $DEMO_DIR/scripts/init_nebula.sh"
        exit 1
    fi
}

# 启动Web API服务
start_web_api() {
    print_info "启动Web API服务..."
    
    cd "$DEMO_DIR"

    # 1. 清理旧的 PID 文件记录的进程
    if [ -f "$DEMO_DIR/api.pid" ]; then
        OLD_PID=$(cat "$DEMO_DIR/api.pid")
        if kill -0 $OLD_PID 2>/dev/null; then
            print_warning "停止旧的 API 服务 (PID: $OLD_PID)..."
            kill $OLD_PID
            sleep 2
            # 如果还在运行，强制杀死
            if kill -0 $OLD_PID 2>/dev/null; then
                 print_warning "服务未响应，强制停止 (PID: $OLD_PID)..."
                 kill -9 $OLD_PID
            fi
        fi
        rm -f "$DEMO_DIR/api.pid"
    fi

    # 2. 按端口检查并清理 (防止 PID 文件丢失的情况)
    # 检查端口 5000 是否被占用
    if lsof -i :5000 -t >/dev/null 2>&1; then
        print_warning "端口 5000 被占用，正在清理..."
        # 获取占用端口 5000 的进程 PID
        PORT_PIDS=$(lsof -i :5000 -t)
        for PID in $PORT_PIDS; do
            print_info "杀死占用端口 5000 的进程: $PID"
            kill -9 $PID 2>/dev/null
        done
    fi

    # 3. 按进程名检查并清理 (防止残留的 Python 进程)
    # 使用 pgrep 查找 src/interfaces/api.py
    # 注意：排除当前的 nohup 进程（如果它已经启动了，虽然这里还没启动）
    # 但 pgrep -f "src/interfaces/api.py" 可能会匹配到当前正在运行的脚本（如果脚本本身包含这个字符串）
    # 不过我们是在 start.sh 中，应该没事。
    
    PY_PIDS=$(pgrep -f "src/interfaces/api.py" || true)
    if [ ! -z "$PY_PIDS" ]; then
        print_warning "发现残留的 API 进程，正在清理..."
        for PID in $PY_PIDS; do
            # 检查是否是当前脚本的子进程（理论上 nohup 还没运行，不应该是）
            # 直接杀
            print_info "杀死残留进程: $PID"
            kill -9 $PID 2>/dev/null || true
        done
    fi
    
    # 检查Python依赖
    if ! python3 -c "import flask" &> /dev/null; then
        print_warning "Flask未安装，尝试安装..."
        pip3 install flask
    fi
    
    # 启动API服务
    nohup python3 src/interfaces/api.py > api.log 2>&1 &
    API_PID=$!
    echo $API_PID > api.pid
    
    # 等待几秒检查是否启动成功
    sleep 5
    if kill -0 $API_PID 2>/dev/null; then
        print_success "Web API服务已启动 (PID: $API_PID)"
    else
        print_error "Web API服务启动失败，请查看 api.log"
        cat api.log
        exit 1
    fi
}

# 启动Web界面
start_web_ui() {
    print_info "启动Web界面..."
    
    # 检查API服务是否已启动
    if [ ! -f "$DEMO_DIR/api.pid" ]; then
        print_warning "API服务未启动，先启动API服务..."
        start_web_api
    fi
    
    # 打开浏览器
    if command -v xdg-open &> /dev/null; then
        xdg-open "http://localhost:5000"
    elif command -v open &> /dev/null; then
        open "http://localhost:5000"
    else
        print_info "请手动打开浏览器访问: http://localhost:5000"
    fi
    
    print_success "Web界面已启动"
}

# 运行测试
run_tests() {
    print_info "运行测试套件..."
    
    cd "$DEMO_DIR/tests/e2e"
    
    # 检查Python依赖
    if ! python3 -c "import unittest" &> /dev/null; then
        print_warning "unittest模块不可用"
    fi
    
    # 运行测试
    python3 run_all_tests.py
    
    print_success "测试套件执行完成"
}

# 显示帮助信息
show_help() {
    echo "Nebula Graph Demo 启动脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  start           启动所有服务 (Nebula Graph, Web API, Web界面)"
    echo "  stop            停止所有服务"
    echo "  restart         重启所有服务"
    echo "  nebula          仅启动Nebula Graph服务"
    echo "  api             仅启动Web API服务"
    echo "  web             仅启动Web界面"
    echo "  init            仅初始化Nebula Graph数据"
    echo "  test            运行测试套件"
    echo "  status          显示服务状态"
    echo "  logs            显示服务日志"
    echo "  help            显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start        # 启动所有服务"
    echo "  $0 test         # 运行测试套件"
    echo "  $0 stop         # 停止所有服务"
}

# 停止所有服务
stop_services() {
    print_info "停止所有服务..."
    
    cd "$DEMO_DIR/docker"
    
    # 停止Docker容器
    docker-compose down
    
    # 停止API服务
    if [ -f "$DEMO_DIR/api.pid" ]; then
        API_PID=$(cat "$DEMO_DIR/api.pid")
        if kill -0 $API_PID 2>/dev/null; then
            kill $API_PID
            print_success "Web API服务已停止 (PID: $API_PID)"
        fi
        rm -f "$DEMO_DIR/api.pid"
    fi
    
    print_success "所有服务已停止"
}

# 重启所有服务
restart_services() {
    print_info "重启所有服务..."
    stop_services
    sleep 5
    start_all_services
}

# 启动所有服务
start_all_services() {
    check_docker
    check_docker_compose
    start_nebula
    init_nebula
    start_web_api
    start_web_ui
    print_success "所有服务已启动"
}

# 显示服务状态
show_status() {
    print_info "检查服务状态..."
    
    cd "$DEMO_DIR/docker"
    
    # 检查Docker容器状态
    echo "Docker容器状态:"
    docker-compose ps
    
    # 检查API服务状态
    if [ -f "$DEMO_DIR/api.pid" ]; then
        API_PID=$(cat "$DEMO_DIR/api.pid")
        if kill -0 $API_PID 2>/dev/null; then
            echo "Web API服务运行中 (PID: $API_PID)"
        else
            echo "Web API服务未运行"
        fi
    else
        echo "Web API服务未运行"
    fi
}

# 显示服务日志
show_logs() {
    print_info "显示服务日志..."
    
    cd "$DEMO_DIR/docker"
    
    # 显示Docker容器日志
    echo "Docker容器日志:"
    docker-compose logs --tail=50
    
    # 显示API服务日志
    if [ -f "$DEMO_DIR/api.log" ]; then
        echo "Web API服务日志:"
        tail -50 "$DEMO_DIR/api.log"
    fi
}

# 主函数
main() {
    case "${1:-start}" in
        start)
            start_all_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        nebula)
            check_docker
            check_docker_compose
            start_nebula
            ;;
        api)
            start_web_api
            ;;
        web)
            start_web_ui
            ;;
        init)
            init_nebula
            ;;
        test)
            run_tests
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs
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