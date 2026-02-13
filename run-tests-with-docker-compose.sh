# Запуск всех API и UI тестов в Docker-окружении.
#
#
# Что делает скрипт:
# 1. Поднимает тестовое окружение (docker compose up -d)
# 2. Запускает контейнер с тестами
# 3. Останавливает окружение (в т.ч. при ошибке)


set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
COMPOSE_FILE="${SCRIPT_DIR}/infra/docker-compose.yml"
PROJECT_DIR="${SCRIPT_DIR}/infra"
TEST_IMAGE="zeeero/avtests"

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() { echo -e "${GREEN}[INFO]${NC} $1"; }
log_step() { echo -e "${YELLOW}[STEP]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Вызов docker compose down при выходе
cleanup() {
    local exit_code=$?
    log_step "Остановка окружения (docker compose down)..."
    if docker compose -f "${COMPOSE_FILE}" --project-directory "${PROJECT_DIR}" down 2>/dev/null; then
        log_info "Окружение остановлено."
    else
        log_error "Не удалось остановить окружение."
    fi
    exit $exit_code
}

trap cleanup EXIT

# --- 1. Поднятие окружения ---
log_step "Поднятие тестового окружения (docker compose up -d)..."
docker compose -f "${COMPOSE_FILE}" --project-directory "${PROJECT_DIR}" up -d

log_info "Ожидание готовности сервисов (15 сек)..."
sleep 15

# --- 2. Запуск контейнера с тестами ---
log_step "Обновление образа с тестами (docker pull)..."
if ! docker pull "${TEST_IMAGE}" 2>/dev/null; then
    log_info "Образ не найден в Docker Hub. Сборка локально из Dockerfile..."
    docker build -t "${TEST_IMAGE}" "${SCRIPT_DIR}"
fi

log_step "Запуск контейнера с тестами..."

# Предотвращаем преобразование путей Git Bash в Windows-пути при запуске на Windows
MSYS_NO_PATHCONV=1 docker run --rm \
    --network nbank-network \
    -e SHELL=/bin/bash \
    -e APIBASEURL=http://backend:4111 \
    -e UIBASEURL=http://nginx:80 \
    -e UIREMOTE=http://selenoid:4444/wd/hub \
    -e SELENOID_URL=http://selenoid:4444 \
    -e SELENOID_UI_URL=http://selenoid-ui:8080 \
    "${TEST_IMAGE}" \
    /bin/bash -c "mvn test -P api && mvn test -P ui"

