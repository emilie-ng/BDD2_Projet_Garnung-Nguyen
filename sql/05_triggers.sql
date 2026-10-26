delimiter //
CREATE TRIGGER verifier_stock
BEFORE INSERT ON ligne_commande
FOR EACH ROW
BEGIN
	DECLARE v_stock INT;

	SELECT stock INTO v_stock FROM produit WHERE id_produit = new.id_produit;
    
    IF new.quantite > v_stock THEN
		SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock insuffisant pour ce produit.';
	END IF;
END;
//
delimiter ;
