import { Link } from "react-router-dom";
import { Car, MapPin, Phone, Mail } from "lucide-react";

import site, { emailHref, phoneHref } from "../../../config/site";
import "./Footer.css";

function Footer() {
  const hasContact = Boolean(site.address || site.phone || site.email);
  const hasSocials = Boolean(site.facebook || site.instagram);

  return (
    <footer className="gmt-footer">
      <div className="footer-container">
        <div className="footer-brand">
          <Link to="/" className="footer-logo">
            <span className="footer-logo-icon"><Car size={22} /></span>
            <span>GMT</span>
          </Link>
          <p>Location de voitures en Tunisie. Choisissez un modèle et demandez votre devis en ligne.</p>
          {hasSocials && (
            <div className="footer-socials">
              {site.facebook && <a href={site.facebook} target="_blank" rel="noreferrer">Facebook</a>}
              {site.instagram && <a href={site.instagram} target="_blank" rel="noreferrer">Instagram</a>}
            </div>
          )}
        </div>

        <div className="footer-column">
          <h3>NAVIGATION</h3>
          <Link to="/">Accueil</Link>
          <Link to="/catalogue">Catalogue véhicules</Link>
          <Link to="/about">À propos</Link>
          <Link to="/faq">FAQ</Link>
          <Link to="/devis">Demander un devis</Link>
          <Link to="/contact">Contact</Link>
        </div>

        {hasContact && (
          <div className="footer-column">
            <h3>CONTACT</h3>
            {site.address && (
              <div className="footer-contact-item"><MapPin size={19} /><span>{site.address}</span></div>
            )}
            {site.phone && (
              <a href={phoneHref} className="footer-contact-item"><Phone size={19} /><span>{site.phone}</span></a>
            )}
            {site.email && (
              <a href={emailHref} className="footer-contact-item"><Mail size={19} /><span>{site.email}</span></a>
            )}
          </div>
        )}

        {site.hours.length > 0 && (
          <div className="footer-column">
            <h3>HORAIRES</h3>
            {site.hours.map(([days, time]) => (
              <div className="footer-hours-row" key={days}><span>{days}</span><span>{time}</span></div>
            ))}
          </div>
        )}
      </div>

      <div className="footer-bottom">
        <p>© {new Date().getFullYear()} {site.name}. Tous droits réservés.</p>
      </div>
    </footer>
  );
}

export default Footer;
