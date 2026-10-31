# LUNÉA.mode – Application console Java (JDBC / DAO)

> **Transparence IA** : cette application a été générée avec l'aide d'une IA (Claude). Les scripts SQL du dossier `sql/` sont écrits par le groupe,
> qui reste responsable de chaque requête présente dans les DAO.

## Architecture

```
src/main/java/lunea/
├── util/      ConnectionManager (connexion PAR RÔLE), Erreurs (messages lisibles)
├── model/     Client, Produit, Commande, LigneCommande, MouvementStock
├── dao/       une interface XxxDAO + une implémentation XxxDAOImpl par entité  ← TOUT le SQL est ici
├── service/   BoutiqueService (ouvre la connexion du rôle courant, crée les DAO)
└── ui/        MainMenu (menus client et gestionnaire), TestConnection
```

Ce que l'application montre de la base :

| Objet de la base | Où dans le code |
|---|---|
| Vue (catalogue, mes commandes, mes lignes, mon profil) | `ProduitDAOImpl.catalogue`, `CommandeDAOImpl.mesCommandes`, … |
| Vue matérialisée (table `mv_ca_jour`) | `StatsDAOImpl.caParJour`, `rafraichir` |
| Fonction | `CommandeDAOImpl.calculerTotal` → `fn_total_panier` |
| Procédures | `passer_commande`, `ajuster_stock`, `changer_statut_commande`, `rafraichir_mv_ca_jour` |
| Trigger | déclenché par `ajuster_stock` / `passer_commande` (erreurs affichées par `Erreurs`) |
| Requête complexe | `StatsDAOImpl.topProduits` (CTE + jointures/agrégat + fonctions de fenêtre) |
| Rôles et cloisonnement | `ConnectionManager` (compte `app_client` / `app_gestionnaire`, variable `@app_user_id`) |

Les objets SQL attendus (noms, colonnes, signatures, droits) sont listés dans **`CONTRAT_SQL.md`**.

## Installation

1. JDK 17+ et Maven 3.6+.
2. MySQL 8 : exécuter les scripts du dossier `sql/` dans l'ordre (`01_schema.sql` … `07_tests.sql`), par exemple
   `mysql -u root -p < sql/01_schema.sql` ou *File → Run SQL Script* dans MySQL Workbench.
3. Définir les variables d'environnement (aucun mot de passe n'est écrit dans le code) :

   | Variable | Rôle |
   |---|---|
   | `DB_CLIENT_PASSWORD` | mot de passe du compte `app_client` (obligatoire) |
   | `DB_GESTIONNAIRE_PASSWORD` | mot de passe du compte `app_gestionnaire` (obligatoire) |
   | `DB_URL` | facultatif, défaut `jdbc:mysql://localhost:3306/boutique_en_ligne?serverTimezone=Europe/Paris` |
   | `DB_CLIENT_USER`, `DB_GESTIONNAIRE_USER` | facultatifs, défauts `app_client` et `app_gestionnaire` |

   Windows : `set DB_CLIENT_PASSWORD=...`   Linux/macOS : `export DB_CLIENT_PASSWORD=...`

## Lancement

```bash
mvn compile
mvn exec:java -Dexec.mainClass="lunea.ui.TestConnection"   # vérifie les deux comptes
mvn exec:java -Dexec.mainClass="lunea.ui.MainMenu"         # application

# ou, en un seul JAR :
mvn package
java -jar target/lunea-app-1.0-SNAPSHOT.jar
```

Sous Windows, pour un affichage correct des accents : `chcp 65001` avant de lancer.

## Comptes de test

| Profil | Connexion |
|---|---|
| Client | numéro de compte 1, 2 ou 3 (à adapter à votre `02_data.sql`) |
| Gestionnaire | aucun numéro, choisir « Se connecter comme gestionnaire » |

## Scénarios à montrer dans la vidéo

- Client : catalogue → panier → total (fonction) → validation (procédure) → « Mes commandes » (cloisonnement).
- Client : commander plus que le stock → message d'erreur de la procédure ou du trigger.
- Gestionnaire : ajuster un stock (procédure + trigger), changer un statut, tableau de bord, rafraîchissement.
- Les deux profils : tenter une action interdite → « Droit refusé ».
