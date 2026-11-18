import './KursInfo.css';

function KursInfo({kurs, setCurrentPage}) {
    if (!kurs){
        return (
            <main className="kursinfo-main">
                <div className="kursinfo-box kursinfo-empty">
                    <h2>Fant ikke kurset</h2>
                    <button className="main-btn" onClick={() => setCurrentPage('kurskalender')}>
                        Tilbake til kurskalender
                    </button>
                </div>
            </main>
        );
    }

    return (
        <main className="kursinfo-main">
            <div className="kursinfo-box">
                <img src={kurs.image} alt={kurs.title} className="kursinfo-img"/>


                {kurs.tags && (
                    <div className="kursinfo-tags">
                        {kurs.tags.map((tag, index) => (
                            <span key={index} className="kursinfo-tag"> {tag}</span>
                        ))}
                    </div>
                )}

                <h2 className="kursinfo-title">{kurs.title}</h2>
                <p className="kursinfo-description"> {kurs.description}</p>

                <div className="kursinfo-details">
                    <p>Arrangør: {kurs.lokallag ? `${kurs.lokallag} Husflidslag` : 'Østfold Husflidslag'}</p>
                    <p>Tidspunkt: {kurs.dato}</p>
                    <p>Pris: {kurs.pris}</p>
                </div>

                <button className="main-btn login-btn" onClick={() => setCurrentPage('paamelding')}> PÅMELDING</button>

                <div className="register-link">
                    <a href="#" onClick={(e) => {
                        e.preventDefault();
                        setCurrentPage('kurskalender');
                    }}>
                        ← Tilbake til kursoversikten
                    </a>
                </div>
            </div>
        </main>
    );
}

export default KursInfo;