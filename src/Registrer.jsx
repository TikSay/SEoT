import { useState } from 'react';

function Registrer({setCurrentPage}) {
    const [name, setName] = useState('');
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();

        //*Ganske bra passord check https://stackoverflow.com/questions/51143800/how-to-set-match-password-in-react-js *//
        if (password !== confirmPassword){
            alert("Passordet stemmer ikke");
            return;
        }
        alert('Bruker opprettet');
        setCurrentPage('login');

    };

    return (
        <main className="login-main">
            <div className="login-box">
                <h2>Bli medlem</h2>
                <form onSubmit={handleSubmit}>
                    <div className="input-group">
                        <label>Navn</label>
                        <input type="text" value={name} onChange={(e) => setName(e.target.value)} required/>
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