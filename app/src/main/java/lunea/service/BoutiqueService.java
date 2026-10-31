package lunea.service;

import lunea.dao.*;
import lunea.dao.StatsDAO.JourCa;
import lunea.dao.StatsDAO.TopProduit;
import lunea.model.*;
import lunea.util.ConnectionManager;
import lunea.util.ConnectionManager.Role;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Couche service : ouvre la connexion du rôle courant, crée les DAO et appelle les objets de la base.
 * La couche UI ne contient aucun SQL et ne manipule aucune Connection.
 */
public class BoutiqueService {

    @FunctionalInterface
    private interface Operation<T> { T executer(Connection c) throws SQLException; }

    private static <T> T avec(Operation<T> op) throws SQLException {
        try (Connection c = ConnectionManager.getConnection()) { return op.executer(c); }
    }

    // ---- connexion ------------------------------------------------------

    /** Retourne le client, ou null si le compte n'existe pas. */
    public Client connecterClient(int id) throws SQLException {
        ConnectionManager.ouvrirSession(Role.CLIENT, id);
        try {
            Client client = avec(c -> new ClientDAOImpl(c).trouver(id).orElse(null));
            if (client == null) ConnectionManager.fermerSession();
            return client;
        } catch (SQLException | RuntimeException e) {
            ConnectionManager.fermerSession();
            throw e;
        }
    }

    public void connecterGestionnaire() throws SQLException {
        ConnectionManager.ouvrirSession(Role.GESTIONNAIRE, null);
        try { avec(c -> Boolean.TRUE); }
        catch (SQLException | RuntimeException e) { ConnectionManager.fermerSession(); throw e; }
    }

    public void deconnecter() { ConnectionManager.fermerSession(); }

    // ---- profil client --------------------------------------------------

    public List<Produit> catalogue(String texte) throws SQLException { return avec(c -> new ProduitDAOImpl(c).catalogue(texte)); }

    public Produit produit(int id) throws SQLException { return avec(c -> new ProduitDAOImpl(c).trouver(id).orElse(null)); }

    /** Total calculé par la FONCTION SQL fn_total_panier. */
    public double totalPanier(Map<Integer, Integer> panier) throws SQLException {
        return panier.isEmpty() ? 0 : avec(c -> new CommandeDAOImpl(c).calculerTotal(versJson(panier)));
    }

    /** CALL passer_commande : procédure transactionnelle. Retourne l'id de la commande créée. */
    public int validerCommande(int clientId, Map<Integer, Integer> panier) throws SQLException {
        return avec(c -> new CommandeDAOImpl(c).passer(clientId, versJson(panier)));
    }

    public List<Commande> mesCommandes() throws SQLException { return avec(c -> new CommandeDAOImpl(c).mesCommandes(20)); }

    public List<LigneCommande> mesLignes(int commandeId) throws SQLException { return avec(c -> new LigneCommandeDAOImpl(c).mesLignes(commandeId)); }

    // ---- profil gestionnaire --------------------------------------------

    public List<Produit> tousLesProduits(String texte) throws SQLException { return avec(c -> new ProduitDAOImpl(c).tous(texte)); }

    /** delta > 0 : entrée de stock ; delta < 0 : sortie. Un trigger met le stock à jour. */
    public void ajusterStock(int produitId, int delta, String motif) throws SQLException {
        String type = delta > 0 ? "entree" : "sortie";
        avec(c -> { new MouvementStockDAOImpl(c).ajuster(produitId, type, Math.abs(delta), motif); return null; });
    }

    public List<MouvementStock> mouvementsRecents() throws SQLException { return avec(c -> new MouvementStockDAOImpl(c).recents(10)); }

    public List<Commande> toutesLesCommandes() throws SQLException { return avec(c -> new CommandeDAOImpl(c).toutes(20)); }

    public List<LigneCommande> lignes(int commandeId) throws SQLException { return avec(c -> new LigneCommandeDAOImpl(c).lignes(commandeId)); }

    public void changerStatut(int commandeId, String statut) throws SQLException {
        avec(c -> { new CommandeDAOImpl(c).changerStatut(commandeId, statut); return null; });
    }

    public List<JourCa> caParJour() throws SQLException { return avec(c -> new StatsDAOImpl(c).caParJour()); }

    public List<TopProduit> topProduits() throws SQLException { return avec(c -> new StatsDAOImpl(c).topProduits()); }

    public void rafraichirIndicateurs() throws SQLException {
        avec(c -> { new StatsDAOImpl(c).rafraichir(); return null; });
    }

    // ---- utilitaire -----------------------------------------------------

    /** {1=2, 5=1} -> [{"id":1,"q":2},{"id":5,"q":1}] (uniquement des entiers : pas de risque d'injection). */
    static String versJson(Map<Integer, Integer> panier) {
        StringJoiner j = new StringJoiner(",", "[", "]");
        panier.forEach((id, q) -> j.add("{\"id\":" + id + ",\"q\":" + q + "}"));
        return j.toString();
    }
}
