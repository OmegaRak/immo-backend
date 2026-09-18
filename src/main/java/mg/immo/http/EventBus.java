package mg.immo.http;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Bus d'evenements en memoire (pub/sub) utilise pour le temps reel (SSE).
 *
 * Chaque abonne (une page HTML ouverte) possede une file d'attente. Quand un
 * evenement est publie (nouvelle annonce, nouvelle demande, validation...),
 * il est pousse dans TOUTES les files ; chaque page filtre ensuite cote
 * navigateur ce qui la concerne (grace au nom d'evenement SSE).
 */
public final class EventBus {

    private final CopyOnWriteArrayList<BlockingQueue<String>> abonnes = new CopyOnWriteArrayList<>();

    public BlockingQueue<String> abonner() {
        BlockingQueue<String> file = new LinkedBlockingQueue<>();
        abonnes.add(file);
        return file;
    }

    public void desabonner(BlockingQueue<String> file) {
        abonnes.remove(file);
    }

    /**
     * Publie un evenement au format Server-Sent Events :
     *   event: <type>
     *   data: <jsonUneLigne>
     *   (ligne vide)
     */
    public void publier(String type, String jsonUneLigne) {
        String trame = "event: " + type + "\ndata: " + jsonUneLigne + "\n\n";
        for (BlockingQueue<String> file : abonnes) {
            file.offer(trame);
        }
    }

    public int nombreAbonnes() {
        return abonnes.size();
    }

    public static byte[] bytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
