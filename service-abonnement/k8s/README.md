# Service Abonnement Kubernetes Test Stack

## Prerequisites

- Minikube
- kubectl
- Helm
- Docker images for abonnement and its dependencies available to Minikube

## Manual Image Build (Minikube)

Since some images (User, Payment) are not available on Docker Hub, you must build them locally in your Minikube environment:

1. **Point your terminal to Minikube's Docker daemon:**
   ```powershell
   # PowerShell
   minikube -p minikube docker-env --shell powershell | Invoke-Expression
   ```
   *Note: For Bash, use `eval $(minikube docker-env)`*

2. **Build the images:**
   From the root of the project:
   ```bash
   docker build -t service-abonnement:latest ./service-abonnement
   docker build -t g3-user-service:1.0 ./service-utilisateur
   docker build -t sgitu/payment-service:1.0.0 ./service-paiement
   docker build -t sgitu/g8-analytics-service:latest ./service-analytique
   docker build -t api-gateway:latest ./api-gateway
   ```

## Start Minikube

```bash
minikube start --cpus=4 --memory=8192 --driver=docker
```

## Deploy

```bash
chmod +x deploy.sh
./deploy.sh
```

Run the script from the `service-abonnement` directory. It creates the `sgitu` namespace, installs Strimzi if needed, deploys Kafka, Redis, the required databases, the three downstream services abonnement calls, and `service-abonnement` last.

Kept services:

- `service-abonnement` with MySQL
- `service-utilisateur` with PostgreSQL
- `service-paiement` with MySQL
- `service-analytique` with MongoDB
- Kafka and Redis infrastructure

## Access the App

```bash
minikube service service-abonnement -n sgitu
```

## Logs

```bash
kubectl logs -n sgitu deploy/service-abonnement
kubectl logs -n sgitu deploy/service-utilisateur
kubectl logs -n sgitu deploy/service-paiement
kubectl logs -n sgitu deploy/service-analytique
```

Replace the deployment name with any service you want to inspect.

## Delete Everything

```bash
kubectl delete namespace sgitu
```
