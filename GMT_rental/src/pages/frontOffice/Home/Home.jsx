import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import useCurrency from "../../../context/useCurrency";
import { vehicleApi } from "../../../services/api";
import { groupByModel } from "../../../utils/fleet";
import VehicleSelector from "../../../components/frontoffice/VehicleSelector/VehicleSelector";
import VehiclesSection from "../../../components/frontoffice/VehiclesSection/VehiclesSection";
import AdvantagesSection from "../../../components/frontoffice/AdvantagesSection/AdvantagesSection";
import CTASection from "../../../components/frontoffice/CTASection/CTASection";
import "./Home.css";

function Home() {
  const { currency, getPrice } = useCurrency();
  const [vehicles, setVehicles] = useState([]);

  useEffect(() => {
    let active = true;
    vehicleApi.getAll()
      .then(({ data }) => { if (active) setVehicles(Array.isArray(data) ? data : []); })
      .catch(() => { /* Le hero reste utilisable sans les tarifs. */ });
    return () => { active = false; };
  }, []);

  const models = useMemo(
    () => groupByModel(vehicles).sort((a, b) => Number(a.prixParJour) - Number(b.prixParJour)),
    [vehicles],
  );
  const unit = currency === "DT" ? "DT" : "€";
  const format = (priceDT) => {
    const price = getPrice(priceDT);
    return price == null ? "—" : Number(price).toLocaleString("fr-TN", { maximumFractionDigits: 2 });
  };

  return (
    <main>
      <section className="hero hero--lux" aria-labelledby="hero-title">
        <div className="hero-ambience" aria-hidden="true">
          <span className="hero-glow hero-glow--gold" />
          <span className="hero-glow hero-glow--shadow" />
        </div>

        <div className="hero-inner">
          <div className="hero-copy">
            <p className="hero-badge">
              <span aria-hidden="true" />
              {vehicles.length ? `${vehicles.length} véhicules en flotte` : "Location de voitures"}
            </p>
            <h1 id="hero-title">Louez une voiture <span>en Tunisie.</span></h1>
            <p className="hero-description">
              {models.length
                ? `Citadines, berlines et SUV compacts, à partir de ${format(models[0].prixParJour)} ${unit} par jour. `
                : "Citadines, berlines et SUV compacts. "}
              Choisissez un modèle, envoyez votre demande de devis en ligne, sans paiement à l’avance.
            </p>
            <div className="hero-actions">
              <Link className="hero-button-primary" to="/devis">Demander un devis <span aria-hidden="true">↗</span></Link>
              <Link className="hero-button-secondary" to="/catalogue">Voir les véhicules</Link>
            </div>
          </div>

          {models.length > 0 && (
            <aside className="fleet-panel" aria-label="Tarifs journaliers par modèle">
              <h2>Tarifs par jour</h2>
              <ul>
                {models.map((model) => (
                  <li key={`${model.marque}-${model.modele}`}>
                    <Link to={`/vehicule/${model.id}`}>
                      <span className="fleet-panel-name">{model.marque} {model.modele}</span>
                      <span className="fleet-panel-meta">{model.categorie?.nom || "Véhicule"} · {model.places ?? "—"} places</span>
                      <span className="fleet-panel-price">{format(model.prixParJour)} <small>{unit}</small></span>
                    </Link>
                  </li>
                ))}
              </ul>
            </aside>
          )}
        </div>
      </section>

      <VehicleSelector />
      <VehiclesSection />
      <AdvantagesSection />
      <CTASection />
    </main>
  );
}

export default Home;
