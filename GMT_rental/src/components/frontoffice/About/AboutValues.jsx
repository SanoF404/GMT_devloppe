function AboutValues() {
  return (
    <section className="about-values">

      <div className="about-container">

        <div className="about-section-heading">

          <span className="about-label">
            NOTRE FAÇON DE TRAVAILLER
          </span>

          <h2>
            Simple et clair
          </h2>

        </div>

        <div className="values-grid">

          <article className="value-card">
            <div className="value-icon">1</div>
            <h3>Choisissez un modèle</h3>
            <p>Parcourez le catalogue : chaque fiche indique le tarif par jour, le nombre de places et la boîte de vitesses.</p>
          </article>

          <article className="value-card">
            <div className="value-icon">2</div>
            <h3>Envoyez votre demande</h3>
            <p>Indiquez vos dates et vos lieux de prise en charge et de restitution. Aucun paiement en ligne.</p>
          </article>

          <article className="value-card">
            <div className="value-icon">3</div>
            <h3>Nous vous recontactons</h3>
            <p>Votre demande est enregistrée et notre équipe prend contact avec vous pour la confirmer.</p>
          </article>

        </div>

      </div>

    </section>
  );
}

export default AboutValues;
