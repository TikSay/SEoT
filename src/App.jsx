import './App.css';
import { useState } from 'react';
import Login from './Login';
import husflidLogo from './assets/husflidlog.jpg';

function App() {
  const [currentPage, setCurrentPage] = useState('home');

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
            <a href="#">KONTAKT OSS</a>
          </nav>

          <nav className="icon-nav">
            <div className="icon-item">
              <span><i className="fa-solid fa-magnifying-glass"></i></span>SØK
            </div>
            <div className="icon-item">
              <span><i className="fa-solid fa-calendar"></i></span>KALENDER
            </div>
            <div className="icon-item" onClick={() => setCurrentPage('login')}>
              <span><i className="fa-solid fa-cart-shopping"></i></span>NETTBUTIKK
            </div>
          </nav>
        </header>

      {currentPage === 'home' ? (
        <main>
          <section className="left-column">
            <h2>Siste Nytt</h2>
            <div className="news-pic-temp"></div>
            <div className="news-text">
              <p>Østfold Husflidslag ble stiftet 28.09.1932. Vår første bunad, Østfold kvinnebunad, ble lansert i 1936.
                Deretter kom Østfold mannsbunad i 1990. Og Herregårdsbunad for kvinner ble lansert i 2015.</p>
              <p>For alle som liker å skape med hendene, fordype seg i tradisjonshåndverk eller lære noe nytt i
                fellesskap med andre så kan du bli medlem i et av våre lokallag!</p>
            </div>
            <button className="main-btn">BLI MEDLEM</button>
          </section>

          <section className="right-column">
            <h2>Kommende Kurs- og Aktiviteter</h2>

            <div className="cards-grid">
              <div className="card">
                <div className="card-img" style={{ backgroundImage: "url('https://placehold.co/300x180')" }}></div>
                <div className="card-content">
                  <h3>Sytreff i Råde</h3>
                  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore
                    et dolore magna aliqua</p>
                  <div className="card-footer">
                    <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                    <button className="btn-secondary">Til Påmelding</button>
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="card-img" style={{ backgroundImage: "url('https://placehold.co/300x180')" }}></div>
                <div className="card-content">
                  <h3>Meksikansk fletting</h3>
                  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore
                    et dolore magna aliqua</p>
                  <div className="card-footer">
                    <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                    <button className="btn-secondary">Til Påmelding</button>
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="card-img" style={{ backgroundImage: "url('https://placehold.co/300x180')" }}></div>
                <div className="card-content">
                  <h3>Lær å brodere, plattsøm og tellesøm</h3>
                  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore
                    et dolore magna aliqua</p>
                  <div className="card-footer">
                    <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                    <button className="btn-secondary">Til Påmelding</button>
                  </div>
                </div>
              </div>

              <div className="card">
                <div className="card-img" style={{ backgroundImage: "url('https://placehold.co/300x180')" }}></div>
                <div className="card-content">
                  <h3>Bunadkurs</h3>
                  <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore
                    et dolore magna aliqua</p>
                  <div className="card-footer">
                    <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                    <button className="btn-secondary">Til Påmelding</button>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </main>
      ) : (
          <Login/>
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