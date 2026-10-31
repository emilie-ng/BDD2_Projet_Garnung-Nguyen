package lunea.ui;

import lunea.service.BoutiqueService;
import lunea.util.Erreurs;

import java.sql.SQLException;

/** Vérifie que les deux comptes applicatifs peuvent se connecter (variables d'environnement DB_*). */
public class TestConnection {
    public static void main(String[] args) {
        BoutiqueService service = new BoutiqueService();
        try {
            service.connecterGestionnaire();
            System.out.println("✓ Connexion gestionnaire OK");
            service.deconnecter();
            System.out.println(service.connecterClient(1) != null ? "✓ Connexion client OK" : "✗ Client 1 introuvable");
            service.deconnecter();
        } catch (SQLException e) {
            System.out.println("✗ " + Erreurs.message(e));
        } catch (IllegalStateException e) {
            System.out.println("✗ " + e.getMessage());
        }
    }
}
