$ErrorActionPreference = "Stop"
$Namespace = "sgitu"

kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-configmap.yaml
kubectl apply -f k8s/02-secret.yaml

# --- Strimzi check (safe probe) ---
$strimziInstalled = $true
try {
    kubectl get crd kafkas.kafka.strimzi.io -n $Namespace 2>$null | Out-Null
} catch {
    $strimziInstalled = $false
}

if (-not $strimziInstalled) {
    Write-Host "Installing Strimzi operator..."
    helm repo add strimzi https://strimzi.io/charts/ | Out-Null
    helm repo update | Out-Null
    helm upgrade --install strimzi-kafka-operator strimzi/strimzi-kafka-operator `
        --namespace $Namespace `
        --set watchNamespaces="{$Namespace}"

    Write-Host "Waiting for Strimzi CRDs..."
    kubectl wait --for=condition=Established `
        crd/kafkas.kafka.strimzi.io `
        --timeout=120s

    Write-Host "Waiting for Strimzi operator..."
    kubectl rollout status deployment/strimzi-cluster-operator `
        -n $Namespace --timeout=120s
} else {
    Write-Host "Strimzi operator already installed. Ensuring CRDs are ready..."
    kubectl wait --for=condition=Established `
        crd/kafkas.kafka.strimzi.io `
        --timeout=30s 2>$null
}

# Refresh local kubectl cache to ensure CRDs are recognized
kubectl api-resources > $null

# --- Kafka & Redis ---
Write-Host "Applying Kafka and Redis resources..."
kubectl apply -f k8s/kafka/
kubectl apply -f k8s/redis/

Write-Host "Waiting for Kafka to be ready (this takes ~2min on Minikube)..."
kubectl wait kafka --all `
    --for=condition=Ready `
    --timeout=300s `
    -n $Namespace

kubectl rollout status deployment/redis -n $Namespace --timeout=120s

# --- Databases ---
Write-Host "Applying database resources..."
$dbDirs = @(
    "k8s/service-abonnement/db",
    "k8s/service-analytique/db",
    "k8s/service-paiement/db",
    "k8s/service-utilisateur/db"
)
foreach ($dbDir in $dbDirs) {
    kubectl apply -f $dbDir
    Start-Sleep -Seconds 5
}

# --- Application services ---
Write-Host "Applying application services..."
$serviceDirs = @(
    "k8s/service-utilisateur",
    "k8s/service-paiement",
    "k8s/service-analytique",
    "k8s/service-abonnement"
)
foreach ($serviceDir in $serviceDirs) {
    kubectl apply -f $serviceDir
}

kubectl get pods -n $Namespace