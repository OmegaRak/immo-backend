package mg.immo.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebResult;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import mg.immo.model.Bien;
import mg.immo.model.Statistiques;

/**
 * Contrat du service SOAP (SEI = Service Endpoint Interface).
 *
 * Style RPC/LITERAL : les parametres apparaissent sans prefixe de namespace
 * dans l'enveloppe SOAP, ce qui rend les requetes ecrites a la main (page HTML)
 * beaucoup plus simples a construire.
 */
@WebService(name = "ImmobilierService", targetNamespace = "http://soap.immo.mg/")
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public interface ImmobilierService {

    @WebMethod(operationName = "listerBiens")
    @WebResult(name = "bien")
    Bien[] listerBiens();

    @WebMethod(operationName = "obtenirBien")
    @WebResult(name = "bien")
    Bien obtenirBien(@WebParam(name = "id") int id);

    @WebMethod(operationName = "rechercherBiens")
    @WebResult(name = "bien")
    Bien[] rechercherBiens(@WebParam(name = "ville") String ville,
                               @WebParam(name = "type") String type,
                               @WebParam(name = "prixMax") double prixMax,
                               @WebParam(name = "surfaceMin") double surfaceMin);

    @WebMethod(operationName = "ajouterBien")
    @WebResult(name = "bien")
    Bien ajouterBien(@WebParam(name = "titre") String titre,
                     @WebParam(name = "type") String type,
                     @WebParam(name = "ville") String ville,
                     @WebParam(name = "quartier") String quartier,
                     @WebParam(name = "surface") double surface,
                     @WebParam(name = "nbPieces") int nbPieces,
                     @WebParam(name = "prix") double prix);

    @WebMethod(operationName = "supprimerBien")
    @WebResult(name = "supprime")
    boolean supprimerBien(@WebParam(name = "id") int id);

    @WebMethod(operationName = "obtenirStatistiques")
    @WebResult(name = "statistiques")
    Statistiques obtenirStatistiques();
}
