import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import CarCard from "../CarCard/CarCard";
import useCurrency from "../../../context/useCurrency";
import { vehicleApi } from "../../../services/api";
import { groupByModel, titleCase } from "../../../utils/fleet";
import { resolveVehicleImage } from "../../../utils/vehicleImage";
import "./VehiclesSection.css";

function VehiclesSection() {
  const { currency, setCurrency } = useCurrency();
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    vehicleApi.getAll()
      .then(({ data }) => { if (active) setVehicles(Array.isArray(data) ? groupByModel(data).slice(0, 6) : []); })
      .catch(() => { if (active) setError("Impossible de charger les véhicules."); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  return (
    <section className="vehicles-section">
      <div className="vehicles-header">
        <div className="vehicles-title">
          <span className="section-label">NOTRE FLOTTE</span>
          <h2>Nos véhicules</h2>
          <p>Les modèles disponibles à la location, avec leur tarif par jour.</p>
        </div>
        <div className="vehicles-actions">
          <div className="currency-toggle">
            <button className={currency === "DT" ? "currency-active" : ""} onClick={() => setCurrency("DT")}>DT</button>
            <button className={currency === "EUR" ? "currency-active" : ""} onClick={() => setCurrency("EUR")}>€ EUR</button>
          </div>
          <NavLink to="/catalogue" className="see-all">Voir tous les véhicules <span>→</span></NavLink>
        </div>
      </div>

      {loading ? <p role="status">Chargement des véhicules…</p> : error ? <p role="alert">{error}</p> : vehicles.length ? (
        <div className="cars-grid">
          {vehicles.map((vehicle) => <CarCard
            key={vehicle.id}
            id={vehicle.id}
            image={resolveVehicleImage(vehicle.image)}
            brand={vehicle.marque}
            name={vehicle.modele}
            category={vehicle.categorie?.nom || "Véhicule"}
            seats={`${vehicle.places ?? "—"} places`}
            transmission={titleCase(vehicle.transmission)}
            fuel={titleCase(vehicle.carburant)}
            priceDT={vehicle.prixParJour}
            count={vehicle.count}
          />)}
        </div>
      ) : <p>Aucun véhicule disponible pour le moment.</p>}
    </section>
  );
}

export default VehiclesSection;
