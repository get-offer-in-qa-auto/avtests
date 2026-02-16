#!/bin/bash
# Quality gate: fail if API coverage < 50%
# Uses jq to parse swagger-coverage-results.json

set -e

COVERAGE_JSON="${1:-swagger-coverage-results.json}"
MIN_COVERAGE="${2:-50}"

if [ ! -f "$COVERAGE_JSON" ]; then
  echo "ERROR: $COVERAGE_JSON not found. Run Swagger coverage first."
  exit 1
fi

# Extract counters: full, party, empty operations
FULL=$(jq -r '.coverageOperationMap.counter.full // 0' "$COVERAGE_JSON")
PARTY=$(jq -r '.coverageOperationMap.counter.party // 0' "$COVERAGE_JSON")
ALL=$(jq -r '.coverageOperationMap.counter.all // 1')

# API coverage % = (full + partial) / all * 100
COVERED=$((FULL + PARTY))
if [ "$ALL" -eq 0 ]; then
  COVERAGE_PCT=0
else
  COVERAGE_PCT=$((COVERED * 100 / ALL))
fi

echo "API Coverage: $COVERAGE_PCT% (full=$FULL, partial=$PARTY, total=$ALL)"
echo "Required: >= $MIN_COVERAGE%"

if [ "$COVERAGE_PCT" -lt "$MIN_COVERAGE" ]; then
  echo "FAIL: API coverage $COVERAGE_PCT% is below $MIN_COVERAGE%"
  exit 1
fi

echo "PASS: API coverage meets quality gate"
