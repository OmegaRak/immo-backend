import { useEffect, useState } from "react";
import type { Bien, DemandeVisite } from "../types";
import { listerAnnonces } from "../api/annonces";
import { creerDemande, listerVisites } from "../api/visites";
import { useClientId } from "../hooks/useClientId";
import { useFluxTempsReel } from "../hooks/useFluxTempsReel";
import { useToasts } from "../components/Toasts";
import { IndicateurConnexion } from "../components/IndicateurConnexion";
import { CarteBien } from "../components/CarteBien";
import { ModaleDemandeVisite } from "../components/ModaleDemandeVisite";
import { BadgeStatut } from "../components/BadgeStatut";

export function PageClient() {
  const clientId = useClientId();
  const { ajouter: toast } = useToasts();

  const [biens, setBiens] = useState<Bien[]>([]);
  const [mesDemandes, setMesDemandes] = useState<DemandeVisite[]>([]);
  const [bienSelectionne, setBienSelectionne] = useState<Bien | null>(null);
  const [chargement, setChargement] = useState(true);

  async function rechargerDemandes() {
    try {
      setMesDemandes(await listerVisites(clientId));
    } catch {
      /* silencieux : l'utilisateur verra simplement l'ancienne liste */
    }
  }

  useEffect(() => {
    (async () => {
      try {
        setBiens(await listerAnnonces());
      } finally {
        setChargement(false);
      }
      await rechargerDemandes();
    })();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const etatConnexion = useFluxTempsReel({
    annonce: (donnees) => {
      const bien: Bien = JSON.parse(donnees);
      setBiens((precedents) => [bien, ...precedents]);
      toast("🏠 Nouvelle annonce", `${bien.titre} — ${bien.ville}`);
    },
    validation: (donnees) => {
      const demande: DemandeVisite = JSON.parse(donnees);
      if (demande.clientId !== clientId) return;
      toast("✅ Demande validée", `Votre visite pour "${demande.bienTitre}" est confirmée.`, "succes");
      rechargerDemandes();
    },
    refus: (donnees) => {
      const demande: DemandeVisite = JSON.parse(donnees);
      if (demande.clientId !== clientId) return;
      toast("❌ Demande refusée", `Votre demande pour "${demande.bienTitre}" a été refusée.`, "erreur");
      rechargerDemandes();
    },
  });

  async function envoyerDemande(donnees: {
    nomClient: string;
    telephone: string;
    dateSouhaitee: string;
    message: string;
  }) {
    if (!bienSelectionne) return;
    await creerDemande({
      bienId: bienSelectionne.id,
      clientId,
      ...donnees,
    });
    setBienSelectionne(null);
    toast("Demande envoyée", "L'agence a été notifiée de votre demande.");
    await rechargerDemandes();
  }

  return (
    <div className="min-h-screen bg-slate-50">
      <header className="bg-gradient-to-br from-teal-600 to-teal-800 px-7 py-5.5 text-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2.5">
          <div>
            <h1 className="text-xl font-bold">🏠 Espace Client</h1>
            <p className="text-sm text-teal-50/90">
              Parcourez les annonces et suivez vos demandes de visite en temps réel
            </p>
          </div>
          <div className="flex items-center gap-3.5">
            <IndicateurConnexion etat={etatConnexion} />
            <a href="/agence.html" className="text-xs text-white/85 hover:text-white">
              Espace agence →
            </a>
          </div>
        </div>
      </header>

      <main className="mx-auto max-w-6xl px-5 py-7 pb-16">
        <h2 className="mb-3.5 flex items-center gap-2 text-base font-semibold text-teal-800">
          📋 Annonces disponibles
        </h2>
        {chargement ? (
          <p className="text-sm text-slate-500">Chargement...</p>
        ) : biens.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-5 text-center text-sm text-slate-500">
            Aucune annonce pour le moment.
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {biens.map((bien, index) => (
              <CarteBien
                key={bien.id}
                bien={bien}
                estNouveau={index === 0 && biens.length > 0}
                onDemanderVisite={setBienSelectionne}
              />
            ))}
          </div>
        )}

        <h2 className="mt-9 mb-3.5 flex items-center gap-2 text-base font-semibold text-teal-800">
          📨 Mes demandes de visite
        </h2>
        {mesDemandes.length === 0 ? (
          <div className="rounded-xl border border-dashed border-slate-300 bg-white p-5 text-center text-sm text-slate-500">
            Vous n'avez pas encore fait de demande de visite.
          </div>
        ) : (
          <div className="flex flex-col gap-2.5">
            {mesDemandes.map((demande) => (
              <div
                key={demande.id}
                className="flex flex-wrap items-center justify-between gap-3 rounded-xl border-l-4 border-amber-400 bg-white p-4 shadow-sm data-[statut=VALIDEE]:border-emerald-500 data-[statut=REFUSEE]:border-rose-500 data-[statut=REFUSEE]:opacity-70"
                data-statut={demande.statut}
              >
                <div>
                  <p className="text-sm font-semibold text-slate-900">{demande.bienTitre}</p>
                  <p className="text-xs text-slate-500">
                    Demande du {new Date(demande.dateCreation).toLocaleDateString("fr-FR")}
                    {demande.dateSouhaitee ? ` · visite souhaitée le ${demande.dateSouhaitee}` : ""}
                  </p>
                </div>
                <BadgeStatut statut={demande.statut} />
              </div>
            ))}
          </div>
        )}
      </main>

      {bienSelectionne && (
        <ModaleDemandeVisite
          bien={bienSelectionne}
          onFermer={() => setBienSelectionne(null)}
          onEnvoyer={envoyerDemande}
        />
      )}
    </div>
  );
}
