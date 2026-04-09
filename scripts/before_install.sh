#!/bin/bash
set -e

mkdir -p /opt/paceleague
chown -R ec2-user:ec2-user /opt/paceleague

if ! getent group docker >/dev/null; then
  groupadd docker || true
fi

usermod -aG docker ec2-user || true