import { useEffect, useState } from "react";
import { NavLink, useParams } from "react-router-dom";
import useCurrency from "../../../context/useCurrency";
import { CalendarDays, CarFront, Fuel, Settings2, Users } from "lucide-react";
import { vehicleApi } from "../../../services/api";
import { resolveVehicleImage } from "../../../utils/vehicleImage";
import { titleCase } from "../../../utils/fleet";
import "./VehicleDetail.css";

function VehicleDetail() {
  const { id } = useParams();
  const { currency, getPrice } = useCurrency();
  const [result, setResult] = useState({ id: null, vehicle: null, error: "" });
  const loading = result.id !== id;
  const vehicle = loading ? null : result.vehicle;
  const error = loading ? "" : result.error;

  useEffect(() => {
    let active = true;
    vehicleApi.getById(id)
      .then(({ data }) => { if (active) setResult({ id, vehicle: data, error: "" }); })
      .catch((requestError) => {
        if (!active) return;
        const message = requestError.response?.status === 404 ? "Véhicule introuvable" : "Impossible de charger ce véhicule.";
        setResult({ id, vehicle: null, error: message });
      });
    return () => { active = false; };
  }, [id]);

  if (loading) return <main className="vehicle-not-found"><h1>Chargement du véhicule…</h1></main>;
  if (error || !vehicle) return <main className="vehicle-not-found"><h1>{error || "Véhicule introuvable"}</h1><NavLink to="/catalogue">Retour au catalogue</NavLink></main>;

  const category = vehicle.categorie?.nom || "Véhicule";
  const image = resolveVehicleImage(vehicle.image);
  const price = getPrice(vehicle.prixParJour);

  return (
    <main className="vehicle-detail-page">
      <div className="vehicle-detail-container">
        <NavLink to="/catalogue" className="back-catalogue">← Retour au catalogue</NavLink>
        <section className="vehicle-detail">
          <div className="vehicle-image-wrapper">
            <span className="vehicle-category">{category}</span>
            {image
              ? <img src={image} alt={`${vehicle.marque} ${vehicle.modele}`} />
              : <div className="vehicle-image-missing" role="img" aria-label={`Photo indisponible pour ${vehicle.marque} ${vehicle.modele}`}><CarFront size={56} aria-hidden="true" /><span>Photo indisponible</span></div>}
          </div>
          <div className="vehicle-info">
            <span className="vehicle-brand">{vehicle.marque}</span>
            <h1>{vehicle.modele}</h1>
            <div className="price-box">
              <span className="price-title">TARIF À PARTIR DE</span>
              <div className="price"><strong>{price == null ? "—" : Number(price).toLocaleString("fr-TN", { maximumFractionDigits: 2 })}</strong><span className="currency">{currency === "DT" ? "DT" : "€"}</span><span className="per-day">/jour</span></div>
              <NavLink to={`/devis?vehicule=${vehicle.id}`} className="detail-quote-button">Demander un devis</NavLink>
            </div>
            <div className="technical-box">
              <h2>FICHE TECHNIQUE</h2>
              <div className="technical-row"><span><span className="tech-icon"><CarFront size={16} aria-hidden="true" /></span>Catégorie</span><strong>{category}</strong></div>
              {vehicle.annee && <div className="technical-row"><span><span className="tech-icon"><CalendarDays size={16} aria-hidden="true" /></span>Année</span><strong>{vehicle.annee}</strong></div>}
              <div className="technical-row"><span><span className="tech-icon"><Users size={16} aria-hidden="true" /></span>Places</span><strong>{vehicle.places ?? "—"}</strong></div>
              <div className="technical-row"><span><span className="tech-icon"><Settings2 size={16} aria-hidden="true" /></span>Transmission</span><strong>{titleCase(vehicle.transmission)}</strong></div>
              <div className="technical-row"><span><span className="tech-icon"><Fuel size={16} aria-hidden="true" /></span>Carburant</span><strong>{titleCase(vehicle.carburant)}</strong></div>
            </div>
            <div className="description-box"><h2>DESCRIPTION</h2><p>{vehicle.description || "Contactez notre équipe pour plus d’informations sur ce véhicule."}</p></div>
          </div>
        </section>
      </div>
    </main>
  );
}

export default VehicleDetail;
