#!/bin/bash

# GitLab 迁移验证脚本
# 功能：验证GitLab目录是否已正确迁移到 /data 分区

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_pass() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

print_fail() {
    echo -e "${RED}[FAIL]${NC} $1"
}

print_info() {
    echo -e "${YELLOW}[INFO]${NC} $1"
}

check_link() {
    local link=$1
    local expected_target=$2
    
    if [[ -L "$link" ]]; then
        local target=$(readlink -f "$link")
        if [[ "$target" == "$expected_target" ]]; then
            print_pass "符号链接正常: $link -> $target"
        else
            print_fail "符号链接指向错误: $link -> $target (应为: $expected_target)"
        fi
    else
        if [[ -d "$link" ]]; then
             print_fail "$link 是一个普通目录，不是符号链接"
        else
             print_fail "$link 不存在"
        fi
    fi
}

check_dir() {
    local dir=$1
    if [[ -d "$dir" ]]; then
        local size=$(du -sh "$dir" 2>/dev/null | cut -f1)
        print_pass "数据目录存在: $dir (大小: $size)"
    else
        print_fail "数据目录不存在: $dir"
    fi
}

main() {
    echo "=== 开始验证 GitLab 迁移状态 ==="
    
    echo -e "\n1. 检查目录结构映射"
    check_link "/opt/gitlab" "/data/gitlab"
    check_link "/var/opt/gitlab" "/data/var/opt/gitlab"
    check_link "/etc/gitlab" "/data/etc/gitlab"
    
    echo -e "\n2. 检查实际数据目录"
    check_dir "/data/gitlab"
    check_dir "/data/var/opt/gitlab"
    check_dir "/data/etc/gitlab"
    
    echo -e "\n3. 检查配置文件"
    if grep -q "/data/var/opt/gitlab" /etc/gitlab/gitlab.rb; then
        print_pass "gitlab.rb 中已包含新数据路径"
    else
        print_info "gitlab.rb 中未显式包含 /data 路径 (如果是通过软链接方式可能不需要，但建议更新)"
    fi
    
    echo -e "\n4. 检查服务状态"
    if systemctl is-active gitlab-runsvdir >/dev/null 2>&1; then
        print_pass "GitLab 服务 (runsvdir) 正在运行"
        gitlab-ctl status | head -n 5
        echo "..."
    else
        print_fail "GitLab 服务未运行"
    fi
    
    echo -e "\n5. 验证文件权限 (抽样)"
    if [[ -d "/data/var/opt/gitlab" ]]; then
        local owner=$(stat -c '%U' "/data/var/opt/gitlab")
        if [[ "$owner" == "git" ]]; then
            print_pass "/data/var/opt/gitlab 所有者为 git"
        else
            print_fail "/data/var/opt/gitlab 所有者为 $owner (应为 git)"
        fi
    fi

    echo -e "\n=== 验证结束 ==="
    echo "建议进一步运行: gitlab-rake gitlab:check 以进行深度应用级检查"
}

main
