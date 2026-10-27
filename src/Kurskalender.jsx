import KursCard from './KursCard.jsx'
import SearchBar from './search.jsx';
import {useState} from 'react';

function Kurskalender({handleVelgKurs}){

    //**kobler den til kurskalender**//
    const [kursListe, setKursListe] = useState([]);
    const [searchQuery, setSearchQuery] = useState('');

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    //**Plasser api kall her?**//
    //** useEffect(() => {



    const filtrerteKurs = kursListe.filter((kurs) =>
        kurs.title.toLowerCase().includes(searchQuery.toLowerCase())
    );



    return(
        <main className="kurs-grid">
            <div className="top-action-bar">
                <button className="btn-outline">
                    <i className="fa-solid fa-location-dot" style={{color: '#bd2a4e'}}></i> Se kurs nær meg
                </button>
            </div>
            <div className="searchBar">
                <SearchBar placeholder="Søk etter kurs..." searchQuery={searchQuery} onSearchChange={setSearchQuery}/>
            </div>
            <div className="filter-container">
                <select className="filter-select">
                    <option>Husflidslag</option>
                    <option>Halden</option>
                    <option>Moss</option>
                </select>
                <select className="filter-select">
                    <option>Tema/kategori</option>
                    <option>Vev</option>
                    <option>Trearbeid</option>
                </select>
                <select className="filter-select">
                    <option>Arrangementstype</option>
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

                <div className="sorting-container">
                    <button className="sort-btn active">
                        <i className="fa-regular fa-calendar"></i> SORTER ETTER OPPRETTET <i
                        className="fa-solid fa-chevron-down"></i>
                    </button>
                    <button className="sort-btn">
                        <i className="fa-regular fa-calendar"></i> SORTER ETTER STARTDATO
                    </button>
                    <button className="sort-btn">
                        <i className="fa-regular fa-calendar"></i> SORTER ETTER SLUTTDATO
                    </button>
                </div>

                <div className="pagination">
                    <div className="pagination-controls">
                        <button className="page-btn disabled">&lt;</button>
                        <button className="page-btn">&gt;</button>
                    </div>
                    <span style={{fontSize: '14px', color: '#333'}}>Side 1 av 24</span>
                </div>
            </div>

            <div className="cards-grid2">
                {filtrerteKurs.length > 0 ? (
                    filtrerteKurs.map((kurs) => (
                        <KursCard
                            key={kurs.id}
                            title={kurs.title}
                            image={kurs.image}
                            tags={kurs.tags}
                            onPaamelding={() => handleVelgKurs(kurs.title)}
                        />
                    ))
                ) : (
                    <div style={{gridColumn: '1 / -1', textAlign: 'center', padding: '40px'}}>
                        <h3>Ingen kurs funnet</h3>
                        <p>Prøv å søke etter noe annet.</p>
                    </div>
                )}
            </div>

        </main>
    );
}

export default Kurskalender;
