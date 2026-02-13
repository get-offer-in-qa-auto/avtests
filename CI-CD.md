# CI/CD

## Сборка и публикация Docker-образа

Workflow **Build and Push Docker Image** (`build-and-push-docker.yml`):

- **Триггер:** push в любую ветку при изменениях в `src/`
- **Этапы:** Maven build → Checkstyle → Docker build → Push в Docker Hub
- **Тег образа:** `{DOCKERHUB_USERNAME}/nbank-tests:{commit-hash}`

### Настройка

**Вариант 1 — через GitHub CLI:**
```powershell
# winget install GitHub.cli   # если не установлен
.\scripts\setup-docker-secrets.ps1 -Username zeeero -Token "dckr_pat_YOUR_TOKEN"
```

**Вариант 2 — вручную:** Settings → Secrets and variables → Actions → New repository secret:

| Секрет           | Значение                         |
|------------------|----------------------------------|
| DOCKERHUB_USERNAME | zeeero                         |
| DOCKERHUB_TOKEN   | Access Token из Docker Hub     |

### Ручной запуск

Через **Actions → Build and Push Docker Image → Run workflow** (workflow_dispatch).
