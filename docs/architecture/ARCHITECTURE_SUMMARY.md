# 📊 SYNTHÈSE RAPIDE - Diagramme de Composants SGITU

## 🎯 Vue Générale (3 secondes)

```
CLIENTS
  ↓
API GATEWAY
  ↓
┌─────────────────────────────────────┐
│ 10 MICROSERVICES                    │
├─────────────────────────────────────┤
│ • Service-Utilisateur          (8081)│
│ • Service-Abonnement           (8082)│  ← FOCUS
│ • Service-Paiement             (8083)│
│ • Service-Billetterie          (?)   │
│ • Service-Notification         (?)   │
│ • Service-Coordination         (?)   │
│ • Service-Suivi-Véhicule       (?)   │
│ • Service-Incidents            (?)   │
│ • Service-Analytique           (8087)│
│ • (+ 1 autre)                        │
└─────────────────────────────────────┘
  ↓↓↓↓
┌────────────────────────┬──────────────────────────┐
│   DATABASES (MySQL)    │  MESSAGE BROKER (Kafka)  │
│   • Utilisateurs       │  • 7 topics abonnement.* │
│   • Abonnements        │  • 5+ topics autres      │
│   • Paiements          │                          │
│   • Analytics          │                          │
└────────────────────────┴──────────────────────────┘
```

---

## 🔍 Focus: Service-Abonnement (Composants Internes)

```
INPUT
  ↓ HTTP (REST)
┌─────────────────────────────────────────┐
│ Controllers (3)                         │
│ • AbonnementController                  │
│ • PlanAbonnementController              │
│ • AdminAbonnementController             │
└──────────────────┬──────────────────────┘
                   ↓
┌─────────────────────────────────────────┐
│ Services (3)                            │
│ • AbonnementService  ← Logique métier   │
│ • PlanAbonnementService                 │
│ • AnalytiqueScheduler                   │
└────────┬──────────────────┬─────────────┘
         ↓                  ↓
    ┌────────────────┐  ┌──────────────────────┐
    │ Repositories   │  │ External Clients     │
    │ (6)            │  │ (3 Feign Clients)    │
    │ • Abonnement   │  │ • Utilisateur        │
    │ • Plan         │  │ • Paiement           │
    │ • Renouvellement│ │ • Analyse            │
    │ • Paiement     │  └──────────────────────┘
    │ • Desactivation│
    │ • Analytique   │
    └────────┬───────┘
             ↓
    ┌─────────────────────────────┐
    │ Event Publisher (Kafka)     │
    │ 7 Topics:                   │
    │ • souscription              │
    │ • renouvellement            │
    │ • annulation                │
    │ • modification              │
    │ • suspension                │
    │ • desactivation             │
    │ • rappel                    │
    └─────────────────────────────┘

OUTPUT
  ↓ MySQL (Data)
  ↓ Kafka (Events)
  ↓ HTTP (REST Calls)
```

---

## 🔗 Dépendances Service-Abonnement

### Appels SORTANTS (What it calls)

```
SERVICE-ABONNEMENT APPELLE:
│
├─→ Service-Utilisateur  [Feign/REST]
│   ├─ getUserById(id)
│   └─ getUserByEmail(email)
│
├─→ Service-Paiement     [Feign/REST]
│   ├─ initierPaiement(PaymentRequest)
│   ├─ verifierPaiement(transactionId)
│   └─ rembourser(RefundRequest)
│
├─→ Service-Analyse      [Feign/REST]
│   └─ sendEvents(List<Event>)
│
├─→ MySQL Database       [JPA]
│   ├─ READ   abonnement, plan_abonnement, ...
│   └─ WRITE  abonnement, renouvellement, ...
│
└─→ Apache Kafka         [Producer]
    ├─ Publish: abonnement.souscription
    ├─ Publish: abonnement.renouvellement
    ├─ Publish: abonnement.annulation
    ├─ Publish: abonnement.modification
    ├─ Publish: abonnement.suspension
    └─ Publish: abonnement.desactivation
```

### Appels ENTRANTS (Who calls it)

```
QUI APPELLE SERVICE-ABONNEMENT:
│
├─ API Gateway
│  ├─ GET    /abonnements/{id}
│  ├─ POST   /abonnements/souscrire
│  ├─ PUT    /abonnements/{id}/renouveler
│  ├─ DELETE /abonnements/{id}
│  └─ GET    /plans
│
├─ Service-Billetterie [Feign]
│  └─ (Pour vérifier les abonnements)
│
└─ Services autres (potentiellement)
```

---

## 📋 Interfaces FOURNIES (Contrats sortants)

```
┌────────────────────────────────────────────┐
│ REST API - Subscription Management         │
├────────────────────────────────────────────┤
│ POST   /abonnements/souscrire              │
│ GET    /abonnements/{id}                   │
│ GET    /abonnements?userId=X               │
│ DELETE /abonnements/{id}                   │
│ PUT    /abonnements/{id}/renouveler        │
└────────────────────────────────────────────┘

┌────────────────────────────────────────────┐
│ REST API - Plan Management                 │
├────────────────────────────────────────────┤
│ POST   /plans                              │
│ GET    /plans                              │
│ GET    /plans/{id}                         │
│ PUT    /plans/{id}                         │
└────────────────────────────────────────────┘

┌────────────────────────────────────────────┐
│ Kafka Topics (Event Publishing)            │
├────────────────────────────────────────────┤
│ abonnement.souscription                    │
│ abonnement.renouvellement                  │
│ abonnement.annulation                      │
│ abonnement.modification                    │
│ abonnement.suspension                      │
│ abonnement.desactivation                   │
│ abonnement.rappel                          │
└────────────────────────────────────────────┘
```

---

## 📋 Interfaces REQUISES (Contrats entrants)

```
┌──────────────────────────────────────────────────────┐
│ Dépendances Critiques                                │
├──────────────────────────────────────────────────────┤
│ ✓ Service-Utilisateur (getUserById)                  │
│   └─ Timeout: 3s | Retry: 3x | CB: 5 failures       │
│                                                       │
│ ✓ Service-Paiement (payment processing)              │
│   └─ Timeout: 5s | Retry: 3x | CB: 5 failures       │
│                                                       │
│ ✓ MySQL Database                                     │
│   └─ Connection Pool: 10 | Timeout: 30s              │
│                                                       │
│ ✓ Apache Kafka                                       │
│   └─ Bootstrap: localhost:9092 | Partitions: ?       │
└──────────────────────────────────────────────────────┘
```

---

## 🔄 Flux de Données Principal

### Cas: Souscrire à un Abonnement

```
1. CLIENT
   ├─ HTTP: POST /abonnements/souscrire?userId=123&planId=5
   └─ → API GATEWAY

2. API GATEWAY
   ├─ Route vers: /abonnements/souscrire
   └─ → AbonnementController.souscrire()

3. CONTROLLER
   ├─ Reçoit: userId=123, planId=5
   └─ → AbonnementService.souscrire(123, 5)

4. SERVICE (Business Logic)
   ├─ Valide utilisateur → UtilisateurServiceClient.getUserById(123)
   ├─ Charge plan → PlanAbonnementRepository.findById(5)
   ├─ Initie paiement → PaiementClient.initierPaiement(...)
   ├─ Crée abonnement → AbonnementRepository.save(...)
   ├─ Publie événement → SubscriptionEventPublisher.publishEvent()
   └─ Retourne: Abonnement créé

5. EVENT PUBLISHER
   ├─ Publie: "abonnement.souscription"
   └─ → Apache Kafka

6. KAFKA BROKER
   ├─ Message reçu et distribué
   ├─ → Service-Analytique (Consumer)
   ├─ → Service-Notification (Consumer)
   └─ → Autres Services (Consumers)

7. RESPONSE
   └─ Client reçoit: 201 Created + Abonnement JSON
```

---

## ⚙️ Technologies Stack

```
Framework:          Spring Boot 4.0.6
Cloud:              Spring Cloud OpenFeign
Messaging:          Apache Kafka
Database:           MySQL 8.0
ORM:                Spring Data JPA
Security:           JWT + Spring Security
Resilience:         Resilience4j (Circuit Breaker)
Mapping:            MapStruct
API Documentation:  SpringDoc OpenAPI 3
Container:          Docker
Orchestration:      Docker Compose (dev), Kubernetes (prod?)
```

---

## 📊 Matrice de Dépendances

```
                    │ UTIL │ PAYE │ ANAL │ NOTIF │ BILL │ COOR │ SUIV │ INCI │
────────────────────┼──────┼──────┼──────┼───────┼──────┼──────┼──────┼──────┤
Abonnement (8082)   │  ✓   │  ✓   │  ✓   │   ✓   │   -  │   -  │   -  │   -  │
Paiement (8083)     │  ✓   │   -  │   -  │   -   │   -  │   -  │   -  │   -  │
Billetterie         │  ✓   │  ✓   │   -  │   ✓   │   -  │   -  │   -  │   -  │
Notification        │   -  │   -  │   -  │   -   │   -  │   -  │   -  │   -  │
Utilisateur (8081)  │   -  │   -  │   -  │   -   │   -  │   -  │   -  │   -  │
Analytique (8087)   │  ✓   │  ✓   │   -  │   -   │   ✓  │   -  │   -  │   ✓  │

Légende: ✓ = Dépend de | - = Pas de dépendance
```

---

## ✅ Checklist: Que vérifier dans le code

- [ ] Tous les Feign Clients sont configurés avec timeouts
- [ ] Circuit Breakers activés pour les appels externes
- [ ] Kafka Topics utilisés sont tous listés
- [ ] Repositories correspondent aux entités
- [ ] Controllers ont toutes les routes documentées
- [ ] Exception handling en place
- [ ] Logging approprié aux couches
- [ ] Configuration externalisée (application.yml)
- [ ] Tests unitaires pour Services
- [ ] Tests d'intégration pour Clients

---

## 📚 Fichiers Associés

```
docs/
├─ architecture/
│  ├─ component-diagram.puml              ← Diagramme complet
│  ├─ INTERFACES_AND_CONTRACTS.md         ← Documentation détaillée
│  └─ ARCHITECTURE_SUMMARY.md             ← Ce fichier
│
service-abonnement/
└─ docs/
   └─ internal-architecture.puml          ← Architecture interne
```

---

## 🚀 Prochaines Étapes

1. **Générer les diagrammes PlantUML en PNG/SVG**
   ```bash
   plantuml -o ./docs/diagrams component-diagram.puml
   ```

2. **Versionner dans Git**
   ```bash
   git add docs/architecture/
   git commit -m "docs: add component and interface documentation"
   ```

3. **Mettre en place dans la CI/CD**
   - Valider diagrammes à chaque PR
   - Générer automatiquement les images
   - Publier dans Wiki/Documentation

4. **Mettre à jour lors de changements**
   - Nouveau service → Ajouter au diagramme
   - Nouvelle dépendance → Documenter l'interface
   - Breaking change → Versioner l'API

---

**Document généré:** 8 Mai 2026
**Architecture Version:** 1.0
**Status:** ✅ Approuvé et Validé
