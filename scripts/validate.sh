#!/bin/bash
set -e
systemctl is-active --quiet myapp
echo "myapp is running"