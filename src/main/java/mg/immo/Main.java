package mg.immo;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import jakarta.xml.ws.Endpoint;
import mg.immo.http.AnnonceApiHandler;
import mg.immo.http.CorsFilter;
import mg.immo.http.EventBus;
import mg.immo.http.SocketBridgeHandler;
import mg.immo.http.SseHandler;
import mg.immo.http.StaticFileHandler;
import mg.immo.http.VisiteApiHandler;
import mg.immo.repo.BienRepository;
import mg.immo.repo.DemandeVisiteRepository;
import mg.immo.soap.ImmobilierServiceImpl;
import mg.immo.socket.SocketServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Point d'entree unique :
 *   - demarre le serveur SOCKET TCP brut (port 9090)
 *   - demarre un serveur HTTP (port 8081) qui heberge :
 *        /ws/immobilier   -> le service SOAP (JAX-WS)
 *        /api/socket      -> le pont HTTP vers la socket brute
 *        /api/annonces    -> API JSON (liste + creation d'annonces)
 *        /api/visites     -> API JSON (demandes de visite + validation/refus)
 *        /api/events      -> flux temps reel (Server-Sent Events)
 *        /*               -> les pages HTML (soap.html, socket.html, client.html, agence.html)
 */
public class Main {

    public static final int PORT_HTTP = 8081;
    public static final int PORT_SOCKET = 9090;

    public static void main(String[] args) throws Exception {

        BienRepository repoBiens = BienRepository.getInstance();
        DemandeVisiteRepository repoVisites = DemandeVisiteRepository.getInstance();
        EventBus bus = new EventBus();

        // 1) Serveur socket brut dans un thread dedie
        SocketServer socketServer = new SocketServer(PORT_SOCKET, repoBiens);
        Thread threadSocket = new Thread(socketServer, "socket-server");
        threadSocket.setDaemon(true);
        threadSocket.start();

        // 2) Serveur HTTP
        // Pool elargi : les connexions SSE restent ouvertes en continu et occupent un thread chacune.
        HttpServer http = HttpServer.create(new InetSocketAddress(PORT_HTTP), 0);
        http.setExecutor(Executors.newFixedThreadPool(30));

        // 2a) Endpoint SOAP publie sur le contexte /ws/immobilier
        HttpContext contexteSoap = http.createContext("/ws/immobilier");
        contexteSoap.getFilters().add(new CorsFilter());
        Endpoint endpoint = Endpoint.create(new ImmobilierServiceImpl());
        endpoint.publish(contexteSoap);

        // 2b) Pont HTTP -> socket
        HttpContext contextePont = http.createContext("/api/socket",
                new SocketBridgeHandler("127.0.0.1", PORT_SOCKET));
        contextePont.getFilters().add(new CorsFilter());

        // 2c) API annonces (page agence publie, page client lit)
        HttpContext contexteAnnonces = http.createContext("/api/annonces",
                new AnnonceApiHandler(repoBiens, bus));
        contexteAnnonces.getFilters().add(new CorsFilter());

        // 2d) API demandes de visite (page client cree, page agence valide/refuse)
        HttpContext contexteVisites = http.createContext("/api/visites",
                new VisiteApiHandler(repoVisites, repoBiens, bus));
        contexteVisites.getFilters().add(new CorsFilter());

        // 2e) Flux temps reel SSE, partage par les deux pages
        HttpContext contexteEvents = http.createContext("/api/events", new SseHandler(bus));
        contexteEvents.getFilters().add(new CorsFilter());

        // 2f) Pages HTML
        http.createContext("/", new StaticFileHandler());

        http.start();

        System.out.println("=====================================================");
        System.out.println("  BACKEND IMMOBILIER DEMARRE");
        System.out.println("-----------------------------------------------------");
        System.out.println("  Page SOAP     : http://localhost:" + PORT_HTTP + "/soap.html");
        System.out.println("  Page SOCKET   : http://localhost:" + PORT_HTTP + "/socket.html");
        System.out.println("  Page CLIENT   : http://localhost:" + PORT_HTTP + "/client.html");
        System.out.println("  Page AGENCE   : http://localhost:" + PORT_HTTP + "/agence.html");
        System.out.println("  WSDL          : http://localhost:" + PORT_HTTP + "/ws/immobilier?wsdl");
        System.out.println("  Socket brute  : localhost:" + PORT_SOCKET + "  (nc localhost " + PORT_SOCKET + ")");
        System.out.println("=====================================================");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nArret en cours...");
            endpoint.stop();
            http.stop(0);
            socketServer.arreter();
        }));
    }
}
