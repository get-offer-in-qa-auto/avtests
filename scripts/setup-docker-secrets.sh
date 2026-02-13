#!/bin/bash
# Добавляет Docker Hub секреты в GitHub для пайплайна build-and-push-docker.
# Требует: gh cli (brew install gh) и gh auth login.
#
# Использование:
#   ./scripts/setup-docker-secrets.sh
# (введёте данные при запросе)
#
# Или через переменные:
#   DOCKERHUB_USERNAME=zeeero DOCKERHUB_TOKEN=your_token ./scripts/setup-docker-secrets.sh

set -e

if ! command -v gh &>/dev/null; then
  echo "Установите GitHub CLI: https://cli.github.com/"
  exit 1
fi

echo "Docker Hub credentials for GitHub Actions"
echo "=========================================="

if [ -z "$DOCKERHUB_USERNAME" ]; then
  read -p "Docker Hub username: " DOCKERHUB_USERNAME
fi
if [ -z "$DOCKERHUB_TOKEN" ]; then
  read -sp "Docker Hub token (Access Token): " DOCKERHUB_TOKEN
  echo
fi

if [ -z "$DOCKERHUB_USERNAME" ] || [ -z "$DOCKERHUB_TOKEN" ]; then
  echo "Username и token обязательны"
  exit 1
fi

echo "Adding DOCKERHUB_USERNAME..."
echo -n "$DOCKERHUB_USERNAME" | gh secret set DOCKERHUB_USERNAME

echo "Adding DOCKERHUB_TOKEN..."
echo -n "$DOCKERHUB_TOKEN" | gh secret set DOCKERHUB_TOKEN

echo "Done. Secrets added."
