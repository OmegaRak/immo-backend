package mg.immo.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Endpoint SSE : GET /api/events
 *
 * Le navigateur ouvre cette connexion UNE FOIS avec l'API JavaScript EventSource,
 * et la garde ouverte indefiniment. Le serveur y ecrit une trame a chaque fois
 * qu'un evenement se produit ailleurs dans l'application (EventBus.publier).
 *
 * Un commentaire SSE ("keep-alive") est envoye toutes les 15s quand il n'y a
 * rien de neuf, pour garder la connexion active et detecter une deconnexion.
 */
public class SseHandler implements HttpHandler {

    private final EventBus bus;

    public SseHandler(EventBus bus) {
        this.bus = bus;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "text/event-stream; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-cache");
        exchange.getResponseHeaders().set("Connection", "keep-alive");
        exchange.getResponseHeaders().set("X-Accel-Buffering", "no");
        // Longueur de reponse 0 = transfert en chunks (flux ouvert indefiniment)
        exchange.sendResponseHeaders(200, 0);

        OutputStream sortie = exchange.getResponseBody();
        BlockingQueue<String> file = bus.abonner();

        try {
            sortie.write(EventBus.bytes(": connecte\n\n"));
            sortie.flush();

            while (true) {
                String trame = file.poll(15, TimeUnit.SECONDS);
                if (trame == null) {
                    sortie.write(EventBus.bytes(": keep-alive\n\n"));
                } else {
                    sortie.write(EventBus.bytes(trame));
                }
                sortie.flush();
            }
        } catch (IOException e) {
            // Le client a ferme l'onglet ou perdu la connexion : normal, on arrete simplement.
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bus.desabonner(file);
            exchange.close();
        }
    }
}
