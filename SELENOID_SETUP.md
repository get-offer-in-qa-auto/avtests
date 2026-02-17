# Настройка Selenoid

## Проблема
Selenoid UI не мог подключиться к Selenoid из-за того, что контейнеры находились в разных сетях Docker и не могли разрешать имена друг друга.

## Решение
Используется пользовательская сеть Docker `selenoid-network`, в которой оба контейнера могут обращаться друг к другу по имени.

## Запуск через Docker Compose (рекомендуется)

```bash
docker-compose -f docker-compose-selenoid.yml up -d
```

## Ручной запуск

### 1. Создать пользовательскую сеть (если еще не создана)
```bash
docker network create selenoid-network
```

### 2. Запустить Selenoid
```bash
docker run -d --name selenoid \
  --network selenoid-network \
  -p 4444:4444 \
  -v C:/selenoid:/etc/selenoid \
  -v C:/selenoid/video:/opt/selenoid/video \
  -v /var/run/docker.sock:/var/run/docker.sock \
  aerokube/selenoid:latest \
  -conf /etc/selenoid/browsers.json \
  -video-dir /opt/selenoid/video
```

### 3. Запустить Selenoid UI
```bash
docker run -d --name selenoid-ui \
  --network selenoid-network \
  -p 8080:8080 \
  aerokube/selenoid-ui:latest \
  --selenoid-uri http://selenoid:4444
```

## Проверка работы

1. Selenoid: http://localhost:4444/status
2. Selenoid UI: http://localhost:8080

## Конфигурация браузеров

Конфигурация находится в `C:\selenoid\browsers.json`

## Важно

- Оба контейнера должны быть в одной сети Docker (`selenoid-network`)
- Selenoid UI должен использовать URL `http://selenoid:4444` (по имени контейнера, а не по IP или localhost)
