import KursCard from './KursCard.jsx'
import SearchBar from './search.jsx';
import {useState} from 'react';

function Kurskalender({handleVelgKurs}){
    const kursListe = [
        //Fylt med dummy data inni filen for å se om det faktisk funker, er bare å slette etter påp//
        { id: 1, title: 'Eksempel1', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff!' },
        { id: 2, title: 'Eksempel2', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff2!' },
        { id: 3, title: 'Eksempel3', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff!3' },
        { id: 4, title: 'Eksempel4', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff!4' }
    ];

    const [searchQuery, setSearchQuery] = useState('');

    const filtrerteKurs = kursListe.filter((kurs) =>
    kurs.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
    kurs.description.toLowerCase().includes(searchQuery.toLowerCase()));
    //tester søkemoto funksjon//



    return(
      <main className="kurs-grid">
          <h2>Kommende Kurs- og Aktiviteter</h2>
          <div className="searchBar">
              <SearchBar placeholder="Søk etter kurs..." searchQuery={searchQuery} onSearchChange={setSearchQuery} />
          </div>
          <p className="result-count"> {filtrerteKurs.length} resultater</p>

          <div className="cards-grid2">
              {filtrerteKurs.length > 0? (
                  filtrerteKurs.map((kurs)=> (
                      <KursCard key={kurs.id} title={kurs.title} image={kurs.image} description={kurs.description} onPaamelding={()=> handleVelgKurs(kurs)}/>

                  ))
              ) : (
                  <div className="no-results">
                      <h3>Ingen kurs funnet</h3>
                      <p>Prøv å søke etter noe annet.</p>
                  </div>
              )}

          </div>

      </main>
    );
}

export default Kurskalender;
