#!/bin/bash
# Сохранение результатов выполнения команд kubectl для отчёта/ПР.
# Запускать из infra/kube после: minikube start, helm install nbank ./nbank-chart
# Результаты пишутся в kubectl-results.txt

OUTPUT_FILE="${1:-kubectl-results.txt}"

{
  echo "=== kubectl get svc ==="
  kubectl get svc
  echo ""
  echo "=== kubectl get pods ==="
  kubectl get pods
  echo ""
  echo "=== kubectl get deployment ==="
  kubectl get deployment
  echo ""
  echo "=== kubectl get configmap ==="
  kubectl get configmap
  echo ""
  echo "=== helm list ==="
  helm list
  echo ""
  echo "=== kubectl logs deployment/backend (tail=15) ==="
  kubectl logs deployment/backend --tail=15 2>/dev/null || echo "(под ещё не готов)"
  echo ""
  echo "=== kubectl scale deployment/backend --replicas=2 ==="
  kubectl scale deployment/backend --replicas=2
  sleep 15
  echo ""
  echo "=== kubectl get pods после scale backend=2 ==="
  kubectl get pods
  echo ""
  echo "=== kubectl scale deployment/backend --replicas=1 ==="
  kubectl scale deployment/backend --replicas=1
  echo ""
  echo "=== kubectl get pods (финальное состояние) ==="
  kubectl get pods
} > "$OUTPUT_FILE" 2>&1

echo "Результаты сохранены в $OUTPUT_FILE"
