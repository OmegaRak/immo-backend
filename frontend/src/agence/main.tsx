import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "../index.css";
import { FournisseurToasts } from "../components/Toasts";
import { PageAgence } from "../pages/PageAgence";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <FournisseurToasts>
      <PageAgence />
    </FournisseurToasts>
  </StrictMode>,
);
