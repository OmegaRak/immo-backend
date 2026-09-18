package mg.immo.model;

import mg.immo.util.Json;

/**
 * Represente un bien immobilier.
 * Classe JavaBean (constructeur vide + getters/setters) : indispensable pour JAXB / JAX-WS.
 */
public class Bien {

    private int id;
    private String titre;
    private String type;        // APPARTEMENT, MAISON, VILLA, TERRAIN, BUREAU
    private String ville;
    private String quartier;
    private double surface;     // en m2
    private int nbPieces;
    private double prix;        // en Ariary
    private boolean disponible;
    private String dateAjout;   // format ISO yyyy-MM-dd

    public Bien() {
    }

    public Bien(int id, String titre, String type, String ville, String quartier,
                double surface, int nbPieces, double prix, boolean disponible, String dateAjout) {
        this.id = id;
        this.titre = titre;
        this.type = type;
        this.ville = ville;
        this.quartier = quartier;
        this.surface = surface;
        this.nbPieces = nbPieces;
        this.prix = prix;
        this.disponible = disponible;
        this.dateAjout = dateAjout;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getVille() { return ville; }
    public void setVille(String ville) { this.ville = ville; }

    public String getQuartier() { return quartier; }
    public void setQuartier(String quartier) { this.quartier = quartier; }

    public double getSurface() { return surface; }
    public void setSurface(double surface) { this.surface = surface; }

    public int getNbPieces() { return nbPieces; }
    public void setNbPieces(int nbPieces) { this.nbPieces = nbPieces; }

    public double getPrix() { return prix; }
    public void setPrix(double prix) { this.prix = prix; }

    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }

    public String getDateAjout() { return dateAjout; }
    public void setDateAjout(String dateAjout) { this.dateAjout = dateAjout; }

    /** Serialisation JSON manuelle (utilisee par le serveur Socket, zero dependance). */
    public String toJson() {
        return "{"
                + "\"id\":" + id
                + ",\"titre\":" + Json.str(titre)
                + ",\"type\":" + Json.str(type)
                + ",\"ville\":" + Json.str(ville)
                + ",\"quartier\":" + Json.str(quartier)
                + ",\"surface\":" + surface
                + ",\"nbPieces\":" + nbPieces
                + ",\"prix\":" + prix
                + ",\"disponible\":" + disponible
                + ",\"dateAjout\":" + Json.str(dateAjout)
                + "}";
    }

    @Override
    public String toString() {
        return "Bien#" + id + " " + titre + " (" + ville + ", " + prix + " Ar)";
    }
}
