
set -e

# === Переменные ===
DOCKERHUB_USERNAME="${DOCKERHUB_USERNAME:-zeeero}"
IMAGE_NAME="${IMAGE_NAME:-nbank-tests}"
TAG="${TAG:-latest}"

# Локальное имя образа 
LOCAL_IMAGE="${LOCAL_IMAGE:-${IMAGE_NAME}:${TAG}}"
REMOTE_IMAGE="${DOCKERHUB_USERNAME}/${IMAGE_NAME}:${TAG}"

# Токен берём из переменной окружения
if [ -z "${DOCKERHUB_TOKEN}" ]; then
    echo "Ошибка: переменная DOCKERHUB_TOKEN не задана."
    echo "Задайте её перед запуском:"
    echo "  export DOCKERHUB_TOKEN=your_token"
    echo "или добавьте в .env и выполните: source .env"
    exit 1
fi

echo ">>> Логин в Docker Hub (пользователь: ${DOCKERHUB_USERNAME})..."
echo "${DOCKERHUB_TOKEN}" | docker login -u "${DOCKERHUB_USERNAME}" --password-stdin

echo ">>> Тегирование образа: ${LOCAL_IMAGE} -> ${REMOTE_IMAGE}"
docker tag "${LOCAL_IMAGE}" "${REMOTE_IMAGE}"

echo ">>> Пуш образа в Docker Hub..."
docker push "${REMOTE_IMAGE}"

echo ""
echo "=== Готово ==="
echo "Образ опубликован: ${REMOTE_IMAGE}"
echo ""
echo "Скачать образ можно командой:"
echo "  docker pull ${REMOTE_IMAGE}"
echo ""
