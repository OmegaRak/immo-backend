package mg.immo.soap;

import jakarta.jws.WebService;
import mg.immo.model.Bien;
import mg.immo.model.Statistiques;
import mg.immo.repo.BienRepository;


/** Implementation du service SOAP. */
@WebService(endpointInterface = "mg.immo.soap.ImmobilierService",
        serviceName = "ImmobilierService",
        portName = "ImmobilierPort",
        targetNamespace = "http://soap.immo.mg/")
public class ImmobilierServiceImpl implements ImmobilierService {

    private final BienRepository repo = BienRepository.getInstance();

        @Override
    public Bien[] listerBiens() {
        log("listerBiens()");
        return repo.listerTous().toArray(new Bien[0]);
    }

    @Override
    public Bien obtenirBien(int id) {
        log("obtenirBien(" + id + ")");
        return repo.trouverParId(id);
    }

        @Override
    public Bien[] rechercherBiens(String ville, String type, double prixMax, double surfaceMin) {
        log("rechercherBiens(" + ville + ", " + type + ", " + prixMax + ", " + surfaceMin + ")");
        return repo.rechercher(ville, type, prixMax, surfaceMin).toArray(new Bien[0]);
    }

    @Override
    public Bien ajouterBien(String titre, String type, String ville, String quartier,
                            double surface, int nbPieces, double prix) {
        log("ajouterBien(" + titre + ")");
        Bien b = new Bien(0, titre, type == null ? "AUTRE" : type.toUpperCase(),
                ville, quartier, surface, nbPieces, prix, true, null);
        return repo.ajouter(b);
    }

    @Override
    public boolean supprimerBien(int id) {
        log("supprimerBien(" + id + ")");
        return repo.supprimer(id);
    }

    @Override
    public Statistiques obtenirStatistiques() {
        log("obtenirStatistiques()");
        return repo.statistiques();
    }

    private void log(String message) {
        System.out.println("[SOAP] " + message);
    }
}
