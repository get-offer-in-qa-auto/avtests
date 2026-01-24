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

## Доступ браузеров к приложению на хосте

Если ваше приложение работает на хосте Windows или другом компьютере в сети и браузеры в контейнерах не могут к нему подключиться (ошибка `ERR_CONNECTION_REFUSED`):

### Автоматическое решение (уже реализовано)

Код в `BaseUiTest` автоматически определяет, если `uiBaseUrl` содержит IP адрес вида `192.168.x.x`, и:
1. Добавляет `hostsEntries` capability для маппинга `app-host.local` на этот IP
2. Автоматически заменяет IP на `app-host.local` в `baseUrl`

**Никаких дополнительных действий не требуется** - просто используйте IP адрес в `config.properties`:
```
uiBaseUrl=http://192.168.0.16:3000
```

### Альтернативные варианты

#### Вариант 1: Использовать host.docker.internal (если приложение на хосте Windows)

Если приложение работает на хосте Windows, можно использовать специальное DNS имя:
```
uiBaseUrl=http://host.docker.internal:3000
```

`host.docker.internal` - это специальное DNS имя в Docker Desktop для Windows, которое автоматически разрешается в IP адрес хоста.

#### Вариант 2: Настроить extra_hosts в browsers.json (ручная настройка)

Если автоматическое решение не работает, можно настроить `extra_hosts` в `C:\selenoid\browsers.json`:

```json
{
  "chrome": {
    "default": "128.0",
    "versions": {
      "128.0": {
        "image": "selenoid/chrome:128.0",
        "port": "4444",
        "path": "/",
        "volumes": [
          "/dev/shm:/dev/shm"
        ],
        "extra_hosts": [
          "app-host.local:192.168.0.16"
        ]
      }
    }
  }
}
```

И затем использовать `http://app-host.local:3000` в конфигурации.
