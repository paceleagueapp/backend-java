#!/bin/bash
set -e
cd /opt/myapp
JAR=$(ls -1 *.jar | head -n 1)
[ -z "$JAR" ] && echo "No jar found" && exit 1
mv -f "$JAR" app.jar
chown ec2-user:ec2-user /opt/myapp/app.jar
systemctl start myapp