#!/bin/bash
set -e

if [ ! -f .env.local ]; then
  echo "❌ .env.local not found!"
  exit 1
fi

# Export all variables from .env.local
export $(cat .env.local | grep -v '^#' | xargs)

# Run the application
./gradlew bootRun "$@"
