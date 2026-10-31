package lunea.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Gestion de la connexion JDBC PAR RÔLE.
 *
 * L'application ne se connecte jamais avec root ni avec le propriétaire des tables :
 *   - profil client        -> compte MySQL app_client
 *   - profil gestionnaire  -> compte MySQL app_gestionnaire
 * Pour un client, la variable de session @app_user_id est positionnée juste après la connexion ;
 * les vues de cloisonnement l'utilisent (via la fonction fn_user_id()) pour ne montrer que ses lignes.
 *
 * Réglages par variables d'environnement (aucun mot de passe dans le code) :
 *   DB_URL (facultatif), DB_CLIENT_USER / DB_CLIENT_PASSWORD, DB_GESTIONNAIRE_USER / DB_GESTIONNAIRE_PASSWORD
 */
public final class ConnectionManager {

    public enum Role { CLIENT, GESTIONNAIRE }

    private static final String DB_URL = System.getenv().getOrDefault(
            "DB_URL", "jdbc:mysql://localhost:3306/boutique_en_ligne?serverTimezone=Europe/Paris");

    private static Role role;
    private static Integer userId;

    private ConnectionManager() {}

    /** Mémorise l'utilisateur connecté : les connexions suivantes utiliseront son rôle. */
    public static void ouvrirSession(Role r, Integer idClient) { role = r; userId = idClient; }

    public static void fermerSession() { role = null; userId = null; }

    public static Connection getConnection() throws SQLException {
        if (role == null) throw new IllegalStateException("Aucun utilisateur connecté");
        String nom = role.name();   // CLIENT ou GESTIONNAIRE
        String user = System.getenv().getOrDefault("DB_" + nom + "_USER", "app_" + nom.toLowerCase());
        String password = System.getenv("DB_" + nom + "_PASSWORD");
        if (password == null) throw new IllegalStateException("Variable d'environnement DB_" + nom + "_PASSWORD manquante");

        Connection conn = DriverManager.getConnection(DB_URL, user, password);
        if (userId != null) {
            try (PreparedStatement ps = conn.prepareStatement("SET @app_user_id = ?")) {
                ps.setInt(1, userId);
                ps.execute();
            }
        }
        return conn;
    }
}
