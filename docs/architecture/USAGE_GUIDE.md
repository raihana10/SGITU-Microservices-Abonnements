# 📖 Guide d'Utilisation - Diagrammes de Composants UML

## 📌 Vue d'Ensemble

Ce guide explique comment utiliser et maintenir les diagrammes de composants UML pour votre architecture microservices SGITU.

---

## 📂 Structure des Fichiers

```
docs/architecture/
├── component-diagram.puml              # 🟢 Diagramme global de tous les services
├── INTERFACES_AND_CONTRACTS.md         # 📋 Contrats détaillés (API, Kafka, DB)
├── ARCHITECTURE_SUMMARY.md             # 📊 Synthèse rapide et vérifiée
└── USAGE_GUIDE.md                      # 📖 Ce fichier

service-abonnement/docs/
└── internal-architecture.puml          # 🟡 Architecture interne détaillée
```

---

## 🎯 Comment Lire les Diagrammes

### Diagramme Global (component-diagram.puml)

**Niveaux du diagramme:**

1. **Clients & Infrastructure** (Haut)
   - Web Browser, Mobile App, Admin Portal
   - Points d'entrée de l'utilisateur

2. **API Gateway** (Centre-Haut)
   - Routage central
   - Authentification
   - Load balancing

3. **Microservices** (Centre)
   - 10 services organisés par fonction
   - **Service-Abonnement en VERT** (focus)
   - Autres services en BLEU/ORANGE

4. **Infrastructure** (Bas)
   - Bases de données MySQL
   - Apache Kafka pour messaging asynchrone
   - Redis cache (optionnel)
   - Docker & Monitoring

**Conventions visuelles:**
- `→` flèche pleine = Communication synchrone (REST/HTTP)
- `->>` flèche pointillée = Communication asynchrone (Kafka)
- Couleurs = Catégories (Orange=Gateway, Rouge=Kafka, Bleu=Services)

### Diagramme Interne (internal-architecture.puml)

**6 Couches du Service-Abonnement:**

```
Layer 1: Controllers      ← REST endpoints
    ↓ depend on
Layer 2: Services         ← Business logic
    ↓ depend on
Layer 3: Clients          ← External service calls (Feign)
Layer 4: Event Producer   ← Kafka publishing
Layer 5: Repositories     ← Data access (JPA)
    ↓ depend on
Layer 6: Configuration    ← Security, JWT, etc.
```

---

## 🔍 Identifier les Composants

### Composants INTERNES (appartiennent au service)

✅ **À inclure dans le service:**
- REST Controllers
- Service classes
- Repository interfaces
- Mapper/DTO classes
- Configuration classes
- Exception handlers

**Exemple: Service-Abonnement**
```
Internes:
├── AbonnementController (REST)
├── AbonnementService (Logique)
├── AbonnementRepository (Data)
└── Configuration (Sécurité, Feign)
```

### Composants EXTERNES (dépendances)

🔴 **À connecter comme dépendance:**
- Autres microservices
- Bases de données
- Message brokers (Kafka)
- Services tiers (SMS, Email)

**Exemple: Dépendances du Service-Abonnement**
```
Externes (appelés par ce service):
├── Service-Utilisateur (getUserById)
├── Service-Paiement (processPayment)
├── Service-Analyse (sendEvents)
├── MySQL Database (CRUD)
└── Apache Kafka (Events)
```

---

## 🔌 Identifier les INTERFACES

### Interfaces FOURNIES (Contrats sortants)

**❓ Question:** "Qu'est-ce que ce service OFFRE aux autres?"

**Réponse: Service-Abonnement fournit...**

```
REST Endpoints:
├── POST   /abonnements/souscrire         → Create subscription
├── GET    /abonnements/{id}              → Get subscription
├── GET    /abonnements?userId=X          → List user subscriptions
├── DELETE /abonnements/{id}              → Cancel subscription
└── PUT    /abonnements/{id}/renouveler   → Renew subscription

Kafka Topics Publiés:
├── abonnement.souscription               → New subscription
├── abonnement.renouvellement             → Renewal event
├── abonnement.annulation                 → Cancellation
└── abonnement.modification               → Any change
```

### Interfaces REQUISES (Contrats entrants)

**❓ Question:** "Qu'est-ce que ce service A BESOIN d'autres?"

**Réponse: Service-Abonnement requiert...**

```
Appels HTTP:
├── Service-Utilisateur → getUserById(id)
├── Service-Paiement    → initierPaiement(request)
└── Service-Analyse     → sendEvents(list)

Ressources:
├── MySQL Database      → Tables abonnements, plans, etc.
└── Apache Kafka        → Topics pour événements
```

---

## 🛠️ Comment Mettre à Jour les Diagrammes

### Scenario 1: Ajouter un NOUVEAU Service

**Pas:**

1. **Éditer component-diagram.puml**
   ```plantuml
   package "SUPPORT SERVICES" {
       component [Service-NewFeature\n(Port 8089)] as SNF <<Microservice>>
   }
   ```

2. **Ajouter les connexions**
   ```plantuml
   % Si ce service appelle d'autres services
   SNF --> SU : Feign\ngetUserById()
   
   % Si d'autres services l'appelent
   SA --> SNF : Feign\ncallNewFeature()
   ```

3. **Documenter dans INTERFACES_AND_CONTRACTS.md**
   ```markdown
   ### Service-NewFeature
   
   **Interfaces Fournies:**
   - POST /feature/create
   - GET /feature/{id}
   
   **Interfaces Requises:**
   - Service-Utilisateur
   - MySQL Database
   ```

4. **Ajouter à ARCHITECTURE_SUMMARY.md**
   ```markdown
   | Service-NewFeature | Port 8089 | GET/POST /feature |
   ```

### Scenario 2: Ajouter une NOUVELLE Interface REST

**Pas:**

1. **Éditer service-abonnement/docs/internal-architecture.puml**
   ```plantuml
   component [AbonnementController] as AC <<REST>>
   %% Ajouter une ligne pour la nouvelle route
   ```

2. **Documenter le contrat**
   ```markdown
   PUT    /abonnements/{id}/upgrade
   ├─ Description: Upgrade subscription plan
   ├─ Paramètres: planId (Long)
   ├─ Réponse: Abonnement (200 OK)
   └─ Errors: 404 (Not Found), 409 (Invalid State)
   ```

3. **Tester l'endpoint**
   ```bash
   curl -X PUT http://localhost:8082/abonnements/123/upgrade \
        -H "Content-Type: application/json" \
        -d '{"planId": 6}'
   ```

### Scenario 3: Ajouter un NOUVEAU Topic Kafka

**Pas:**

1. **Éditer component-diagram.puml**
   ```plantuml
   KAFKA["Apache Kafka<br/>Topics:<br/>...<br/>- abonnement.upgrade"]
   ```

2. **Éditer internal-architecture.puml**
   ```plantuml
   PUB["SubscriptionEventPublisher<br/>...<br/>- abonnement.upgrade"]
   ```

3. **Documenter le schéma**
   ```markdown
   **abonnement.upgrade**
   ```json
   {
     "event_id": "uuid",
     "event_type": "SUBSCRIPTION_UPGRADED",
     "subscription_id": 999,
     "old_plan_id": 5,
     "new_plan_id": 6,
     "additional_charge": 10.00
   }
   ```
   ```

### Scenario 4: Modifier une DÉPENDANCE

**Pas:**

1. **Identifier l'impact**
   ```
   Si je change Service-Paiement:
   ├─ Qui l'appelle?     → Service-Abonnement, Service-Billetterie
   └─ Qu'est-ce qui change? → Timeout? URL? Contrat API?
   ```

2. **Mettre à jour le diagramme**
   ```plantuml
   SA --> SP : Feign\nnewContractVersion()
   ```

3. **Documenter le changement**
   ```markdown
   **BREAKING CHANGE**: Timeout reduced from 5s to 3s
   - Service: Service-Paiement
   - Affects: Service-Abonnement
   - Action Required: Update FeignClient configuration
   ```

---

## 🔄 Flux de Mise à Jour

### Pour chaque changement architecture:

```
1. IDENTIFIER
   ├─ Quel composant change?
   ├─ Quels autres composants sont affectés?
   └─ C'est sync ou async?

2. MODIFIER les diagrammes
   ├─ component-diagram.puml (global)
   ├─ internal-architecture.puml (si Service-Abonnement)
   └─ Valider la syntaxe PlantUML

3. DOCUMENTER
   ├─ INTERFACES_AND_CONTRACTS.md
   ├─ ARCHITECTURE_SUMMARY.md
   └─ Ajouter commentaires dans le code

4. VALIDER
   ├─ Tester les connexions
   ├─ Vérifier les dépendances
   └─ Exécuter les tests

5. VERSIONNER
   ├─ git add docs/
   ├─ git commit -m "docs: update architecture - add feature X"
   └─ git push

6. COMMUNIQUER
   ├─ Notifier l'équipe
   ├─ Mettre à jour la documentation wiki
   └─ Présenter dans la prochaine review
```

---

## 🎨 Générer les Images

### Installer PlantUML

```bash
# Sur Windows (avec Chocolatey)
choco install plantuml

# Sur Mac (avec Homebrew)
brew install plantuml

# Ou télécharger: https://plantuml.com/download
```

### Générer les Diagrammes

```bash
# Un fichier
plantuml docs/architecture/component-diagram.puml

# Tous les fichiers
plantuml docs/architecture/*.puml

# Générer en PNG (default)
plantuml -o ./docs/diagrams component-diagram.puml

# Générer en SVG
plantuml -tsvg -o ./docs/diagrams component-diagram.puml

# Générer en PDF
plantuml -tpdf -o ./docs/diagrams component-diagram.puml
```

### Intégrer dans le CI/CD

```yaml
# .github/workflows/docs.yml
name: Generate Diagrams
on: [push]

jobs:
  generate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install PlantUML
        run: sudo apt-get install -y plantuml
      - name: Generate Diagrams
        run: plantuml -o ./docs/diagrams docs/architecture/*.puml
      - name: Commit changes
        run: |
          git add docs/diagrams/
          git commit -m "docs: auto-generate diagrams" || true
```

---

## ✅ Checklist de Validation

Avant de versionner les diagrammes:

- [ ] Tous les services visibles sur le diagramme global
- [ ] Tous les composants internes du Service-Abonnement représentés
- [ ] Les interfaces fournies et requises sont claires
- [ ] Les flèches pointent dans la bonne direction
- [ ] Les couleurs distinguent les catégories
- [ ] La documentation correspond aux diagrammes
- [ ] Les noms de services correspondent au code
- [ ] Les ports sont corrects
- [ ] Les topics Kafka sont à jour
- [ ] Pas d'erreurs PlantUML (validate avec `plantuml -validate`)

---

## 📚 Ressources

### PlantUML Documentation
- Component Diagram: https://plantuml.com/component-diagram
- Syntax Guide: https://plantuml.com/guide

### UML Standards
- Component Diagram: https://www.uml-diagrams.org/component-diagrams.html
- Interfaces: https://www.uml-diagrams.org/interface.html

### Outils en Ligne
- PlantUML Online: https://www.plantuml.com/plantuml/uml/
- Mermaid Editor: https://mermaid.live/

---

## 🆘 Dépannage

### PlantUML ne compile pas

```bash
# Vérifier la syntaxe
plantuml -validate component-diagram.puml

# Plus de détails
plantuml -debug component-diagram.puml

# Erreur commune: Import mal formé
# ❌ BAD: @import "diagram.puml"
# ✅ GOOD: !include diagram.puml
```

### Diagramme trop complexe

```
Solution 1: Créer plusieurs diagrammes
  - Diagramme global (tous les services)
  - Diagrammes détaillés (par domaine)
  
Solution 2: Organiser avec des packages
  - package "Core Services" { }
  - package "Support Services" { }
  
Solution 3: Simplifier les connexions
  - Afficher seulement les dépendances critiques
```

### Changements non reflétés

```
✓ Vérifier que la version PlantUML est à jour
✓ Vérifier que le fichier est sauvegardé
✓ Effacer le cache: rm *.png *.svg
✓ Régénérer: plantuml -o ./diagrams *.puml
```

---

## 📞 Questions Fréquentes

**Q: À quelle fréquence mettre à jour les diagrammes?**
A: À chaque changement architectural majeur (nouveau service, changement d'interface)

**Q: Les diagrammes doivent-ils être versionnés?**
A: Oui, les fichiers .puml et les images générées

**Q: Qui doit maintenir les diagrammes?**
A: L'architecte ou lead technique, avec validation du team

**Q: Comment versionner les changements d'API?**
A: Documenter dans INTERFACES_AND_CONTRACTS.md avec dates/versions

**Q: Peut-on avoir plusieurs versions des diagrammes?**
A: Oui, créer un dossier `docs/architecture/v1/` pour les versions anciennes

---

**Dernière mise à jour:** 8 Mai 2026
**Maintenu par:** Architecture Team
**Version:** 1.0
