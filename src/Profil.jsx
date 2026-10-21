import { useState } from 'react';

function Profil({ setCurrentPage, setIsLoggedIn, user, setUser }) {
    const [redigerer, setRedigerer] = useState(false);
    const [fornavn, setFornavn] = useState(user ? user.fornavn : '');
    const [etternavn, setEtternavn] = useState(user ? user.etternavn : '');
    const [email, setEmail] = useState(user ? user.email : '');
    const [lokallag, setLokallag] = useState(user ? user.lokallag : '');

    const handleLogout = () => {
        setIsLoggedIn(false);
        setUser(null);
        setCurrentPage('home');
    };

    const handleSave = (e) => {
        e.preventDefault();

        setUser({
            ...user,
            fornavn,
            etternavn,
            email,
            lokallag,
        });
        setRedigerer(false);
    };

    if (!user) return null;

    return (
        <main className="profil-main">
            <div className="profil-box">
                <h2>Min Profil</h2>
                {redigerer ? (
                    <form onSubmit={handleSave}>
                        <div className="input-group">
                        <label htmlFor="fornavn">Fornavn:</label>
                        <input
                            type="text"
                            id="fornavn"
                            value={fornavn}
                            onChange={(e) => setFornavn(e.target.value)}
                            required
                        />
                    </div>
                    
                    <div className="input-group">
                        <label htmlFor="etternavn">Etternavn</label>
                        <input
                        type="text"
                        id="etternavn"
                        value={etternavn}
                        onChange={(e) => setEtternavn(e.target.value)}
                        required
                    />
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

                    <div className  ="input-group">
                        <label htmlFor="lokallag">Lokallag:</label>
                        <select
                            id="lokallag"
                            className="by-options"
                            value={lokallag}
                            onChange={(e) => setLokallag(e.target.value)}
                        >
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
                        <button type="submit" className="main-btn Login-btn">
                            LAGRE ENDRINGER
                        </button>
                    </form>
                ) : (
                    <div className="profil-info">
                        <p><strong>Navn:</strong> {user.fornavn} {user.etternavn}</p>
                        <p><strong>E-post:</strong> {user.email}</p>
                        <p><strong>Lokallag:</strong> {user.lokallag}</p>
                    
                

                    <button
                        type="button"
                        className="main-btn login-btn"
                        onClick={() => setRedigerer(true)}
                    >
                        REDIGER PROFIL
                    </button>
                    </div>
                )}

                <button
                    type="button"
                    className="main-btn login-btn"
                    style={{ backgroundColor: '#333', marginTop: '15px' }}
                    onClick={handleLogout}
                >
                    LOGG UT
                </button>

                <div className="register-link">
                    <a href="#" onClick={(e) => {
                        e.preventDefault();
                        setCurrentPage('home');
                    }}>Tilbake til forsiden</a>
                </div>

            </div>
        </main>
    );
}

export default Profil;