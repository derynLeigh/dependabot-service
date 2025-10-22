#!/bin/bash

# Quick health check script

PORT=${1:-8081}

echo "Checking application health on port $PORT..."
echo ""

response=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:$PORT/health)

if [ "$response" = "200" ]; then
    echo "✓ Application is healthy!"
    echo ""
    echo "Service details:"
    curl -s http://localhost:$PORT/health | jq '.'
    exit 0
else
    echo "✗ Application is not responding (HTTP $response)"
    exit 1
fi