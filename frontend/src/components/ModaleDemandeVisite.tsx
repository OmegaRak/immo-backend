import { useState } from "react";
import type { Bien } from "../types";

interface Props {
  bien: Bien;
  onFermer: () => void;
  onEnvoyer: (donnees: {
    nomClient: string;
    telephone: string;
    dateSouhaitee: string;
    message: string;
  }) => Promise<void>;
}

export function ModaleDemandeVisite({ bien, onFermer, onEnvoyer }: Props) {
  const [nomClient, setNomClient] = useState("");
  const [telephone, setTelephone] = useState("");
  const [dateSouhaitee, setDateSouhaitee] = useState("");
  const [message, setMessage] = useState("");
  const [envoiEnCours, setEnvoiEnCours] = useState(false);

  async function gererEnvoi() {
    if (!nomClient.trim()) {
      alert("Le nom est obligatoire.");
      return;
    }
    setEnvoiEnCours(true);
    try {
      await onEnvoyer({ nomClient, telephone, dateSouhaitee, message });
    } finally {
      setEnvoiEnCours(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-2xl">
        <h3 className="text-lg font-semibold text-slate-900">Demander une visite</h3>
        <p className="mb-4.5 text-sm text-slate-500">
          {bien.titre} — {bien.ville}
        </p>

        <div className="mb-3">
          <label className="mb-1.5 block text-xs font-semibold text-slate-500">Nom complet</label>
          <input
            value={nomClient}
            onChange={(e) => setNomClient(e.target.value)}
            placeholder="Ex : Rakoto Jean"
            className="w-full rounded-lg border border-slate-200 px-3 py-2.5 text-sm"
          />
        </div>

        <div className="mb-3">
          <label className="mb-1.5 block text-xs font-semibold text-slate-500">Téléphone</label>
          <input
            value={telephone}
            onChange={(e) => setTelephone(e.target.value)}
            placeholder="Ex : 034 00 000 00"
            className="w-full rounded-lg border border-slate-200 px-3 py-2.5 text-sm"
          />
        </div>

        <div className="mb-3">
          <label className="mb-1.5 block text-xs font-semibold text-slate-500">Date souhaitée</label>
          <input
            type="date"
            value={dateSouhaitee}
            onChange={(e) => setDateSouhaitee(e.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2.5 text-sm"
          />
        </div>

        <div className="mb-4.5">
          <label className="mb-1.5 block text-xs font-semibold text-slate-500">
            Message (optionnel)
          </label>
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            rows={2}
            placeholder="Précisions sur votre disponibilité..."
            className="w-full rounded-lg border border-slate-200 px-3 py-2.5 text-sm"
          />
        </div>

        <div className="flex gap-2.5">
          <button
            type="button"
            onClick={onFermer}
            className="flex-1 rounded-lg bg-slate-100 py-2.5 text-sm font-semibold text-slate-700"
          >
            Annuler
          </button>
          <button
            type="button"
            disabled={envoiEnCours}
            onClick={gererEnvoi}
            className="flex-1 rounded-lg bg-teal-600 py-2.5 text-sm font-semibold text-white hover:bg-teal-700 disabled:bg-slate-300"
          >
            {envoiEnCours ? "Envoi..." : "Envoyer la demande"}
          </button>
        </div>
      </div>
    </div>
  );
}
