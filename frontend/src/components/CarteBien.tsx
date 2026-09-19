import type { Bien } from "../types";

function formatPrix(prix: number): string {
  return `${prix.toLocaleString("fr-FR")} Ar`;
}

interface Props {
  bien: Bien;
  estNouveau?: boolean;
  onDemanderVisite: (bien: Bien) => void;
}

export function CarteBien({ bien, estNouveau, onDemanderVisite }: Props) {
  return (
    <div
      className={`rounded-2xl border bg-white p-4.5 shadow-sm transition-shadow hover:shadow-md ${
        estNouveau ? "animate-apparition border-teal-400 ring-4 ring-teal-100" : "border-slate-200"
      }`}
    >
      <span className="mb-2 inline-block rounded-full bg-teal-50 px-2.5 py-1 text-[11px] font-semibold text-teal-700">
        {bien.type}
      </span>
      <h3 className="mb-1.5 text-base font-semibold text-slate-900">{bien.titre}</h3>
      <p className="mb-2.5 text-sm text-slate-500">
        📍 {bien.ville}
        {bien.quartier ? `, ${bien.quartier}` : ""}
      </p>
      <div className="mb-3 flex gap-3.5 text-xs text-slate-500">
        <span>📐 {Math.round(bien.surface)} m²</span>
        <span>🚪 {bien.nbPieces} pièce(s)</span>
      </div>
      <p className="mb-3 text-lg font-bold text-teal-700">{formatPrix(bien.prix)}</p>
      <button
        type="button"
        disabled={!bien.disponible}
        onClick={() => onDemanderVisite(bien)}
        className="w-full rounded-lg bg-teal-600 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-teal-700 disabled:cursor-not-allowed disabled:bg-slate-300"
      >
        {bien.disponible ? "Demander une visite" : "Indisponible"}
      </button>
    </div>
  );
}
