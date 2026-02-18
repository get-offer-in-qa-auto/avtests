#!/bin/bash
# Проверка работы сервисов и пример масштабирования.
# Запускать из каталога infra/kube (после helm install nbank ./nbank-chart).

set -e

echo "=== 1. Список сервисов (kubectl get svc) ==="
kubectl get svc

echo ""
echo "=== 2. Список подов (kubectl get pods) ==="
kubectl get pods

echo ""
echo "=== 3. ConfigMap selenoid-config ==="
kubectl get configmap selenoid-config -o yaml

echo ""
echo "=== 4. Масштабирование backend до 2 реплик (kubectl scale) ==="
kubectl scale deployment/backend --replicas=2

echo "Ожидание готовности подов (30 сек)..."
sleep 30

echo ""
echo "=== 5. Поды после масштабирования backend ==="
kubectl get pods -l app=backend
kubectl get deployment backend

echo ""
echo "=== 6. Логи backend (первый под) ==="
kubectl logs deployment/backend --tail=20

echo ""
echo "=== 7. Возврат backend к 1 реплике ==="
kubectl scale deployment/backend --replicas=1

echo ""
echo "Проверка логов других сервисов:"
echo "  kubectl logs deployment/frontend --tail=10"
echo "  kubectl logs deployment/selenoid --tail=10"
echo "  kubectl logs deployment/selenoid-ui --tail=10"
echo ""
echo "Проброс портов (запустить в отдельном терминале или в фоне):"
echo "  kubectl port-forward svc/frontend 3000:80 &"
echo "  kubectl port-forward svc/backend 4111:4111 &"
echo "  kubectl port-forward svc/selenoid 4444:4444 &"
echo "  kubectl port-forward svc/selenoid-ui 8080:8080 &"
