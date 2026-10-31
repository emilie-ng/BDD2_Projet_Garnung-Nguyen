package lunea.dao;
import lunea.model.Produit;
import java.sql.*;
import java.util.*;
public class ProduitDAOImpl implements ProduitDAO {
    private final Connection c;
    public ProduitDAOImpl(Connection c) { this.c = c; }

    private static List<Produit> liste(PreparedStatement ps) throws SQLException {
        List<Produit> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                out.add(new Produit(rs.getInt("id_produit"), rs.getString("categorie"), rs.getString("nom"),
                        rs.getString("_description"), rs.getDouble("prix"), rs.getInt("stock"), rs.getBoolean("actif")));
        }
        return out;
    }

    @Override
    public List<Produit> catalogue(String texte) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_produit, categorie, nom, _description, prix, stock, actif "
              + "FROM v_catalogue WHERE nom LIKE ? ORDER BY nom")) {
            ps.setString(1, Sql.like(texte));
            return liste(ps);
        }
    }

    @Override
    public Optional<Produit> trouver(int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_produit, categorie, nom, _description, prix, stock, actif FROM v_catalogue WHERE id_produit = ?")) {
            ps.setInt(1, id);
            return liste(ps).stream().findFirst();
        }
    }

    @Override
    public List<Produit> tous(String texte) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT p.id_produit, c.nom AS categorie, p.nom, p._description, p.prix, p.stock, p.actif "
              + "FROM produit p JOIN categorie c ON c.id_categorie = p.id_categorie "
              + "WHERE p.nom LIKE ? ORDER BY p.nom")) {
            ps.setString(1, Sql.like(texte));
            return liste(ps);
        }
    }
}
