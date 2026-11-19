import { useState, useEffect } from 'react';
import './KontaktOss.css';

function KontaktOss() {
    const [kontaktpersoner, setKontaktpersoner] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchKontaktpersoner = async () => {
            try {
                // 1. Erstatt URL-en under med ditt faktiske backend-endepunkt når API-et er klart.
                // 2. Sørg for at backend returnerer en JSON-liste med objekter som har feltene:
                //    - id, navn, avdeling, mobil, epost, bilde
                const apiEndpoint = 'http://localhost:5000/api/kontaktpersoner';

                const response = await fetch(apiEndpoint);

                if (!response.ok) {
                    throw new Error(`Feil ved henting av data. Status: ${response.status}`);
                }

                const data = await response.json();
                setKontaktpersoner(data);

            } catch (err) {
                console.warn("Kunne ikke hente fra API, faller tilbake til dummy-data midlertidig.", err);

                // MIDLERTIDIG DUMMY-DATA:
                // Dette gjør at siden fungerer visuelt frem til backend er oppe og kjører.
                // Du kan slette denne fallbacken når API-et er ferdig.
                setKontaktpersoner([
                    {
                        id: 1,
                        navn: 'Kari Nordmann',
                        avdeling: 'Leder / Styret',
                        mobil: '+47 900 11 222',
                        epost: 'kari.nordmann@husflid.no',
                        bilde: 'https://placehold.co/200x200?text=Kari'
                    },
                    {
                        id: 2,
                        navn: 'Ola Hansen',
                        avdeling: 'Kurs- og Aktivitetsansvarlig',
                        mobil: '+47 911 22 333',
                        epost: 'kurs@husflid.no',
                        bilde: 'https://placehold.co/200x200?text=Ola'
                    }
                ]);

                // setError("Klarte ikke laste kontaktpersoner."); // Kan aktiveres når dummy-data fjernes
            } finally {
                // Skrur alltid av lastevisningen når kallet er ferdig
                setLoading(false);
            }
        };

        fetchKontaktpersoner();
    }, []);

    return (
        <main className="kontakt-main">
            <section>
                <h2>Kontakt Oss</h2>
                <p className="news-text">
                    Lurer du på noe rundt våre aktiviteter, medlemskap eller kurs? Ta gjerne direkte kontakt med en av våre tillitsvalgte eller avdelingsledere nedenfor.
                </p>

                {loading && <p>Laster inn kontaktpersoner...</p>}
                {error && <p style={{ color: 'var(--primary-red)' }}>{error}</p>}

                {!loading && (
                    <div className="cards-grid">
                        {kontaktpersoner.map((person) => (
                            <div className="card kontakt-card" key={person.id}>
                                <div
                                    className="kontakt-card-img"
                                    style={{ backgroundImage: `url('${person.bilde}')` }}
                                ></div>
                                <div className="card-content">
                                    <h3>{person.navn}</h3>
                                    <p className="kontakt-avdeling">
                                        {person.avdeling}
                                    </p>
                                    <p className="kontakt-detalj">
                                        <i className="fa-solid fa-phone"></i>
                                        <a href={`tel:${person.mobil}`}>
                                            {person.mobil}
                                        </a>
                                    </p>
                                    <p className="kontakt-detalj">
                                        <i className="fa-solid fa-envelope"></i>
                                        <a href={`mailto:${person.epost}`}>
                                            {person.epost}
                                        </a>
                                    </p>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </main>
    );
}

export default KontaktOss;