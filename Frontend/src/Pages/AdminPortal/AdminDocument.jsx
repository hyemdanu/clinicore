import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";
import "./css/AdminDocument.css";
import "../Shared/css/residents.css";

export default function AdminDocuments({ sidebarOpen }) {
    const navigate = useNavigate();
    const [residents, setResidents] = useState([]);
    const [filteredResidents, setFilteredResidents] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchResidents();
    }, []);

    useEffect(() => {
        let filtered = [...residents];
        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(resident =>
                `${resident.firstName} ${resident.lastName}`.toLowerCase().includes(query)
            );
        }
        setFilteredResidents(filtered);
    }, [residents, searchQuery]);

    const fetchResidents = async () => {
        setLoading(true);
        setError(null);
        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) return navigate("/");
            const currentUser = JSON.parse(currentUserStr);
            const residentsData = await get(`/residents/full?currentUserId=${currentUser.id}`);
            setResidents(residentsData);
        } catch (error) {
            console.error("Error fetching residents:", error);
            setError("Failed to load residents.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="documents-page">
            <div className="search-container">
                <i className="pi pi-search search-icon"></i>
                <input
                    type="text"
                    className="search-input"
                    placeholder="Search residents..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </div>

            {error && <div className="error-message">{error}</div>}

            {loading ? (
                <div className="residents-loading">
                    <i className="pi pi-spin pi-spinner"></i>
                    <span>Loading...</span>
                </div>
            ) : (
                <div className="residents-list">
                    {filteredResidents.length === 0 ? (
                        <div className="no-residents">No residents found</div>
                    ) : (
                        filteredResidents.map(resident => (
                            <div key={resident.id} className="resident-item">
                                <div className="resident-avatar"><i className="pi pi-folder"></i></div>
                                <span className="resident-name">
                                    {resident.firstName} {resident.lastName}
                                </span>
                            </div>
                        ))
                    )}
                </div>
            )}
        </div>
    );
}