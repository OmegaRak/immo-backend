import { useEffect, useRef, useState } from "react";
import type { TypeEvenement } from "../types";

export type EtatConnexion = "connexion" | "en-direct" | "reconnexion";

type GestionnairesEvenements = Partial<
  Record<TypeEvenement, (donnees: string) => void>
>;

/**
 * Ouvre une connexion Server-Sent Events vers /api/events et appelle le
 * gestionnaire correspondant a chaque evenement recu (annonce, demande,
 * validation, refus). Le navigateur reconnecte automatiquement en cas de
 * coupure ; ce hook se contente de refleter l'etat de connexion a l'ecran.
 */
export function useFluxTempsReel(gestionnaires: GestionnairesEvenements) {
  const [etat, setEtat] = useState<EtatConnexion>("connexion");
  // On garde les gestionnaires dans une ref pour ne pas avoir a rouvrir
  // la connexion SSE a chaque rendu du composant appelant.
  const gestionnairesRef = useRef(gestionnaires);
  gestionnairesRef.current = gestionnaires;

  useEffect(() => {
    const source = new EventSource("/api/events");

    source.onopen = () => setEtat("en-direct");
    source.onerror = () => setEtat("reconnexion");

    const types: TypeEvenement[] = ["annonce", "demande", "validation", "refus"];
    const ecouteurs = types.map((type) => {
      const ecouteur = (evenement: MessageEvent<string>) => {
        gestionnairesRef.current[type]?.(evenement.data);
      };
      source.addEventListener(type, ecouteur);
      return { type, ecouteur };
    });

    return () => {
      ecouteurs.forEach(({ type, ecouteur }) => source.removeEventListener(type, ecouteur));
      source.close();
    };
  }, []);

  return etat;
}
