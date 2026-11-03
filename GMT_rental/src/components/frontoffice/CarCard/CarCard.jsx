import "./CarCard.css";
import { NavLink } from "react-router-dom";
import { Fuel, Settings2, Users } from "lucide-react";
import useCurrency from "../../../context/useCurrency";

function CarCard({
  image,
  brand,
  name,
  category,
  seats,
  transmission,
  fuel,
  priceDT,
  count,
  id
}) {
  const { currency, getPrice } = useCurrency();

  const price = getPrice(priceDT);

  return (
    <article className="car-card">

      <div className="car-image">
        {image ? <img
          src={image}
          alt={`${brand} ${name}`}
        /> : <div className="car-image-placeholder" role="img" aria-label={`Photo indisponible pour ${brand} ${name}`}>Photo à venir</div>}

        <span className="car-category">
          {category}
        </span>

        {count > 1 && (
          <span className="car-status">
            {count} véhicules
          </span>
        )}
      </div>

      <div className="car-content">

        <span className="car-brand">
          {brand}
        </span>

        <h3>{name}</h3>

        <div className="car-specs">

          <span>
            <span className="spec-icon"><Users size={15} aria-hidden="true" /></span>
            {seats}
          </span>

          <span>
            <span className="spec-icon"><Settings2 size={15} aria-hidden="true" /></span>
            {transmission}
          </span>

          <span>
            <span className="spec-icon"><Fuel size={15} aria-hidden="true" /></span>
            {fuel}
          </span>

        </div>

        <div className="car-separator"></div>

        <p className="price-label">
          À partir de
        </p>

        <div className="car-bottom">

          <div className="car-price">

            <strong>
              {price == null ? "—" : Number(price).toLocaleString("fr-TN", { maximumFractionDigits: 2 })}
            </strong>

            <span>
              {currency === "DT" ? "DT / jour" : "€ / jour"}
            </span>

          </div>

          <div className="car-buttons">

            <NavLink
              to={`/vehicule/${id}`}
              className="details-button"
            >
              Voir le véhicule
            </NavLink>

            <NavLink to={`/devis?vehicule=${id}`} className="quote-button">
              Devis
            </NavLink>

          </div>

        </div>

      </div>

    </article>
  );
}

export default CarCard;
