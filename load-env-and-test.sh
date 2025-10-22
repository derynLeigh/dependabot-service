#!/bin/bash

# Exit on error
set -e

echo "Loading environment variables from .env.local..."

# Load environment variables from .env.local
set -a
source .env.local
set +a

echo "Environment variables loaded successfully"
echo "Running Gauge tests..."

# Run Gauge tests
./gradlew gaugeTest