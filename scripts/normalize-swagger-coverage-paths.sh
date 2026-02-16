#!/bin/bash
# Adds /api/v1 prefix to paths in swagger-coverage-output JSON files
# so they match the OpenAPI spec and coverage is correctly calculated.

set -e
DIR="${1:-swagger-coverage-output}"
PREFIX="/api/v1"

if [ ! -d "$DIR" ]; then
  echo "Directory $DIR not found, skipping path normalization"
  exit 0
fi

count=0
for f in "$DIR"/*-coverage.json; do
  [ -f "$f" ] || continue
  tmp=$(mktemp)
  jq --arg prefix "$PREFIX" '
    if .paths then
      .paths |= (
        to_entries |
        map(.key |= if startswith($prefix) then . else $prefix + (if startswith("/") then . else "/" + . end) end) |
        from_entries
      )
    else
      .
    end
  ' "$f" > "$tmp"
  mv "$tmp" "$f"
  count=$((count + 1))
done

echo "Normalized paths in $count coverage file(s)"
