import { Link } from "react-router-dom";
import "./CTASection.css";

function CTASection() {
  return (
    <section className="cta-section">

      <div className="cta-overlay" />

      <div className="cta-content">

        <span className="cta-badge">
          Devis gratuit, sans paiement en ligne
        </span>

        <h2>
          Prêt à prendre
          <br />
          la route ?
        </h2>

        <p>
          Choisissez votre véhicule, indiquez vos dates et envoyez
          votre demande : nous revenons vers vous pour la confirmer.
        </p>

        <Link className="cta-primary" to="/devis">Demander un devis</Link>

      </div>

    </section>
  );
}

export default CTASection;
