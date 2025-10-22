#!/bin/bash

set -e  # Exit on error

echo "Loading environment variables from .env.local..."

# Load environment variables from .env.local
set -a
source .env.local
set +a

echo "Environment variables loaded successfully"

echo "Building test classes..."
./gradlew testClasses

echo "Getting classpath..."
CLASSPATH=$(./gradlew -q printTestClasspath)

if [ -z "$CLASSPATH" ]; then
    echo "Error: Could not get classpath"
    exit 1
fi

echo "Setting Gauge environment variables..."
export gauge_custom_compile_dir="build/classes/java/main:build/classes/java/test"
export gauge_custom_classpath="$CLASSPATH"

echo "Running Gauge tests..."
gauge run specs "$@"