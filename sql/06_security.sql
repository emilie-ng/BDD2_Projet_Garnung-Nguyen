USE boutique_en_ligne;

CREATE ROLE IF NOT EXISTS 'role_client', 'role_gestionnaire', 'role_admin_app';
GRANT SELECT ON boutique_en_ligne.categorie TO 'role_client';
GRANT SELECT ON boutique_en_ligne.produit TO 'role_client';
GRANT SELECT ON boutique_en_ligne.categorie TO 'role_gestionnaire';
GRANT SELECT, INSERT, UPDATE ON boutique_en_ligne.produit TO 'role_gestionnaire';
GRANT SELECT ON boutique_en_ligne.clients TO 'role_gestionnaire';
GRANT SELECT ON boutique_en_ligne.commande TO 'role_gestionnaire';
GRANT SELECT ON boutique_en_ligne.ligne_commande TO 'role_gestionnaire';
GRANT SELECT ON boutique_en_ligne.paiement TO 'role_gestionnaire';
GRANT SELECT ON boutique_en_ligne.mouvement_stock TO 'role_gestionnaire';

--Admin
GRANT SELECT, INSERT, UPDATE, DELETE ON boutique_en_ligne.* TO 'role_admin_app';



DROP PROCEDURE IF EXISTS boutique_en_ligne.mes_commandes;
DELIMITER $$
CREATE PROCEDURE boutique_en_ligne.mes_commandes()
SQL SECURITY DEFINER
BEGIN
    DECLARE v_id_client INT DEFAULT NULL;
    DECLARE v_compte VARCHAR(100);
    SET v_compte = SUBSTRING_INDEX(USER(), '@', 1);
    SELECT id_client INTO v_id_client
      FROM boutique_en_ligne.identite_sql_client
     WHERE nom_compte = v_compte;
    IF v_id_client IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Compte client non associe';
    END IF;
    SELECT id_commande, date_commande, statut, montant_total
      FROM boutique_en_ligne.commande
     WHERE id_client = v_id_client
     ORDER BY date_commande DESC;
END$$
DELIMITER ;
GRANT EXECUTE ON PROCEDURE boutique_en_ligne.mes_commandes TO 'role_client';

SHOW GRANTS FOR 'role_client';
SHOW GRANTS FOR 'role_gestionnaire';
SHOW GRANTS FOR 'role_admin_app';
