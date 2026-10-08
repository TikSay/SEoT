import { useState } from 'react';

//Hvis du trenger info for å vite hvordan dette funker: her kan du lese om det: https://medium.com/@AnthonyBostic/creating-a-login-form-utilizing-react-hooks-da7d7685cbb6

function Login({setCurrentPage}) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        console.log('Logget inn med:', { email, password });
        const response = await fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
        });
        const result = await response.json();
        if (result.success) {
            setCurrentPage('profil');
        } else {
            alert(result.message);
        }
    };

    return (
        //Hjelpemiddel når det kommer til å sette opp registrering og info endring i react: https://stackoverflow.com/questions/64063348/react-password-validation-onchange
        <main className="login-main">
            <div className="login-box">
                <h2>Logg inn</h2>
                <form onSubmit={handleSubmit}>
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

                    <div className="login-options">
                        <label>
                            <input type="checkbox" name="remember"/> Husk meg
                        </label>
                        <a href="#" onClick={(e) => {e.preventDefault(); setCurrentPage('glemt-passord');}}>Glemt passord?</a>
                    </div>

                    <button type="submit" className="main-btn login-btn">
                        LOGG INN
                    </button>
                </form>

                <div className="register-link">
                    Ikke medlem enda? <a href="#" onClick={(e) => {e.preventDefault(); setCurrentPage('registrer');}}>Bli medlem her</a>
                </div>
            </div>
        </main>
    );
}

export default Login;