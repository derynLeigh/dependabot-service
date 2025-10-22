#!/bin/bash

# Development mode with auto-restart on code changes

set -e

echo "Starting Dependabot PR Service in DEVELOPMENT mode"
echo "Live reload enabled - application will restart on code changes"
echo ""

set -a
source .env.local
set +a

# Run with devtools enabled
./gradlew bootRun --args='--spring.devtools.restart.enabled=true'