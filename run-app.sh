#!/bin/bash

# Exit on error
set -e

echo "================================================"
echo "  Dependabot PR Service - Application Startup  "
echo "================================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if .env.local file exists
if [ ! -f .env.local ]; then
    echo -e "${RED}Error: .env.local file not found!${NC}"
    echo "Please ensure .env.local exists and is decrypted with git-crypt"
    exit 1
fi

# Check if .env.local is encrypted (binary file)
if file .env.local | grep -q "data"; then
    echo -e "${RED}Error: .env.local file appears to be encrypted!${NC}"
    echo "Please unlock with: git-crypt unlock /path/to/your/key"
    exit 1
fi

echo -e "${GREEN}✓${NC} .env.local file found and readable"
echo ""

# Load environment variables from .env.local
echo "Loading environment variables from .env.local..."
set -a
source .env.local
set +a
echo -e "${GREEN}✓${NC} Environment variables loaded"
echo ""

# Verify critical environment variables
echo "Verifying required environment variables..."

if [ -z "$GITHUB_APP_ID" ]; then
    echo -e "${RED}✗${NC} GITHUB_APP_ID is not set"
    exit 1
fi
echo -e "${GREEN}✓${NC} GITHUB_APP_ID: $GITHUB_APP_ID"

if [ -z "$GITHUB_INSTALLATION_ID" ]; then
    echo -e "${RED}✗${NC} GITHUB_INSTALLATION_ID is not set"
    exit 1
fi
echo -e "${GREEN}✓${NC} GITHUB_INSTALLATION_ID: $GITHUB_INSTALLATION_ID"

if [ -z "$GITHUB_OWNER" ]; then
    echo -e "${RED}✗${NC} GITHUB_OWNER is not set"
    exit 1
fi
echo -e "${GREEN}✓${NC} GITHUB_OWNER: $GITHUB_OWNER"

if [ -z "$GITHUB_REPOS" ]; then
    echo -e "${RED}✗${NC} GITHUB_REPOS is not set"
    exit 1
fi
echo -e "${GREEN}✓${NC} GITHUB_REPOS: $GITHUB_REPOS"

echo ""
echo "Building application..."
./gradlew build -x test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Build successful"
else
    echo -e "${RED}✗${NC} Build failed"
    exit 1
fi

echo ""
echo "================================================"
echo "  Starting Dependabot PR Service...            "
echo "================================================"
echo ""
echo -e "${YELLOW}Application will be available at:${NC}"
echo "  • Main API:     http://localhost:8081"
echo "  • Health Check: http://localhost:8081/health"
echo "  • Swagger UI:   http://localhost:8081/swagger-ui.html"
echo "  • OpenAPI Docs: http://localhost:8081/api-docs"
echo "  • PRs Endpoint: http://localhost:8081/api/prs"
echo ""
echo -e "${YELLOW}Press Ctrl+C to stop the application${NC}"
echo ""

# Run the application
./gradlew bootRun