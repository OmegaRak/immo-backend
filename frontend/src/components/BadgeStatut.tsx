import type { StatutDemande } from "../types";

const STYLES: Record<StatutDemande, string> = {
  EN_ATTENTE: "bg-amber-50 text-amber-700",
  VALIDEE: "bg-emerald-50 text-emerald-700",
  REFUSEE: "bg-rose-50 text-rose-700",
};

const LIBELLES: Record<StatutDemande, string> = {
  EN_ATTENTE: "En attente",
  VALIDEE: "✓ Validée",
  REFUSEE: "✗ Refusée",
};

export function BadgeStatut({ statut }: { statut: StatutDemande }) {
  return (
    <span className={`whitespace-nowrap rounded-full px-3 py-1 text-xs font-bold ${STYLES[statut]}`}>
      {LIBELLES[statut]}
    </span>
  );
}
