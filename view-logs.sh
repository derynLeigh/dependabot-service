#!/bin/bash

# View application logs in real-time

LOG_FILE="logs/application.log"

if [ ! -f "$LOG_FILE" ]; then
    echo "Log file not found: $LOG_FILE"
    echo "Start the application first with ./run-app.sh"
    exit 1
fi

echo "Tailing application logs (Ctrl+C to stop)..."
echo ""

tail -f "$LOG_FILE"