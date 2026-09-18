package mg.immo.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mg.immo.model.Bien;
import mg.immo.model.DemandeVisite;
import mg.immo.repo.BienRepository;
import mg.immo.repo.DemandeVisiteRepository;
import mg.immo.util.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * API HTTP/JSON pour les demandes de visite :
 *   GET  /api/visites          -> toutes les demandes (page agence)
 *                                  ou ?clientId=xxx -> demandes d'un seul client (page client)
 *   POST /api/visites          -> creation d'une demande par le client (diffusee en temps reel)
 *   POST /api/visites/valider  -> l'agence valide une demande (id en parametre) (diffuse en temps reel)
 *   POST /api/visites/refuser  -> l'agence refuse une demande (id en parametre) (diffuse en temps reel)
 */
public class VisiteApiHandler implements HttpHandler {

    private final DemandeVisiteRepository repoVisites;
    private final BienRepository repoBiens;
    private final EventBus bus;

    public VisiteApiHandler(DemandeVisiteRepository repoVisites, BienRepository repoBiens, EventBus bus) {
        this.repoVisites = repoVisites;
        this.repoBiens = repoBiens;
        this.bus = bus;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String chemin = exchange.getRequestURI().getPath();
        String methode = exchange.getRequestMethod();

        try {
            if (chemin.equals("/api/visites/valider") && "POST".equalsIgnoreCase(methode)) {
                changerStatut(exchange, DemandeVisite.VALIDEE, "validation");
            } else if (chemin.equals("/api/visites/refuser") && "POST".equalsIgnoreCase(methode)) {
                changerStatut(exchange, DemandeVisite.REFUSEE, "refus");
            } else if (chemin.equals("/api/visites") && "GET".equalsIgnoreCase(methode)) {
                lister(exchange);
            } else if (chemin.equals("/api/visites") && "POST".equalsIgnoreCase(methode)) {
                creer(exchange);
            } else {
                repondre(exchange, 404, Json.erreur("Route inconnue : " + chemin));
            }
        } catch (Exception e) {
            repondre(exchange, 500, Json.erreur("Erreur serveur : " + e.getMessage()));
        }
    }

    private void lister(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getRawQuery();
        String clientId = extraireParametre(query, "clientId");

        List<DemandeVisite> liste = (clientId == null || clientId.isBlank())
                ? repoVisites.listerTous()
                : repoVisites.listerParClient(clientId);

        repondre(exchange, 200, Json.ok(Json.array(liste, DemandeVisite::toJson)));
    }

    private void creer(HttpExchange exchange) throws IOException {
        String corps = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> champs = Forms.parse(corps);

        int bienId = Forms.getInt(champs, "bienId", -1);
        Bien bien = repoBiens.trouverParId(bienId);
        if (bien == null) {
            repondre(exchange, 400, Json.erreur("Bien introuvable (id " + bienId + ")"));
            return;
        }

        String nomClient = Forms.get(champs, "nomClient", "").trim();
        if (nomClient.isEmpty()) {
            repondre(exchange, 400, Json.erreur("Le nom est obligatoire"));
            return;
        }

        DemandeVisite d = new DemandeVisite();
        d.setBienId(bienId);
        d.setBienTitre(bien.getTitre());
        d.setClientId(Forms.get(champs, "clientId", "anonyme"));
        d.setNomClient(nomClient);
        d.setTelephone(Forms.get(champs, "telephone", ""));
        d.setDateSouhaitee(Forms.get(champs, "dateSouhaitee", ""));
        d.setMessage(Forms.get(champs, "message", ""));

        repoVisites.ajouter(d);
        bus.publier("demande", d.toJson());
        System.out.println("[API] Nouvelle demande de visite : " + d.getNomClient() + " -> " + d.getBienTitre());

        repondre(exchange, 201, Json.ok(d.toJson()));
    }

    private void changerStatut(HttpExchange exchange, String statut, String nomEvenement) throws IOException {
        String corps = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> champs = Forms.parse(corps);
        int id = Forms.getInt(champs, "id", -1);

        DemandeVisite d = repoVisites.changerStatut(id, statut);
        if (d == null) {
            repondre(exchange, 404, Json.erreur("Demande introuvable (id " + id + ")"));
            return;
        }

        bus.publier(nomEvenement, d.toJson());
        System.out.println("[API] Demande " + id + " -> " + statut);

        repondre(exchange, 200, Json.ok(d.toJson()));
    }

    private String extraireParametre(String query, String nom) {
        if (query == null) {
            return null;
        }
        for (String paire : query.split("&")) {
            int eq = paire.indexOf('=');
            if (eq > 0 && paire.substring(0, eq).equals(nom)) {
                return java.net.URLDecoder.decode(paire.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    private void repondre(HttpExchange exchange, int code, String jsonCorps) throws IOException {
        byte[] octets = jsonCorps.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, octets.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(octets);
        }
    }
}
