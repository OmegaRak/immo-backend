import type { Bien, ReponseApi } from "../types";

/**
 * Recupere la liste des biens depuis le backend Java.
 * GET /api/annonces
 */
export async function listerAnnonces(): Promise<Bien[]> {
  const reponse = await fetch("/api/annonces");
  const json: ReponseApi<Bien[]> = await reponse.json();
  if (json.status !== "OK" || !json.data) {
    throw new Error(json.message ?? "Impossible de charger les annonces");
  }
  return json.data;
}

export interface NouvelleAnnonce {
  titre: string;
  type: string;
  ville: string;
  quartier: string;
  surface: number;
  nbPieces: number;
  prix: number;
}

/**
 * Publie une nouvelle annonce (cote agence).
 * POST /api/annonces (application/x-www-form-urlencoded)
 */
export async function publierAnnonce(annonce: NouvelleAnnonce): Promise<Bien> {
  const corps = new URLSearchParams({
    titre: annonce.titre,
    type: annonce.type,
    ville: annonce.ville,
    quartier: annonce.quartier,
    surface: String(annonce.surface),
    nbPieces: String(annonce.nbPieces),
    prix: String(annonce.prix),
  });

  const reponse = await fetch("/api/annonces", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: corps,
  });
  const json: ReponseApi<Bien> = await reponse.json();
  if (json.status !== "OK" || !json.data) {
    throw new Error(json.message ?? "Impossible de publier l'annonce");
  }
  return json.data;
}
