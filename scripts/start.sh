#!/bin/bash
set -e

cd /opt/paceleague

# jar 위치를 우선 build/libs에서 찾고, 없으면 현재 폴더도 확인
JAR=$(ls -1 build/libs/*.jar 2>/dev/null | head -n 1 || true)
if [ -z "$JAR" ]; then
  JAR=$(ls -1 *.jar 2>/dev/null | head -n 1 || true)
fi

if [ -z "$JAR" ]; then
  echo "No jar found under /opt/paceleague (checked build/libs and current dir)"
  ls -al
  ls -al build || true
  ls -al build/libs || true
  exit 1
fi

cp -f "$JAR" /opt/paceleague/app.jar
chown ec2-user:ec2-user /opt/paceleague/app.jar

systemctl start paceleague