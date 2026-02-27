#!/bin/bash
set -e

systemctl is-active --quiet paceleague

# 30초 동안 기동 대기 (Spring Boot 기동 시간 대비)
for i in {1..30}; do
  if curl -fsS http://localhost:8080/ >/dev/null; then
    echo "OK"
    exit 0
  fi
  sleep 1
done

echo "App not responding on :8080"
exit 3