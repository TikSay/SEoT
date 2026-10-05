import { useState } from 'react';

//Hvis du trenger info for å vite hvordan dette funker: her kan du lese om det: https://medium.com/@AnthonyBostic/creating-a-login-form-utilizing-react-hooks-da7d7685cbb6

function Login() {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Logget inn med:', { email, password });
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
                        <a href="#">Glemt passord?</a>
                    </div>

                    <button type="submit" className="main-btn login-btn">
                        LOGG INN
                    </button>
                </form>

                <div className="register-link">
                    Ikke medlem enda? <a href="#">Bli medlem her</a>
                </div>
            </div>
        </main>
    );
}

export default Login;