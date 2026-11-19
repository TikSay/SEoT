import "./Footer.css";

function Footer({ setCurrentPage }) {

  const naviger = (side) => {
    setCurrentPage(side);
    window.scrollTo(0, 0);
  };

   return (
    <footer className="site-footer">

      <nav className="footer-nav">

        <section>
          <h3>Hjelp og kontakt</h3>

          <button onClick={() => naviger('faq')}>
            Ofte stilte spørsmål
          </button>

          <button onClick={() => naviger('kontakt')}>
            Kontakt oss
          </button>
        </section>

        <section>
          <h3>Kurs og aktiviteter</h3>

          <button onClick={() => naviger('kurskalender')}>
            Kurskalender
          </button>

          <button onClick={() => naviger('home')}>
            Kommende aktiviteter
          </button>
        </section>

        <section>
          <h3>Om oss</h3>

          <button onClick={() => naviger('home')}>
            Østfold Husflidslag
          </button>

          <button onClick={() => naviger('registrer')}>
            Bli medlem
          </button>
        </section>

        <section>
          <h3>Informasjon</h3>

          <button onClick={() => naviger('home')}>
            Siste nytt
          </button>
        </section>

      </nav>

      <div className="footer-bottom">
        <p>© {new Date().getFullYear()} Østfold Husflidslag</p>
      </div>

    </footer>
  );
}

export default Footer;