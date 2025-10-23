#!/bin/bash
set -e

if [ ! -f .env.local ]; then
  echo "⚠️  .env.local not found, using test values"
  export GITHUB_APP_ID=test-app-id
  export GITHUB_INSTALLATION_ID=test-installation-id
  export GITHUB_PRIVATE_KEY=test-private-key
  export GITHUB_OWNER=test-owner
  export GITHUB_REPOS=test-repo1,test-repo2
fi

./gradlew test "$@"
