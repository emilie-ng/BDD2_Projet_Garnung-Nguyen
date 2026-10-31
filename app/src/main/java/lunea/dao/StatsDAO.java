package lunea.dao;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
public interface StatsDAO {
    record JourCa(LocalDate jour, int articles, double ca) {}
    record TopProduit(int rang, String nom, int quantite, double ca, double part) {}

    /** Lit la table mv_ca_jour (vue matérialisée simulée sous MySQL). */
    List<JourCa> caParJour() throws SQLException;
    /** CALL rafraichir_mv_ca_jour : stratégie de rafraîchissement à la demande. */
    void rafraichir() throws SQLException;
    /** REQUÊTE COMPLEXE : CTE + jointures/agrégat + fonctions de fenêtre (MySQL 8). */
    List<TopProduit> topProduits() throws SQLException;
}
