package lunea.dao;
import lunea.model.LigneCommande;
import java.sql.*;
import java.util.*;
public class LigneCommandeDAOImpl implements LigneCommandeDAO {
    private final Connection c;
    public LigneCommandeDAOImpl(Connection c) { this.c = c; }

    private static List<LigneCommande> liste(PreparedStatement ps) throws SQLException {
        List<LigneCommande> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                out.add(new LigneCommande(rs.getInt("id_ligne"), rs.getInt("id_commande"), rs.getInt("id_produit"),
                        rs.getString("produit"), rs.getInt("quantite"), rs.getDouble("prix_unitaire")));
        }
        return out;
    }

    @Override
    public List<LigneCommande> mesLignes(int commandeId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_ligne, id_commande, id_produit, produit, quantite, prix_unitaire "
              + "FROM v_mes_lignes WHERE id_commande = ? ORDER BY produit")) {
            ps.setInt(1, commandeId);
            return liste(ps);
        }
    }

    @Override
    public List<LigneCommande> lignes(int commandeId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT l.id_ligne, l.id_commande, l.id_produit, p.nom AS produit, l.quantite, l.prix_unitaire "
              + "FROM ligne_commande l JOIN produit p ON p.id_produit = l.id_produit "
              + "WHERE l.id_commande = ? ORDER BY p.nom")) {
            ps.setInt(1, commandeId);
            return liste(ps);
        }
    }
}
