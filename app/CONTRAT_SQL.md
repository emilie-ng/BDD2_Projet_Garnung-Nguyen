# Contrat entre l'application Java et la base MySQL

L'application attend les objets ci-dessous. Si vos noms diffèrent, **adaptez les DAO** (`src/main/java/lunea/dao`).
Les noms de tables et colonnes viennent de votre `01_schema.sql`.

## Variable de session et cloisonnement (remplace la RLS, absente de MySQL)
- Après connexion d'un client, l'application exécute `SET @app_user_id = <id_client>`.
- `fn_user_id()` : fonction `RETURNS INT`, `NOT DETERMINISTIC READS SQL DATA`, qui retourne `@app_user_id`
  (une vue MySQL ne peut pas lire directement une variable de session, d'où la fonction).

## Vues (03_views.sql)
| Objet | Colonnes lues par l'application |
|---|---|
| `v_mon_profil` | id_client, nom, prenom, email — filtrée sur `id_client = fn_user_id()` |
| `v_catalogue` | id_produit, categorie, nom, _description, prix, stock, actif — produits actifs |
| `v_mes_commandes` | id_commande, id_client, date_commande, statut, montant_total — filtrée sur `fn_user_id()` |
| `v_mes_lignes` | id_ligne, id_commande, id_produit, produit, quantite, prix_unitaire — lignes de ses commandes |
| `mv_ca_jour` (table) | jour (DATE), nb_articles, ca — vue matérialisée simulée, remplie par `rafraichir_mv_ca_jour()` |

À expliquer dans le rapport : `SQL SECURITY DEFINER` ou `INVOKER` (équivalent du `security_invoker` PostgreSQL),
et la stratégie de rafraîchissement (à la demande, événement planifié...).

## Fonction et procédures (04_functions_procedures.sql)
- `fn_total_panier(p_lignes JSON) RETURNS DECIMAL(12,2)` — lignes au format `[{"id":1,"q":2}, ...]`
  (`JSON_TABLE`), prix lus dans `produit`.
- `passer_commande(p_id_client INT, p_lignes JSON)` — transactionnelle (`START TRANSACTION`, `EXIT HANDLER`
  avec `ROLLBACK`), crée la commande, ses lignes (prix_unitaire figé) et le paiement.
  **Elle doit se terminer par `SELECT <id_commande créé> AS id_commande;`** (l'application lit ce résultat).
- `ajuster_stock(p_id_produit INT, p_type VARCHAR(25), p_quantite INT, p_motif VARCHAR(255))` — `entree` ou `sortie`,
  insère dans `mouvement_stock`.
- `changer_statut_commande(p_id_commande INT, p_statut VARCHAR(25))` — refuse les transitions interdites.
- `rafraichir_mv_ca_jour()` — recalcule `mv_ca_jour`.
- Erreurs métier : `SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '...'` — le message est affiché tel quel.

## Triggers (05_triggers.sql)
Par exemple un trigger sur `mouvement_stock` qui met à jour `produit.stock` et refuse un stock négatif.
MySQL ne propose que `FOR EACH ROW` : à justifier dans le rapport (BEFORE/AFTER, ROW).

## Sécurité (06_security.sql)
- Propriétaire des objets : compte verrouillé (`ACCOUNT LOCK`) ; jamais utilisé par l'application.
- `app_client` : `SELECT` sur `v_mon_profil`, `v_catalogue`, `v_mes_commandes`, `v_mes_lignes` ;
  `EXECUTE` sur `fn_total_panier` et `passer_commande`. Aucun accès direct aux tables.
- `app_gestionnaire` : `SELECT` sur `produit`, `categorie`, `commande`, `ligne_commande`, `mouvement_stock`, `mv_ca_jour` ;
  `EXECUTE` sur `ajuster_stock`, `changer_statut_commande`, `rafraichir_mv_ca_jour`. Pas d'`INSERT/UPDATE/DELETE` direct.
- Limite à discuter (point noté) : un compte applicatif peut lui-même exécuter `SET @app_user_id = ...`
  et usurper un autre client. Pistes : accès uniquement par procédures `SQL SECURITY DEFINER` qui contrôlent
  l'identité, ou un compte MySQL par utilisateur.
