import {useState} from 'react';

function Paamelding({valgtKurs, setCurrentPage, user}) {
    //Hadde vært kult hvis vi kunne ha den autofylt når man er logget inn//
    const [fornavn, setFornavn] = useState(user?.fornanvn || '');
    const [etternavn, setEtternavn] = useState(user?.etternavn ||'');
    const [email, setEmail] = useState(user?.email ||'');
    const [kursperiode, setKursperiode] = useState(valgtKurs || 'Eksempel kurs - 20/08-2026');
    const [isMedlem, setIsMedlem] = useState(true);
    const [medlemsnummer, setMedlemsnummer] = useState('');
    const [godtattBetingelser, setGodtattBetingelser] = useState(false);

    const handleSubmit = (e) => {
        e.preventDefault();
        if (!godtattBetingelser) {
            alert('Du må ha godta salgsbetingelsene for å melde deg på.');
            return;
        }

        const paameldingData = {
            fornavn,
            etternavn,
            email,
            kursperiode,
            isMedlem,
            medlemsnummer: isMedlem ? medlemsnummer : null
        };

        console.log('Mottatt påmelding:', paameldingData);
        alert('Takk for din påmelding!');
        setCurrentPage('kurskalender');
    };

    return(
        <main className="login-main">
            <div className="login-box">
                <h2>Påmelding</h2>

                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <input type="text" placeholder="Fornavn" value={fornanv}
                               onChange={(e) => setFornavn(e.target.value)} required/>
                    </div>

                    <div className="input-group">
                        <input type="text" placeholder="Etternavn" value={etternavn}
                               onChange={(e) => setEtternavn(e.target.value)} required/>
                    </div>

                    <div className="input-group">
                        <input type="text" placeholder="E-post" value={email} onChange={(e) => setEmail(e.target.value)} required/>
                    </div>

                    <div className="input-group">
                        <label>Velg kursperiode</label>
                        <select className="by-options" value={kursperiode}
                                onChange={(e) => setKursperiode(e.target.value)}>
                            <option value="Eksempel kurs1 (kan fylle med dummy data)">Eksempel 1 - 20/08-2026</option>
                            <option value="Eksempel kurs2 (kan fylle med dummy data)">Eksempel 2 - 24/12-2026</option>
                            <option value="Eksempel kurs3 (kan fylle med dummy data)">Eksempel 3 - 22/04-2027</option>
                        </select>
                    </div>

                    <div>
                        <label>
                            <input type="radio" name="medlemsskap" checked={isMedlem} onChange={() => setIsMedlem(true)}/>
                            Medlem (Kr pris for medlem,-)
                        </label>

                        <label>
                            <input type="radio" name="medlemsskap" checked={!isMedlem} onChange={()=> setIsMedlem(false)}/>
                            Pris (Kr pris for ikke medlem,-)
                        </label>
                    </div>

                    <div>
                        <label>
                            <input type="checkbox" checked={godtattBetingelser} onChange={(e)=> setGodtattBetingelser(e.target.value)}/>
                            Jeg har lest og godtar salgsbetingelsene
                        </label>
                    </div>

                    <div>
                        <button className="btn-secondary">
                            Meld på
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}

export default Paamelding;