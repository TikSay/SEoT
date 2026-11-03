


function KursCard({title, image, tags, onPaamelding}) {
    return (
        <div className="course-card" onClick={onPaamelding}>

            <div className="course-img-container" style={{backgroundImage: `url('${image}')`}}>
                <div className="course-tags">
                    {/* Henter tags fra kursobjektet og viser dem */}
                    {tags && tags.map((tag, index) => (
                        <span key={index} className="course-tag">{tag}</span>
                    ))}
                </div>
            </div>

            <div className="course-content">
                <h3>{title}</h3>
            </div>

        </div>
    )
}

export default KursCard;
