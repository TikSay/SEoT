import { useState } from 'react';

function GlemtPassord({setCurrentPage}) {
    const [step, setStep] = useState('email');
    const [email, setEmail] = useState('');
    const [code, setCode] = useState('');
    const [newPassword, setNewPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    const handleRequestCode = async (e) => {
        e.preventDefault();
        const response = await fetch('/api/forgot-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email }),
        });
        const result = await response.json();
        alert(result.message);
        if (result.success) {
            setStep('code');
        }
    };

    const handleResetPassword = async (e) => {
        e.preventDefault();
        if (newPassword !== confirmPassword) {
            alert("Passordet stemmer ikke");
            return;
        }
        const response = await fetch('/api/reset-password', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, code, newPassword }),
        });
        const result = await response.json();
        if (result.success) {
            alert('Passordet er endret. Logg inn med det nye passordet.');
            setCurrentPage('login');
        } else {
            alert(result.message);
        }
    };

    return (
        <main className="login-main">
            <div className="login-box">
                <h2>Glemt passord</h2>

                {step === 'email' && (
                    <form onSubmit={handleRequestCode}>
                        <div className="input-group">
                            <label htmlFor="email">E-postadresse</label>
                            <input type="email" id="email" value={email}
                                   onChange={(e) => setEmail(e.target.value)} required />
                        </div>
                        <button type="submit" className="main-btn login-btn">SEND KODE</button>
                    </form>
                )}

                {step === 'code' && (
                    <form onSubmit={handleResetPassword}>
                        <div className="input-group">
                            <label htmlFor="code">Kode</label>
                            <input type="text" id="code" inputMode="numeric" maxLength={6} value={code}
                                   onChange={(e) => setCode(e.target.value)} required />
                        </div>
                        <div className="input-group">
                            <label htmlFor="newPassword">Nytt passord</label>
                            <input type="password" id="newPassword" value={newPassword}
                                   onChange={(e) => setNewPassword(e.target.value)} required />
                        </div>
                        <div className="input-group">
                            <label htmlFor="confirmPassword">Bekreft nytt passord</label>
                            <input type="password" id="confirmPassword" value={confirmPassword}
                                   onChange={(e) => setConfirmPassword(e.target.value)} required />
                        </div>
                        <button type="submit" className="main-btn login-btn">ENDRE PASSORD</button>
                    </form>
                )}

                <div className="register-link">
                    <a href="#" onClick={(e) => {e.preventDefault(); setCurrentPage('login');}}>Tilbake til innlogging</a>
                </div>
            </div>
        </main>
    );
}

export default GlemtPassord;
