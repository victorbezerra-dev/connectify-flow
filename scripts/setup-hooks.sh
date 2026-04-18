#!/bin/sh
set -e

echo "🔧 Configuring Git hooks..."

if ! git rev-parse --git-dir > /dev/null 2>&1; then
  echo "Not a git repository"
  exit 1
fi

git config core.hooksPath .githooks

echo "Setting executable permissions..."

chmod +x .githooks/pre-commit 2>/dev/null || true
git update-index --chmod=+x .githooks/pre-commit || true

echo "Git hooks configured successfully!"