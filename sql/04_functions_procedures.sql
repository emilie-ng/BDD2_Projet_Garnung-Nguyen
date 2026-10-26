delimiter //
CREATE FUNCTION calculer_remise(p_montant DECIMAL(12,2))
RETURNS DECIMAL(12,2) DETERMINISTIC
BEGIN
    IF p_montant < 100 THEN
        RETURN 0;
    ELSEIF p_montant < 200 THEN
        RETURN p_montant * 0.05;
    ELSE
        RETURN p_montant * 0.10;
	END IF;
END;
//
delimiter ;
