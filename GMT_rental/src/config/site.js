// Informations publiques de l'agence.
//
// Renseignez ici uniquement des informations réelles : chaque champ laissé vide est
// simplement masqué sur le site (pied de page, page Contact, FAQ). Rien n'est inventé.
const site = {
  name: "GMT Rent a Car",

  // Coordonnées affichées dans le pied de page et sur la page Contact.
  address: "",          // ex. "12 rue de la République, Tunis"
  phone: "",            // ex. "+216 71 234 567"
  whatsapp: "",         // numéro international sans espaces, ex. "21620123456"
  email: "",            // ex. "contact@mon-domaine.tn"
  hours: [],            // ex. [["Lun — Ven", "08h — 19h"], ["Samedi", "09h — 14h"]]
  facebook: "",         // URL complète de la page, ex. "https://www.facebook.com/gmtrentacar"
  instagram: "",        // URL complète du profil

  // Conditions de location affichées dans la FAQ. Une réponse vide masque la question.
  conditions: {
    documents: "",      // ex. "Permis de conduire valide et pièce d'identité ou passeport."
    ageMinimum: "",
    assurance: "",
    caution: "",
    kilometrage: "",
    paiement: "",
    livraison: "",
    panneAccident: "",
  },
};

export const phoneHref = site.phone ? `tel:${site.phone.replace(/[^+\d]/g, "")}` : "";
export const whatsappHref = site.whatsapp ? `https://wa.me/${site.whatsapp.replace(/\D/g, "")}` : "";
export const emailHref = site.email ? `mailto:${site.email}` : "";
export const hasContact = Boolean(site.address || site.phone || site.whatsapp || site.email);

export default site;
