#!/usr/bin/env bash
set -euo pipefail

APP_DIR="/opt/paceleague"
cd "$APP_DIR"

# plain 제외한 SNAPSHOT.jar 우선 선택
JAR="$(ls -1 "$APP_DIR"/*SNAPSHOT.jar 2>/dev/null | grep -v -- '-plain\.jar$' | head -n 1 || true)"

if [[ -z "${JAR}" ]]; then
  echo "[ERROR] fat jar not found in $APP_DIR"
  ls -al "$APP_DIR"
  exit 1
fi

echo "[INFO] Using jar: $JAR"
cp -f "$JAR" "$APP_DIR/app.jar"
chown ec2-user:ec2-user "$APP_DIR/app.jar"
chmod 0644 "$APP_DIR/app.jar"

sudo systemctl daemon-reload
sudo systemctl restart paceleague