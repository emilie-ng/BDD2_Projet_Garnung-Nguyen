package lunea.dao;
import lunea.model.Client;
import java.sql.SQLException;
import java.util.Optional;
public interface ClientDAO {
    /** Lit la vue v_mon_profil (le client ne voit que sa propre ligne). */
    Optional<Client> trouver(int id) throws SQLException;
}
