delimiter //
CREATE TRIGGER verifier_stock
BEFORE INSERT ON concerner
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

delimiter //
CREATE TRIGGER actualiser_stock_mouvement
AFTER INSERT ON mouvement_stock
FOR EACH ROW
BEGIN
    IF NEW.type_mouvement = 'entrée' THEN
        UPDATE produit
        SET stock = stock + NEW.quantite
        WHERE id_produit = NEW.id_produit;

    ELSEIF NEW.type_mouvement = 'sortie' THEN
        UPDATE produit
        SET stock = stock - NEW.quantite
        WHERE id_produit = NEW.id_produit;
    END IF;
END;
//
delimiter ;

delimiter //
CREATE TRIGGER diminuer_stock_achat
AFTER INSERT ON concerner
FOR EACH ROW
BEGIN
    UPDATE produit
    SET stock = stock - NEW.quantite
    WHERE id_produit = NEW.id_produit;
END;
//
delimiter ;
