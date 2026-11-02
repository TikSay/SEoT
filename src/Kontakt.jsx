import './KontaktOss.css';

function KontaktOss() {
    const kontaktpersoner = [
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
        },
        {
            id: 3,
            navn: 'Berit Svendsen',
            avdeling: 'Bunad- og Tekstildivisjon',
            mobil: '+47 922 33 444',
            epost: 'bunad@husflid.no',
            bilde: 'https://placehold.co/200x200?text=Berit'
        },
        {
            id: 4,
            navn: 'Jonas Lie',
            avdeling: 'Medlemsansvarlig & Økonomi',
            mobil: '+47 933 44 555',
            epost: 'medlem@husflid.no',
            bilde: 'https://placehold.co/200x200?text=Jonas'
        }
    ];

    return (
        <main className="kontakt-main">
            <section>
                <h2>Kontakt Oss</h2>
                <p className="news-text">
                    Lurer du på noe rundt våre aktiviteter, medlemskap eller kurs? Ta gjerne direkte kontakt med en av våre tillitsvalgte eller avdelingsledere nedenfor.
                </p>

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
            </section>
        </main>
    );
}

export default KontaktOss;