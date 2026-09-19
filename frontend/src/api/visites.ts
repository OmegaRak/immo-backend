import type { DemandeVisite, ReponseApi } from "../types";

/**
 * Liste toutes les demandes de visite (cote agence), ou seulement celles
 * d'un client precis si clientId est fourni (cote client).
 * GET /api/visites  ou  GET /api/visites?clientId=xxx
 */
export async function listerVisites(clientId?: string): Promise<DemandeVisite[]> {
  const url = clientId
    ? `/api/visites?clientId=${encodeURIComponent(clientId)}`
    : "/api/visites";
  const reponse = await fetch(url);
  const json: ReponseApi<DemandeVisite[]> = await reponse.json();
  if (json.status !== "OK" || !json.data) {
    throw new Error(json.message ?? "Impossible de charger les demandes");
  }
  return json.data;
}

export interface NouvelleDemande {
  bienId: number;
  clientId: string;
  nomClient: string;
  telephone: string;
  dateSouhaitee: string;
  message: string;
}

/**
 * Cree une demande de visite (cote client).
 * POST /api/visites (application/x-www-form-urlencoded)
 */
export async function creerDemande(demande: NouvelleDemande): Promise<DemandeVisite> {
  const corps = new URLSearchParams({
    bienId: String(demande.bienId),
    clientId: demande.clientId,
    nomClient: demande.nomClient,
    telephone: demande.telephone,
    dateSouhaitee: demande.dateSouhaitee,
    message: demande.message,
  });

  const reponse = await fetch("/api/visites", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: corps,
  });
  const json: ReponseApi<DemandeVisite> = await reponse.json();
  if (json.status !== "OK" || !json.data) {
    throw new Error(json.message ?? "Impossible d'envoyer la demande");
  }
  return json.data;
}

/**
 * Valide ou refuse une demande (cote agence).
 * POST /api/visites/valider  ou  POST /api/visites/refuser
 */
export async function repondreDemande(
  id: number,
  decision: "valider" | "refuser",
): Promise<DemandeVisite> {
  const corps = new URLSearchParams({ id: String(id) });

  const reponse = await fetch(`/api/visites/${decision}`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: corps,
  });
  const json: ReponseApi<DemandeVisite> = await reponse.json();
  if (json.status !== "OK" || !json.data) {
    throw new Error(json.message ?? "Impossible de mettre a jour la demande");
  }
  return json.data;
}
