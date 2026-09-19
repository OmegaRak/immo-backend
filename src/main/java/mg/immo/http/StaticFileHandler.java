package mg.immo.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** Sert les pages HTML de test depuis le classpath (src/main/resources/web). */
public class StaticFileHandler implements HttpHandler {

    private static final String RACINE = "/web";

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String chemin = exchange.getRequestURI().getPath();
        if ("/".equals(chemin) || chemin.isEmpty()) {
            chemin = "/index.html";
        }
        if (chemin.contains("..")) {
            envoyer(exchange, 400, "text/plain", "Requete invalide".getBytes(StandardCharsets.UTF_8));
            return;
        }

        try (InputStream is = getClass().getResourceAsStream(RACINE + chemin)) {
            if (is == null) {
                envoyer(exchange, 404, "text/plain; charset=utf-8",
                        ("404 - Introuvable : " + chemin).getBytes(StandardCharsets.UTF_8));
                return;
            }
            envoyer(exchange, 200, typeMime(chemin), is.readAllBytes());
        }
    }

    private void envoyer(HttpExchange exchange, int code, String type, byte[] corps) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", type);
        exchange.sendResponseHeaders(code, corps.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(corps);
        }
    }

    private String typeMime(String chemin) {
        if (chemin.endsWith(".html")) return "text/html; charset=utf-8";
        if (chemin.endsWith(".css"))  return "text/css; charset=utf-8";
        if (chemin.endsWith(".js") || chemin.endsWith(".mjs")) return "application/javascript; charset=utf-8";
        if (chemin.endsWith(".json")) return "application/json; charset=utf-8";
        if (chemin.endsWith(".svg"))  return "image/svg+xml";
        if (chemin.endsWith(".png"))  return "image/png";
        if (chemin.endsWith(".jpg") || chemin.endsWith(".jpeg")) return "image/jpeg";
        if (chemin.endsWith(".ico"))  return "image/x-icon";
        if (chemin.endsWith(".woff2")) return "font/woff2";
        if (chemin.endsWith(".woff"))  return "font/woff";
        return "application/octet-stream";
    }
}
