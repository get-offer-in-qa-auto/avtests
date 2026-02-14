#!/usr/bin/env bash
# Генерация Allure-отчёта с сохранением истории прошлых прогонов.
# Использование: ./scripts/allure-report-with-history.sh
# Предварительно: mvn test -P ci (или mvn clean test -P ci)

set -e

ALLURE_RESULTS="target/allure-results"
ALLURE_REPORT="target/site/allure-maven-plugin"
HISTORY_DIR="allure-history"

# Копируем history из предыдущего отчёта в allure-results
if [ -d "$HISTORY_DIR" ]; then
  echo "Restoring Allure history from $HISTORY_DIR..."
  mkdir -p "$ALLURE_RESULTS/history"
  cp -r "$HISTORY_DIR/." "$ALLURE_RESULTS/history/"
fi

# Генерируем отчёт
mvn allure:report -q

# Сохраняем history для следующего прогона
if [ -d "$ALLURE_REPORT/history" ]; then
  echo "Saving Allure history to $HISTORY_DIR..."
  mkdir -p "$HISTORY_DIR"
  cp -r "$ALLURE_REPORT/history/." "$HISTORY_DIR/"
fi

echo "Report generated: $ALLURE_REPORT"
echo "Open with: allure serve $ALLURE_RESULTS  or  mvn allure:serve"
