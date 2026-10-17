import KursCard from './KursCard.jsx'

function Kurskalender({handleVelgKurs}){
    const kursListe = [
        //Fylt med dummy data inni filen for å se om det faktisk funker, er bare å slette etter påp//
        { id: 1, title: 'Eksempel1', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff!'},
        { id: 2, title: 'Eksempel2', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff2!'},
        { id: 3, title: 'Eksempel3', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff!3'},
        { id: 4, title: 'Eksempel4', image: 'https://placehold.co/300x180', description: 'Episk gruppetreff!4'}

    ];

    return(
      <main>
          <h2>Kommende Kurs- og Aktiviteter</h2>
          <div className="cards-grid">
              {kursListe.map((kurs)=> (
                  <KursCard key={kurs.id} title={kurs.title} image={kurs.image} description={kurs.description} onPaamelding={handleVelgKurs}/>

                  ))}
          </div>
      </main>
    );
}

export default Kurskalender;
