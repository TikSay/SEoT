import { useState } from 'react';

function Registrer({setCurrentPage}) {
    const [fornavn, setFornavn] = useState('');
    const [etternavn, setEtternavn] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();

        //*Ganske bra passord check https://stackoverflow.com/questions/51143800/how-to-set-match-password-in-react-js *//
        if (password !== confirmPassword){
            alert("Passordet stemmer ikke");
            return;
        }
        const response = await fetch('/api/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ fornavn, etternavn, email, password, lokallag: e.target.By.value }),
        });
        const result = await response.json();
        if (result.success) {
            alert('Bruker opprettet');
            setCurrentPage('login');
        } else {
            alert(result.message);
        }

    };

    return (
        <main className="login-main">
            <div className="login-box">
                <h2>Bli medlem</h2>
                <form onSubmit={handleSubmit}>
                    <div className="input-group side-by-side">
                        <label>Fornavn
                            <input type="text" value={fornavn} onChange={(e) => setFornavn(e.target.value)} required/>
                        </label>

                        <label>Etternavn
                            <input type="text" value={etternavn} onChange={(e) => setEtternavn(e.target.value)} required/>
                        </label>
                    </div>
                    <div className="input-group">
                        <label htmlFor="email">E-postadresse</label>
                        <input
                            type="email"
                            id="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="password">Passord</label>
                        <input
                            type="password"
                            id="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="confirmPassword">Bekreft passord</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            value={confirmPassword}
                            onChange={(e) => setConfirmPassword(e.target.value)}
                            required
                        />
                    </div>

                    <div className="input-group">
                        <label htmlFor="By">By adresse</label>
                        <select className="by-options" name="By" required>
                            <option value="fredrikstad husflidslag">Fredrikstad</option>
                            <option value="halden husflidslag">Halden</option>
                            <option value="hobøl husflidslag">Hobøl</option>
                            <option value="indre østfold husflidslag">Indre Østfold</option>
                            <option value="moss husflidslag">Moss</option>
                            <option value="rygge husflidslag">Rygge</option>
                            <option value="råde husflidslag">Råde</option>
                            <option value="sarpsborg husflidslag">Sarpsborg</option>
                        </select>
                    </div>

                    <button type="submit" className="main-btn login-btn">REGISTRER DEG</button>
                </form>

                <div className="register-link">
                    Har du konto? <a href="#" onClick={(e) => {e.preventDefault(); setCurrentPage('login'); }}>Logg inn her</a>
                </div>
            </div>
        </main>
    );
}

export default Registrer;