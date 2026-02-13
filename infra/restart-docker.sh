#!/bin/bash

echo ">>> Остановить Docker Compose"
docker compose down

echo ">>> Docker pull все образы браузеров"

# Путь до файла
json_file="./config/browsers.json"

# Проверяем, что jq установлен
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

# Извлекаем все значения .image через jq
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")

# Пробегаем по каждому образу и выполняем docker pull
for image in $images; do
    echo "Pulling $image..."
    docker pull "$image"
done

echo ">>> Запуск Docker Compose (detached)"
docker compose up -d

echo ">>> Ожидание готовности backend (порт 4111)..."
for i in $(seq 1 60); do
  code=$(curl -s -o /dev/null -w "%{http_code}" --connect-timeout 2 --max-time 5 http://localhost:4111/actuator/health 2>/dev/null || echo "000")
  if [ "$code" = "200" ]; then
    echo "Backend готов."
    break
  fi
  if [ $i -eq 60 ]; then
    echo "Таймаут ожидания backend (последний ответ: $code)"
    exit 1
  fi
  sleep 1
done