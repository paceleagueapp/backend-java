#!/bin/bash
set -e

APP_DIR=/opt/paceleague
cd "$APP_DIR"

# 1) 실행 가능한 fat jar 우선 (plain 제외)
JAR=$(ls -1 *SNAPSHOT.jar 2>/dev/null | grep -v -- '-plain\.jar' | head -n 1 || true)

# 2) 혹시 없으면, build/libs에서도 찾아보고
if [ -z "$JAR" ] && [ -d build/libs ]; then
  JAR=$(ls -1 build/libs/*SNAPSHOT.jar 2>/dev/null | grep -v -- '-plain\.jar' | head -n 1 || true)
fi

# 3) 그래도 없으면 "가장 큰 jar" 선택 (plain 제외)
if [ -z "$JAR" ]; then
  JAR=$(ls -S1 *.jar 2>/dev/null | grep -v -- '-plain\.jar' | head -n 1 || true)
fi

if [ -z "$JAR" ]; then
  echo "No executable jar found in $APP_DIR"
  ls -al
  exit 1
fi

echo "Using jar: $JAR"
cp -f "$JAR" "$APP_DIR/app.jar"
chown ec2-user:ec2-user "$APP_DIR/app.jar"

systemctl restart paceleague