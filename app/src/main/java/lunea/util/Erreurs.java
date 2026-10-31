package lunea.util;

import java.sql.SQLException;

/** Transforme une erreur MySQL en message lisible pour l'utilisateur. */
public final class Erreurs {
    private Erreurs() {}

    public static String message(SQLException e) {
        String etat = e.getSQLState() == null ? "" : e.getSQLState();
        int code = e.getErrorCode();
        // Erreur volontaire levée par une procédure ou un trigger : SIGNAL SQLSTATE '45000'
        if (etat.equals("45000")) return e.getMessage();
        // Droits : SELECT / EXECUTE / ... command denied
        if (code == 1142 || code == 1143 || code == 1044 || code == 1370) return "Droit refusé : cette action n'est pas autorisée pour votre profil.";
        if (code == 1045) return "Connexion à la base refusée : vérifiez les variables d'environnement DB_*_PASSWORD.";
        if (code == 3819) return "Donnée refusée par une contrainte CHECK de la base.";
        if (etat.startsWith("23")) return "Donnée refusée par la base (clé étrangère ou valeur déjà existante).";
        if (etat.startsWith("08")) return "Base de données inaccessible (MySQL est-il démarré ?).";
        return "Erreur de base de données : " + e.getMessage();
    }
}
