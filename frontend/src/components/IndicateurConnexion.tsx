import type { EtatConnexion } from "../hooks/useFluxTempsReel";

const LIBELLES: Record<EtatConnexion, string> = {
  connexion: "Connexion...",
  "en-direct": "En direct",
  reconnexion: "Reconnexion...",
};

export function IndicateurConnexion({ etat }: { etat: EtatConnexion }) {
  return (
    <span className="flex items-center gap-1.5 rounded-full bg-white/15 px-3 py-1.5 text-xs">
      <span
        className={`h-2 w-2 rounded-full ${etat === "en-direct" ? "bg-emerald-400" : "bg-rose-400"}`}
      />
      {LIBELLES[etat]}
    </span>
  );
}
