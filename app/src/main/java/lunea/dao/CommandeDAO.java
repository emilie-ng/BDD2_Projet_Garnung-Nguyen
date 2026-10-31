package lunea.dao;
import lunea.model.Commande;
import java.sql.SQLException;
import java.util.List;
public interface CommandeDAO {
    /** Profil client : lit la VUE v_mes_commandes (cloisonnement par client). */
    List<Commande> mesCommandes(int limite) throws SQLException;
    /** Profil gestionnaire : toutes les commandes. */
    List<Commande> toutes(int limite) throws SQLException;
    /** CALL passer_commande : procédure transactionnelle. lignesJson = [{"id":1,"q":2},...]. Retourne l'id de la commande. */
    int passer(int clientId, String lignesJson) throws SQLException;
    /** Appelle la FONCTION fn_total_panier. */
    double calculerTotal(String lignesJson) throws SQLException;
    /** CALL changer_statut_commande. */
    void changerStatut(int commandeId, String statut) throws SQLException;
}
