import { useState, useEffect } from 'react';

function Profil({ setCurrentPage, setIsLoggedIn, userId }) {
    const [userData, setUserData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // Navngi funksjonen logisk. Den henter brukerdata, ikke databasen.
        const fetchUserData = async () => {
            try {
                //*Bytt dette med din local host ip*//
                const apiEndpoint = `http://localhost:0000/api/bruker/${userId}`;

                const response = await fetch(apiEndpoint);


                if (!response.ok) {
                    throw new Error(`Klarte ikke å hente data. Status: ${response.status}`);
                }

                const data = await response.json();
                setUserData(data);

            } catch (error) {
                console.error("Din bruker finnes ikke", error);
            } finally {
                // Skru alltid av loading, uansett om det gikk bra eller dårlig
                setLoading(false);
            }
        };

        fetchUserData();

        // Kjør funksjonen, men sjekk at vi faktisk har en userId først
        if (userId) {
            console.error("Mangler userId for å hente profil.");
            setTimeout(() => setLoading(false), 0 );
            return;

        }

    }, [userId]);

    const handleLogout = () => {
        setIsLoggedIn(false);
        setCurrentPage('home');
    };

    if (loading) {
        return (
            <main className="login-main">
                <div className="login-box">
                    <h2>Henter profilen din...</h2>
                </div>
            </main>
        );
    }

    return (
        <main className="login-main">
            <div className="login-box">
                <h2>Min Profil</h2>
                {userData && (
                    <div style={{ marginBottom: '20px', lineHeight: '1.6' }}>
                        {/* Husk å endre .navn, .epost osv. til NØYAKTIG
                            det kolonnene i databasen/API-et ditt heter! */}
                        <p><strong>Navn:</strong> {userData.navn}</p>
                        <p><strong>E-post:</strong> {userData.epost}</p>
                        <p><strong>Lokallag:</strong> {userData.lokallag}</p>
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