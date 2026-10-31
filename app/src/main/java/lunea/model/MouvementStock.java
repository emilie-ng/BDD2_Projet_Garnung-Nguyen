package lunea.model;
import java.time.LocalDateTime;
public record MouvementStock(int id, int produitId, String produitNom, String type, int quantite, LocalDateTime date, String motif) {}
