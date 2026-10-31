package lunea.dao;
import lunea.model.Produit;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
public interface ProduitDAO {
    /** Profil client : lit la VUE v_catalogue. */
    List<Produit> catalogue(String texte) throws SQLException;
    Optional<Produit> trouver(int id) throws SQLException;
    /** Profil gestionnaire : tous les produits (table produit + categorie). */
    List<Produit> tous(String texte) throws SQLException;
}
