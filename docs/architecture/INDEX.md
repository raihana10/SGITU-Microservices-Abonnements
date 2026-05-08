# 📑 INDEX - Documentation Architecture Microservices SGITU

## 🎯 Quick Start (30 secondes)

Nouveau dans le projet? Suivez cet ordre:

1. **Lire:** [ARCHITECTURE_SUMMARY.md](#architecture-summary) (5 min)
2. **Voir:** [component-diagram.puml](#diagrammes-uml) (2 min)
3. **Comprendre:** [INTERFACES_AND_CONTRACTS.md](#interfaces-et-contrats) (10 min)
4. **Utiliser:** [USAGE_GUIDE.md](#guide-dutilisation) (au besoin)

---

## 📚 Documentation par Type

### 🟢 Pour Démarrer (Nouveaux Membres)

| Document | Durée | Contenu | Accès |
|----------|-------|---------|-------|
| **ARCHITECTURE_SUMMARY.md** | 5 min | Vue globale + schémas ASCII | [→](#architecture-summary) |
| **component-diagram.puml** | 2 min | Diagramme graphique | [→](#diagrammes-uml) |
| **USAGE_GUIDE.md - Comment Lire** | 5 min | Interpréter les diagrammes | [→](#guide-dutilisation) |

### 🔵 Pour Développeurs (Intégration)

| Document | Durée | Contenu | Accès |
|----------|-------|---------|-------|
| **INTERFACES_AND_CONTRACTS.md** | 15 min | APIs, Kafka topics, payloads | [→](#interfaces-et-contrats) |
| **internal-architecture.puml** | 5 min | Couches Service-Abonnement | [→](#diagrammes-uml) |
| **component-diagram.puml** | 5 min | Voir les dépendances du service | [→](#diagrammes-uml) |

### 🟠 Pour Architectes (Évolution)

| Document | Durée | Contenu | Accès |
|----------|-------|---------|-------|
| **USAGE_GUIDE.md - Mise à jour** | 10 min | Comment modifier les diagrammes | [→](#guide-dutilisation) |
| **INTERFACES_AND_CONTRACTS.md** | 20 min | Spécifications complètes | [→](#interfaces-et-contrats) |
| **Tous les diagrammes** | 30 min | Vue complète du système | [→](#diagrammes-uml) |

---

## 📂 Structure des Dossiers

```
SGITU-Microservices-Abonnements/
├── docs/
│   └── architecture/
│       ├── 📄 INDEX.md                          ← Vous êtes ici
│       ├── 📊 ARCHITECTURE_SUMMARY.md           ← Vue synthétique
│       ├── 📋 INTERFACES_AND_CONTRACTS.md       ← Contrats détaillés
│       ├── 📖 USAGE_GUIDE.md                    ← Comment utiliser
│       ├── 📈 component-diagram.puml            ← Diagramme global UML
│       └── diagrams/                            ← Images générées
│           ├── component-diagram.png
│           └── component-diagram.svg
│
└── service-abonnement/
    └── docs/
        └── 📈 internal-architecture.puml        ← Diagramme interne UML
```

---

## 📊 Diagrammes UML

### Diagramme Global (Tous les Services)

**Fichier:** `docs/architecture/component-diagram.puml`

**Montre:**
- ✓ Les 10 microservices du système
- ✓ API Gateway (entrée)
- ✓ Bases de données MySQL
- ✓ Apache Kafka (messaging)
- ✓ Infrastructure (Docker, Monitoring)
- ✓ Connexions REST et asynchrones

**À utiliser pour:**
- Comprendre la vue globale
- Ajouter/supprimer un service
- Identifier les dépendances
- Documenter l'architecture

**Générer l'image:**
```bash
plantuml -o ./docs/diagrams docs/architecture/component-diagram.puml
```

### Diagramme Interne (Service-Abonnement)

**Fichier:** `service-abonnement/docs/internal-architecture.puml`

**Montre:**
- ✓ Les 6 couches du service
- ✓ Les composants internes (Controllers, Services, Repos)
- ✓ Les clients Feign externes
- ✓ Le producer Kafka
- ✓ La configuration

**À utiliser pour:**
- Comprendre la structure interne
- Ajouter un nouveau composant
- Documenter les dépendances internes
- Onboarding développeurs

**Générer l'image:**
```bash
plantuml -o ./docs/diagrams service-abonnement/docs/internal-architecture.puml
```

---

## 📋 Interfaces et Contrats

**Fichier:** `docs/architecture/INTERFACES_AND_CONTRACTS.md`

### Sections

1. **Interfaces Fournies par Service-Abonnement**
   - REST Endpoints (Subscription Management)
   - REST Endpoints (Plan Management)
   - Kafka Topics publiés (7 topics)
   - Schémas JSON des événements

2. **Interfaces Requises par Service-Abonnement**
   - Service-Utilisateur (Feign Client)
   - Service-Paiement (Feign Client)
   - Service-Analyse (Feign Client)
   - MySQL Database
   - Apache Kafka

3. **Composants Internes**
   - Controllers (API endpoints)
   - Services (Business logic)
   - Feign Clients (External calls)
   - Event Producer (Kafka)
   - Repositories (Data access)

4. **Composants Externes**
   - Bases de données
   - Message Broker
   - Services microservices

### Utilisation

**Pour ajouter une nouvelle API:**
```
1. Documenter l'endpoint dans "Interfaces Fournies"
2. Spécifier request/response schemas
3. Lister les erreurs possibles
4. Mettre à jour component-diagram.puml
```

**Pour ajouter une nouvelle dépendance:**
```
1. Documenter dans "Interfaces Requises"
2. Spécifier le timeout, retry policy, circuit breaker
3. Ajouter au diagramme interne
4. Tester avant production
```

---

## 📊 Architecture Summary

**Fichier:** `docs/architecture/ARCHITECTURE_SUMMARY.md`

### Sections

1. **Vue Générale (3 secondes)** - Diagramme ASCII simple
2. **Focus Service-Abonnement** - Ses composants internes
3. **Dépendances** - Qui appelle quoi
4. **Interfaces Fournies & Requises** - Les contrats
5. **Flux de Données Principal** - Cas "Souscrire à un abonnement"
6. **Technologies** - Stack technique
7. **Matrice de Dépendances** - Table qui dépend de qui
8. **Checklist** - À vérifier dans le code

### Utilisation

**Pour un aperçu rapide:** Lire les 3 premières sections (5 min)

**Pour comprendre un flux:** Aller à "Flux de Données Principal" (2 min)

**Pour vérifier la cohérence:** Utiliser "Checklist" avant une PR (5 min)

---

## 📖 Guide d'Utilisation

**Fichier:** `docs/architecture/USAGE_GUIDE.md`

### Sections

1. **Comment Lire les Diagrammes** - Conventions visuelles
2. **Identifier les Composants** - Internes vs Externes
3. **Identifier les Interfaces** - Fournies vs Requises
4. **Comment Mettre à Jour** - Procédures pas à pas
5. **Flux de Mise à Jour** - Checklist complète
6. **Générer les Images** - PlantUML commands
7. **Validation** - Checklist avant versionning
8. **Ressources** - Liens utiles
9. **Dépannage** - FAQ & solutions

### Utilisation

**Avant de modifier les diagrammes:** Lire les sections 1-3 (10 min)

**Pour ajouter un nouveau service:** Suivre "Scenario 1" (section 4)

**Avant de commit:** Exécuter la "Checklist de Validation" (section 7)

---

## 🔄 Flux de Travail Recommandé

### 🆕 Nouveau Développeur

```
1. Lire ARCHITECTURE_SUMMARY.md (5 min)
2. Regarder component-diagram.puml (2 min)
3. Lire internal-architecture.puml pour Service-Abonnement (3 min)
4. Explorer le code: src/main/java/com/serviceabonnement/
5. Reférer à INTERFACES_AND_CONTRACTS.md pour les APIs
```

### 🔧 Nouveau Composant Interne

```
1. Créer la classe/interface
2. Mettre à jour internal-architecture.puml
3. Documenter dans INTERFACES_AND_CONTRACTS.md
4. Ajouter à la checklist de validation
5. Commit avec message descriptif
```

### 🆕 Nouveau Microservice

```
1. Créer le nouveau service
2. Mettre à jour component-diagram.puml
3. Documenter les interfaces fournies/requises
4. Ajouter la matrice de dépendances
5. Générer les images PNG/SVG
6. Commit avec [docs] tag
```

### 🔌 Nouvelle Dépendance Externe

```
1. Identifier le service cible et l'interface
2. Documenter dans INTERFACES_AND_CONTRACTS.md
3. Mettre à jour le diagramme concerné
4. Ajouter retry policy / timeout / circuit breaker
5. Tester et valider
6. Documenter dans ARCHITECTURE_SUMMARY.md
```

---

## 🎯 Scénarios d'Utilisation

### Scénario 1: "Je veux ajouter une nouvelle API"

```
Étapes:
1. Lire: Comment Lire les Diagrammes (5 min)
2. Créer le controller + endpoint
3. Lire: INTERFACES_AND_CONTRACTS.md - section "Interfaces Fournies"
4. Ajouter la documentation du nouvel endpoint
5. Mettre à jour component-diagram.puml si dépendances changent
6. Valider avec checklist
7. Commit avec description
```

### Scénario 2: "Je veux comprendre comment ça communique"

```
Étapes:
1. Regarder component-diagram.puml
2. Lire ARCHITECTURE_SUMMARY.md - section "Flux de Données"
3. Lire INTERFACES_AND_CONTRACTS.md - section "Interfaces Requises"
4. Trouver le code source du client Feign/producer Kafka
5. Tracer l'exécution à travers les couches
```

### Scénario 3: "Je dois ajouter un nouveau service"

```
Étapes:
1. Lire USAGE_GUIDE.md - Scenario 1: Ajouter un NOUVEAU Service
2. Créer la structure du service
3. Mettre à jour component-diagram.puml
4. Documenter les interfaces
5. Ajouter à ARCHITECTURE_SUMMARY.md
6. Générer les images
7. PR avec [arch] tag
```

### Scénario 4: "Le diagramme n'est pas à jour"

```
Étapes:
1. Identifier ce qui a changé
2. Lire USAGE_GUIDE.md - section "Comment Mettre à Jour"
3. Modifier le fichier .puml
4. Valider avec `plantuml -validate`
5. Générer les images PNG/SVG
6. Mettre à jour la documentation correspondante
7. Commit avec [docs] tag
```

---

## 📈 Diagrammes par Service

### Service-Abonnement (Attaché)

| Document | Fichier | Format |
|----------|---------|--------|
| Architecture Interne | `service-abonnement/docs/internal-architecture.puml` | PlantUML |
| Interfaces | `docs/architecture/INTERFACES_AND_CONTRACTS.md` | Markdown |
| Synthèse | `docs/architecture/ARCHITECTURE_SUMMARY.md` | Markdown |

### Autres Services

Même structure à créer pour:
- Service-Utilisateur
- Service-Paiement
- Service-Notification
- Service-Analytique
- etc.

---

## 🔗 Liens Internes

### References Croisées

- ARCHITECTURE_SUMMARY.md → Flux de Données Principal → INTERFACES_AND_CONTRACTS.md
- component-diagram.puml → Services → internal-architecture.puml (pour détails)
- USAGE_GUIDE.md → Scenarios → component-diagram.puml (exemples)
- INTERFACES_AND_CONTRACTS.md → External Clients → component-diagram.puml

---

## ✅ Checklist de Maintenance

**À faire régulièrement:**

- [ ] Tous les nouveaux services dans component-diagram.puml?
- [ ] Tous les nouveaux endpoints documentés?
- [ ] Tous les nouveaux topics Kafka listés?
- [ ] Les images PNG/SVG générées et versionnées?
- [ ] La documentation correspond au code?
- [ ] Les ports et URLs sont corrects?
- [ ] Les timeouts/retry sont à jour?
- [ ] Les dépendances critiques identifiées?
- [ ] Les erreurs possibles documentées?

**À chaque release:**

- [ ] Réviser tous les diagrammes
- [ ] Mettre à jour les versions
- [ ] Tester toutes les interfaces
- [ ] Mettre à jour la matrice de dépendances
- [ ] Générer les images finales
- [ ] Versionner avec tag Git

---

## 📞 FAQ

**Q: Où trouver la documentation d'une API spécifique?**
A: Dans `INTERFACES_AND_CONTRACTS.md` - Section "Interfaces Fournies"

**Q: Comment ajouter un nouveau service?**
A: Lire `USAGE_GUIDE.md` - Scenario 1

**Q: Qui peut modifier les diagrammes?**
A: Architecte + Lead Technique (validation pair avant merge)

**Q: À quelle fréquence mettre à jour?**
A: À chaque changement architecture majeur + 1x/release

**Q: Les diagrammes sont-ils testés automatiquement?**
A: Oui, via `plantuml -validate` en CI/CD

**Q: Peut-on avoir des diagrammes pour les tests?**
A: Oui, créer un dossier `docs/architecture/tests/`

---

## 🚀 Intégration CI/CD

### GitHub Actions

```yaml
# .github/workflows/validate-diagrams.yml
name: Validate Architecture Diagrams
on: [pull_request]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Install PlantUML
        run: sudo apt-get install -y plantuml
      - name: Validate Diagrams
        run: |
          plantuml -validate docs/architecture/*.puml
          plantuml -validate service-*/docs/*.puml
      - name: Generate Images
        run: |
          mkdir -p docs/diagrams
          plantuml -o ../diagrams docs/architecture/*.puml
      - name: Commit Changes
        run: |
          git add docs/diagrams/
          git commit -m "chore: auto-generate diagrams" || true
```

---

## 📞 Support

Pour des questions:
1. Consulter ce guide d'index
2. Vérifier la section FAQ
3. Contacter l'architecte du projet
4. Ouvrir une issue avec le tag [docs]

---

## 📋 Version & Historique

| Version | Date | Changements |
|---------|------|------------|
| 1.0 | 8 Mai 2026 | Version initiale |
| - | À venir | Évolutions futures |

---

## 📄 Licence & Attribution

- Architecture: SGITU Team
- Documentation: Architecture Team
- Dernière mise à jour: 8 Mai 2026

---

## 🔗 Fichiers Connexes

```
Le reste du projet:
├── README.md                           ← Vue générale du projet
├── docker-compose.yml                  ← Infrastructure locale
├── service-abonnement/                 ← Service étudié en détail
├── service-paiement/                   ← Dépendance clé
├── service-utilisateur/                ← Dépendance clé
└── api-gateway/                        ← Entrée du système
```

---

**Documentation centralissée:** 8 Mai 2026
**Mainteneur:** Architecture Team
**Status:** ✅ Complet et Validé
