package lunea.dao;
import lunea.model.Client;
import java.sql.*;
import java.util.Optional;
public class ClientDAOImpl implements ClientDAO {
    private final Connection c;
    public ClientDAOImpl(Connection c) { this.c = c; }
    @Override
    public Optional<Client> trouver(int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT id_client, nom, prenom, email FROM v_mon_profil WHERE id_client = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new Client(rs.getInt("id_client"), rs.getString("nom"),
                        rs.getString("prenom"), rs.getString("email"))) : Optional.empty();
            }
        }
    }
}
