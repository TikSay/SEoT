


function KursCard({title, image, description, onPaamelding}) {

    return (
        <div className="cards-grid">
            <div className="card">
                <div className="card-img" style={{backgroundImage: `url('${image}')`}}></div>
                <div className="card-content">
                    <h3>{title}</h3>
                    <p>{description}</p>
                    <div className="card-footer">
                        <button>
                            <div className="info-icon"><i className="fa-solid fa-info"></i></div>
                        </button>
                        <button className="btn-secondary" onClick={()=> onPaamelding(title)}>Til Påmelding</button>
                    </div>
                </div>
            </div>
        </div>

    )
}

export default KursCard
