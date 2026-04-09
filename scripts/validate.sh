#!/usr/bin/env bash
set -euo pipefail

CONTAINER_NAME="paceleague"
URL="http://127.0.0.1:8080/"

for i in {1..30}; do
  if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    break
  fi
  sleep 2
done

if ! docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  echo "[ERROR] container is not running: $CONTAINER_NAME"
  docker ps -a
  exit 3
fi

for i in {1..30}; do
  if curl -fsS "$URL" >/dev/null; then
    echo "[OK] service is up: $URL"
    exit 0
  fi
  sleep 2
done

echo "[ERROR] HTTP check failed: $URL"
docker logs "$CONTAINER_NAME" --tail 100 || true
exit 3