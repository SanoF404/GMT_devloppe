import { useMemo, useState } from "react";
import {
  HelpCircle,
  ChevronDown,
  Search,
  MessageCircle,
  FileText,
} from "lucide-react";

import { Link } from "react-router-dom";
import site from "../../../config/site";
import "./FAQ.css";

const { conditions } = site;

// Les réponses sur les conditions de location viennent de config/site.js :
// une question dont la réponse est vide n'est pas affichée.
const faqData = [
  {
    categorie: "RÉSERVATION & DEVIS",
    question: "Comment demander un devis ?",
    reponse:
      "Rendez-vous sur la page « Demander un devis », remplissez le formulaire en 3 étapes (dates, véhicule, contact) et envoyez votre demande. Notre équipe vous recontacte avec une proposition.",
  },
  {
    categorie: "RÉSERVATION & DEVIS",
    question: "Y a-t-il un paiement en ligne ?",
    reponse: "Non, la demande de devis ne comporte aucun paiement en ligne.",
  },
  {
    categorie: "RÉSERVATION & DEVIS",
    question: "Les prix sont-ils en DT et en € ?",
    reponse:
      "Oui. Le catalogue permet de consulter les tarifs en dinars tunisiens et en euros grâce au sélecteur de devise. Les tarifs en euros sont indicatifs.",
  },
  {
    categorie: "RÉSERVATION & DEVIS",
    question: "Comment régler la location ?",
    reponse: conditions.paiement,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "Quels documents sont nécessaires pour louer un véhicule ?",
    reponse: conditions.documents,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "Quel est l'âge minimum ?",
    reponse: conditions.ageMinimum,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "L'assurance est-elle incluse ?",
    reponse: conditions.assurance,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "Faut-il verser une caution ?",
    reponse: conditions.caution,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "Y a-t-il une limitation de kilométrage ?",
    reponse: conditions.kilometrage,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "Puis-je me faire livrer ou récupérer le véhicule ailleurs qu'à l'agence ?",
    reponse: conditions.livraison,
  },
  {
    categorie: "LOCATION & CONDITIONS",
    question: "Que faire en cas de panne ou d'accident ?",
    reponse: conditions.panneAccident,
  },
]
  .filter((faq) => faq.reponse)
  .map((faq, index) => ({ ...faq, id: index + 1 }));

function FAQ() {
  const [openId, setOpenId] = useState(1);
  const [search, setSearch] = useState("");

  const filteredFaqs = useMemo(() => {
    const value = search.toLowerCase().trim();

    if (!value) {
      return faqData;
    }

    return faqData.filter(
      (faq) =>
        faq.question.toLowerCase().includes(value) ||
        faq.reponse.toLowerCase().includes(value)
    );
  }, [search]);

  const reservationFaqs = filteredFaqs.filter(
    (faq) => faq.categorie === "RÉSERVATION & DEVIS"
  );

  const locationFaqs = filteredFaqs.filter(
    (faq) => faq.categorie === "LOCATION & CONDITIONS"
  );

  const toggleFaq = (id) => {
    setOpenId((currentId) => (currentId === id ? null : id));
  };

  return (
    <main className="faq-page">

      {/* HERO */}
      <section className="faq-hero">

        <div className="faq-badge">
          <HelpCircle size={18} />
          <span>Centre d'aide</span>
        </div>

        <h1>
          Questions
          <br />
          fréquentes
        </h1>

        <p>
          Tout ce que vous devez savoir avant de louer un véhicule chez GMT.
        </p>

      </section>

      {/* FAQ */}
      <section className="faq-section">

        {/* SEARCH */}
        <div className="faq-search-wrapper">

          <Search size={20} />

          <input
            type="text"
            placeholder="Rechercher une question..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />

          {search && (
            <button
              type="button"
              className="faq-clear"
              onClick={() => setSearch("")}
            >
              ×
            </button>
          )}

        </div>

        <div className="faq-result-count">
          {filteredFaqs.length}{" "}
          {filteredFaqs.length > 1 ? "questions" : "question"}
        </div>

        {/* EMPTY STATE */}
        {filteredFaqs.length === 0 && (
          <div className="faq-empty">

            <div className="faq-empty-icon">
              <Search size={26} />
            </div>

            <h2>
              Aucun résultat pour « {search} »
            </h2>

            <p>
              Nous n'avons pas trouvé de réponse correspondant à votre
              recherche.
            </p>

            <Link to="/contact" className="faq-empty-link">
              Contactez-nous
            </Link>

          </div>
        )}

        {/* RESERVATION */}
        {reservationFaqs.length > 0 && (
          <FAQCategory
            title="RÉSERVATION & DEVIS"
            faqs={reservationFaqs}
            openId={openId}
            onToggle={toggleFaq}
          />
        )}

        {/* LOCATION */}
        {locationFaqs.length > 0 && (
          <FAQCategory
            title="LOCATION & CONDITIONS"
            faqs={locationFaqs}
            openId={openId}
            onToggle={toggleFaq}
          />
        )}

      </section>

      {/* CTA */}
      <section className="faq-cta">

        <div className="faq-cta-icon">
          <MessageCircle size={28} />
        </div>

        <h2>
          Vous ne trouvez pas votre réponse ?
        </h2>

        <p>
          Notre équipe est disponible pour répondre à toutes vos questions.
        </p>

        <div className="faq-cta-buttons">

          <Link to="/contact" className="faq-btn-primary">
            Nous contacter
          </Link>

          <Link to="/devis" className="faq-btn-ghost">
            <FileText size={18} />
            Demander un devis
          </Link>

        </div>

      </section>

    </main>
  );
}

function FAQCategory({
  title,
  faqs,
  openId,
  onToggle,
}) {
  return (
    <div className="faq-category">

      <h2 className="faq-category-title">
        {title}
      </h2>

      <div className="faq-list">

        {faqs.map((faq) => {
          const isOpen = openId === faq.id;

          return (
            <div
              className={`faq-item ${isOpen ? "faq-item-open" : ""}`}
              key={faq.id}
            >

              <button
                type="button"
                className="faq-question"
                aria-expanded={isOpen}
                onClick={() => onToggle(faq.id)}
              >

                <span>
                  {faq.question}
                </span>

                <span className="faq-chevron">
                  <ChevronDown size={20} />
                </span>

              </button>

              <div
                className={`faq-answer-wrapper ${
                  isOpen ? "faq-answer-open" : ""
                }`}
              >
                <div className="faq-answer">
                  <p>{faq.reponse}</p>
                </div>
              </div>

            </div>
          );
        })}

      </div>

    </div>
  );
}

export default FAQ;