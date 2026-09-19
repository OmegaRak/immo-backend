import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "../index.css";
import { FournisseurToasts } from "../components/Toasts";
import { PageClient } from "../pages/PageClient";

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <FournisseurToasts>
      <PageClient />
    </FournisseurToasts>
  </StrictMode>,
);
