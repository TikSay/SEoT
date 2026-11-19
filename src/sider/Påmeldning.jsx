import {useState} from 'react';

// S-013/S-014: kjente feilmeldinger fra POST /api/enrollments, oversatt til norsk
const FEILMELDINGER = {
    'Sorry, this course is full.': 'Kurset er dessverre fullt.',
    'This email is already enrolled in this course.': 'Denne e-postadressen er allerede påmeldt dette kurset.',
    'Please enter a valid email address.': 'Skriv inn en gyldig e-postadresse.',
    'No member found with this email.':'Vi fant ingen medlemskonto med denne e-postadressen. Bruk e-posten du er registrert med, eller velg ikke-medlem.',
    'Age must be between 1 and 120.': 'Alder må være mellom 1 og 120.',
    'First and last name are required.': 'Fornavn og etternavn må fylles ut.',
    'First and last name cannot be longer than 50 characters.': 'Fornavn og etternavn kan ikke være lengre enn 50 tegn.',
    'Course not found.': 'Kurset finnes ikke lenger. Velg et annet kurs.',
};

// Brukes når backend sender en melding vi ikke har oversatt
function feilmeldingForStatus(status) {
    if (status === 400) return 'Sjekk at alle feltene er riktig fylt ut.';
    if (status === 404) return 'Kurset finnes ikke lenger. Velg et annet kurs.';
    if (status === 409) return 'Du kan ikke melde deg på dette kurset. Det er fullt, eller e-posten er allerede påmeldt.';
    return 'Påmeldingen er ikke tilgjengelig akkurat nå. Prøv igjen senere.';
}

function Paamelding({courses = [], valgtKursId, valgtKurs, setCurrentPage, user}) {
    //Hadde vært kult hvis vi kunne ha den autofylt når man er logget inn//
    const [fornavn, setFornavn] = useState(user?.fornavn || '');
    const [etternavn, setEtternavn] = useState(user?.etternavn ||'');
    const [email, setEmail] = useState(user?.email ||'');
    const [alder, setAlder] = useState('');
    // S-013: kurset velges med ID fra GET /api/courses. Fra kurskalenderen kommer bare tittelen, så den slås opp her.
    const [kursId, setKursId] = useState(
        valgtKursId ?? courses.find(k => k.tittel === valgtKurs)?.id ?? courses[0]?.id ?? '');
    // Backend sjekker medlemskap mot e-posten, så innloggede brukere starter som medlem
    const [isMedlem, setIsMedlem] = useState(Boolean(user));
    const [medlemsnummer, setMedlemsnummer] = useState('');
    const [godtattBetingelser, setGodtattBetingelser] = useState(false);
    const [sender, setSender] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (!godtattBetingelser) {
            alert('Du må ha godta salgsbetingelsene for å melde deg på.');
            return;
        }
        if (!kursId) {
            alert('Velg et kurs for å melde deg på.');
            return;
        }

        // Feltene må hete det samme som EnrollmentRequest i backend
        const paameldingData = {
            courseId: Number(kursId),
            fornavn,
            etternavn,
            alder: Number(alder),
            email,
            erMedlem: isMedlem
        };

        setSender(true);
        try {
            const response = await fetch('/api/enrollments', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(paameldingData)
            });
            const result = await response.json().catch(() => null);

            if (response.ok && result?.success) {
                const tittel = courses.find(k => k.id === Number(kursId))?.tittel;
                alert(`Takk for din påmelding${tittel ? ' til ' + tittel : ''}! Pris: ${result.pris} kr`);
                setCurrentPage('home');
            } else {
                alert(FEILMELDINGER[result?.message] || feilmeldingForStatus(response.status));
            }
        } catch {
            alert('Fikk ikke kontakt med serveren. Prøv igjen senere.');
        } finally {
            setSender(false);
        }
    };

    return(
        <main className="login-main">
            <div className="login-box paamelding-box">
                <h2>Påmelding</h2>

                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <input type="text" placeholder="Fornavn" value={fornavn}
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
                        <input type="number" placeholder="Alder" min="1" max="120" value={alder}
                               onChange={(e) => setAlder(e.target.value)} required/>
                    </div>

                    <div className="input-group">
                        <label>Velg kursperiode</label>
                        <select className="by-options" value={kursId}
                                onChange={(e) => setKursId(e.target.value)} required>
                            {courses.length === 0 && <option value="">Ingen kurs tilgjengelig</option>}
                            {courses.map(kurs => (
                                <option key={kurs.id} value={kurs.id}>
                                    {kurs.tittel} - {new Date(kurs.dato).toLocaleDateString('nb-NO')}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="paamelding-options">
                        <label className="paamelding-options-item">
                            <input type="radio" name="medlemsskap" checked={isMedlem} onChange={() => setIsMedlem(true)}/>
                            Medlem (Kr pris for medlem,-)
                        </label>

                        {isMedlem && (
                            <div className="input-group">
                                <input type="text" placeholder="Medlemsnummer" value={medlemsnummer} onChange={(e)=> setMedlemsnummer(e.target.value)} required={isMedlem}/>
                            </div>
                        )}


                        <label className="paamelding-options-item">
                            <input type="radio" name="medlemsskap" checked={!isMedlem} onChange={()=> setIsMedlem(false)}/>
                            Pris (Kr pris for ikke medlem,-)
                        </label>
                    </div>

                    <div className="paamelding-terms">
                        <label className="paamelding-options-item">
                            <input type="checkbox" checked={godtattBetingelser} onChange={(e)=> setGodtattBetingelser(e.target.checked)}/>
                            Jeg har lest og godtar salgsbetingelsene
                        </label>
                    </div>

                    <div className="paamelding-submit-container">
                        <button type="submit" className="btn-paamelding" disabled={sender}>
                            {sender ? 'Sender ...' : 'Meld på'}
                        </button>
                    </div>
                </form>
            </div>
        </main>
    );
}

export default Paamelding;