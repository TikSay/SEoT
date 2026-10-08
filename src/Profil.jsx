function Profil({ setCurrentPage, setIsLoggedIn, user, setUser }) {

    const handleLogout = () => {
        setIsLoggedIn(false);
        setUser(null);
        setCurrentPage('home');
    };

    if (!user) return null;

    return (
        <main className="login-main">
            <div className="login-box">
                <h2>Min Profil</h2>
                {user && (
                    <div style={{ marginBottom: '20px', lineHeight: '1.6' }}>
                        {/* Feltene kommer fra result.user i /api/login */}
                        <p><strong>Navn:</strong> {user.fornavn} {user.etternavn}</p>
                        <p><strong>E-post:</strong> {user.email}</p>
                        <p><strong>Lokallag:</strong> {user.lokallag}</p>
                    </div>
                )}

                <button
                    className="main-btn login-btn"
                    style={{ backgroundColor: '#333' }}
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