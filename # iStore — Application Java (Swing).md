# iStore — Application Java (Swing)

Application de gestion d’inventaire multi-magasins (projet Java) avec interface Swing, authentification, gestion utilisateurs, gestion magasins, gestion stock et whitelist. 

## Prérequis
- Java JDK 19. 
- Une base de données (MySQL) accessible en local ou sur un serveur.
- Un IDE (IntelliJ IDEA).

## Installation
1. télécharger le projet.
2. Ouvrir le projet dans IntelliJ.
3. Configurer la base de données (URL, user, password) dans le fichier de configuration de ton projet.
4. Créer le schéma/tables nécessaires.

## Configuration base de données
La connexion MySQL est centralisée dans:

`src/main/java/fr/supinfo/istore/db/DbConnection.java`

Modifier si besoin :
- `URL` (hôte, port, nom de base `istore`)
- `USER`
- `PASSWORD`

Exemple (par défaut) :
- URL: `jdbc:mysql://localhost:3306/istore?useSSL=false&serverTimezone=UTC`
- USER: `root`
- PASSWORD: `""`

## Initialisation de la base de données (MySQL)

1) Créer la base et les tables

Dans un client MySQL (phpMyAdmin), exécuter le script SQL contenue dans le fichier `istore.sql`.

- Base: `istore`
- Encodage: `utf8mb4` + `utf8mb4_unicode_ci`

Tables créées :
- `users` (utilisateurs: email unique, pseudo, password_hash, role) 
- `whitelist_emails` (emails autorisés à s’inscrire) 
- `stores` (magasins) 
- `store_employees` (accès employés ↔ magasin) 
- `items` (catalogue articles) 
- `store_items` (stock par magasin et par article, quantity >= 0 côté application) 

> Note: la contrainte `CHECK (quantity >= 0)` est commentée. Elle est supportée à partir de MySQL 8.0.16 si tu veux la réactiver. 

2) Configurer la connexion côté Java

La connexion MySQL est centralisée dans :
`src/main/java/fr/supinfo/istore/db/DbConnection.java`

Modifier si besoin :
- `URL` (host/port/db)
- `USER`
- `PASSWORD`

Exemple actuel :
`jdbc:mysql://localhost:3306/istore?useSSL=false&serverTimezone=UTC`

## Lancement
- Lancer la classe `App` (ou la classe `main` équivalente) depuis IntelliJ.
- Au démarrage, l’application affiche l’écran de connexion / création de compte. 

## Comptes et rôles
- Le premier utilisateur est **ADMIN** (ou un admin par défaut est créé). 
- Pour créer un compte, l’email doit être présent dans la **whitelist**. 
- Rôles :
  - **ADMIN** : gère whitelist, magasins, articles, stock (admin), et peut gérer les employés.
  - **EMPLOYEE** : accède uniquement aux magasins auxquels il a été ajouté. 

## Fonctionnalités (selon l’énoncé)
### Authentification
- Inscription (email + mot de passe) possible uniquement si l’email est whitelisté. 
- Connexion via identifiant (email) + mot de passe. 
- Mot de passe non stocké en clair (stockage sécurisé via hash). 

### Gestion des utilisateurs (CRUD)
- Créer, lire, mettre à jour, supprimer un utilisateur. 
- Un utilisateur peut consulter les infos publiques d’un autre utilisateur (sans mot de passe). 
- Un utilisateur peut modifier/supprimer **son propre compte**.
- L’admin peut mettre à jour ou supprimer un compte **EMPLOYEE**. 

### Gestion administrative
- Whitelist : l’admin ajoute des emails autorisés à s’inscrire. 
- Magasins : l’admin crée/supprime un magasin (id, nom). 
- Articles : l’admin crée/supprime un article (id, nom, prix). 

### Gestion des magasins & accès
- Un employé a accès uniquement aux magasins où il a été ajouté. 
- L’application affiche la liste des personnes ayant accès au magasin (admin + employés autorisés). 

### Gestion des stocks
- 1 inventaire par magasin. 
- Un inventaire contient des articles (quantité >= 0). 
- L’inventaire est consultable (liste des articles). 
- Un employé peut augmenter/diminuer la quantité (vente/réception). 

## Structure du projet (packages)
Exemple de découpage:
- `fr.supinfo.istore.ui` : écrans Swing (LoginFrame, AdminFrame, EmployeeFrame, etc.).
- `fr.supinfo.istore.ui.table` : `TableModel` Swing (ItemTableModel, InventoryTableModel, StoreTableModel, etc.).
- `fr.supinfo.istore.dao` : accès base de données (UserDao, StoreDao, StockDao, WhitelistDao, ...).
- `fr.supinfo.istore.model` : entités métier (User, Store, Item, InventoryLine, Role, ...).
- `fr.supinfo.istore.service` : logique métier (AuthService, UserService, ...).
- `fr.supinfo.istore.security` : session + hash mot de passe (Session, PasswordHasher, ...).

Ce découpage suit le principe : UI (présentation) → services (métier) → DAO (persistence) → models (données). 

## Qualité & sécurité
- Validation des entrées utilisateur (emails, prix, quantités, champs obligatoires). 
- Gestion d’erreurs avec messages explicites côté UI (login/inscription/CRUD). 
- Pas de stockage de mot de passe en clair (hash). 

## Notes
- Si le projet ne démarre pas, vérifier en priorité la configuration DB (URL/user/password) et l’existence des tables.  