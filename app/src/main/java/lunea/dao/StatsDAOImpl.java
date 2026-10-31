package lunea.dao;
import java.sql.*;
import java.util.*;
public class StatsDAOImpl implements StatsDAO {
    private final Connection c;
    public StatsDAOImpl(Connection c) { this.c = c; }

    @Override
    public List<JourCa> caParJour() throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT jour, nb_articles, ca FROM ("
              + "  SELECT jour, nb_articles, ca FROM mv_ca_jour ORDER BY jour DESC LIMIT 24"
              + ") t ORDER BY jour");
             ResultSet rs = ps.executeQuery()) {
            List<JourCa> out = new ArrayList<>();
            while (rs.next()) out.add(new JourCa(rs.getDate("jour").toLocalDate(), rs.getInt("nb_articles"), rs.getDouble("ca")));
            return out;
        }
    }

    @Override
    public void rafraichir() throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("CALL rafraichir_mv_ca_jour()")) { ps.execute(); }
    }

    @Override
    public List<TopProduit> topProduits() throws SQLException {
        // 3 techniques : CTE, jointures + agrégation, fonctions de fenêtre (RANK, SUM OVER).
        try (PreparedStatement ps = c.prepareStatement(
                "WITH ventes AS ("
              + "  SELECT p.id_produit, p.nom, SUM(l.quantite) AS qte, SUM(l.quantite * l.prix_unitaire) AS ca"
              + "  FROM ligne_commande l"
              + "  JOIN produit p ON p.id_produit = l.id_produit"
              + "  JOIN commande o ON o.id_commande = l.id_commande"
              + "  WHERE o.statut NOT IN ('panier', 'annulee')"
              + "  GROUP BY p.id_produit, p.nom"
              + ") "
              + "SELECT RANK() OVER (ORDER BY ca DESC) AS rang, nom, qte, ca,"
              + "       ROUND(100 * ca / SUM(ca) OVER (), 1) AS part "
              + "FROM ventes ORDER BY rang, nom LIMIT 5");
             ResultSet rs = ps.executeQuery()) {
            List<TopProduit> out = new ArrayList<>();
            while (rs.next())
                out.add(new TopProduit(rs.getInt("rang"), rs.getString("nom"), rs.getInt("qte"), rs.getDouble("ca"), rs.getDouble("part")));
            return out;
        }
    }
}
