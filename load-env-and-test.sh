#!/bin/bash

# Exit on error
set -e

echo "Loading environment variables from .env..."

# Load environment variables from .env
set -a
source .env
set +a

echo "Environment variables loaded successfully"
echo "Running Gauge tests..."

# Run Gauge tests
./gradlew gaugeTest