#!/bin/bash
set -e

# Build the application with production profile
echo "Building application..."
./mvnw clean package -Pproduction

# Build Docker image
echo "Building Docker image..."
docker build -t webhooksite-app:latest .

# Apply Kubernetes manifests
echo "Deploying to Kubernetes..."
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/postgres.yaml
kubectl apply -f k8s/app.yaml

echo "Waiting for database to be ready..."
kubectl wait --for=condition=ready pod -l app=webhooksite-db --timeout=120s

echo "Waiting for application to be ready..."
kubectl wait --for=condition=ready pod -l app=webhooksite --timeout=120s

echo "Deployment completed successfully!"
echo "You can access the application at http://localhost:8083"
