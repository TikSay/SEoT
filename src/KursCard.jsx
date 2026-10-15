


function KursCard() {

    return (
        <div className="cards-grid">
            <div className="card">
                <div className="card-img" style={{backgroundImage: "url('https://placehold.co/280x180')"}}></div>
                <div className="card-content">
                    <h3>Sytreff i Råde</h3>
                    <p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut
                        labore et dolore magna aliqua</p>
                    <div className="card-footer">
                        <button>
                            <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                        </button>
                        <button className="btn-secondary">Til Påmelding</button>
                    </div>
                </div>
            </div>
        </div>

    )
}

export default KursCard
