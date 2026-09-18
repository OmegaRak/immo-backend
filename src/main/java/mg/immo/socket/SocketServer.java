package mg.immo.socket;

import mg.immo.repo.BienRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Serveur SOCKET TCP BRUT (java.net.ServerSocket).
 * Aucun framework : on accepte les connexions et on delegue chaque client a un thread.
 *
 * Testable directement avec :  telnet localhost 9090   ou   nc localhost 9090
 */
public class SocketServer implements Runnable {

    private final int port;
    private final BienRepository repo;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = true;
    private ServerSocket serverSocket;

    public SocketServer(int port, BienRepository repo) {
        this.port = port;
        this.repo = repo;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[SOCKET] Serveur socket brut demarre sur le port " + port);
            while (running) {
                Socket client = serverSocket.accept();
                System.out.println("[SOCKET] Nouveau client : " + client.getRemoteSocketAddress());
                pool.submit(new ClientHandler(client, repo));
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("[SOCKET] Erreur serveur : " + e.getMessage());
            }
        }
    }

    public void arreter() {
        running = false;
        pool.shutdownNow();
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException ignored) {
        }
        System.out.println("[SOCKET] Serveur arrete.");
    }
}
