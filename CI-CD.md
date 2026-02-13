# CI/CD

## Сборка и публикация Docker-образа

Workflow **Build and Push Docker Image** (`build-and-push-docker.yml`):

- **Триггер:** push в любую ветку при изменениях в `src/`
- **Этапы:** Maven build → Checkstyle → Docker build → Push (если заданы секреты)
- **Образ:** `{DOCKERHUB_USERNAME}/avtests` (теги: `{commit-hash}`, `latest`)

### Настройка секретов (опционально)

Без секретов пайплайн всё равно отработает — соберёт образ локально (avtests:local). Push в Docker Hub выполняется **только** при наличии секретов.

Для push в Docker Hub добавьте в **Settings → Secrets → Actions**:
- `DOCKERHUB_USERNAME` — логин Docker Hub
- `DOCKERHUB_TOKEN` — Personal Access Token (Docker Hub → Settings → Personal access tokens)

### Ручной запуск

Через **Actions → Build and Push Docker Image → Run workflow** (workflow_dispatch).
