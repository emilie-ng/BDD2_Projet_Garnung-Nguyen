CREATE OR REPLACE VIEW stats_clients AS
SELECT cl.id_client, cl.nom, cl.prenom, COUNT(DISTINCT c.id_commande) AS nb_commande, SUM(co.quantite * co.prix_unitaire) AS total_depense FROM clients cl
JOIN commande c ON c.id_client = cl.id_client
JOIN concerner co ON co.id_commande = c.id_commande
WHERE c.statut NOT IN ('panier', 'annulee')
GROUP BY cl.id_client, cl.nom, cl.prenom;
