package mg.immo.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import mg.immo.util.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * PONT HTTP -> SOCKET.
 *
 * Un navigateur ne peut pas ouvrir une socket TCP brute. Ce handler recoit la commande
 * en HTTP, ouvre lui-meme une VRAIE socket cliente (java.net.Socket) vers le serveur
 * socket, envoie la commande, lit la reponse et la renvoie au navigateur.
 *
 * C'est donc un client socket Java a part entiere : le protocole brut est bien utilise.
 */
public class SocketBridgeHandler implements HttpHandler {

    private final String host;
    private final int port;

    public SocketBridgeHandler(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String commande;

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            commande = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).trim();
        } else {
            String query = exchange.getRequestURI().getRawQuery();
            commande = extraireParametre(query, "cmd");
        }

        String reponse;
        if (commande == null || commande.isBlank()) {
            reponse = Json.erreur("Commande vide");
        } else {
            reponse = envoyerAuServeurSocket(commande);
        }

        byte[] corps = reponse.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(200, corps.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(corps);
        }
    }

    /** Ouverture d'une socket TCP brute vers le serveur socket. */
    private String envoyerAuServeurSocket(String commande) {
        try (Socket socket = new Socket(host, port)) {
            socket.setSoTimeout(5000);

            PrintWriter out = new PrintWriter(
                    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            out.println(commande);
            String ligne = in.readLine();
            return ligne == null ? Json.erreur("Pas de reponse du serveur socket") : ligne;

        } catch (IOException e) {
            return Json.erreur("Connexion socket impossible (" + host + ":" + port + ") : " + e.getMessage());
        }
    }

    private String extraireParametre(String query, String nom) {
        if (query == null) {
            return null;
        }
        for (String paire : query.split("&")) {
            int eq = paire.indexOf('=');
            if (eq > 0 && paire.substring(0, eq).equals(nom)) {
                return URLDecoder.decode(paire.substring(eq + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
