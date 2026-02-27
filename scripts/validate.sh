#!/bin/bash
set -e
systemctl is-active --quiet paceleague
echo "myapp is running"