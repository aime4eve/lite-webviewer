#!/bin/bash
set -e

# Build Images (Optional, uncomment if needed)
# ./build-images.sh

# Apply Infrastructure
kubectl apply -f 00-namespace.yaml
kubectl apply -f 01-postgres.yaml
kubectl apply -f 02-redis.yaml
kubectl apply -f 03-kafka.yaml
kubectl apply -f 04-influxdb.yaml

echo "Waiting for infrastructure to be ready..."
sleep 10

# Apply Discovery & Gateway
kubectl apply -f 05-discovery.yaml
kubectl apply -f 06-gateway.yaml

# Apply Microservices
kubectl apply -f 10-device-service.yaml
kubectl apply -f 11-ai-service.yaml
kubectl apply -f 12-control-service.yaml
kubectl apply -f 13-subscription-service.yaml
kubectl apply -f 14-report-service.yaml

# Apply Frontend
kubectl apply -f 20-frontend.yaml

# Apply Ingress
kubectl apply -f 30-ingress.yaml

echo "Deployment applied successfully!"
echo "Check status with: kubectl get pods -n smartmoldguard"
