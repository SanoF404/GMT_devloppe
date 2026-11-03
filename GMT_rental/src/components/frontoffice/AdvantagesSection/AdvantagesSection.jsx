import { CalendarCheck, FileText, Wallet, Coins } from "lucide-react";

import "./AdvantagesSection.css";

const advantages = [
  {
    icon: Wallet,
    title: "Tarifs affichés",
    text: "Le prix par jour de chaque véhicule est indiqué dans le catalogue, avant toute demande.",
  },
  {
    icon: FileText,
    title: "Devis en ligne",
    text: "Une demande en trois étapes, sans paiement en ligne.",
  },
  {
    icon: CalendarCheck,
    title: "Disponibilité contrôlée",
    text: "Les dates déjà réservées sont vérifiées avant l’envoi de votre demande.",
  },
  {
    icon: Coins,
    title: "Dinars ou euros",
    text: "Consultez les tarifs dans la devise de votre choix.",
  },
];

function AdvantagesSection() {
  return (
    <section className="advantages-section">

      <div className="advantages-glow" />

      <div className="advantages-container">

        <div className="advantages-header">

          <span className="advantages-label">
            COMMENT ÇA MARCHE
          </span>

          <h2>
            Une location
            <br />
            simple à demander
          </h2>

        </div>

        <div className="advantages-grid">

          {advantages.map((advantage, index) => (
            <AdvantageCard
              key={advantage.title}
              {...advantage}
              delay={index}
            />
          ))}

        </div>

      </div>

    </section>
  );
}

function AdvantageCard({ icon: Icon, title, text, delay }) {
  return (
    <article
      className="advantage-card"
      style={{ "--delay": `${delay * 0.08}s` }}
    >

      <div className="advantage-icon">
        <Icon size={23} />
      </div>

      <h3>{title}</h3>

      <p>{text}</p>

    </article>
  );
}

export default AdvantagesSection;
