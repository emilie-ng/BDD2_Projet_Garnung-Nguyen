package lunea.dao;
import lunea.model.MouvementStock;
import java.sql.SQLException;
import java.util.List;
public interface MouvementStockDAO {
    List<MouvementStock> recents(int limite) throws SQLException;
    /** CALL ajuster_stock : enregistre un mouvement ; un trigger met le stock à jour. type = 'entree' ou 'sortie'. */
    void ajuster(int produitId, String type, int quantite, String motif) throws SQLException;
}
