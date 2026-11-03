function SisteNytt({ setCurrentPage }) {
    const nyhet = {
        id: 1,
        tittel: "Siste nytt fra Østfold Husflidslag",
        dato: "02.11.2026",
        beskrivelse: "Her kan du lese om de siste nyhetene, arrangementene og aktivitetene hos Østfold Husflidslag.",
        bilde: "/images/nyhet.png"
    };

    return(
        <article className="news-card">
        <img
            src={nyhet.bilde}
            alt={nyhet.tittel}
            className="news-img"
        />

        <div className="news-content">
            <p className="news-date">{nyhet.dato}</p>

            <h3>{nyhet.tittel}</h3>

            <p>{nyhet.beskrivelse}</p>

            <button
                className="btn-secondary"
                onClick={() => setCurrentPage ("nyheter")}
            >
                Les Mer 
            </button>
        </div>
      </article>
    )
}

export default SisteNytt;