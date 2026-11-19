import './App.css';
import { useState, useEffect } from 'react';
import Login from './sider/Login.jsx';
import Registrer from './sider/Registrer.jsx';
import Profil from './sider/Profil.jsx';
import GlemtPassord from './sider/GlemtPassord.jsx';
import husflidLogo from './assets/husflidlog.jpg';
import Kurskalender from "./sider/Kurskalender.jsx";
import Paamelding from "./sider/Påmeldning.jsx";
import KontaktOss from "./Komponenter/Kontakt.jsx";
import SisteNytt from "./Komponenter/SisteNytt.jsx";

// Kursbilder fra public/images (Sander, ba46566), koblet på kursnavn.
// Forsiden viser bare kursene som har et bilde her, slik det opprinnelige designet med fire kurs.
const kursBilder = {
  'Sytreff i Råde': '/images/sytreff.png',
  'Meksikansk fletting': '/images/fletting.png',
  'Lær å brodere, plattsøm og tellesøm': '/images/brodering.png',
  'Bunadkurs': '/images/bunad.png',
};

function App() {
  const [currentPage, setCurrentPage] = useState('home');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
  const [sjekkerSesjon, setSjekkerSesjon] = useState(true);
  // S-012 (Hiba): kurs fra GET /api/courses
  const [courses, setCourses] = useState([]);
  const [valgtKursId, setValgtKursId] = useState(null);
  const [lasterKurs, setLasterKurs] = useState(true);
  const [kursFeil, setKursFeil] = useState(false);
  // Kurs med bilde, til forsiden. Hele listen (courses) brukes fortsatt av påmeldingen.
  const forsideKurs = courses.filter(kurs => kursBilder[kurs.tittel]);

  // S-013 (Hiba): påmelding fra de dynamiske kurskortene bruker kurs-ID
  function apnePamelding(kursId) {
    setValgtKursId(kursId);
    setCurrentPage('pamelding');
  }

  // S-012 (Hiba): henter kommende kurs. Feil fra backend vises som feilmelding, ikke som tom liste.
  useEffect(() => {
    fetch('/api/courses')
        .then(res => res.ok ? res.json() : Promise.reject(new Error('HTTP ' + res.status)))
        .then(data => {
          if (data.success) {
            setCourses(data.courses);
          } else {
            setKursFeil(true);
          }
        })
        .catch(() => setKursFeil(true))
        .finally(() => setLasterKurs(false));
  }, []);

  // Spør backend om brukeren fortsatt er innlogget, så innloggingen huskes etter refresh
  useEffect(() => {
    fetch('/api/me')
        .then(res => res.ok ? res.json() : null)
        .then(result => {
          if (result?.success) {
            setUser(result.user);
            setIsLoggedIn(true);
          }
        })
        .catch(() => {})
        .finally(() => setSjekkerSesjon(false));
  }, []);

  return (
      <div className="container">
        <header>
          <div className="logo-section" onClick={()=> setCurrentPage('home')}>
            <div className="logo-icon">
              <img
                  src={husflidLogo}
                  alt="Østfolds Husflidslag Logo"
                  style={{ width: '60px', height: 'auto' }}
              />
            </div>
            <div className="logo-text">Østfolds Husflidslag</div>
          </div>

          <nav className="main-nav">
            <a href="#" onClick={(e) => { e.preventDefault(); setCurrentPage('home'); }}>AKTUELT</a>
            <a href="#">FAGSIDER</a>
            <a href="#" onClick={(e)=> {e.preventDefault(); setCurrentPage('kontakt')}}>KONTAKT OSS</a>
          </nav>

          <nav className="icon-nav">
            <div className="icon-item">
              <span><i className="fa-solid fa-magnifying-glass"></i></span>SØK
            </div>
            <div className="icon-item" onClick={() => setCurrentPage('kurskalender')}>
              <span><i className="fa-solid fa-calendar"></i></span>KALENDER
            </div>
            <div className="icon-item">
              <span><i className="fa-solid fa-cart-shopping"></i></span>NETTBUTIKK
            </div>
            <div className="icon-item" onClick={() => !sjekkerSesjon && setCurrentPage(isLoggedIn ? 'profil' : 'login')}>
              <span><i className="fa-solid fa-user"></i></span>
              {sjekkerSesjon ? '...' : isLoggedIn ? 'MIN SIDE' : 'LOGG INN'}
            </div>
          </nav>
        </header>

        {currentPage === 'home' && (
            <main>
              <section className="left-column">
                <h2>Siste Nytt</h2>
                <SisteNytt setCurrentPage={setCurrentPage}/>
                <div className="news-text">
              <p>Østfold Husflidslag ble stiftet 28.09.1932. Vår første bunad, Østfold kvinnebunad, ble lansert i 1936.
                Deretter kom Østfold mannsbunad i 1990. Og Herregårdsbunad for kvinner ble lansert i 2015.</p>
              <p>For alle som liker å skape med hendene, fordype seg i tradisjonshåndverk eller lære noe nytt i
                fellesskap med andre så kan du bli medlem i et av våre lokallag!</p>
            </div>
            <button className="main-btn" onClick={() => setCurrentPage('registrer')}>BLI MEDLEM</button>
          </section>

          <section className="right-column">
            <h2>Kommende Kurs- og Aktiviteter</h2>

            <div className="cards-grid">
              {lasterKurs && <p>Henter kurs ...</p>}
              {kursFeil && <p role="alert">Kursene kan ikke hentes akkurat nå. Prøv igjen senere.</p>}
              {!lasterKurs && !kursFeil && forsideKurs.length === 0 && <p>Ingen kommende kurs akkurat nå.</p>}

              {forsideKurs.map(kurs => (
                  <div className="card" key={kurs.id}>
                      <div className="card-img" style={{ backgroundImage: `url('${kursBilder[kurs.tittel]}')` }}></div>
                      <div className="card-content">
                          <h3>{kurs.tittel}</h3>
                          <p>{kurs.beskrivelse}</p>
                          <ul className="card-meta">
                              <li>Dato: {new Date(kurs.dato).toLocaleDateString('nb-NO')}</li>
                              <li>Tid: {kurs.starttid.slice(0, 5)}</li>
                              <li>Maks {kurs.kapasitet} deltakere</li>
                              <li>Pris: {kurs.pris} kr</li>
                              <li>Nivå: {kurs.nivaa}</li>
                          </ul>
                          <div className="card-footer">
                              <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                              <button className="btn-secondary" onClick={() => apnePamelding(kurs.id)}>Til Påmelding</button>
                          </div>
                      </div>
                  </div>
              ))}
            </div>
          </section>
        </main>
      )}
        {currentPage === 'login' && (
            <Login setCurrentPage={setCurrentPage} setIsLoggedIn={setIsLoggedIn} setUser={setUser} />
        )}
        {currentPage === 'registrer' && (
            <Registrer setCurrentPage={setCurrentPage} />
        )}
        {currentPage === 'glemt-passord' && (
            <GlemtPassord setCurrentPage={setCurrentPage} />
        )}
        {currentPage === 'profil' && (
            <Profil setCurrentPage={setCurrentPage} setIsLoggedIn={setIsLoggedIn} user={user} setUser={setUser} />
        )}

        {currentPage === 'kurskalender' && (
            // S-011/S-019: kurskalenderen bruker samme kurs fra GET /api/courses, og sender kurs-ID videre til påmeldingen
            <Kurskalender courses={courses} lasterKurs={lasterKurs} kursFeil={kursFeil} kursBilder={kursBilder}
                          handleVelgKurs={apnePamelding} />
        )}
        {/* S-013 (Hiba): påmelding fra kurskortene på forsiden og i kurskalenderen. valgtKurs og user sendes også, så Påmeldning.jsx viser valgt kurs og innlogget bruker. */}
        {currentPage === 'pamelding' && (
            <Paamelding courses={courses} valgtKursId={valgtKursId} setCurrentPage={setCurrentPage}
                        valgtKurs={courses.find(k => k.id === valgtKursId)?.tittel} user={user} />
        )}

        {currentPage === 'kontakt' && (
            <KontaktOss setCurrentpage={setCurrentPage} />
        )}

        <footer>
          <div className="footer-logo">
            <img
                src={husflidLogo}
                alt="Østfolds Husflidslag Logo"
                style={{ width: '60px', height: 'auto' }}
            />
          </div>
        </footer>
      </div>
  );
}

export default App;