package lunea.dao;
import lunea.model.Commande;
import java.sql.*;
import java.util.*;
public class CommandeDAOImpl implements CommandeDAO {
    private final Connection c;
    public CommandeDAOImpl(Connection c) { this.c = c; }

    private static List<Commande> liste(PreparedStatement ps) throws SQLException {
        List<Commande> out = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                out.add(new Commande(rs.getInt("id_commande"), rs.getInt("id_client"),
                        rs.getTimestamp("date_commande").toLocalDateTime(), rs.getString("statut"), rs.getDouble("montant_total")));
        }
        return out;
    }

    @Override
    public List<Commande> mesCommandes(int limite) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_commande, id_client, date_commande, statut, montant_total FROM v_mes_commandes "
              + "ORDER BY date_commande DESC LIMIT ?")) {
            ps.setInt(1, limite);
            return liste(ps);
        }
    }

    @Override
    public List<Commande> toutes(int limite) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_commande, id_client, date_commande, statut, montant_total FROM commande "
              + "ORDER BY date_commande DESC LIMIT ?")) {
            ps.setInt(1, limite);
            return liste(ps);
        }
    }

    @Override
    public int passer(int clientId, String lignesJson) throws SQLException {
        // La procédure doit se terminer par un SELECT renvoyant l'id de la commande créée.
        try (PreparedStatement ps = c.prepareStatement("CALL passer_commande(?, CAST(? AS JSON))")) {
            ps.setInt(1, clientId);
            ps.setString(2, lignesJson);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    @Override
    public double calculerTotal(String lignesJson) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT fn_total_panier(CAST(? AS JSON))")) {
            ps.setString(1, lignesJson);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble(1);
            }
        }
    }

    @Override
    public void changerStatut(int commandeId, String statut) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("CALL changer_statut_commande(?, ?)")) {
            ps.setInt(1, commandeId);
            ps.setString(2, statut);
            ps.execute();
        }
    }
}
