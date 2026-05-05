# Rapport d'Avancement - SGITU (Groupe 2 : Gestion des Abonnements)

## 1. Présentation du microservice
**Description du sous-système** :
Le microservice "Gestion des Abonnements" est un composant central du système SGITU (Système de Gestion Intelligente des Transports Urbains). Son rôle est de gérer le cycle de vie des abonnements de transport pour les utilisateurs (création, renouvellement, annulation), ainsi que les plans tarifaires associés.

**Fonctionnalités métier implémentées** :
- Souscription à un nouvel abonnement.
- Consultation d'un abonnement spécifique par son identifiant.
- Consultation de la liste des abonnements d'un utilisateur.
- Résiliation d'un abonnement.
- Communication avec les autres microservices (Utilisateur, Paiement, Notification) pour valider les opérations.

## 2. Conception
**Identification des endpoints REST** :
- `POST /api/v1/abonnements/souscrire` : Souscription à un abonnement.
- `GET /api/v1/abonnements/{id}` : Récupération des détails d'un abonnement.
- `GET /api/v1/abonnements/utilisateur/{utilisateurId}` : Récupération des abonnements d'un utilisateur.
- `DELETE /api/v1/abonnements/{id}/resilier` : Résiliation d'un abonnement.

**Choix des méthodes HTTP** :
- **POST** pour la création (souscription).
- **GET** pour la lecture de données sans modification.
- **DELETE** pour la suppression/résiliation.

**Formats d’échange** :
Toutes les communications se font en format JSON via des DTOs (Data Transfer Objects) comme `SouscriptionRequestDTO` et `AbonnementResponseDTO`.

**Gestion des erreurs** :
Les codes de statut HTTP standard sont utilisés (`201 Created`, `200 OK`, `204 No Content`). *(Note: Un gestionnaire global d'exceptions est en cours d'implémentation pour standardiser les réponses d'erreur 4xx et 5xx).*

## 3. Implémentation
**Description de l'architecture** :
Le projet suit l'architecture multicouche classique :
- **Controller** (`AbonnementController`) : Expose les API REST.
- **Service** (`AbonnementService`) : Contient la logique métier.
- **Repository** : Gère l'accès aux données via Spring Data JPA.
- **Entity/DTO** : Représente le modèle de base de données et les objets de transfert.
- **Client (Feign)** : Gère la communication inter-microservices.

**Technologies utilisées** :
- **Spring Boot 3 / Java 21** : Framework de base.
- **Spring Data JPA & MySQL** : Pour la persistance des données.
- **Spring Cloud OpenFeign** : Pour les requêtes REST entre microservices.
- **MapStruct & Lombok** : Pour générer le code répétitif (getters/setters, mappers).
- **Flyway** : Pour le versionnage de la base de données.

## 4. Documentation API
L'intégration de Swagger / OpenAPI est configurée via `springdoc-openapi`. L'interface de documentation est accessible (lorsque l'application tourne) sur `/swagger-ui.html`. La documentation détaillée des requêtes/réponses est en cours d'enrichissement avec les annotations `@Operation` et `@ApiResponse`.

## 5. Tests
*(En cours)*
La dépendance `spring-boot-starter-test` est intégrée. Les tests unitaires (Mockito/JUnit) et les tests Postman sont prévus pour la prochaine itération afin de valider les scénarios nominaux et d'erreurs.

## 6. Conteneurisation
**Dockerfile** :
Un fichier `Dockerfile` optimisé (multi-stage build) est présent, utilisant `maven:3.9.6-eclipse-temurin-21` pour la compilation et `eclipse-temurin:21-jre-alpine` pour l'exécution, incluant un `HEALTHCHECK` pour vérifier l'état du service.

## 7. Orchestration
**Fichier docker-compose.yml** :
Un fichier `docker-compose.yml` est défini pour orchestrer le microservice `service-abonnement` ainsi que sa base de données dédiée `db-abonnement` (MySQL). Il configure les variables d'environnement, les réseaux et gère l'ordre de démarrage avec `depends_on: condition: service_healthy`.

## 8. Sécurité
**Mise en place de l’authentification** :
Le module `spring-boot-starter-security` est intégré avec la configuration `SecurityConfig`. Actuellement, le service est configuré pour intercepter les requêtes de manière `STATELESS`. La validation stricte du JWT (JwtAuthFilter) est la prochaine étape clé pour protéger les endpoints.

## 9. État d’avancement
**Fonctionnalités finalisées** :
- Modélisation de la base de données et des entités JPA.
- Structure du projet et architecture multicouche.
- Endpoints CRUD de base pour les abonnements.
- Configuration Docker & Docker Compose.
- Clients Feign pour la communication inter-services.

**Fonctionnalités en cours / À faire** :
- Implémentation du filtre JWT pour valider les requêtes.
- Gestionnaire d'exceptions global (`@ControllerAdvice`).
- Enrichissement de la documentation Swagger (Annotations).
- Rédaction des tests unitaires et intégration (Postman).
- Implémentation des endpoints pour la gestion des `PlanAbonnement`.

**Difficultés rencontrées** :
- Coordination des formats de données avec les autres groupes (ex: ID des utilisateurs et requêtes de paiement).
- Test de la communication inter-services (Feign) en l'absence des autres microservices déployés (nécessite des Mocks ou l'attente du déploiement global).
