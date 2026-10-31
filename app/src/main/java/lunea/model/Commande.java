package lunea.model;
import java.time.LocalDateTime;
public record Commande(int id, int clientId, LocalDateTime date, String statut, double montantTotal) {}
