package mg.immo.model;

import mg.immo.util.Json;

/** Represente une demande de visite faite par un client pour un bien. */
public class DemandeVisite {

    public static final String EN_ATTENTE = "EN_ATTENTE";
    public static final String VALIDEE = "VALIDEE";
    public static final String REFUSEE = "REFUSEE";

    private int id;
    private int bienId;
    private String bienTitre;
    private String clientId;
    private String nomClient;
    private String telephone;
    private String dateSouhaitee;
    private String message;
    private String statut;
    private String dateCreation;

    public DemandeVisite() {
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBienId() { return bienId; }
    public void setBienId(int bienId) { this.bienId = bienId; }

    public String getBienTitre() { return bienTitre; }
    public void setBienTitre(String bienTitre) { this.bienTitre = bienTitre; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getDateSouhaitee() { return dateSouhaitee; }
    public void setDateSouhaitee(String dateSouhaitee) { this.dateSouhaitee = dateSouhaitee; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getDateCreation() { return dateCreation; }
    public void setDateCreation(String dateCreation) { this.dateCreation = dateCreation; }

    public String toJson() {
        return "{"
                + "\"id\":" + id
                + ",\"bienId\":" + bienId
                + ",\"bienTitre\":" + Json.str(bienTitre)
                + ",\"clientId\":" + Json.str(clientId)
                + ",\"nomClient\":" + Json.str(nomClient)
                + ",\"telephone\":" + Json.str(telephone)
                + ",\"dateSouhaitee\":" + Json.str(dateSouhaitee)
                + ",\"message\":" + Json.str(message)
                + ",\"statut\":" + Json.str(statut)
                + ",\"dateCreation\":" + Json.str(dateCreation)
                + "}";
    }
}
