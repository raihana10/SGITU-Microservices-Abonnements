# 📊 Guide Diagramme HYBRIDE - Service-Abonnement

## 🎯 Qu'est-ce que ce Diagramme?

C'est un **diagramme de composants UML HYBRIDE** qui montre:

1. **VUE INTERNE** ← Les 6 couches du Service-Abonnement
2. **VUE EXTERNE** ← Interactions avec autres microservices
3. **EN UN SEUL DIAGRAMME!**

```
🎯 OBJECTIF: Voir à la fois la structure interne ET les interactions externes
```

---

## 📁 Où se Trouve le Fichier?

```
service-abonnement/docs/hybrid-component-diagram.puml
```

**Pour générer l'image PNG/SVG:**
```bash
plantuml -o ../docs/diagrams service-abonnement/docs/hybrid-component-diagram.puml
```

---

## 🔍 Structure du Diagramme

### Zone 1: Acteurs (Haut Gauche)
```
👤 Abonné
👨‍💼 Superviseur
🔧 Admin
```
**Qui utilise le système**

### Zone 2: Entrée (API Gateway)
```
🔗 API Gateway (G10)
```
**Point d'entrée unique**

### Zone 3: Service-Abonnement (CENTRE - PRINCIPAL)

**LAYER 1: REST Controllers** (Réception requêtes)
```
├─ AbonnementController
│  ├─ POST /souscrire
│  ├─ GET /abonnements
│  └─ DELETE /annuler
│
├─ PlanAbonnementController
│  ├─ GET /plans
│  ├─ POST /plans
│  └─ PUT /plans/{id}
│
└─ AdminAbonnementController
   └─ Admin operations
```

**LAYER 2: Business Logic** (Logique métier)
```
├─ AbonnementService
│  ├─ souscrire()
│  ├─ renouveler()
│  ├─ annuler()
│  └─ valider()
│
├─ PlanAbonnementService
│  ├─ createPlan()
│  ├─ getPlan()
│  └─ updatePlan()
│
└─ AnalytiqueScheduler
   ├─ periodic tasks
   ├─ cleanup
   └─ alerts
```

**LAYER 3: Integration Clients** (Appels externes)
```
├─ UtilisateurServiceClient
│  ├─ → Service-Utilisateur
│  └─ getUser(), validateUser()
│
├─ PaiementClient
│  ├─ → Service-Paiement
│  └─ initierPaiement(), verifierPaiement(), rembourser()
│
└─ AnalyseClient
   ├─ → Service-Analyse
   └─ sendEvents()
```

**LAYER 4: Event Producer** (Publications asynchrones)
```
SubscriptionEventPublisher
├─ → Apache Kafka
└─ 7 Topics:
   ├─ abonnement.souscription
   ├─ abonnement.renouvellement
   ├─ abonnement.annulation
   ├─ abonnement.modification
   ├─ abonnement.suspension
   ├─ abonnement.desactivation
   └─ abonnement.rappel
```

**LAYER 5: Data Access** (Repositories)
```
├─ AbonnementRepository
├─ PlanAbonnementRepository
├─ RenouvellementRepository
├─ PaiementRepository
├─ DesactivationRepository
└─ AnalytiqueTraceRepository
```

**LAYER 6: Configuration & Security**
```
├─ JwtUtils + JwtAuthenticationFilter
├─ FeignConfig
└─ SecurityConfig + OpenApiConfig
```

### Zone 4: Persistence (Bas Centre)
```
💾 MySQL Database (abonnement_db)
Tables:
├─ abonnement
├─ plan_abonnement
├─ renouvellement
├─ paiement
├─ desactivation
└─ analytique_trace
```

### Zone 5: Message Broker
```
📨 Apache Kafka
7 Topics de communication asynchrone
```

### Zone 6: Microservices Collaborateurs (Droite)
```
🔵 Service-Utilisateur (8081)
🔵 Service-Paiement (8083)
🔵 Service-Analyse (8087)
🔵 Service-Notification
🔵 Service-Billetterie
```

---

## 🔗 Types de Connexions

### Ligne Solide → (Synchrone/REST)
```
AbonnementService → UtilisateurServiceClient → Service-Utilisateur
```
**Utilité:** Communication synchrone (appel direct HTTP)
**Timeout:** 3-5 secondes
**Risque:** Couplage fort

### Ligne Pointillée -..-> (Asynchrone/Kafka)
```
SubscriptionEventPublisher → Kafka → Service-Notification
```
**Utilité:** Communication asynchrone (eventual consistency)
**Timing:** Délai possible
**Avantage:** Découplage total

### Ligne Pointillée Fine ..-> (Configuration)
```
FeignConfig -..-> PaiementClient
```
**Utilité:** Configuration, not a direct call
**Timing:** Initialization

---

## 📊 Flux de Données: Cas "Souscrire à un Abonnement"

```
1️⃣ ACTEUR
   👤 Abonné → HTTP POST /abonnements/souscrire
                        ↓
2️⃣ GATEWAY
   🔗 API Gateway → Route vers /abonnements
                        ↓
3️⃣ CONTROLLER (LAYER 1)
   📌 AbonnementController.souscrire()
                        ↓
4️⃣ SERVICE (LAYER 2)
   ⚙️ AbonnementService.souscrire()
        ├─ Valide: UtilisateurServiceClient.getUserById()
        │           ↓
        │      Service-Utilisateur (GET /users/{id})
        │
        ├─ Initie: PaiementClient.initierPaiement()
        │           ↓
        │      Service-Paiement (POST /payments)
        │
        └─ Crée: Repository.save()
                        ↓
5️⃣ REPOSITORY (LAYER 5)
   🗄️ AbonnementRepository.save()
                        ↓
6️⃣ DATABASE
   💾 MySQL INSERT INTO abonnement
                        ↓
7️⃣ EVENT PUBLISHER (LAYER 4)
   📤 publishEvent("abonnement.souscription")
                        ↓
8️⃣ KAFKA
   📨 Apache Kafka Topic
                        ↓
9️⃣ CONSUMERS
   🔔 Service-Notification (send email)
   📊 Service-Analyse (track event)
   🎫 Service-Billetterie (prepare ticket)
```

---

## ✅ Comment Utiliser ce Diagramme?

### Pour COMPRENDRE le service:
```
1. Lire le diagramme de haut en bas
2. Suivre une ligne verticale (le flux)
3. Comprendre où chaque composant intervient
```

### Pour DÉVELOPPER:
```
1. Identifier la couche où tu travailles
2. Voir quels composants l'appelent
3. Voir quels composants elle appelle
```

### Pour DÉBOGUER:
```
1. Tracer le problème à travers les couches
2. Identifier où l'erreur survient
3. Consulter l'intégration concernée
```

### Pour COMMUNIQUER:
```
1. Montrer le diagramme aux non-dev
2. Expliquer une couche à la fois
3. Pointer les flèches pour montrer les interactions
```

---

## 🎨 Légende des Couleurs

| Couleur | Signification | Composants |
|---------|---------------|-----------|
| 🟠 Orange | REST Controllers | 3 controllers |
| 🟢 Vert | Business Services | 3 services |
| 🟤 Marron | Integration Clients | 3 Feign clients |
| 🔴 Rose/Rouge | Event Publishing | 1 publisher + Kafka |
| 🔵 Bleu | External Services | 5 microservices |
| 🟣 Violet | Database | MySQL |
| 🟡 Jaune/Gold | Gateway | Entry point |

---

## 📋 Tableau Synthétique: Responsabilités par Couche

| Couche | Nombre | Responsabilité | Exemple |
|--------|--------|-----------------|---------|
| **LAYER 1** | 3 | Recevoir requêtes HTTP | GET /abonnements |
| **LAYER 2** | 3 | Logique métier | valider(), renouveler() |
| **LAYER 3** | 3 | Appeler autres services | getUserById() → Utilisateur |
| **LAYER 4** | 1 | Publier événements | Kafka Topics |
| **LAYER 5** | 6 | Accès données | CRUD Database |
| **LAYER 6** | 3 | Configuration | Security, JWT, Feign |

---

## 🔄 Interactions par Type

### ✅ Interactions Synchrones (REST/Feign)

Service-Abonnement **appelle** (dépend de):
```
→ Service-Utilisateur (valider utilisateur)
→ Service-Paiement (traiter paiement)
→ Service-Analyse (envoyer événements)
```

Service-Abonnement **est appelé par** (qui dépend de lui):
```
← Service-Billetterie (vérifier abonnements)
← Service-Notification (notifications)
← Clients (via API Gateway)
```

**Caractéristiques:**
- ✓ Immédiat (synchrone)
- ✗ Couplage fort
- ⚠️ Timeout risk
- ✓ Response directe

### ✅ Interactions Asynchrones (Kafka)

Service-Abonnement **publie** vers Kafka:
```
→ abonnement.souscription
→ abonnement.renouvellement
→ abonnement.annulation
→ abonnement.modification
→ abonnement.suspension
→ abonnement.desactivation
→ abonnement.rappel
```

Service-Abonnement **consomme** potentiellement:
```
← Événements d'autres services (optionnel)
```

**Caractéristiques:**
- ✓ Découplage complet
- ✓ Scalable
- ✓ Eventual consistency
- ⚠️ Latence possible

---

## 💡 Cas d'Usage du Diagramme

### Cas 1: Ajouter un Nouvel Endpoint

```
1. Ajouter au Controller (LAYER 1)
2. Appeler le Service (LAYER 2)
3. Modifier le Service si besoin
4. Mettre à jour le diagramme
5. Documenter la nouvelle route
```

### Cas 2: Appeler un Nouveau Service

```
1. Créer un Feign Client (LAYER 3)
2. Injecter dans le Service (LAYER 2)
3. Appeler le client depuis le Service
4. Documenter la nouvelle dépendance
5. Mettre à jour le diagramme
```

### Cas 3: Ajouter un Nouveau Topic Kafka

```
1. Modifier Event Publisher (LAYER 4)
2. Ajouter le topic
3. Documenter le schéma JSON
4. Créer les consumers externes
5. Mettre à jour le diagramme
```

### Cas 4: Ajouter un Nouveau Repository

```
1. Créer l'interface Repository (LAYER 5)
2. Créer l'entité JPA
3. Ajouter les migrations Flyway
4. Utiliser dans le Service (LAYER 2)
5. Mettre à jour le diagramme
```

---

## 🎯 Lecture Simplifiée (3 Niveaux)

### 🟢 NIVEAU 1: Vue Très Haute (2 min)
```
Acteurs
  ↓
API Gateway
  ↓
Service-Abonnement (boîte noire)
  ↓
Database + Kafka + Autres services
```

### 🟡 NIVEAU 2: Vue Interne (5 min)
```
Controllers (reçoivent)
  ↓
Services (décident)
  ↓
Repositories (persistent)
  ↓
Database (stockent)
```

### 🔴 NIVEAU 3: Vue Complète (10 min)
```
Tous les composants + interactions + flux détaillés
```

---

## ✨ Avantages de ce Diagramme HYBRIDE

✅ **Tout en un seul diagramme**
- Pas besoin de basculer entre plusieurs fichiers
- Vue complète du service

✅ **Montre les responsabilités**
- Chaque couche a un rôle clair
- Facile de localiser où ajouter du code

✅ **Montre les dépendances**
- Qui appelle qui
- Les couplages sont visibles

✅ **Facile à mettre à jour**
- Ajouter un composant = ajouter une boîte
- Ajouter une interaction = ajouter une flèche

✅ **Aide au debugging**
- Tracer le flux problématique
- Localiser l'erreur rapidement

---

## 🔧 Comment Modifier le Diagramme?

### Pour AJOUTER un Controller:
```plantuml
' LAYER 1: REST Controllers
component [MaController\n✓ GET /ma-route] as MA_CTRL <<component>>
```

### Pour AJOUTER une interaction:
```plantuml
MA_CTRL --> ABON_SVC : call()
```

### Pour AJOUTER un Feign Client:
```plantuml
component [MonServiceClient\n→ Mon-Service] as MON_CLIENT <<component>>
ABON_SVC --> MON_CLIENT : call()
MON_CLIENT -.-> MON_SVC : HTTP
```

### Pour AJOUTER un Topic Kafka:
```plantuml
EVENT_PUB: SubscriptionEventPublisher
├─ ... existing topics ...
└─ abonnement.new-topic
```

---

## 📌 Points Clés à Retenir

1. **6 Couches = 6 Responsabilités**
   - LAYER 1: Recevoir (Controllers)
   - LAYER 2: Décider (Services)
   - LAYER 3: Intégrer (Feign Clients)
   - LAYER 4: Publier (Event Publisher)
   - LAYER 5: Persister (Repositories)
   - LAYER 6: Configurer (Configuration)

2. **Types de Flux**
   - → = Synchrone (REST direct)
   - -..-> = Asynchrone (Kafka)
   - ..-> = Configuration

3. **Composants Critiques**
   - AbonnementService (logique centrale)
   - PaiementClient (dépendance critique)
   - Event Publisher (découplage)
   - Repositories (persistence)

4. **Dépendances Importantes**
   - ✓ Service-Utilisateur (validation)
   - ✓ Service-Paiement (paiements)
   - ✓ Service-Analyse (analytics)
   - ✓ Kafka (async events)
   - ✓ MySQL (persistence)

---

## 🚀 Prochaines Étapes

```
✅ Diagramme hybride créé
✅ Structure claire
✅ Interactions documentées

Ensuite:
☐ Générer PNG/SVG avec PlantUML
☐ Montrer à l'équipe
☐ Utiliser pour l'onboarding
☐ Mettre à jour quand tu ajoutes des composants
```

---

## 📞 FAQ Rapide

**Q: Par où je commence à lire?**
A: De haut en bas → Acteurs → Gateway → Service (6 couches) → DB/Kafka

**Q: Où je vois mes APIs?**
A: LAYER 1 Controllers (POST /souscrire, GET /abonnements, etc.)

**Q: Où je vois les appels à d'autres services?**
A: LAYER 3 Integration Clients (UtilisateurServiceClient, PaiementClient, etc.)

**Q: Où je vois les bases de données?**
A: LAYER 5 Repositories → MySQL Database

**Q: Où je vois les événements Kafka?**
A: LAYER 4 Event Publisher → Apache Kafka

**Q: Je dois ajouter une dépendance, par où je mets?**
A: LAYER 3 (créer un Feign Client)

---

## 📎 Fichiers Liés

- **PlantUML source:** `service-abonnement/docs/hybrid-component-diagram.puml`
- **Image générée:** `service-abonnement/docs/diagrams/hybrid-component-diagram.png` (à générer)
- **Vue interne simple:** `service-abonnement/docs/internal-architecture.puml`
- **Vue globale:** `docs/architecture/component-diagram.puml`
- **Documentation:** `docs/architecture/INTERFACES_AND_CONTRACTS.md`

---

**Document créé:** 8 Mai 2026
**Version:** 1.0 - Diagramme HYBRIDE Complet
**Status:** ✅ Prêt à l'emploi
