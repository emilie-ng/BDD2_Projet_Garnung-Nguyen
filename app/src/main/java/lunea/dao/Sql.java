package lunea.dao;
final class Sql {
    private Sql() {}
    /** Prépare un motif LIKE sûr : % et _ saisis par l'utilisateur sont traités comme du texte. */
    static String like(String s) {
        return "%" + s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }
}
