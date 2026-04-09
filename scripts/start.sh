#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/paceleague"
CONTAINER_NAME="paceleague"
IMAGE_NAME="paceleague:latest"

cd "$APP_DIR"

echo "[INFO] Stopping old container if exists"
docker rm -f "$CONTAINER_NAME" 2>/dev/null || true

echo "[INFO] Building docker image"
docker build -t "$IMAGE_NAME" .

echo "[INFO] Starting new container"
docker run -d \
  --name "$CONTAINER_NAME" \
  -p 8080:8080 \
  --restart unless-stopped \
  "$IMAGE_NAME"

echo "[INFO] Running containers"
docker ps