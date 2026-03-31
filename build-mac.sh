#!/bin/bash
# Virtual Closet - Mac App Builder
# Run this script on macOS with JDK 21+ installed

set -e

APP_NAME="Virtual Closet"
VERSION="1.0.0"
MAIN_CLASS="com.virtualcloset.app.VirtualClosetApp"
BUNDLE_JAR="virtual-closet-1.0.0-SNAPSHOT-mac-bundle.jar"

echo "🔍 Checking Java version..."
java -version

echo ""
echo "📦 Building fat JAR with JavaFX bundled..."
mvn clean package -Pmac-bundle -DskipTests

echo ""
echo "🧹 Preparing input folder..."
rm -rf target/jpackage-input target/app
mkdir -p target/jpackage-input

# Copy the shaded jar and all dependencies
cp target/$BUNDLE_JAR target/jpackage-input/app.jar

echo ""
echo "🍎 Creating Mac app bundle..."

# Check if icon exists
ICON_ARG=""
if [ -f "icon.icns" ]; then
    echo "✓ Found icon.icns"
    ICON_ARG="--icon icon.icns"
else
    echo "⚠ No icon.icns found - app will use default icon"
    echo "  To add an icon, place icon.icns in the project root"
fi

# Detect architecture
ARCH=$(uname -m)
echo "📱 Detected architecture: $ARCH"

jpackage \
    --type app-image \
    --name "$APP_NAME" \
    --app-version "$VERSION" \
    --input target/jpackage-input \
    --main-jar "app.jar" \
    --main-class "$MAIN_CLASS" \
    --dest target/app \
    --java-options "-Xmx2g" \
    --java-options "-Dapple.awt.application.appearance=system" \
    --mac-package-name "VirtualCloset" \
    --vendor "Virtual Closet" \
    $ICON_ARG

echo ""
echo "✅ Done! Your app is ready:"
echo "   target/app/$APP_NAME.app"
echo ""
echo "You can now:"
echo "   • Double-click to run"
echo "   • Drag to /Applications to install"
echo ""
echo "📝 Note: This app bundle includes JavaFX and works on macOS only."
