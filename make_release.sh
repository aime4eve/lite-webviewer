#!/bin/bash
set -e

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

echo "=========================================="
echo "Release created successfully!"
echo "Package: ${DIST_DIR}/${RELEASE_NAME}.tar.gz"
echo "Checksums generated."
echo "=========================================="
