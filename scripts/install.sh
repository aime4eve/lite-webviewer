#!/bin/bash
# Nexus-Lite Installer
# Usage: ./install.sh [--silent] [--install-dir /path/to/install]

set -e

DEFAULT_INSTALL_DIR="/home/hkt/hkt-knowledge"
INSTALL_DIR=""
SILENT=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    key="$1"
    case $key in
        -s|--silent)
        SILENT=true
        shift
        ;;
        -d|--install-dir)
        INSTALL_DIR="$2"
        shift
        shift
        ;;
        *)
        echo "Unknown option: $1"
        exit 1
        ;;
    esac
done

if [ -z "$INSTALL_DIR" ]; then
    if [ "$SILENT" = true ]; then
        INSTALL_DIR="$DEFAULT_INSTALL_DIR"
    else
        read -p "Enter installation directory [$DEFAULT_INSTALL_DIR]: " user_dir
        INSTALL_DIR="${user_dir:-$DEFAULT_INSTALL_DIR}"
    fi
fi

log() {
    echo "[INFO] $1"
}

check_dependencies() {
    log "Checking dependencies..."
    if ! command -v java &> /dev/null; then
        echo "[ERROR] Java is not installed. Please install Java 17+."
        exit 1
    fi
    if ! command -v docker &> /dev/null; then
        echo "[WARNING] Docker is not installed. KG-Agent requires Docker."
    fi
    if ! command -v python3 &> /dev/null; then
        echo "[ERROR] Python 3 is not installed. Please install Python 3.10+."
        exit 1
    fi
}

setup_python() {
    log "Setting up Python environment in $INSTALL_DIR/kg-agent..."
    
    # Check if pip is available
    if ! command -v pip3 &> /dev/null && ! command -v pip &> /dev/null; then
        echo "[WARNING] pip is not installed. Skipping dependency installation."
        return
    fi

    pushd "$INSTALL_DIR/kg-agent" > /dev/null

    if [ ! -d "venv" ]; then
        log "Creating virtual environment..."
        python3 -m venv venv
    fi

    if [ -f "venv/bin/activate" ]; then
        source venv/bin/activate
        
        if [ -f "requirements.txt" ]; then
            log "Installing Python dependencies using Huawei Cloud mirror..."
            # Attempt to install using Huawei Cloud mirror
            # Added --no-cache-dir to prevent OOM kills on low memory systems
            if pip install --no-cache-dir -i https://repo.huaweicloud.com/repository/pypi/simple -r requirements.txt; then
                log "Python dependencies installed successfully."
            else
                echo "[ERROR] Failed to install Python dependencies. Please check your network or memory."
                # We exit here because the app won't run without dependencies
                exit 1
            fi
        else
            echo "[WARNING] requirements.txt not found."
        fi
        
        deactivate
    else
        echo "[ERROR] Failed to create virtual environment."
        exit 1
    fi

    popd > /dev/null
}

install_files() {
    log "Installing to $INSTALL_DIR..."
    mkdir -p "$INSTALL_DIR"
    mkdir -p "$INSTALL_DIR/bin"
    mkdir -p "$INSTALL_DIR/config"
    mkdir -p "$INSTALL_DIR/logs"
    mkdir -p "$INSTALL_DIR/data"

    # Copy files
    # Protect bin/data directory from being overwritten
    if [ -d "bin" ]; then
        for file in bin/*; do
            filename=$(basename "$file")
            if [ "$filename" == "data" ]; then
                if [ ! -d "$INSTALL_DIR/bin/data" ]; then
                     cp -r "$file" "$INSTALL_DIR/bin/"
                else
                     log "Skipping bin/data directory to protect existing data."
                fi
            else
                cp -r "$file" "$INSTALL_DIR/bin/"
            fi
        done
    fi
    
    cp -r kg-agent "$INSTALL_DIR/"
    cp -r docs "$INSTALL_DIR/"
    
    # Set permissions
    chmod +x "$INSTALL_DIR/bin/"*.sh

    log "Installation complete."
    log "Web Server: $INSTALL_DIR/bin/start-web.sh"
    log "KG Agent: $INSTALL_DIR/bin/start-agent.sh"
}

check_dependencies
install_files
setup_python
