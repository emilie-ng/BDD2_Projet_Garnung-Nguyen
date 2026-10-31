package lunea.dao;
import lunea.model.LigneCommande;
import java.sql.SQLException;
import java.util.List;
public interface LigneCommandeDAO {
    /** Profil client : VUE v_mes_lignes. */
    List<LigneCommande> mesLignes(int commandeId) throws SQLException;
    /** Profil gestionnaire : table ligne_commande. */
    List<LigneCommande> lignes(int commandeId) throws SQLException;
}
