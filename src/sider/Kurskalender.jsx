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
            lokallag: 'Halden',
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
            kategori: 'Annet',
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

    const filtrerteKurs = kursListe.filter((kurs) => {kurs.title.toLowerCase().includes(searchQuery.toLowerCase());
        const matcherSok = kurs.title.toLowerCase().includes(searchQuery.toLowerCase());
        const matcherLag = valgtLag === 'Alle' || kurs.lokallag === valgtLag;
        const matcherKategori = valgtKategori === 'Alle' || kurs.kategori === valgtKategori;
        const matcherType = valgtType === 'Alle' || kurs.type === valgtType;

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
                <select className="filter-select">
                    <option>Husflidslag (Alle)</option>
                    <option>Halden</option>
                    <option>Moss</option>
                    <option>Fredrikstad</option>
                    <option>Råde</option>
                </select>
                <select className="filter-select">
                    <option>Tema/kategori (Alle)</option>
                    <option>Vev</option>
                    <option>Trearbeid</option>
                    <option>Søm</option>
                    <option>Annet</option>
                </select>
                <select className="filter-select">
                    <option>Arrangementstype (Alle)</option>
                    <option>Kurs</option>
                    <option>Treff</option>
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
