import './App.css';
import { useState, useEffect } from 'react';
import Login from './sider/Login.jsx';
import Registrer from './sider/Registrer.jsx';
import Profil from './sider/Profil.jsx';
import GlemtPassord from './sider/GlemtPassord.jsx';
import husflidLogo from './assets/husflidlog.jpg';
import Footer from "./Komponenter/Footer.jsx";
import Kurskalender from './sider/Kurskalender.jsx';
import KursInfo from './sider/KursInfo.jsx';
import Paamelding from './sider/Påmeldning.jsx';
import KontaktOss from './Komponenter/Kontakt.jsx';
import SisteNytt from './Komponenter/SisteNytt.jsx';
import ShareButton from "./Komponenter/ShareButton.jsx";

function App() {
  const [currentPage, setCurrentPage] = useState('home');
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [user, setUser] = useState(null);
    const [sjekkerSesjon, setSjekkerSesjon] = useState(true);
  const [courses, setCourses] = useState([]);
  const [valgtKursId, setValgtKursId] = useState(null);
  const [valgtKurs, setValgtKurs] = useState('');

  const handleSeKurs = (kursObjekt) => {
    setValgtKurs(kursObjekt);
    setCurrentPage('kursinfo');
  };

  const  handleVelgKurs = (kursTittel) => {
    setValgtKurs({title: kursTittel});
    setCurrentPage('paamelding');
  };

  function apnePamelding(kursId) {
        setValgtKursId(kursId);
        setCurrentPage('pamelding');
  }

  useEffect(() => {
        fetch('/api/courses')
            .then(res => res.json())
            .then(data => { if (data.success) setCourses(data.courses); })
            .catch(() => setCourses([]));
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
                  {courses.length === 0 && <p>Ingen kommende kurs akkurat nå.</p>}

                  {courses.map(kurs => (
                      <div className="card" key={kurs.id}>
                        <div className="card-img" style={{backgroundImage: "url('/images/sytreff.png')"}}></div>
                        <div className="card-img" style={{backgroundImage: "url('/images/fletting.png')"}}></div>
                        <div className="card-img" style={{backgroundImage: "url('/images/brodering.png')"}}></div>
                        <div className="card-img" style={{backgroundImage: "url('/images/bunad.png')"}}></div>
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
                            <ShareButton kurs={kurs}/>
                            <button className="btn-secondary" onClick={() => apnePamelding(kurs.id)}>Til Påmelding
                            </button>
                          </div>
                        </div>
                      </div>
                  ))}
                </div>


              </section>
            </main>
        )}
        {currentPage === 'login' && (
            <Login setCurrentPage={setCurrentPage} setIsLoggedIn={setIsLoggedIn} setUser={setUser}/>
        )}
        {currentPage === 'registrer' && (
            <Registrer setCurrentPage={setCurrentPage}/>
        )}
        {currentPage === 'glemt-passord' && (
            <GlemtPassord setCurrentPage={setCurrentPage}/>
        )}
        {currentPage === 'profil' && (
            <Profil setCurrentPage={setCurrentPage} setIsLoggedIn={setIsLoggedIn} user={user} setUser={setUser}/>
        )}

        {currentPage === 'kurskalender' && (
            <Kurskalender handleSeKurs={handleSeKurs}/>
        )}
        {currentPage === 'kursinfo' && (
            <KursInfo kurs={valgtKurs} setCurrentPage={setCurrentPage}/>
        )}

        {currentPage === 'paamelding' && (
            <Paamelding valgtKurs={valgtKurs?.title || valgtKurs} setCurrentPage={setCurrentPage} user={user}/>
        )}

        {currentPage === 'kontakt' && (
            <KontaktOss setCurrentpage={setCurrentPage}/>
        )}


        <Footer setCurrentPage={setCurrentPage}/>

      </div>
  );
}

export default App;