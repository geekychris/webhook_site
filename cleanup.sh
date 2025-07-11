#!/bin/bash
set -e

echo "Cleaning up Kubernetes resources..."
kubectl delete -f k8s/app.yaml --ignore-not-found
kubectl delete -f k8s/postgres.yaml --ignore-not-found
kubectl delete -f k8s/secret.yaml --ignore-not-found
kubectl delete -f k8s/configmap.yaml --ignore-not-found

echo "Cleanup completed successfully!"
