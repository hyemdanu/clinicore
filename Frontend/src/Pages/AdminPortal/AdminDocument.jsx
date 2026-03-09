import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";
import "./css/AdminDocument.css";
import "../Shared/css/residents.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";

export default function AdminDocuments({ sidebarOpen }) {
    const navigate = useNavigate();
    const [residents, setResidents] = useState([]);
    const [filteredResidents, setFilteredResidents] = useState([]);
    const [selectedResident, setSelectedResident] = useState(null);
    const [documents, setDocuments] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Upload states
    const [selectedFile, setSelectedFile] = useState(null);
    const [docTitle, setDocTitle] = useState("");
    const [docType, setDocType] = useState("");
    const [uploading, setUploading] = useState(false);
    const [showUploadForm, setShowUploadForm] = useState(false);

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
        setShowUploadForm(false);
    };

    const handleUpload = async () => {
        if (!selectedResident) return alert("Select a resident first");
        if (!selectedFile || !docTitle || !docType)
            return alert("Please select a file and enter title/type");

        setUploading(true);

        try {
            const currentUser = JSON.parse(localStorage.getItem("currentUser"));
            const formData = new FormData();
            formData.append("currentUserId", currentUser.id);
            formData.append("residentId", selectedResident.id);
            formData.append("title", docTitle);
            formData.append("type", docType);
            formData.append("file", selectedFile);

            const response = await fetch(`http://localhost:8080/api/documents/upload`, {
                method: "POST",
                body: formData,
            });

            const result = await response.json();
            if (!response.ok) throw new Error(result.message || "Upload failed");

            alert(result.message);

            // refresh documents
            fetchDocuments(selectedResident);

            // reset upload form
            setSelectedFile(null);
            setDocTitle("");
            setDocType("");
            setShowUploadForm(false);
        } catch (err) {
            alert("Upload failed: " + err.message);
            console.error(err);
        } finally {
            setUploading(false);
        }
    };

    const filteredDocuments = documents.filter(doc =>
        (doc.title || "").toLowerCase().includes(searchQuery.toLowerCase())
    );

    return (
        <div className="dashboard-container">
            <main
                className={`main-content ${
                    sidebarOpen ? "content-with-sidebar" : ""
                }`}
            >
                <h2 className="dashboard-title">Documents</h2>
                {/* Search */}
                <div className="documents-search">
                    <img src={searchIcon} alt="Search" className="search-icon" />
                    <input
                        type="text"
                        placeholder="Search Document Name or Code"
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                    />
                </div>

                {!selectedResident && (
                    <div className="residents-section-label">
                        All Residents
                    </div>
                )}

                <div className="documents-box">
                    {error && <div style={{ color: "red" }}>{error}</div>}

                    {loading && (
                        <div className="residents-loading">
                            <i className="pi pi-spin pi-spinner"></i>
                            <span>Loading...</span>
                        </div>
                    )}

                    {!selectedResident
                        ? filteredResidents.map((resident) => (
                            <div
                                key={resident.id}
                                className="document-row"
                                onClick={() => fetchDocuments(resident)}
                                style={{ cursor: "pointer" }}
                            >
                                <div className="resident-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <span className="document-name">
                                    {resident.firstName} {resident.lastName}
                                </span>
                            </div>
                        ))
                        : !loading &&
                        !error && (
                            <>
                                <div className="documents-header">
                                    <button className="back-button" onClick={goBack} aria-label="Back">
                                        <span className="back-arrow">←</span>
                                    </button>
                                    <div className="resident-documents-title">
                                        {selectedResident.firstName} {selectedResident.lastName}
                                    </div>
                                </div>

                                {/* Upload form (only shows after clicking +) */}
                                {showUploadForm && (
                                    <div className="upload-section">
                                        <input type="file" onChange={(e) => setSelectedFile(e.target.files[0])} />
                                        <input
                                            type="text"
                                            placeholder="Document Title"
                                            value={docTitle}
                                            onChange={(e) => setDocTitle(e.target.value)}
                                        />
                                        <input
                                            type="text"
                                            placeholder="Document Type"
                                            value={docType}
                                            onChange={(e) => setDocType(e.target.value)}
                                        />
                                        <button className="upload-button" onClick={handleUpload} disabled={uploading}>
                                            <span>{uploading ? "Uploading..." : "Upload"}</span>
                                        </button>
                                        <button className="cancel-button" onClick={() => setShowUploadForm(false)}>
                                            <span>Cancel</span>
                                        </button>
                                    </div>
                                )}

                                {filteredDocuments.length > 0 ? (
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
                                )}
                            </>
                        )}
                </div>

                {selectedResident && (
                    <button
                        className="documents-fab"
                        onClick={() => setShowUploadForm(true)}
                        title="Add Document"
                    >
                        +
                    </button>
                )}
            </main>
        </div>
    );
}
