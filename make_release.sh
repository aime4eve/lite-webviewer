#!/bin/bash
set -e

# Default ports
WEB_PORT=8090
AGENT_PORT=5000
PUBLIC_DIR="/home/hkt/dist_release"

# Parse arguments
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --web-port) WEB_PORT="$2"; shift ;;
        --agent-port) AGENT_PORT="$2"; shift ;;
        --public-dir) PUBLIC_DIR="$2"; shift ;;
        *) echo "Unknown parameter passed: $1"; exit 1 ;;
    esac
    shift
done

echo "Using Web Port: $WEB_PORT"
echo "Using Agent Port: $AGENT_PORT"
if [ -n "$PUBLIC_DIR" ]; then
    echo "Using Public Directory: $PUBLIC_DIR"
fi

# Version Management
if [ -f "VERSION" ]; then
    CURRENT_VERSION=$(cat VERSION)
else
    CURRENT_VERSION="1.0.0"
    echo "1.0.0" > VERSION
fi

# Increment Version (Patch level)
IFS='.' read -r -a parts <<< "$CURRENT_VERSION"
MAJOR="${parts[0]}"
MINOR="${parts[1]}"
PATCH="${parts[2]}"
NEW_PATCH=$((PATCH + 1))
VERSION="${MAJOR}.${MINOR}.${NEW_PATCH}"

# Update VERSION file
echo "$VERSION" > VERSION
echo "Bumping version: $CURRENT_VERSION -> $VERSION"

RELEASE_NAME="nexus-lite-v${VERSION}"
DIST_DIR="dist_release"
PKG_DIR="${DIST_DIR}/${RELEASE_NAME}"

echo "Starting packaging for ${RELEASE_NAME}..."

# 1. Prepare directories
rm -rf "${DIST_DIR}"
mkdir -p "${PKG_DIR}/bin"
mkdir -p "${PKG_DIR}/kg-agent"
mkdir -p "${PKG_DIR}/docs"

# 2. Copy Build Artifacts (Backend/Frontend)
echo "Copying application artifacts..."
if [ -f "backend/target/nexus-lite-1.0.0-SNAPSHOT.jar" ]; then
    cp backend/target/nexus-lite-1.0.0-SNAPSHOT.jar "${PKG_DIR}/bin/nexus-lite.jar"
    cp scripts/start-web.sh "${PKG_DIR}/bin/"
    cp scripts/start-agent.sh "${PKG_DIR}/bin/"
    
    # Configure ports in scripts
    echo "Configuring ports in start scripts..."
    
    # Update start-web.sh
    sed -i "s/SERVER_PORT=8090/SERVER_PORT=${WEB_PORT}/g" "${PKG_DIR}/bin/start-web.sh"
    # Inject --server.port argument to java command
    sed -i "s/java -jar nexus-lite.jar/java -jar nexus-lite.jar --server.port=\${SERVER_PORT}/g" "${PKG_DIR}/bin/start-web.sh"
    
    # Update start-agent.sh
    sed -i "s/AGENT_PORT=5000/AGENT_PORT=${AGENT_PORT}/g" "${PKG_DIR}/bin/start-agent.sh"
    # Inject PORT env var to python command
    sed -i "s/nohup \$PYTHON_EXEC src\/backend\/main.py/export PORT=\${AGENT_PORT} \&\& nohup \$PYTHON_EXEC src\/backend\/main.py/g" "${PKG_DIR}/bin/start-agent.sh"
    
    chmod +x "${PKG_DIR}/bin/"*.sh
else
    echo "Error: backend/target/nexus-lite-1.0.0-SNAPSHOT.jar not found. Please run ./build.sh first."
    exit 1
fi

# 3. Copy KG-Agent
echo "Copying KG-Agent..."
# Copy everything but exclude junk
rsync -av --exclude='venv' \
          --exclude='__pycache__' \
          --exclude='*.pyc' \
          --exclude='.git' \
          --exclude='.idea' \
          --exclude='elasticsearch/data' \
          kg-agent/ "${PKG_DIR}/kg-agent/"

# 4. Copy Documentation and Installer
echo "Copying documentation and installer..."
cp -r docs/* "${PKG_DIR}/docs/"
cp scripts/install.sh "${PKG_DIR}/"
chmod +x "${PKG_DIR}/install.sh"

# 5. Create Version Info
echo "version=${VERSION}" > "${PKG_DIR}/version.txt"
echo "build_date=$(date)" >> "${PKG_DIR}/version.txt"
echo "git_commit=$(git rev-parse --short HEAD 2>/dev/null || echo 'unknown')" >> "${PKG_DIR}/version.txt"

# 6. Create Archive
echo "Creating tarball..."
cd "${DIST_DIR}"
tar -czf "${RELEASE_NAME}.tar.gz" "${RELEASE_NAME}"

# 7. Generate Checksums
md5sum "${RELEASE_NAME}.tar.gz" > "${RELEASE_NAME}.tar.gz.md5"
sha256sum "${RELEASE_NAME}.tar.gz" > "${RELEASE_NAME}.tar.gz.sha256"

# 8. Set Permissions for 'hkt' group (Apply to all generated files including tarball)
echo "Setting permissions for group 'hkt'..."
# Go back to parent directory to run chgrp on the whole dist folder
cd ..
if getent group hkt > /dev/null; then
    # Change group ownership recursively and grant execute/read permissions
    chgrp -R hkt "${DIST_DIR}" || echo "Warning: Failed to set group 'hkt'. Check permissions."
    chmod -R g+rx "${DIST_DIR}"
else
    echo "Warning: Group 'hkt' not found. Skipping permission update."
fi

# 9. Copy to Public Directory (if specified)
if [ -n "$PUBLIC_DIR" ]; then
    echo "Copying release artifacts to public directory: ${PUBLIC_DIR}"
    
    # Ensure public directory exists
    if [ ! -d "${PUBLIC_DIR}" ]; then
        echo "Directory ${PUBLIC_DIR} does not exist. Creating it..."
        mkdir -p "${PUBLIC_DIR}"
    fi
    
    # Copy tarball and checksums
    cp "${DIST_DIR}/${RELEASE_NAME}.tar.gz" "${PUBLIC_DIR}/"
    cp "${DIST_DIR}/${RELEASE_NAME}.tar.gz.md5" "${PUBLIC_DIR}/"
    cp "${DIST_DIR}/${RELEASE_NAME}.tar.gz.sha256" "${PUBLIC_DIR}/"
    
    # Set permissions on copied files in public directory
    if getent group hkt > /dev/null; then
        chgrp hkt "${PUBLIC_DIR}/${RELEASE_NAME}.tar.gz"* || true
        chmod g+r "${PUBLIC_DIR}/${RELEASE_NAME}.tar.gz"* || true
    fi
    
    echo "Artifacts copied to ${PUBLIC_DIR}"
fi

echo "=========================================="
echo "Release created successfully!"
echo "Package: ${DIST_DIR}/${RELEASE_NAME}.tar.gz"
if [ -n "$PUBLIC_DIR" ]; then
    echo "Public Package: ${PUBLIC_DIR}/${RELEASE_NAME}.tar.gz"
fi
echo "Checksums generated."
echo "=========================================="
