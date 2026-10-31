package lunea.ui;

import lunea.dao.StatsDAO.JourCa;
import lunea.dao.StatsDAO.TopProduit;
import lunea.model.*;
import lunea.service.BoutiqueService;
import lunea.util.Erreurs;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Application console LUNÉA.mode – COUCHE INTERFACE UTILISATEUR.
 * Aucun SQL ici : tout passe par BoutiqueService (puis les DAO).
 * Chaque profil a son propre menu : client et gestionnaire.
 */
public class MainMenu {

    private static final BoutiqueService service = new BoutiqueService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final List<String> STATUTS = List.of("panier", "validee", "expediee", "livree", "annulee");

    /** Panier du client connecté : id produit -> quantité (gardé en mémoire jusqu'à la validation). */
    private static final Map<Integer, Integer> panier = new LinkedHashMap<>();
    private static Client clientConnecte;

    public static void main(String[] args) {
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║            LUNÉA.mode – Boutique         ║");
        System.out.println("╚══════════════════════════════════════════╝");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Se connecter comme client");
            System.out.println("2. Se connecter comme gestionnaire");
            System.out.println("0. Quitter");
            System.out.print("Votre choix : ");
            switch (scanner.nextLine().trim()) {
                case "1" -> executer(MainMenu::connexionClient);
                case "2" -> executer(MainMenu::connexionGestionnaire);
                case "0" -> { System.out.println("Au revoir !"); running = false; }
                default -> System.out.println("Choix invalide.");
            }
        }
    }

    @FunctionalInterface private interface Action { void run() throws SQLException; }

    /** Exécute une action et affiche proprement les erreurs de la base (trigger, procédure, droits...). */
    private static void executer(Action a) {
        try { a.run(); }
        catch (SQLException e) { System.out.println("[ERREUR] " + Erreurs.message(e)); }
        catch (IllegalStateException e) { System.out.println("[CONFIGURATION] " + e.getMessage()); }
    }

    // ------------------------------------------------------------------
    // Profil CLIENT
    // ------------------------------------------------------------------

    private static void connexionClient() throws SQLException {
        System.out.println("Comptes de test : voir le README (par exemple 1, 2 ou 3).");
        int id = lireEntier("Numéro de compte client : ");
        clientConnecte = service.connecterClient(id);
        if (clientConnecte == null) { System.out.println("Compte introuvable."); return; }
        panier.clear();
        System.out.println("Bienvenue " + clientConnecte.prenom() + " " + clientConnecte.nom() + " !");
        boolean actif = true;
        while (actif) {
            System.out.println("\n── Menu client ──────────────────────────");
            System.out.println("1. Voir le catalogue");
            System.out.println("2. Ajouter un produit au panier");
            System.out.println("3. Voir mon panier");
            System.out.println("4. Vider mon panier");
            System.out.println("5. Valider ma commande");
            System.out.println("6. Mes commandes");
            System.out.println("0. Se déconnecter");
            System.out.print("Votre choix : ");
            String choix = scanner.nextLine().trim();
            if (choix.equals("0")) { actif = false; break; }
            executer(() -> {
                switch (choix) {
                    case "1" -> afficherProduits(service.catalogue(lireTexte("Recherche (vide = tout) : ")));
                    case "2" -> ajouterAuPanier();
                    case "3" -> voirPanier();
                    case "4" -> { panier.clear(); System.out.println("Panier vidé."); }
                    case "5" -> validerCommande();
                    case "6" -> mesCommandes();
                    default -> System.out.println("Choix invalide.");
                }
            });
        }
        service.deconnecter();
        clientConnecte = null;
    }

    private static void ajouterAuPanier() throws SQLException {
        int id = lireEntier("Numéro du produit : ");
        Produit p = service.produit(id);
        if (p == null) { System.out.println("Produit introuvable."); return; }
        int qte = lireEntier("Quantité : ");
        int total = panier.getOrDefault(id, 0) + qte;
        if (qte <= 0) { System.out.println("La quantité doit être positive."); return; }
        if (total > p.stock()) { System.out.println("Stock insuffisant : " + p.stock() + " disponible(s)."); return; }
        panier.put(id, total);
        System.out.printf("✓ %d × %s ajouté(s) au panier.%n", qte, p.nom());
    }

    private static void voirPanier() throws SQLException {
        if (panier.isEmpty()) { System.out.println("Votre panier est vide."); return; }
        for (var e : panier.entrySet()) {
            Produit p = service.produit(e.getKey());
            if (p != null) System.out.printf("  %d × %-28s %8.2f €%n", e.getValue(), p.nom(), p.prix());
        }
        System.out.printf("Total (calculé par la base) : %.2f €%n", service.totalPanier(panier));
    }

    private static void validerCommande() throws SQLException {
        if (panier.isEmpty()) { System.out.println("Votre panier est vide."); return; }
        int id = service.validerCommande(clientConnecte.id(), panier);
        panier.clear();
        System.out.println("✓ Commande n° " + id + " enregistrée.");
    }

    private static void mesCommandes() throws SQLException {
        List<Commande> commandes = service.mesCommandes();
        if (commandes.isEmpty()) { System.out.println("Aucune commande."); return; }
        for (Commande c : commandes) {
            System.out.printf("Commande n° %d – %s – %s – %.2f €%n", c.id(), c.date().format(DATE), c.statut(), c.montantTotal());
            for (LigneCommande l : service.mesLignes(c.id()))
                System.out.printf("     %d × %s (%.2f €)%n", l.quantite(), l.produitNom(), l.prixUnitaire());
        }
    }

    // ------------------------------------------------------------------
    // Profil GESTIONNAIRE
    // ------------------------------------------------------------------

    private static void connexionGestionnaire() throws SQLException {
        service.connecterGestionnaire();
        System.out.println("Connecté en tant que gestionnaire.");
        boolean actif = true;
        while (actif) {
            System.out.println("\n── Menu gestionnaire ────────────────────");
            System.out.println("1. Produits et stocks");
            System.out.println("2. Ajuster le stock d'un produit");
            System.out.println("3. Mouvements de stock récents");
            System.out.println("4. Toutes les commandes");
            System.out.println("5. Changer le statut d'une commande");
            System.out.println("6. Tableau de bord (indicateurs)");
            System.out.println("7. Rafraîchir les indicateurs");
            System.out.println("0. Se déconnecter");
            System.out.print("Votre choix : ");
            String choix = scanner.nextLine().trim();
            if (choix.equals("0")) { actif = false; break; }
            executer(() -> {
                switch (choix) {
                    case "1" -> afficherProduits(service.tousLesProduits(lireTexte("Recherche (vide = tout) : ")));
                    case "2" -> ajusterStock();
                    case "3" -> mouvements();
                    case "4" -> toutesLesCommandes();
                    case "5" -> changerStatut();
                    case "6" -> tableauDeBord();
                    case "7" -> { service.rafraichirIndicateurs(); System.out.println("✓ Indicateurs rafraîchis."); }
                    default -> System.out.println("Choix invalide.");
                }
            });
        }
        service.deconnecter();
    }

    private static void ajusterStock() throws SQLException {
        int id = lireEntier("Numéro du produit : ");
        int delta = lireEntier("Variation (+ entrée, - sortie) : ");
        if (delta == 0) { System.out.println("Variation nulle : rien à faire."); return; }
        service.ajusterStock(id, delta, "Ajustement gestionnaire");
        System.out.println("✓ Mouvement de stock enregistré.");
    }

    private static void mouvements() throws SQLException {
        for (MouvementStock m : service.mouvementsRecents())
            System.out.printf("%s  %-7s %4d × %-26s %s%n", m.date().format(DATE), m.type(), m.quantite(),
                    m.produitNom(), m.motif() == null ? "" : m.motif());
    }

    private static void toutesLesCommandes() throws SQLException {
        for (Commande c : service.toutesLesCommandes()) {
            System.out.printf("Commande n° %d – client %d – %s – %s – %.2f €%n", c.id(), c.clientId(),
                    c.date().format(DATE), c.statut(), c.montantTotal());
            for (LigneCommande l : service.lignes(c.id()))
                System.out.printf("     %d × %s (%.2f €)%n", l.quantite(), l.produitNom(), l.prixUnitaire());
        }
    }

    private static void changerStatut() throws SQLException {
        int id = lireEntier("Numéro de la commande : ");
        String statut = lireTexte("Nouveau statut " + STATUTS + " : ");
        if (!STATUTS.contains(statut)) { System.out.println("Statut inconnu."); return; }
        service.changerStatut(id, statut);
        System.out.println("✓ Statut mis à jour.");
    }

    private static void tableauDeBord() throws SQLException {
        System.out.println("=== Chiffre d'affaires par jour (vue matérialisée) ===");
        List<JourCa> jours = service.caParJour();
        if (jours.isEmpty()) System.out.println("Aucune donnée : rafraîchissez les indicateurs (choix 7).");
        double total = 0; int articles = 0;
        for (JourCa j : jours) {
            System.out.printf("%s  %4d article(s)  %10.2f €%n", j.jour(), j.articles(), j.ca());
            total += j.ca(); articles += j.articles();
        }
        System.out.printf("Total : %d article(s), %.2f €%n", articles, total);
        System.out.println("\n=== Meilleurs produits (requête complexe) ===");
        for (TopProduit t : service.topProduits())
            System.out.printf("%d. %-28s %4d vendus  %10.2f €  (%.1f %%)%n", t.rang(), t.nom(), t.quantite(), t.ca(), t.part());
    }

    // ------------------------------------------------------------------
    // Affichage et lecture
    // ------------------------------------------------------------------

    private static void afficherProduits(List<Produit> produits) {
        if (produits.isEmpty()) { System.out.println("Aucun produit."); return; }
        for (Produit p : produits)
            System.out.printf("%4d  %-28s %-14s %8.2f €  stock %d%s%n", p.id(), p.nom(), p.categorie(), p.prix(), p.stock(),
                    p.actif() ? "" : "  (inactif)");
    }

    private static String lireTexte(String invite) {
        System.out.print(invite);
        return scanner.nextLine().trim();
    }

    private static int lireEntier(String invite) {
        while (true) {
            System.out.print(invite);
            try { return Integer.parseInt(scanner.nextLine().trim()); }
            catch (NumberFormatException e) { System.out.println("Veuillez saisir un nombre entier."); }
        }
    }
}
