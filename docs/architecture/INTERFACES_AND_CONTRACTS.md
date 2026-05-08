# Documentation des Interfaces et Contrats - Service-Abonnement

## 📋 Table des Matières
1. [Interfaces Fournies](#interfaces-fournies)
2. [Interfaces Requises](#interfaces-requises)
3. [Composants Internes](#composants-internes)
4. [Composants Externes](#composants-externes)
5. [Patterns de Communication](#patterns-de-communication)

---

## Interfaces Fournies

### 🔵 REST API Endpoints - Subscription Management

```
POST   /abonnements/souscrire
├─ Description: Souscrire à un nouvel abonnement
├─ Paramètres: userId (Long), planId (Long)
├─ Réponse: Abonnement (201 Created)
├─ Errors: 400 (Invalid), 404 (Not Found), 502 (Payment Error)
└─ Contrat: {"id": "...", "userId": "...", "status": "ACTIVE"}

GET    /abonnements/{id}
├─ Description: Récupérer un abonnement par ID
├─ Réponse: Abonnement (200 OK)
└─ Errors: 404 (Not Found)

GET    /abonnements?userId={userId}
├─ Description: Lister tous les abonnements d'un utilisateur
├─ Pagination: Pageable
└─ Réponse: Page<Abonnement>

DELETE /abonnements/{id}
├─ Description: Annuler un abonnement
├─ Réponse: 204 No Content
└─ Errors: 404 (Not Found), 409 (Invalid State)

PUT    /abonnements/{id}/renouveler
├─ Description: Renouveler un abonnement existant
├─ Réponse: Renouvellement (200 OK)
└─ Errors: 404 (Not Found), 402 (Payment Failed)
```

### 🔵 REST API Endpoints - Plan Management

```
POST   /plans
├─ Description: Créer un nouveau plan d'abonnement
├─ Body: CreatePlanRequest {name, price, duration, ...}
├─ Réponse: PlanAbonnement (201 Created)
└─ Errors: 400 (Invalid Input), 409 (Duplicate)

GET    /plans
├─ Description: Lister tous les plans disponibles
├─ Paramètres: categorie (optional), duree (optional)
└─ Réponse: List<PlanAbonnement> (200 OK)

GET    /plans/{id}
├─ Description: Récupérer un plan par ID
└─ Réponse: PlanAbonnement (200 OK)

PUT    /plans/{id}
├─ Description: Mettre à jour un plan
└─ Réponse: PlanAbonnement (200 OK)
```

### 🔵 Kafka Topics Published

**abonnement.souscription**
```json
{
  "event_id": "uuid",
  "event_type": "SUBSCRIPTION_CREATED",
  "timestamp": "2026-05-08T10:30:00Z",
  "user_id": 123,
  "plan_id": 5,
  "subscription_id": 999,
  "amount": 49.99,
  "currency": "MAD"
}
```

**abonnement.renouvellement**
```json
{
  "event_id": "uuid",
  "event_type": "SUBSCRIPTION_RENEWED",
  "timestamp": "2026-05-08T10:30:00Z",
  "subscription_id": 999,
  "new_expiry_date": "2026-06-08",
  "amount": 49.99
}
```

**abonnement.annulation**
```json
{
  "event_id": "uuid",
  "event_type": "SUBSCRIPTION_CANCELLED",
  "timestamp": "2026-05-08T10:30:00Z",
  "subscription_id": 999,
  "reason": "USER_REQUESTED",
  "refund_amount": 0
}
```

**abonnement.modification**
```json
{
  "event_id": "uuid",
  "event_type": "SUBSCRIPTION_MODIFIED",
  "timestamp": "2026-05-08T10:30:00Z",
  "subscription_id": 999,
  "changes": {"status": ["ACTIVE", "SUSPENDED"]}
}
```

**abonnement.suspension**
```json
{
  "event_id": "uuid",
  "event_type": "SUBSCRIPTION_SUSPENDED",
  "subscription_id": 999,
  "reason": "PAYMENT_FAILED",
  "suspension_date": "2026-05-08"
}
```

---

## Interfaces Requises

### 🔴 Service-Utilisateur (Feign Client)

**Endpoint: GET /users/{id}**
```
URL: http://service-utilisateur:8081/users/{id}
Réponse: UserDTO {
  "id": 123,
  "email": "user@example.com",
  "nom": "Dupont",
  "prenom": "Jean",
  "telephone": "+212...",
  "adresse": "...",
  "status": "ACTIF"
}
Timeout: 3000ms
Retry: 3 attempts
Circuit Breaker: Open after 5 failures in 10s window
```

### 🔴 Service-Paiement (Feign Client)

**Endpoint: POST /api/v1/payments/process**
```
URL: http://service-paiement:8083/api/v1/payments/process
Body: PaymentRequestDTO {
  "user_id": 123,
  "amount": 49.99,
  "currency": "MAD",
  "subscription_id": 999,
  "payment_method": "CARD"
}
Réponse: PaymentResponseDTO {
  "transaction_id": "TXN-123",
  "status": "SUCCESS|PENDING|FAILED",
  "amount": 49.99,
  "timestamp": "2026-05-08T10:30:00Z"
}
Timeout: 5000ms
```

**Endpoint: GET /api/v1/payments/{transactionId}**
```
Vérifier le statut d'une transaction
```

**Endpoint: POST /api/v1/payments/refund**
```
Traiter un remboursement
```

### 🔴 Service-Analyse (Feign Client)

**Endpoint: POST /api/events/batch**
```
URL: http://service-analyse:8087/api/events/batch
Body: List<AnalyseEventDTO> [
  {
    "event_type": "SUBSCRIPTION_CREATED",
    "entity_id": 999,
    "entity_type": "ABONNEMENT",
    "timestamp": "...",
    "metadata": {...}
  }
]
Réponse: 200 OK
Timeout: 2000ms
```

### 🔴 MySQL Database (JPA)

**Tables:**
- `abonnement` - Abonnements actifs/inactifs
- `plan_abonnement` - Plans d'abonnement disponibles
- `renouvellement` - Historique des renouvellements
- `paiement` - Transactions de paiement
- `desactivation` - Raisons de désactivation
- `analytique_trace` - Logs analytiques

---

## Composants Internes

### Layer 1: Controllers (REST Endpoints)

| Composant | Responsabilité | Dépend de |
|-----------|---|---|
| **AbonnementController** | Gestion CRUD des abonnements | AbonnementService |
| **PlanAbonnementController** | Gestion des plans | PlanAbonnementService |
| **AdminAbonnementController** | Opérations administrateur | AbonnementService, PlanAbonnementService |

### Layer 2: Services (Business Logic)

| Composant | Responsabilité | Dépend de |
|-----------|---|---|
| **AbonnementService** | Logique métier abonnements | Repos, Clients, Publisher |
| **PlanAbonnementService** | Gestion des plans | PlanAbonnementRepository |
| **AnalytiqueScheduler** | Tâches périodiques | AbonnementRepository |

### Layer 3: Integration Clients (Feign)

| Composant | Appel vers | Méthodes |
|-----------|---|---|
| **UtilisateurServiceClient** | Service-Utilisateur | getUserById() |
| **PaiementClient** | Service-Paiement | initierPaiement(), verifierPaiement(), rembourser() |
| **AnalyseClient** | Service-Analyse | sendEvents() |

### Layer 4: Event Producer

| Composant | Topics | Type |
|-----------|---|---|
| **SubscriptionEventPublisher** | 7 topics Kafka | Producer asynchrone |

### Layer 5: Repositories (Data Access)

| Composant | Entité | Opérations |
|-----------|---|---|
| **AbonnementRepository** | Abonnement | CRUD + Queries |
| **PlanAbonnementRepository** | PlanAbonnement | CRUD + Queries |
| **RenouvellementRepository** | Renouvellement | CRUD |
| **PaiementRepository** | Paiement | Read + Write |
| **DesactivationRepository** | Desactivation | CRUD |
| **AnalytiqueTraceRepository** | AnalytiqueTrace | Write |

---

## Composants Externes

### 🗄️ Bases de Données

| BD | Utilisée par | Tables |
|---|---|---|
| **MySQL** | AbonnementService | 6 tables |

### 📨 Message Broker

| Broker | Rôle | Topics |
|---|---|---|
| **Apache Kafka** | Async Events | 7 topics abonnement.* |

### 🔗 Services Microservices

| Service | Protocole | Utilisation |
|---|---|---|
| **Service-Utilisateur** | REST (Feign) | Validation/Info utilisateurs |
| **Service-Paiement** | REST (Feign) | Traitement des paiements |
| **Service-Analyse** | REST (Feign) | Envoi d'événements |

---

## Patterns de Communication

### 1️⃣ REST Synchrone (Feign)

```
Service-Abonnement → Service-Utilisateur
├─ Timing: Immédiat (3s timeout)
├─ Fallback: Circuit breaker après 5 défaillances
├─ Cas d'usage: Validation utilisateur avant souscription
└─ Risque: Couplage fort, Single Point of Failure
```

### 2️⃣ Kafka Asynchrone (Event Producer)

```
Service-Abonnement → Kafka → Service-Analyse / Service-Notification
├─ Timing: Asynchrone (eventual consistency)
├─ Découplage: Complet
├─ Cas d'usage: Analytics, notifications
└─ Avantage: Scalabilité, résilience
```

### 3️⃣ Database Direct (JPA)

```
Service-Abonnement → MySQL
├─ Performance: Très rapide
├─ Scope: Données locales
└─ Limitation: Pas de partage inter-service
```

---

## Diagrammes Associés

- [Architecture globale](./component-diagram.puml) - Tous les services
- [Architecture interne](./internal-architecture.puml) - Détail Service-Abonnement
- [Diagrammes de séquence](./sequences/) - Workflows spécifiques

---

## Checklist pour Mettre à Jour ce Document

- [ ] Nouvelle API endpoint ajouté
- [ ] Nouveau client Feign créé
- [ ] Nouveau topic Kafka ajouté
- [ ] Dépendance externe modifiée
- [ ] Configuration de timeout changée
- [ ] Nouveau composant interne
- [ ] SLA modifié
