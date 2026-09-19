/** Correspond au modele Java mg.immo.model.Bien */
export interface Bien {
  id: number;
  titre: string;
  type: string;
  ville: string;
  quartier: string;
  surface: number;
  nbPieces: number;
  prix: number;
  disponible: boolean;
  dateAjout: string;
}

/** Statuts possibles d'une demande de visite, alignes sur mg.immo.model.DemandeVisite */
export type StatutDemande = "EN_ATTENTE" | "VALIDEE" | "REFUSEE";

/** Correspond au modele Java mg.immo.model.DemandeVisite */
export interface DemandeVisite {
  id: number;
  bienId: number;
  bienTitre: string;
  clientId: string;
  nomClient: string;
  telephone: string;
  dateSouhaitee: string;
  message: string;
  statut: StatutDemande;
  dateCreation: string;
}

/** Enveloppe generique renvoyee par l'API (mg.immo.util.Json) */
export interface ReponseApi<T> {
  status: "OK" | "ERREUR";
  data?: T;
  message?: string;
}

/** Types d'evenements temps reel diffuses par /api/events (SSE) */
export type TypeEvenement = "annonce" | "demande" | "validation" | "refus";
