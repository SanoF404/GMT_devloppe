import { useState } from "react";
import { Link } from "react-router-dom";

import {
  MapPin,
  Phone,
  Mail,
  Clock,
  Send,
  MessageCircle,
  CheckCircle,
} from "lucide-react";

import site, { emailHref, hasContact, phoneHref, whatsappHref } from "../../../config/site";
import "./Contact.css";

function Contact() {
  const [formData, setFormData] = useState({
    nom: "",
    email: "",
    telephone: "",
    sujet: "",
    message: "",
  });

  const [errors, setErrors] = useState({});
  const [success, setSuccess] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    setErrors((prev) => ({
      ...prev,
      [name]: "",
    }));
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.nom.trim()) {
      newErrors.nom = "Le nom complet est obligatoire.";
    }

    if (!formData.email.trim()) {
      newErrors.email = "L'email est obligatoire.";
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "Veuillez saisir une adresse email valide.";
    }

    if (!formData.sujet) {
      newErrors.sujet = "Veuillez sélectionner un sujet.";
    }

    if (!formData.message.trim()) {
      newErrors.message = "Le message est obligatoire.";
    }

    setErrors(newErrors);

    return Object.keys(newErrors).length === 0;
  };

  const canSend = Boolean(site.email || site.whatsapp);

  const handleSubmit = (event) => {
    event.preventDefault();

    if (!validateForm() || !canSend) {
      return;
    }

    const lines = [
      `Nom : ${formData.nom.trim()}`,
      `Email : ${formData.email.trim()}`,
      formData.telephone.trim() ? `Téléphone : ${formData.telephone.trim()}` : "",
      "",
      formData.message.trim(),
    ].filter((line, index) => line || index === 3);
    const text = lines.join("\n");

    if (site.email) {
      const subject = `${formData.sujet} - ${site.name}`;
      window.location.href = `${emailHref}?subject=${encodeURIComponent(subject)}&body=${encodeURIComponent(text)}`;
    } else {
      window.open(`${whatsappHref}?text=${encodeURIComponent(`${formData.sujet}\n${text}`)}`, "_blank", "noopener");
    }
    setSuccess(true);
  };

  const resetForm = () => {
    setFormData({
      nom: "",
      email: "",
      telephone: "",
      sujet: "",
      message: "",
    });

    setErrors({});
    setSuccess(false);
  };

  return (
    <main className="contact-page">

      {/* =========================
          HERO
      ========================= */}

      <section className="contact-hero">

        <span className="contact-badge">
          CONTACT
        </span>

        <h1>
          Parlons de votre
          <br />
          voyage
        </h1>

        <p>
          Une question, une demande spécifique ou un devis personnalisé ?
          Notre équipe est à votre écoute.
        </p>

      </section>


      {/* =========================
          CONTENU
      ========================= */}

      <section className="contact-grid" style={hasContact ? undefined : { gridTemplateColumns: "minmax(0, 720px)", justifyContent: "center" }}>

        {/* =========================
            COLONNE GAUCHE
        ========================= */}

        {hasContact && <div className="contact-info">

          {site.address && (
            <ContactInfoCard icon={<MapPin size={27} />} label="ADRESSE" value={site.address} />
          )}

          {site.phone && (
            <ContactInfoCard
              icon={<Phone size={27} />}
              label="TÉLÉPHONE"
              value={<a href={phoneHref}>{site.phone}</a>}
            />
          )}

          {site.whatsapp && (
            <ContactInfoCard
              icon={<MessageCircle size={27} />}
              label="WHATSAPP"
              value={<a href={whatsappHref} target="_blank" rel="noreferrer">Écrire sur WhatsApp</a>}
            />
          )}

          {site.email && (
            <ContactInfoCard
              icon={<Mail size={27} />}
              label="EMAIL"
              value={<a href={emailHref}>{site.email}</a>}
            />
          )}

          {site.hours.length > 0 && (
            <ContactInfoCard
              icon={<Clock size={27} />}
              label="HORAIRES"
              value={site.hours.map(([days, time]) => `${days} : ${time}`).join(" · ")}
            />
          )}

          {(site.facebook || site.instagram) && (
            <div className="contact-socials">
              {site.facebook && <a href={site.facebook} target="_blank" rel="noreferrer">Facebook</a>}
              {site.instagram && <a href={site.instagram} target="_blank" rel="noreferrer">Instagram</a>}
            </div>
          )}

        </div>}


        {/* =========================
            FORMULAIRE
        ========================= */}

        <div className="contact-form-card">

          {!success ? (

            <form onSubmit={handleSubmit}>

              {/* NOM + EMAIL */}

              <div className="form-row">

                <FormInput
                  label="Nom complet"
                  name="nom"
                  placeholder="Votre nom"
                  value={formData.nom}
                  onChange={handleChange}
                  error={errors.nom}
                />

                <FormInput
                  label="Email"
                  name="email"
                  type="email"
                  placeholder="vous@email.com"
                  value={formData.email}
                  onChange={handleChange}
                  error={errors.email}
                />

              </div>


              {/* TELEPHONE */}

              <div className="form-group">

                <label htmlFor="telephone">
                  Téléphone <span>(optionnel)</span>
                </label>

                <input
                  id="telephone"
                  name="telephone"
                  type="tel"
                  placeholder="+216"
                  value={formData.telephone}
                  onChange={handleChange}
                />

              </div>


              {/* SUJET */}

              <div className="form-group">

                <label htmlFor="sujet">
                  Sujet
                </label>

                <select
                  id="sujet"
                  name="sujet"
                  value={formData.sujet}
                  onChange={handleChange}
                >

                  <option value="">
                    Sélectionnez un sujet
                  </option>

                  <option value="Demande de devis">
                    Demande de devis
                  </option>

                  <option value="Question sur un véhicule">
                    Question sur un véhicule
                  </option>

                  <option value="Location longue durée">
                    Location longue durée
                  </option>

                  <option value="Partenariat">
                    Partenariat
                  </option>

                  <option value="Autre">
                    Autre
                  </option>

                </select>

                {errors.sujet && (
                  <small>{errors.sujet}</small>
                )}

              </div>


              {/* MESSAGE */}

              <div className="form-group">

                <label htmlFor="message">
                  Message
                </label>

                <textarea
                  id="message"
                  name="message"
                  rows="6"
                  placeholder="Votre message..."
                  value={formData.message}
                  onChange={handleChange}
                />

                {errors.message && (
                  <small>{errors.message}</small>
                )}

              </div>


              {/* BUTTON */}

              <button type="submit" className="contact-submit" disabled={!canSend}>
                Envoyer le message
                <Send size={20} />
              </button>

              {!canSend && (
                <small>
                  Le formulaire n'est pas encore disponible. Passez par la page <Link to="/devis">Demander un devis</Link>.
                </small>
              )}

            </form>

          ) : (

            /* =========================
               SUCCESS
            ========================= */

            <div className="contact-success">

              <div className="success-icon">
                <CheckCircle size={42} />
              </div>

              <h2>
                Votre message est prêt
              </h2>

              <p>
                Votre application de messagerie s'est ouverte avec le message. Envoyez-le pour que nous le recevions.
              </p>

              <button
                className="reset-button"
                onClick={resetForm}
              >
                Écrire un autre message
              </button>

            </div>

          )}

        </div>

      </section>


    </main>
  );
}


/* =========================
   CONTACT INFO CARD
========================= */

function ContactInfoCard({
  icon,
  label,
  value,
  description,
}) {
  return (
    <div className="contact-info-card">

      <div className="contact-info-icon">
        {icon}
      </div>

      <div className="contact-info-content">

        <span className="contact-info-label">
          {label}
        </span>

        <div className="contact-info-value">
          {value}
        </div>

        <p>
          {description}
        </p>

      </div>

    </div>
  );
}


/* =========================
   INPUT
========================= */

function FormInput({
  label,
  name,
  type = "text",
  placeholder,
  value,
  onChange,
  error,
}) {
  return (
    <div className="form-group">

      <label htmlFor={name}>
        {label}
      </label>

      <input
        id={name}
        name={name}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
      />

      {error && (
        <small>{error}</small>
      )}

    </div>
  );
}

export default Contact;