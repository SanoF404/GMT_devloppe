import { NavLink } from "react-router-dom";

function AboutHero() {
  return (
    <section className="about-hero">

      <div className="about-container">

        <div className="about-hero-content">

          <span className="about-label">
            À PROPOS DE GMT
          </span>

          <h1>
            Location de voitures
            <br />
            <span>en Tunisie</span>
          </h1>

          <p>
            GMT loue des citadines, des berlines et des SUV compacts à la journée.
            Le catalogue indique le prix par jour de chaque véhicule : vous choisissez
            un modèle, vous indiquez vos dates, et nous confirmons votre demande.
          </p>

          <NavLink
            to="/devis"
            className="about-primary-button"
          >
            Demander un devis
            <span>→</span>
          </NavLink>

        </div>

      </div>

    </section>
  );
}

export default AboutHero;
