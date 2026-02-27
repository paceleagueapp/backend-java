#!/usr/bin/env bash
set -euo pipefail

SERVICE="paceleague"
URL="http://127.0.0.1:8080/"

# 서비스가 active 될 때까지 최대 60초 대기
for i in {1..30}; do
  if systemctl is-active --quiet "$SERVICE"; then
    break
  fi
  sleep 2
done

if ! systemctl is-active --quiet "$SERVICE"; then
  echo "[ERROR] $SERVICE is not active"
  systemctl status "$SERVICE" -l --no-pager || true
  exit 3
fi

# HTTP 응답 확인(최대 60초)
for i in {1..30}; do
  if curl -fsS "$URL" >/dev/null; then
    echo "[OK] service is up: $URL"
    exit 0
  fi
  sleep 2
done

echo "[ERROR] HTTP check failed: $URL"
exit 3