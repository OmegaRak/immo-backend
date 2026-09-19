import { useEffect, useState } from "react";
import type { Bien, DemandeVisite } from "../types";
import { listerAnnonces, publierAnnonce } from "../api/annonces";
import { listerVisites, repondreDemande } from "../api/visites";
import { useFluxTempsReel } from "../hooks/useFluxTempsReel";
import { useToasts } from "../components/Toasts";
import { IndicateurConnexion } from "../components/IndicateurConnexion";
import { BadgeStatut } from "../components/BadgeStatut";

const TYPES_BIEN = ["APPARTEMENT", "MAISON", "VILLA", "TERRAIN", "BUREAU"];

function formatPrix(prix: number): string {
  return `${prix.toLocaleString("fr-FR")} Ar`;
}

interface FormulaireAnnonce {
  titre: string;
  type: string;
  ville: string;
  quartier: string;
  surface: string;
  nbPieces: string;
  prix: string;
}

const FORMULAIRE_VIDE: FormulaireAnnonce = {
  titre: "",
  type: TYPES_BIEN[0],
  ville: "",
  quartier: "",
  surface: "",
  nbPieces: "",
  prix: "",
};

export function PageAgence() {
  const { ajouter: toast } = useToasts();

  const [biens, setBiens] = useState<Bien[]>([]);
  const [demandes, setDemandes] = useState<DemandeVisite[]>([]);
  const [formulaire, setFormulaire] = useState<FormulaireAnnonce>(FORMULAIRE_VIDE);
  const [publicationEnCours, setPublicationEnCours] = useState(false);

  async function rechargerAnnonces() {
    setBiens(await listerAnnonces());
  }
  async function rechargerDemandes() {
    setDemandes(await listerVisites());
  }

  useEffect(() => {
    rechargerAnnonces();
    rechargerDemandes();
  }, []);

  const etatConnexion = useFluxTempsReel({
    demande: (donnees) => {
      const demande: DemandeVisite = JSON.parse(donnees);
      toast("📨 Nouvelle demande de visite", `${demande.nomClient} pour "${demande.bienTitre}"`);
      rechargerDemandes();
    },
    validation: () => rechargerDemandes(),
    refus: () => rechargerDemandes(),
  });

  async function gererPublication() {
    if (!formulaire.titre.trim()) {
      alert("Le titre est obligatoire.");
      return;
    }
    setPublicationEnCours(true);
    try {
      await publierAnnonce({
        titre: formulaire.titre,
        type: formulaire.type,
        ville: formulaire.ville,
        quartier: formulaire.quartier,
        surface: Number(formulaire.surface) || 0,
        nbPieces: Number(formulaire.nbPieces) || 0,
        prix: Number(formulaire.prix) || 0,
      });
      toast("Annonce publiée", `${formulaire.titre} est maintenant visible côté client.`);
      setFormulaire(FORMULAIRE_VIDE);
      await rechargerAnnonces();
    } catch (e) {
      alert(`Erreur : ${(e as Error).message}`);
    } finally {
      setPublicationEnCours(false);
    }
  }

  async function gererReponse(id: number, decision: "valider" | "refuser") {
    try {
      await repondreDemande(id, decision);
      await rechargerDemandes();
    } catch (e) {
      alert(`Erreur : ${(e as Error).message}`);
    }
  }

  return (
    <div className="min-h-screen bg-violet-50/40">
      <header className="bg-gradient-to-br from-violet-600 to-violet-800 px-7 py-5.5 text-white">
        <div className="mx-auto flex max-w-6xl flex-wrap items-center justify-between gap-2.5">
          <div>
            <h1 className="text-xl font-bold">🏢 Espace Agence</h1>
            <p className="text-sm text-violet-50/90">
              Publiez vos annonces et gérez les demandes de visite en temps réel
            </p>
          </div>
          <div className="flex items-center gap-3.5">
            <IndicateurConnexion etat={etatConnexion} />
            <a href="/client.html" className="text-xs text-white/85 hover:text-white">
              Espace client →
            </a>
          </div>
        </div>
      </header>

      <main className="mx-auto grid max-w-6xl grid-cols-1 gap-6 px-5 py-7 pb-16 md:grid-cols-[360px_1fr]">
        <div className="flex flex-col gap-5">
          <div className="rounded-2xl border border-violet-100 bg-white p-5 shadow-sm">
            <h2 className="mb-3.5 flex items-center gap-2 text-base font-semibold text-violet-800">
              ➕ Publier une annonce
            </h2>

            <Champ label="Titre">
              <input
                value={formulaire.titre}
                onChange={(e) => setFormulaire({ ...formulaire, titre: e.target.value })}
                placeholder="Ex : Appartement F3 lumineux"
                className="champ-input"
              />
            </Champ>

            <div className="mb-3 grid grid-cols-2 gap-2.5">
              <Champ label="Type">
                <select
                  value={formulaire.type}
                  onChange={(e) => setFormulaire({ ...formulaire, type: e.target.value })}
                  className="champ-input"
                >
                  {TYPES_BIEN.map((type) => (
                    <option key={type}>{type}</option>
                  ))}
                </select>
              </Champ>
              <Champ label="Ville">
                <input
                  value={formulaire.ville}
                  onChange={(e) => setFormulaire({ ...formulaire, ville: e.target.value })}
                  placeholder="Ville"
                  className="champ-input"
                />
              </Champ>
            </div>

            <Champ label="Quartier">
              <input
                value={formulaire.quartier}
                onChange={(e) => setFormulaire({ ...formulaire, quartier: e.target.value })}
                placeholder="Quartier"
                className="champ-input"
              />
            </Champ>

            <div className="mb-3 grid grid-cols-2 gap-2.5">
              <Champ label="Surface (m²)">
                <input
                  type="number"
                  value={formulaire.surface}
                  onChange={(e) => setFormulaire({ ...formulaire, surface: e.target.value })}
                  className="champ-input"
                />
              </Champ>
              <Champ label="Nb pièces">
                <input
                  type="number"
                  value={formulaire.nbPieces}
                  onChange={(e) => setFormulaire({ ...formulaire, nbPieces: e.target.value })}
                  className="champ-input"
                />
              </Champ>
            </div>

            <Champ label="Prix (Ar)">
              <input
                type="number"
                value={formulaire.prix}
                onChange={(e) => setFormulaire({ ...formulaire, prix: e.target.value })}
                className="champ-input"
              />
            </Champ>

            <button
              type="button"
              disabled={publicationEnCours}
              onClick={gererPublication}
              className="w-full rounded-lg bg-violet-600 py-2.5 text-sm font-semibold text-white hover:bg-violet-700 disabled:bg-slate-300"
            >
              {publicationEnCours ? "Publication..." : "Publier l'annonce"}
            </button>
          </div>

          <div className="rounded-2xl border border-violet-100 bg-white p-5 shadow-sm">
            <h2 className="mb-3.5 flex items-center gap-2 text-base font-semibold text-violet-800">
              📋 Mes annonces
            </h2>
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-xs text-slate-500">
                  <th className="pb-1.5">Titre</th>
                  <th className="pb-1.5">Ville</th>
                  <th className="pb-1.5">Prix</th>
                  <th className="pb-1.5">Dispo</th>
                </tr>
              </thead>
              <tbody>
                {biens.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="py-3 text-center text-xs text-slate-400">
                      Aucune annonce publiée.
                    </td>
                  </tr>
                ) : (
                  biens.map((bien) => (
                    <tr key={bien.id} className="border-t border-slate-100">
                      <td className="py-1.5">{bien.titre}</td>
                      <td className="py-1.5">{bien.ville}</td>
                      <td className="py-1.5">{formatPrix(bien.prix)}</td>
                      <td className="py-1.5">
                        <span
                          className={`rounded-full px-2 py-0.5 text-[11px] ${
                            bien.disponible ? "bg-emerald-50 text-emerald-700" : "bg-rose-50 text-rose-700"
                          }`}
                        >
                          {bien.disponible ? "oui" : "non"}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div>
          <h2 className="mb-3.5 flex items-center gap-2 text-base font-semibold text-violet-800">
            📨 Demandes de visite reçues
          </h2>
          {demandes.length === 0 ? (
            <div className="rounded-xl border border-dashed border-slate-300 bg-white p-5 text-center text-sm text-slate-500">
              Aucune demande pour le moment.
            </div>
          ) : (
            <div className="flex flex-col gap-3">
              {demandes.map((demande) => (
                <div
                  key={demande.id}
                  className="animate-apparition rounded-xl border-l-4 border-amber-400 bg-white p-4 shadow-sm data-[statut=VALIDEE]:border-emerald-500 data-[statut=REFUSEE]:border-rose-500 data-[statut=REFUSEE]:opacity-65"
                  data-statut={demande.statut}
                >
                  <div className="mb-2 flex items-start justify-between gap-2.5">
                    <div>
                      <p className="font-semibold text-slate-900">{demande.nomClient}</p>
                      <p className="text-xs text-slate-500">souhaite visiter : {demande.bienTitre}</p>
                    </div>
                    <BadgeStatut statut={demande.statut} />
                  </div>
                  <div className="mb-2.5 space-y-1 text-sm text-slate-700">
                    <p>
                      <span className="text-slate-500">📞 Téléphone :</span> {demande.telephone || "—"}
                    </p>
                    <p>
                      <span className="text-slate-500">📅 Date souhaitée :</span>{" "}
                      {demande.dateSouhaitee || "—"}
                    </p>
                    {demande.message && (
                      <p>
                        <span className="text-slate-500">💬 Message :</span> {demande.message}
                      </p>
                    )}
                  </div>
                  {demande.statut === "EN_ATTENTE" && (
                    <div className="flex gap-2">
                      <button
                        type="button"
                        onClick={() => gererReponse(demande.id, "valider")}
                        className="flex-1 rounded-lg bg-emerald-50 py-2 text-sm font-semibold text-emerald-700 hover:bg-emerald-100"
                      >
                        ✓ Valider
                      </button>
                      <button
                        type="button"
                        onClick={() => gererReponse(demande.id, "refuser")}
                        className="flex-1 rounded-lg bg-rose-50 py-2 text-sm font-semibold text-rose-700 hover:bg-rose-100"
                      >
                        ✗ Refuser
                      </button>
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </main>
    </div>
  );
}

/** Petit wrapper pour un champ de formulaire label + contenu, evite la repetition. */
function Champ({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="mb-3">
      <label className="mb-1.5 block text-xs font-semibold text-slate-500">{label}</label>
      {children}
    </div>
  );
}
