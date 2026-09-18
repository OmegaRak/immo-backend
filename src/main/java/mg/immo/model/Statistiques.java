package mg.immo.model;

/** Petit rapport statistique renvoye par le service SOAP et par le serveur Socket. */
public class Statistiques {

    private int nombreTotal;
    private int nombreDisponibles;
    private double prixMoyen;
    private double prixMin;
    private double prixMax;
    private double surfaceMoyenne;

    public Statistiques() {
    }

    public int getNombreTotal() { return nombreTotal; }
    public void setNombreTotal(int nombreTotal) { this.nombreTotal = nombreTotal; }

    public int getNombreDisponibles() { return nombreDisponibles; }
    public void setNombreDisponibles(int nombreDisponibles) { this.nombreDisponibles = nombreDisponibles; }

    public double getPrixMoyen() { return prixMoyen; }
    public void setPrixMoyen(double prixMoyen) { this.prixMoyen = prixMoyen; }

    public double getPrixMin() { return prixMin; }
    public void setPrixMin(double prixMin) { this.prixMin = prixMin; }

    public double getPrixMax() { return prixMax; }
    public void setPrixMax(double prixMax) { this.prixMax = prixMax; }

    public double getSurfaceMoyenne() { return surfaceMoyenne; }
    public void setSurfaceMoyenne(double surfaceMoyenne) { this.surfaceMoyenne = surfaceMoyenne; }

    public String toJson() {
        return "{\"nombreTotal\":" + nombreTotal
                + ",\"nombreDisponibles\":" + nombreDisponibles
                + ",\"prixMoyen\":" + prixMoyen
                + ",\"prixMin\":" + prixMin
                + ",\"prixMax\":" + prixMax
                + ",\"surfaceMoyenne\":" + surfaceMoyenne + "}";
    }
}
