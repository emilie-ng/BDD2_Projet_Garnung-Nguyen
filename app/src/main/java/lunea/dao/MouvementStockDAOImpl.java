package lunea.dao;
import lunea.model.MouvementStock;
import java.sql.*;
import java.util.*;
public class MouvementStockDAOImpl implements MouvementStockDAO {
    private final Connection c;
    public MouvementStockDAOImpl(Connection c) { this.c = c; }

    @Override
    public List<MouvementStock> recents(int limite) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT m.id_mouvement, m.id_produit, p.nom, m.type_mouvement, m.quantite, m.date_mouvement, m.motif "
              + "FROM mouvement_stock m JOIN produit p ON p.id_produit = m.id_produit "
              + "ORDER BY m.date_mouvement DESC, m.id_mouvement DESC LIMIT ?")) {
            ps.setInt(1, limite);
            List<MouvementStock> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    out.add(new MouvementStock(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getString(4),
                            rs.getInt(5), rs.getTimestamp(6).toLocalDateTime(), rs.getString(7)));
            }
            return out;
        }
    }

    @Override
    public void ajuster(int produitId, String type, int quantite, String motif) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("CALL ajuster_stock(?, ?, ?, ?)")) {
            ps.setInt(1, produitId);
            ps.setString(2, type);
            ps.setInt(3, quantite);
            ps.setString(4, motif);
            ps.execute();
        }
    }
}
