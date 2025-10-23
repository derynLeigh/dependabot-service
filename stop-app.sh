#!/bin/bash

# Stop the application gracefully

echo "Stopping Dependabot PR Service..."

# Find the process running on port 8081
PID=$(lsof -ti:8081)

if [ -z "$PID" ]; then
    echo "No application running on port 8081"
    exit 0
fi

echo "Found application running with PID: $PID"
echo "Sending shutdown signal..."

kill -15 $PID

# Wait for graceful shutdown
for i in {1..10}; do
    if ! kill -0 $PID 2>/dev/null; then
        echo "✓ Application stopped gracefully"
        exit 0
    fi
    echo "Waiting for shutdown... ($i/10)"
    sleep 1
done

# Force kill if still running
if kill -0 $PID 2>/dev/null; then
    echo "Force stopping application..."
    kill -9 $PID
    echo "✓ Application force stopped"
fi