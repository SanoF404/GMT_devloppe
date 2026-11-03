import "./About.css";

import AboutHero from "../../../components/frontoffice/About/AboutHero";
import AboutValues from "../../../components/frontoffice/About/AboutValues";
import AboutCTA from "../../../components/frontoffice/About/AboutCTA";

function About() {
  return (
    <main className="about-page">

      <AboutHero />

      <AboutValues />

      <AboutCTA />

    </main>
  );
}

export default About;
