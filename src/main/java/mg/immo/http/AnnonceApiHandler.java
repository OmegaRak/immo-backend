package mg.immo.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mg.immo.model.Bien;
import mg.immo.repo.BienRepository;
import mg.immo.util.Json;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * API HTTP/JSON simple pour les pages client.html et agence.html :
 *   GET  /api/annonces  -> liste des biens
 *   POST /api/annonces  -> creation d'une annonce par l'agence (diffusee en temps reel)
 */
public class AnnonceApiHandler implements HttpHandler {

    private final BienRepository repo;
    private final EventBus bus;

    public AnnonceApiHandler(BienRepository repo, EventBus bus) {
        this.repo = repo;
        this.bus = bus;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String methode = exchange.getRequestMethod();
        try {
            if ("GET".equalsIgnoreCase(methode)) {
                traiterListe(exchange);
            } else if ("POST".equalsIgnoreCase(methode)) {
                traiterCreation(exchange);
            } else {
                repondre(exchange, 405, Json.erreur("Methode non supportee"));
            }
        } catch (Exception e) {
            repondre(exchange, 500, Json.erreur("Erreur serveur : " + e.getMessage()));
        }
    }

    private void traiterListe(HttpExchange exchange) throws IOException {
        List<Bien> biens = repo.listerTous();
        repondre(exchange, 200, Json.ok(Json.array(biens, Bien::toJson)));
    }

    private void traiterCreation(HttpExchange exchange) throws IOException {
        String corps = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> champs = Forms.parse(corps);

        String titre = Forms.get(champs, "titre", "").trim();
        if (titre.isEmpty()) {
            repondre(exchange, 400, Json.erreur("Le titre est obligatoire"));
            return;
        }

        Bien bien = new Bien(0, titre,
                Forms.get(champs, "type", "AUTRE").toUpperCase(),
                Forms.get(champs, "ville", ""),
                Forms.get(champs, "quartier", ""),
                Forms.getDouble(champs, "surface", 0),
                Forms.getInt(champs, "nbPieces", 0),
                Forms.getDouble(champs, "prix", 0),
                true, null);

        repo.ajouter(bien);
        bus.publier("annonce", bien.toJson());
        System.out.println("[API] Nouvelle annonce publiee : " + bien);

        repondre(exchange, 201, Json.ok(bien.toJson()));
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
