package mg.immo.repo;

import mg.immo.model.DemandeVisite;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Depot en memoire des demandes de visite (singleton, thread-safe). */
public final class DemandeVisiteRepository {

    private static final DemandeVisiteRepository INSTANCE = new DemandeVisiteRepository();

    private final Map<Integer, DemandeVisite> demandes = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger(0);

    private DemandeVisiteRepository() {
    }

    public static DemandeVisiteRepository getInstance() {
        return INSTANCE;
    }

    public DemandeVisite ajouter(DemandeVisite d) {
        int id = sequence.incrementAndGet();
        d.setId(id);
        d.setStatut(DemandeVisite.EN_ATTENTE);
        d.setDateCreation(LocalDateTime.now().toString());
        demandes.put(id, d);
        return d;
    }

    public DemandeVisite trouverParId(int id) {
        return demandes.get(id);
    }

    public List<DemandeVisite> listerTous() {
        List<DemandeVisite> liste = new ArrayList<>(demandes.values());
        liste.sort(Comparator.comparingInt(DemandeVisite::getId).reversed());
        return liste;
    }

    public List<DemandeVisite> listerParClient(String clientId) {
        List<DemandeVisite> liste = new ArrayList<>();
        for (DemandeVisite d : listerTous()) {
            if (d.getClientId() != null && d.getClientId().equals(clientId)) {
                liste.add(d);
            }
        }
        return liste;
    }

    public DemandeVisite changerStatut(int id, String statut) {
        DemandeVisite d = demandes.get(id);
        if (d != null) {
            d.setStatut(statut);
        }
        return d;
    }
}
