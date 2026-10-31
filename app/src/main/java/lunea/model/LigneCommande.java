package lunea.model;
public record LigneCommande(int id, int commandeId, int produitId, String produitNom, int quantite, double prixUnitaire) {}
