CREATE OR REPLACE VIEW stats_clients AS
SELECT cl.id_client, cl.nom, cl.prenom, COUNT(DISTINCT c.id_commande) AS nb_commande, SUM(l.quantite * l.prix_unitaire) AS total_depense FROM clients cl
JOIN commande c ON c.id_client = cl.id_client
JOIN ligne_commande l ON l.id_commande = c.id_commande
WHERE c.statut NOT IN ('panier', 'annulee')
GROUP BY cl.id_client, cl.nom, cl.prenom;
