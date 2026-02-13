# CI/CD

## Сборка и публикация Docker-образа

Workflow **Build and Push Docker Image** (`build-and-push-docker.yml`):

- **Триггер:** push в любую ветку при изменениях в `src/`
- **Этапы:** Maven build → Checkstyle → Docker build → Push в Docker Hub
- **Тег образа:** `{DOCKERHUB_USERNAME}/nbank-tests:{commit-hash}`

### Настройка

В GitHub: **Settings → Secrets and variables → Actions** задать:

| Секрет        | Описание                              |
|---------------|----------------------------------------|
| DOCKERHUB_USERNAME | Логин в Docker Hub                 |
| DOCKERHUB_TOKEN    | Access Token (Settings → Security в Docker Hub) |

### Ручной запуск

Через **Actions → Build and Push Docker Image → Run workflow** (workflow_dispatch).
