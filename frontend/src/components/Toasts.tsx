import { createContext, useCallback, useContext, useRef, useState } from "react";
import type { ReactNode } from "react";

interface Toast {
  id: number;
  titre: string;
  message: string;
  variante: "info" | "succes" | "erreur";
}

interface ContexteToasts {
  ajouter: (titre: string, message: string, variante?: Toast["variante"]) => void;
}

const ContexteToastsCtx = createContext<ContexteToasts | null>(null);

const COULEURS: Record<Toast["variante"], string> = {
  info: "border-l-4 border-sky-500",
  succes: "border-l-4 border-emerald-500",
  erreur: "border-l-4 border-rose-500",
};

/** Fournit la fonction `ajouter(...)` a tous les composants enfants et affiche les toasts en haut a droite. */
export function FournisseurToasts({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const compteur = useRef(0);

  const ajouter = useCallback(
    (titre: string, message: string, variante: Toast["variante"] = "info") => {
      const id = ++compteur.current;
      setToasts((precedents) => [...precedents, { id, titre, message, variante }]);
      setTimeout(() => {
        setToasts((precedents) => precedents.filter((t) => t.id !== id));
      }, 6000);
    },
    [],
  );

  return (
    <ContexteToastsCtx.Provider value={{ ajouter }}>
      {children}
      <div className="fixed top-5 right-5 z-50 flex w-full max-w-sm flex-col gap-2.5">
        {toasts.map((toast) => (
          <div
            key={toast.id}
            className={`animate-glisse rounded-lg bg-white p-4 shadow-lg ${COULEURS[toast.variante]}`}
          >
            <p className="text-sm font-semibold text-slate-800">{toast.titre}</p>
            <p className="text-xs text-slate-500">{toast.message}</p>
          </div>
        ))}
      </div>
    </ContexteToastsCtx.Provider>
  );
}

export function useToasts(): ContexteToasts {
  const ctx = useContext(ContexteToastsCtx);
  if (!ctx) {
    throw new Error("useToasts doit etre utilise a l'interieur de <FournisseurToasts>");
  }
  return ctx;
}
