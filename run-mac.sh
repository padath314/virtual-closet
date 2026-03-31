#!/bin/bash
# Virtual Closet - Simple Mac Runner
# Use this if you just want to run the app without creating an .app bundle

set -e

echo "📦 Building application..."
mvn clean package -Pmac-bundle -DskipTests -q

echo "🚀 Launching Virtual Closet..."
java -XstartOnFirstThread -jar target/virtual-closet-1.0.0-SNAPSHOT-mac-bundle.jar
