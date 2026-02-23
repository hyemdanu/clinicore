import React, { useState, useEffect } from "react";
import "./css/CaregiverDocument.css";
import "../Shared/css/residents.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";
import { get } from "../../services/api";

export default function CaregiverDocument({ sidebarOpen }) {
    const [searchTerm, setSearchTerm] = useState("");
    const [assignedResidents, setAssignedResidents] = useState([]);
    const [selectedResident, setSelectedResident] = useState(null);
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchAssignedResidents();
    }, []);

    const fetchAssignedResidents = async () => {
        setLoading(true);
        setError(null);

        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) return;

            const currentUserId = JSON.parse(currentUserStr).id;

            const data = await get(
                `/caregivers/my-residents?currentUserId=${currentUserId}`
            );

            setAssignedResidents(data.assigned || []);
        } catch (err) {
            console.error(err);
            setError("Failed to load assigned residents");
        } finally {
            setLoading(false);
        }
    };

    const fetchDocuments = async (residentId) => {
        setLoading(true);
        setError(null);
        setDocuments([]);

        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) throw new Error("User not logged in");

            const currentUserId = JSON.parse(currentUserStr).id;

            const response = await get(
                `/documents/resident/${residentId}?userId=${currentUserId}`
            );

            console.log("Documents from backend:", response);

            setDocuments(response || []);
        } catch (err) {
            console.error("Error fetching documents:", err);
            setError("Failed to load documents");
        } finally {
            setLoading(false);
        }
    };

    const handleResidentClick = (resident) => {
        setSelectedResident(resident);
        fetchDocuments(resident.id);
    };

    const handleBack = () => {
        setSelectedResident(null);
        setDocuments([]);
        setError(null);
    };

    const filteredDocuments = documents.filter((d) =>
        d.title.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="dashboard-container">
            <main
                className={`main-content ${
                    sidebarOpen ? "content-with-sidebar" : ""
                }`}
            >
                <h2 className="dashboard-title">Documents</h2>
                {/* Search Bar */}
                <div className="documents-search">
                    <img src={searchIcon} alt="Search" className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search Document Name or Code"
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {!selectedResident && (
                    <div className="residents-section-label">
                        Assigned to You
                    </div>
                )}
                {/* Document List */}
                <div className="documents-box">
                    {error && <div style={{ color: "red" }}>{error}</div>}
                    {loading && <div>Loading...</div>}

                    {!selectedResident
                        ? assignedResidents.map((resident) => (
                            <div
                                key={resident.id}
                                className="document-row"
                                onClick={() =>
                                    handleResidentClick(resident)
                                }
                                style={{ cursor: "pointer" }}
                            >
                                <div className="resident-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <span className="document-name">
                                      {resident.firstName}{" "}
                                    {resident.lastName}
                                  </span>
                            </div>
                        ))
                        : !loading &&
                        !error &&
                        (filteredDocuments.length > 0 ? (
                            filteredDocuments.map((doc) => (
                                <div
                                    key={doc.id}
                                    className="document-row"
                                >
                                    <i
                                        className="pi pi-file"
                                        style={{
                                            fontSize: "16px",
                                            marginRight: "12px",
                                        }}
                                    ></i>
                                    <span className="document-name">
                                          {doc.title}
                                      </span>
                                </div>
                            ))
                        ) : (
                            <div>
                                No documents found for{" "}
                                {selectedResident.firstName}.
                            </div>
                        ))}
                </div>
                {/* Floating Action Button */}
                <button className="documents-fab">+</button>

                {selectedResident && (
                    <div
                        style={{
                            marginTop: "12px",
                            cursor: "pointer",
                            color: "#369DF7",
                        }}
                        onClick={handleBack}
                    >
                        ← Back to Residents
                    </div>
                )}
            </main>
        </div>
    );
}