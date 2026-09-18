package mg.immo.repo;

import mg.immo.model.Bien;
import mg.immo.model.Statistiques;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Depot de donnees en memoire (singleton), partage par le service SOAP
 * et par le serveur Socket. Thread-safe.
 */
public final class BienRepository {

    private static final BienRepository INSTANCE = new BienRepository();

    private final Map<Integer, Bien> biens = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    private BienRepository() {
        seed();
    }

    public static BienRepository getInstance() {
        return INSTANCE;
    }

    private void seed() {
        String today = LocalDate.now().toString();
        ajouter(new Bien(0, "Appartement F3 lumineux", "APPARTEMENT", "Antananarivo", "Ivandry",
                85, 3, 450_000_000d, true, today));
        ajouter(new Bien(0, "Villa avec piscine", "VILLA", "Antananarivo", "Ambatobe",
                320, 7, 1_850_000_000d, true, today));
        ajouter(new Bien(0, "Maison familiale", "MAISON", "Antsirabe", "Mahazoarivo",
                140, 5, 320_000_000d, true, today));
        ajouter(new Bien(0, "Terrain constructible 500m2", "TERRAIN", "Toamasina", "Analakininina",
                500, 0, 180_000_000d, true, today));
        ajouter(new Bien(0, "Bureau open-space", "BUREAU", "Antananarivo", "Analakely",
                210, 6, 900_000_000d, false, today));
        ajouter(new Bien(0, "Studio meuble", "APPARTEMENT", "Mahajanga", "Mahabibo",
                35, 1, 95_000_000d, true, today));
    }

    public List<Bien> listerTous() {
        List<Bien> liste = new ArrayList<>(biens.values());
        liste.sort(Comparator.comparingInt(Bien::getId));
        return liste;
    }

    public Bien trouverParId(int id) {
        return biens.get(id);
    }

    /**
     * Recherche multi-criteres. Un critere vide / null / <= 0 est ignore.
     */
    public List<Bien> rechercher(String ville, String type, double prixMax, double surfaceMin) {
        List<Bien> resultat = new ArrayList<>();
        for (Bien b : listerTous()) {
            if (ville != null && !ville.isBlank() && !b.getVille().equalsIgnoreCase(ville.trim())) {
                continue;
            }
            if (type != null && !type.isBlank() && !b.getType().equalsIgnoreCase(type.trim())) {
                continue;
            }
            if (prixMax > 0 && b.getPrix() > prixMax) {
                continue;
            }
            if (surfaceMin > 0 && b.getSurface() < surfaceMin) {
                continue;
            }
            resultat.add(b);
        }
        return resultat;
    }

    public Bien ajouter(Bien bien) {
        int id = sequence.incrementAndGet();
        bien.setId(id);
        if (bien.getDateAjout() == null || bien.getDateAjout().isBlank()) {
            bien.setDateAjout(LocalDate.now().toString());
        }
        biens.put(id, bien);
        return bien;
    }

    public boolean supprimer(int id) {
        return biens.remove(id) != null;
    }

    public Bien modifierDisponibilite(int id, boolean disponible) {
        Bien b = biens.get(id);
        if (b != null) {
            b.setDisponible(disponible);
        }
        return b;
    }

    public Statistiques statistiques() {
        List<Bien> tous = listerTous();
        Statistiques s = new Statistiques();
        s.setNombreTotal(tous.size());
        if (tous.isEmpty()) {
            return s;
        }
        double sommePrix = 0, sommeSurface = 0;
        double min = Double.MAX_VALUE, max = 0;
        int dispo = 0;
        for (Bien b : tous) {
            sommePrix += b.getPrix();
            sommeSurface += b.getSurface();
            min = Math.min(min, b.getPrix());
            max = Math.max(max, b.getPrix());
            if (b.isDisponible()) {
                dispo++;
            }
        }
        s.setNombreDisponibles(dispo);
        s.setPrixMoyen(Math.round(sommePrix / tous.size()));
        s.setPrixMin(min);
        s.setPrixMax(max);
        s.setSurfaceMoyenne(Math.round(sommeSurface / tous.size()));
        return s;
    }
}
