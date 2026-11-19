import './KontaktOss.css';

// S-020: kontaktinformasjon hentet fra Østfold Husflidslags side på husflid.no
// (https://husflid.no/lag/ostfold-husflidslag/#contact), sjekket 13.11.2026.
// Ligger i frontend foreløpig, siden backend ikke har et API for kontaktpersoner.
const KILDE_URL = 'https://husflid.no/lag/ostfold-husflidslag/#contact';

const hovedkontakt = {
    navn: 'Mona Andersen',
    adresse: ['Emil Mørchs vei 25, 2. etg. rom 1', '1653 SELLEBAKK'],
    telefon: '91777582',
    epost: 'ostfoldhusflid@gmail.com'
};

// Styret, i samme rekkefølge som på husflid.no. Linda P. Vallner står med to roller der.
const styret = [
    {navn: 'Mona Andersen', roller: ['Styreleder'], epost: 'mona@sjokkpris.no', telefon: '91777582'},
    {navn: 'Aud Kari Holme', roller: ['Nestleder'], epost: 'Audkari.holme@gmail.com', telefon: '95879367'},
    {navn: 'Heidi Børstad', roller: ['Sekretær'], epost: 'heidi.borstad@gmail.com', telefon: '95222155'},
    {navn: 'Silja Devine Holhjem', roller: ['Nettansvarlig'], epost: 'siljadevine@gmail.com', telefon: '41664904'},
    {navn: 'Linda P. Vallner', roller: ['Styremedlem', 'Ung Husflidskontakt'], epost: 'lindap.vallner@gmail.com', telefon: '97638424'},
    {navn: 'Thore Grøtvedt', roller: ['Styremedlem'], epost: 'thoregro@gmail.com', telefon: '90135455'},
    {navn: 'Julie Mari Brevik', roller: ['Varamedlem'], epost: 'bunad.julie@gmail.com', telefon: '99313244'}
];

// '91777582' -> '91 77 75 82'
function visTelefon(nummer) {
    return nummer.replace(/(\d{2})(?=\d)/g, '$1 ');
}

function Telefon({nummer}) {
    return (
        <p className="kontakt-detalj">
            <i className="fa-solid fa-phone"></i>
            <a href={`tel:+47${nummer}`}>{visTelefon(nummer)}</a>
        </p>
    );
}

function Epost({adresse}) {
    return (
        <p className="kontakt-detalj">
            <i className="fa-solid fa-envelope"></i>
            <a href={`mailto:${adresse}`}>{adresse}</a>
        </p>
    );
}

function KontaktOss() {
    return (
        <main className="kontakt-main">
            <section>
                <h2>Kontakt Oss</h2>
                <p className="news-text">
                    Lurer du på noe rundt våre aktiviteter, medlemskap eller kurs? Ta gjerne kontakt med Østfold Husflidslag, eller direkte med en av de tillitsvalgte i styret nedenfor.
                </p>

                <h3>Østfold Husflidslag</h3>
                <div className="cards-grid">
                    <div className="card kontakt-card">
                        <div className="card-content">
                            <h3>{hovedkontakt.navn}</h3>
                            <p className="kontakt-avdeling">Kontaktperson</p>
                            <p className="kontakt-detalj">
                                <i className="fa-solid fa-location-dot"></i>
                                <span>{hovedkontakt.adresse[0]}<br/>{hovedkontakt.adresse[1]}</span>
                            </p>
                            <Telefon nummer={hovedkontakt.telefon}/>
                            <Epost adresse={hovedkontakt.epost}/>
                        </div>
                    </div>
                </div>

                <h3>Styret</h3>
                <div className="cards-grid">
                    {styret.map((person) => (
                        <div className="card kontakt-card" key={person.navn}>
                            <div className="card-content">
                                <h3>{person.navn}</h3>
                                <p className="kontakt-avdeling">{person.roller.join(' · ')}</p>
                                <Telefon nummer={person.telefon}/>
                                <Epost adresse={person.epost}/>
                            </div>
                        </div>
                    ))}
                </div>

                <p className="kontakt-detalj">
                    Kilde: <a href={KILDE_URL} target="_blank" rel="noreferrer">&nbsp;Østfold Husflidslag på husflid.no</a>
                </p>
            </section>
        </main>
    );
}

export default KontaktOss;
