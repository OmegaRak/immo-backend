package mg.immo.socket;

import mg.immo.model.Bien;
import mg.immo.repo.BienRepository;
import mg.immo.util.Json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Traite un client connecte au serveur socket.
 *
 * PROTOCOLE TEXTE (une commande par ligne, une reponse JSON par ligne) :
 *   HELP
 *   LIST
 *   GET <id>
 *   SEARCH <ville>|<type>|<prixMax>|<surfaceMin>      (champ vide = critere ignore)
 *   ADD <titre>|<type>|<ville>|<quartier>|<surface>|<nbPieces>|<prix>
 *   DELETE <id>
 *   STATS
 *   QUIT
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final BienRepository repo;

    public ClientHandler(Socket socket, BienRepository repo) {
        this.socket = socket;
        this.repo = repo;
    }

    @Override
    public void run() {
        try (Socket s = socket;
             BufferedReader in = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(
                     new java.io.OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8), true)) {

            String ligne;
            while ((ligne = in.readLine()) != null) {
                ligne = ligne.trim();
                if (ligne.isEmpty()) {
                    continue;
                }
                System.out.println("[SOCKET] <- " + ligne);
                if (ligne.equalsIgnoreCase("QUIT") || ligne.equalsIgnoreCase("EXIT")) {
                    out.println(Json.ok("\"Au revoir\""));
                    break;
                }
                String reponse = traiter(ligne);
                out.println(reponse);
            }
        } catch (IOException e) {
            System.err.println("[SOCKET] Client interrompu : " + e.getMessage());
        }
    }

    private String traiter(String ligne) {
        String commande;
        String argument = "";
        int espace = ligne.indexOf(' ');
        if (espace < 0) {
            commande = ligne;
        } else {
            commande = ligne.substring(0, espace);
            argument = ligne.substring(espace + 1).trim();
        }

        try {
            switch (commande.toUpperCase()) {

                case "HELP":
                    return Json.ok("\"Commandes : LIST | GET <id> | SEARCH ville|type|prixMax|surfaceMin"
                            + " | ADD titre|type|ville|quartier|surface|nbPieces|prix | DELETE <id> | STATS | QUIT\"");

                case "LIST": {
                    List<Bien> biens = repo.listerTous();
                    return Json.ok(Json.array(biens, Bien::toJson));
                }

                case "GET": {
                    int id = Integer.parseInt(argument);
                    Bien b = repo.trouverParId(id);
                    if (b == null) {
                        return Json.erreur("Aucun bien avec l'id " + id);
                    }
                    return Json.ok(b.toJson());
                }

                case "SEARCH": {
                    String[] p = decouper(argument, 4);
                    List<Bien> biens = repo.rechercher(p[0], p[1], nombre(p[2]), nombre(p[3]));
                    return Json.ok(Json.array(biens, Bien::toJson));
                }

                case "ADD": {
                    String[] p = decouper(argument, 7);
                    if (p[0].isBlank()) {
                        return Json.erreur("Le titre est obligatoire");
                    }
                    Bien b = new Bien(0, p[0],
                            p[1].isBlank() ? "AUTRE" : p[1].toUpperCase(),
                            p[2], p[3],
                            nombre(p[4]), (int) nombre(p[5]), nombre(p[6]),
                            true, null);
                    repo.ajouter(b);
                    return Json.ok(b.toJson());
                }

                case "DELETE": {
                    int id = Integer.parseInt(argument);
                    boolean supprime = repo.supprimer(id);
                    if (!supprime) {
                        return Json.erreur("Aucun bien avec l'id " + id);
                    }
                    return Json.ok("\"Bien " + id + " supprime\"");
                }

                case "STATS":
                    return Json.ok(repo.statistiques().toJson());

                default:
                    return Json.erreur("Commande inconnue : " + commande + " (tapez HELP)");
            }
        } catch (NumberFormatException e) {
            return Json.erreur("Parametre numerique invalide");
        } catch (Exception e) {
            return Json.erreur("Erreur serveur : " + e.getMessage());
        }
    }

    /** Decoupe sur '|' et complete avec des chaines vides jusqu'a 'taille' elements. */
    private String[] decouper(String argument, int taille) {
        String[] brut = argument.split("\\|", -1);
        String[] resultat = new String[taille];
        for (int i = 0; i < taille; i++) {
            resultat[i] = i < brut.length ? brut[i].trim() : "";
        }
        return resultat;
    }

    private double nombre(String s) {
        if (s == null || s.isBlank()) {
            return 0;
        }
        return Double.parseDouble(s.replace(" ", ""));
    }
}
