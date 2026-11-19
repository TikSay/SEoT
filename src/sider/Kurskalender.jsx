import KursCard from '../Komponenter/KursCard.jsx'
import {useState} from 'react';

// 3 kolonner i .cards-grid2, så 6 kurs gir to hele rader per side
const KURS_PER_SIDE = 6;

// Vises for kurs som ikke har eget bilde i public/images (lokal SVG, krever ikke internett)
const STANDARDBILDE = 'data:image/svg+xml,' + encodeURIComponent(
    '<svg xmlns="http://www.w3.org/2000/svg" width="300" height="220">' +
    '<rect width="300" height="220" fill="#f3e6e9"/>' +
    '<text x="150" y="118" font-family="sans-serif" font-size="18" fill="#bd2a4e" text-anchor="middle">Østfold Husflid</text>' +
    '</svg>');

// 'halden husflidslag' -> 'Halden'
function lokallagNavn(lokallag) {
    const navn = lokallag.replace(/\s*husflidslag$/i, '');
    return navn.charAt(0).toUpperCase() + navn.slice(1);
}

// '2026-11-21' -> '2026-11', brukes som nøkkel for månedsfilteret
function maanedNokkel(dato) {
    return dato.slice(0, 7);
}

function maanedNavn(nokkel) {
    return new Date(nokkel + '-01').toLocaleDateString('nb-NO', {month: 'long', year: 'numeric'});
}

// S-011/S-012/S-019: kursene kommer fra GET /api/courses (hentes i App.jsx), filtreres på sted, nivå og tid
function Kurskalender({courses = [], lasterKurs, kursFeil, kursBilder = {}, handleVelgKurs}){

    const [searchQuery, setSearchQuery] = useState('');
    const [valgtLokallag, setValgtLokallag] = useState('');
    const [valgtNivaa, setValgtNivaa] = useState('');
    const [valgtMaaned, setValgtMaaned] = useState('');
    const [sortering, setSortering] = useState('startdato');
    const [side, setSide] = useState(1);

    // Valgene i filtrene lages fra kursdataene, så de alltid stemmer med det som finnes
    const lokallagValg = [...new Set(courses.map(kurs => kurs.lokallag))].sort();
    const nivaaValg = [...new Set(courses.map(kurs => kurs.nivaa).filter(Boolean))].sort();
    const maanedValg = [...new Set(courses.map(kurs => maanedNokkel(kurs.dato)))].sort();

    const sok = searchQuery.trim().toLowerCase();
    const filtrerteKurs = courses
        .filter((kurs) => !sok || [kurs.tittel, kurs.beskrivelse, kurs.sted, kurs.lokallag]
            .some(felt => felt && felt.toLowerCase().includes(sok)))
        .filter((kurs) => !valgtLokallag || kurs.lokallag === valgtLokallag)
        .filter((kurs) => !valgtNivaa || kurs.nivaa === valgtNivaa)
        .filter((kurs) => !valgtMaaned || maanedNokkel(kurs.dato) === valgtMaaned)
        .sort((a, b) => sortering === 'opprettet'
            ? b.id - a.id                                              // nyest opprettet først
            : (a.dato + a.starttid).localeCompare(b.dato + b.starttid)); // første kurs først

    // Antall sider regnes fra de filtrerte kursene
    const antallSider = Math.max(1, Math.ceil(filtrerteKurs.length / KURS_PER_SIDE));
    const gjeldendeSide = Math.min(side, antallSider);
    const kursPaaSiden = filtrerteKurs.slice((gjeldendeSide - 1) * KURS_PER_SIDE, gjeldendeSide * KURS_PER_SIDE);
    const filterErValgt = Boolean(sok || valgtLokallag || valgtNivaa || valgtMaaned);

    // Nytt søk eller filter starter på side 1
    const endreFilter = (setter) => (e) => {
        setter(e.target.value);
        setSide(1);
    };

    const nullstillFiltre = () => {
        setSearchQuery('');
        setValgtLokallag('');
        setValgtNivaa('');
        setValgtMaaned('');
        setSide(1);
    };

    return(
        <main className="kurs-grid">
            <div className="top-action-bar">
                <button className="btn-outline">
                    <i className="fa-solid fa-location-dot" style={{color: '#bd2a4e'}}></i> Se kurs nær meg
                </button>
            </div>
            <div className="filter-container">
                <select className="filter-select" value={valgtLokallag} onChange={endreFilter(setValgtLokallag)}
                        aria-label="Husflidslag">
                    <option value="">Husflidslag</option>
                    {lokallagValg.map(lokallag => (
                        <option key={lokallag} value={lokallag}>{lokallagNavn(lokallag)}</option>
                    ))}
                </select>
                <select className="filter-select" value={valgtNivaa} onChange={endreFilter(setValgtNivaa)}
                        aria-label="Nivå">
                    <option value="">Nivå</option>
                    {nivaaValg.map(nivaa => (
                        <option key={nivaa} value={nivaa}>{nivaa.charAt(0).toUpperCase() + nivaa.slice(1)}</option>
                    ))}
                </select>
                <select className="filter-select" value={valgtMaaned} onChange={endreFilter(setValgtMaaned)}
                        aria-label="Tidspunkt">
                    <option value="">Tidspunkt</option>
                    {maanedValg.map(maaned => (
                        <option key={maaned} value={maaned}>{maanedNavn(maaned)}</option>
                    ))}
                </select>

                <div className="filter-search-container">
                    <i className="fa-solid fa-magnifying-glass"></i>
                    <input
                        type="text"
                        className="filter-search"
                        placeholder="Søk etter kurs ..."
                        value={searchQuery}
                        onChange={endreFilter(setSearchQuery)}
                    />
                </div>
            </div>

            <div className="results-header">
                <h1>{lasterKurs ? 'Henter kurs ...' : `${filtrerteKurs.length} resultater`}</h1>

                <div className="sorting-container">
                    <button className={`sort-btn${sortering === 'startdato' ? ' active' : ''}`}
                            onClick={() => setSortering('startdato')}>
                        <i className="fa-regular fa-calendar"></i> SORTER ETTER STARTDATO
                        {sortering === 'startdato' && <> <i className="fa-solid fa-chevron-down"></i></>}
                    </button>
                    <button className={`sort-btn${sortering === 'opprettet' ? ' active' : ''}`}
                            onClick={() => setSortering('opprettet')}>
                        <i className="fa-regular fa-calendar"></i> SORTER ETTER OPPRETTET
                        {sortering === 'opprettet' && <> <i className="fa-solid fa-chevron-down"></i></>}
                    </button>
                </div>

                {/* Pagineringen vises bare når det finnes kurs å bla i */}
                {filtrerteKurs.length > 0 && (
                    <div className="pagination">
                        <div className="pagination-controls">
                            <button className={`page-btn${gjeldendeSide <= 1 ? ' disabled' : ''}`}
                                    disabled={gjeldendeSide <= 1} aria-label="Forrige side"
                                    onClick={() => setSide(gjeldendeSide - 1)}>&lt;</button>
                            <button className={`page-btn${gjeldendeSide >= antallSider ? ' disabled' : ''}`}
                                    disabled={gjeldendeSide >= antallSider} aria-label="Neste side"
                                    onClick={() => setSide(gjeldendeSide + 1)}>&gt;</button>
                        </div>
                        <span style={{fontSize: '14px', color: '#333'}}>Side {gjeldendeSide} av {antallSider}</span>
                    </div>
                )}
            </div>

            <div className="cards-grid2">
                {lasterKurs ? null : kursFeil ? (
                    <div role="alert" style={{gridColumn: '1 / -1', textAlign: 'center', padding: '40px'}}>
                        <h3>Kursene kan ikke hentes akkurat nå</h3>
                        <p>Prøv igjen senere.</p>
                    </div>
                ) : kursPaaSiden.length > 0 ? (
                    kursPaaSiden.map((kurs) => (
                        <KursCard
                            key={kurs.id}
                            title={kurs.tittel}
                            image={kursBilder[kurs.tittel] || STANDARDBILDE}
                            tags={[
                                lokallagNavn(kurs.lokallag),
                                new Date(kurs.dato).toLocaleDateString('nb-NO'),
                                Number(kurs.pris) === 0 ? 'Gratis' : `${kurs.pris} kr`
                            ]}
                            onPaamelding={() => handleVelgKurs(kurs.id)}
                        />
                    ))
                ) : (
                    <div style={{gridColumn: '1 / -1', textAlign: 'center', padding: '40px'}}>
                        <h3>Ingen kurs funnet</h3>
                        {filterErValgt ? (
                            <>
                                <p>Ingen kurs passer med søket eller filtrene du har valgt.</p>
                                <button className="btn-outline" onClick={nullstillFiltre}>Nullstill filtre</button>
                            </>
                        ) : (
                            <p>Det er ingen kommende kurs akkurat nå.</p>
                        )}
                    </div>
                )}
            </div>

        </main>
    );
}

export default Kurskalender;
