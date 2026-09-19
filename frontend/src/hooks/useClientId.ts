const CLE_STOCKAGE = "immo_client_id";

/**
 * Genere (une seule fois) et retourne un identifiant anonyme stable pour ce
 * navigateur, stocke dans localStorage. Il permet au backend de savoir
 * quelles demandes de visite appartiennent a quel visiteur, sans compte ni
 * mot de passe.
 */
export function useClientId(): string {
  let clientId = localStorage.getItem(CLE_STOCKAGE);
  if (!clientId) {
    clientId = `client-${Math.random().toString(36).slice(2, 10)}`;
    localStorage.setItem(CLE_STOCKAGE, clientId);
  }
  return clientId;
}
