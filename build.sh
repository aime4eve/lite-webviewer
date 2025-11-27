#!/usr/bin/env bash
set -euo pipefail
mvn -q -DskipTests clean package
pushd frontend
npm ci
npm run build
popd
