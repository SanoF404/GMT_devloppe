import { NavLink } from "react-router-dom";

function AboutCTA() {
  return (
    <section className="about-cta">

      <div className="about-cta-content">

        <span className="about-label">
          GMT LOCATION
        </span>

        <h2>
          Une question avant
          <br />
          de réserver ?
        </h2>

        <p>
          Consultez nos véhicules ou contactez-nous.
        </p>

        <div className="about-cta-buttons">

          <NavLink
            to="/catalogue"
            className="about-primary-button"
          >
            Voir nos véhicules
            <span>→</span>
          </NavLink>

          <NavLink
            to="/contact"
            className="about-secondary-button"
          >
            Nous contacter
          </NavLink>

        </div>

      </div>

    </section>
  );
}

export default AboutCTA;
