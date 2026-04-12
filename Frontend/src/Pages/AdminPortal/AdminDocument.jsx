import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Toast } from "primereact/toast";
import { get, API_BASE_URL } from "../../services/api";
import "./css/AdminDocument.css";
import "../Shared/css/residents.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";

export default function AdminDocuments({ sidebarOpen }) {
    const navigate = useNavigate();
    const toastRef = useRef(null);
    const [residents, setResidents] = useState([]);
    const [filteredResidents, setFilteredResidents] = useState([]);
    const [selectedResident, setSelectedResident] = useState(null);
    const [documents, setDocuments] = useState([]);
    const [searchQuery, setSearchQuery] = useState("");
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const docCache = useRef({});

    // Upload states
    const [selectedFile, setSelectedFile] = useState(null);
    const [docTitle, setDocTitle] = useState("");
    const [docType, setDocType] = useState("");
    const [uploading, setUploading] = useState(false);
    const [showUploadForm, setShowUploadForm] = useState(false);

    const fetchResidents = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) return navigate("/");
            const currentUser = JSON.parse(currentUserStr);
            // just need names here, no need to hydrate medical data
            const residentsData = await get(`/residents/list?currentUserId=${currentUser.id}`);
            setResidents(residentsData);
        } catch (error) {
            console.error("Error fetching residents:", error);
            setError("Failed to load residents.");
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchResidents();
    }, [fetchResidents]);

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

    const fetchDocuments = async (resident, skipCache = false) => {
        setSelectedResident(resident);
        setError(null);

        if (!skipCache && docCache.current[resident.id]) {
            setDocuments(docCache.current[resident.id]);
            return;
        }

        setLoading(true);
        try {
            const currentUser = JSON.parse(localStorage.getItem("currentUser"));
            const docs = await get(`/documents/resident/${resident.id}?userId=${currentUser.id}`);
            const result = docs || [];
            docCache.current[resident.id] = result;
            setDocuments(result);
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
        if (!selectedResident) {
            toastRef.current?.show({ severity: "warn", summary: "No Resident", detail: "Select a resident first" });
            return;
        }
        if (!selectedFile || !docTitle || !docType) {
            toastRef.current?.show({ severity: "warn", summary: "Missing Fields", detail: "Please select a file and enter title/type" });
            return;
        }

        setUploading(true);

        try {
            const currentUser = JSON.parse(localStorage.getItem("currentUser"));
            const formData = new FormData();
            formData.append("currentUserId", currentUser.id);
            formData.append("residentId", selectedResident.id);
            formData.append("title", docTitle);
            formData.append("type", docType);
            formData.append("file", selectedFile);

            const response = await fetch(`${API_BASE_URL}/documents/upload`, {
                method: "POST",
                body: formData,
            });

            const result = await response.json();
            if (!response.ok) {
                toastRef.current?.show({ severity: "error", summary: "Upload Failed", detail: result.message || "Upload failed" });
                return;
            }

            toastRef.current?.show({ severity: "success", summary: "Success", detail: result.message });

            // refresh documents (skip cache to get fresh data after upload)
            fetchDocuments(selectedResident, true);

            // reset upload form
            setSelectedFile(null);
            setDocTitle("");
            setDocType("");
            setShowUploadForm(false);
        } catch (err) {
            toastRef.current?.show({ severity: "error", summary: "Upload Failed", detail: err.message });
            console.error(err);
        } finally {
            setUploading(false);
        }
    };

    const filteredDocuments = documents.filter(doc =>
        (doc.title || "").toLowerCase().includes(searchQuery.toLowerCase())
    );

    const openDocument = (docId) => {
        const currentUser = JSON.parse(localStorage.getItem("currentUser"));
        const url = `${API_BASE_URL}/documents/file/${docId}?userId=${currentUser.id}`;
        window.open(url, "_blank");
    };

    // Keyboard handlers for accessibility
    const handleResidentKeyDown = (e, resident) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            fetchDocuments(resident);
        }
    };

    const handleDocumentKeyDown = (e, docId) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            openDocument(docId);
        }
    };

    return (
        <div className="dashboard-container">
            <Toast ref={toastRef} />
            <main
                className={`admin-documents-content ${
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
                    {error && <div className="error-message">{error}</div>}

                    {loading && (
                        <div className="residents-loading">
                            <i className="pi pi-spin pi-spinner"></i>
                            <span>Loading...</span>
                        </div>
                    )}

                    {!selectedResident
                        ? filteredResidents.map((resident) => (
                            <button
                                key={resident.id}
                                className="document-row btn-reset"
                                onClick={() => fetchDocuments(resident)}
                                onKeyDown={(e) => handleResidentKeyDown(e, resident)}
                                type="button"
                            >
                                <div className="resident-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <span className="document-name">
                                    {resident.firstName} {resident.lastName}
                                </span>
                            </button>
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
                                        <button
                                            key={doc.id}
                                            className="document-row btn-reset"
                                            onClick={() => openDocument(doc.id)}
                                            onKeyDown={(e) => handleDocumentKeyDown(e, doc.id)}
                                            type="button"
                                        >
                                            <i className="pi pi-file document-icon"></i>
                                            <span className="document-name">
                                                {doc.title}
                                            </span>
                                        </button>
                                    ))
                                ) : (
                                    <div className="document-row">
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
                        aria-label="Add new document"
                    >
                        +
                    </button>
                )}
            </main>
        </div>
    );
}