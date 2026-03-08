import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";
import "./css/AdminDocument.css";
import "../Shared/css/residents.css";

export default function AdminDocuments({ sidebarOpen }) {
    const navigate = useNavigate();
    const [residents, setResidents] = useState([]);
    const [filteredResidents, setFilteredResidents] = useState([]);
    const [selectedResident, setSelectedResident] = useState(null);
    const [documents, setDocuments] = useState([]);
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

    const fetchDocuments = async (resident) => {
        setSelectedResident(resident);
        setLoading(true);
        setError(null);
        try {
            const currentUser = JSON.parse(localStorage.getItem("currentUser"));
            const docs = await get(`/documents/resident/${resident.id}?userId=${currentUser.id}`);
            setDocuments(docs || []);
        } catch (error) {
            console.error("Error fetching documents:", error);
            setError("Failed to load documents.");
        } finally {
            setLoading(false);
        }
    };

    const goBack = () => {
        setSelectedResident(null);
        setDocuments([]);
        setSearchQuery("");
    };

    const filteredDocuments = documents.filter(doc =>
        (doc.title || "").toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="documents-page">

            {/* Main page title */}
            <h2 className="dashboard-title">Documents</h2>

            <div className="search-container">
                <i className="pi pi-search search-icon"></i>
                <input
                    type="text"
                    className="search-input"
                    placeholder={selectedResident ? "Search documents..." : "Search residents..."}
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

                    {!selectedResident ? (
                        filteredResidents.map(resident => (
                            <div
                                key={resident.id}
                                className="resident-item"
                                onClick={() => fetchDocuments(resident)}
                            >
                                <div className="resident-avatar"><i className="pi pi-folder"></i></div>
                                <span className="resident-name">
                                    {resident.firstName} {resident.lastName}
                                </span>
                            </div>
                        ))
                    ) : (
                        <>
                            <div className="documents-header">
                                <h3>{selectedResident.firstName}'s Documents</h3>
                                <button className="back-button" onClick={goBack}>← Back</button>
                            </div>

                            {filteredDocuments.length === 0 ? (
                                <div className="no-residents">No documents found</div>
                            ) : (
                                filteredDocuments.map(doc => (
                                    <div key={doc.id} className="resident-item">
                                        <div className="resident-avatar"><i className="pi pi-file"></i></div>
                                        <span className="resident-name">{doc.title}</span>
                                    </div>
                                ))
                            )}
                        </>
                    )}

                </div>
            )}
        </div>
    );
}