import KursCard from '../Komponenter/KursCard.jsx'
import {useState} from 'react';

function Kurskalender({ handleSeKurs }) {

    //dummy-data! Dette er en liste med objekter som fungerer
    // Slett når du får inn APIkall//
    const [kursListe] = useState([
        {
            id: 1,
            title: 'Nissekurs, nålefilting',
            image: 'https://placehold.co/400x250/f4f4f4/666?text=Nissekurs',
            tags: ['Annet', 'Kurs'],
            lokallag: 'Halden',
            kategori: 'Annet',
            type: 'Kurs',
            dato: '15. november 2026, kl. 18:00',
            pris: '450 kr (Medlem) / 600 kr (Ikke-medlem)',
            description: 'Lær grunnleggende nålefilting og lag dine egne julenisser i ull.'
        },
        {
            id: 2,
            title: 'Julenissen, vev',
            image: 'https://placehold.co/400x250/f4f4f4/666?text=Nissekurs',
            tags: ['Vev', 'Kurs'],
            lokallag: 'Halden',
            kategori: 'Annet',
            type: 'Kurs',
            dato: '15. november 2026, kl. 18:00',
            pris: '450 kr (Medlem) / 600 kr (Ikke-medlem)',
            description: 'Lær grunnleggende nålefilting og lag dine egne julenisser i ull.'
        },
        {
            id: 3,
            title: 'Nissekurs, nålefilting',
            image: 'https://placehold.co/400x250/f4f4f4/666?text=Nissekurs',
            tags: ['Annet', 'Kurs'],
            lokallag: 'Moss',
            kategori: 'Annet',
            type: 'Kurs',
            dato: '15. november 2026, kl. 18:00',
            pris: '450 kr (Medlem) / 600 kr (Ikke-medlem)',
            description: 'Lær grunnleggende nålefilting og lag dine egne julenisser i ull.'
        },
        {
            id: 4,
            title: 'Nissekurs, nålefilting',
            image: 'https://placehold.co/400x250/f4f4f4/666?text=Nissekurs',
            tags: ['Annet', 'Kurs'],
            lokallag: 'Halden',
            kategori: 'Annet',
            type: 'Kurs',
            dato: '15. november 2026, kl. 18:00',
            pris: '450 kr (Medlem) / 600 kr (Ikke-medlem)',
            description: 'Lær grunnleggende nålefilting og lag dine egne julenisser i ull.'
        },
        {
            id: 5,
            title: 'Nissekurs, nålefilting',
            image: 'https://placehold.co/400x250/f4f4f4/666?text=Nissekurs',
            tags: ['Annet', 'Kurs'],
            lokallag: 'Halden',
            kategori: 'Vev',
            type: 'Kurs',
            dato: '15. november 2026, kl. 18:00',
            pris: '450 kr (Medlem) / 600 kr (Ikke-medlem)',
            description: 'Lær grunnleggende nålefilting og lag dine egne julenisser i ull.'
        },
        {
            id: 6,
            title: 'Nissekurs, nålefilting',
            image: 'https://placehold.co/400x250/f4f4f4/666?text=Nissekurs',
            tags: ['Annet', 'Kurs'],
            lokallag: 'Halden',
            kategori: 'Annet',
            type: 'Kurs',
            dato: '15. november 2026, kl. 18:00',
            pris: '450 kr (Medlem) / 600 kr (Ikke-medlem)',
            description: 'Lær grunnleggende nålefilting og lag dine egne julenisser i ull.'
        }
    ]);

    const [searchQuery, setSearchQuery] = useState('');
    const [valgtLag, setValgtLag] = useState('Alle');
    const [valgtKategori, setValgtKategori] = useState('Alle');
    const [valgtType, setValgtType] = useState('Alle');

    const filtrerteKurs = kursListe.filter((kurs) => {
        const matcherSok = !searchQuery ||
            (kurs.title && kurs.title.toLowerCase().includes(searchQuery.toLowerCase()));

        const matcherLag = !valgtLag || valgtLag.toLowerCase() === 'alle' ||
            (kurs.lokallag && kurs.lokallag.toLowerCase() === valgtLag.toLowerCase());

        const visAlleKategorier = !valgtKategori ||
            valgtKategori === '' ||
            valgtKategori.toLowerCase() === 'alle' ||
            valgtKategori.toLowerCase().includes('alle') ||
            valgtKategori.toLowerCase().includes('tema');

        const matcherKategori = visAlleKategorier ||
            (kurs.kategori && kurs.kategori.toLowerCase() === valgtKategori.toLowerCase()) ||
            (kurs.tags && Array.isArray(kurs.tags) && kurs.tags.some(tag => tag.toLowerCase() === valgtKategori.toLowerCase()));

        const matcherType = !valgtType || valgtType.toLowerCase() === 'alle' ||
            (kurs.type && kurs.type.toLowerCase() === valgtType.toLowerCase());

        return matcherSok && matcherLag && matcherKategori && matcherType;
    });



    return(
        <main className="kurs-grid">
            <div className="top-action-bar">
                <button className="btn-outline">
                    <i className="fa-solid fa-location-dot" style={{color: '#bd2a4e'}}></i> Se kurs nær meg
                </button>
            </div>
            <div className="filter-container">
                <select className="filter-select" value={valgtLag} onChange={(e) => setValgtLag(e.target.value)}>
                    <option value="">Husflidslag (Alle)</option>
                    <option value="Halden">Halden</option>
                    <option value="Moss">Moss</option>
                    <option value="Fredrikstad">Fredrikstad</option>
                    <option value="Råde">Råde</option>
                </select>
                <select className="filter-select" value={valgtKategori} onChange={(e) => setValgtKategori(e.target.value)}>
                    <option value="">Tema/kategori (Alle)</option>
                    <option value="Vev">Vev</option>
                    <option value="Trearneid">Trearbeid</option>
                    <option value="Søm">Søm</option>
                    <option value="Annet">Annet</option>
                </select>
                <select className="filter-select" value={valgtType} onChange={(e) => setValgtType(e.target.value)}>
                    <option value="">Arrangementstype (Alle)</option>
                    <option value="Kurs">Kurs</option>
                    <option value="Treff">Treff</option>
                </select>

                <div className="filter-search-container">
                    <i className="fa-solid fa-magnifying-glass"></i>
                    <input
                        type="text"
                        className="filter-search"
                        placeholder="Søk etter kurs ..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>
            </div>

            <div className="results-header">
                <h1>{filtrerteKurs.length} resultater</h1>
            </div>

            <div className="results-header">
                {filtrerteKurs.length > 0 ? (
                    filtrerteKurs.map((kurs) => (
                        <KursCard
                            key={kurs.id}
                            title={kurs.title}
                            image={kurs.image}
                            tags={kurs.tags}
                            // Sender til infosiden når man klikker på kortet
                            onPaamelding={() => handleSeKurs(kurs)}
                        />
                    ))
                ) : (
                    <div style={{gridColumn: '1 / -1', textAlign: 'center', padding: '40px'}}>
                        <h3>Ingen kurs funnet</h3>
                        <p>Prøv å søk etter noe annet.</p>
                    </div>
                )}
            </div>

        </main>
    );
}

export default Kurskalender;
