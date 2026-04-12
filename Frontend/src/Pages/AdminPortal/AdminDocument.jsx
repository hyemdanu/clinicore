import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Toast } from "primereact/toast";
import { get, uploadDocument, deleteDocument, API_BASE_URL } from "../../services/api";
import "./css/AdminDocument.css";
import "../Shared/css/residents.css";
import "../Shared/css/document-shared.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";

const ALLOWED_EXTENSIONS = ["pdf", "png", "jpg", "jpeg", "docx", "xlsx"];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

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

    const [selectedFile, setSelectedFile] = useState(null);
    const [docTitle, setDocTitle] = useState("");
    const [uploading, setUploading] = useState(false);
    const [showUploadForm, setShowUploadForm] = useState(false);

    const [viewerUrl, setViewerUrl] = useState(null);
    const [viewerType, setViewerType] = useState(null);
    const [viewerTitle, setViewerTitle] = useState("");
    const [confirmDelete, setConfirmDelete] = useState(null);

    const fetchResidents = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) return navigate("/");
            const currentUser = JSON.parse(currentUserStr);
            const residentsData = await get(`/residents/list?currentUserId=${currentUser.id}`);
            setResidents(residentsData);
        } catch {
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
        } catch {
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

    const validateFile = (file) => {
        if (!file) return "Please select a file";
        const ext = file.name.split(".").pop()?.toLowerCase();
        if (!ALLOWED_EXTENSIONS.includes(ext)) {
            return "File type not allowed. Accepted: PDF, PNG, JPG, DOCX, XLSX";
        }
        if (file.size > MAX_FILE_SIZE) {
            return "File size exceeds 10MB limit";
        }
        return null;
    };

    const handleUpload = async () => {
        if (!selectedResident) {
            toastRef.current?.show({ severity: "warn", summary: "No Resident", detail: "Select a resident first" });
            return;
        }
        if (!docTitle.trim()) {
            toastRef.current?.show({ severity: "warn", summary: "Missing Title", detail: "Please enter a document title" });
            return;
        }
        const fileError = validateFile(selectedFile);
        if (fileError) {
            toastRef.current?.show({ severity: "warn", summary: "Invalid File", detail: fileError });
            return;
        }

        setUploading(true);
        try {
            const result = await uploadDocument(selectedResident.id, docTitle.trim(), selectedFile);
            toastRef.current?.show({ severity: "success", summary: "Success", detail: result.message });
            fetchDocuments(selectedResident, true);
            setSelectedFile(null);
            setDocTitle("");
            setShowUploadForm(false);
        } catch (err) {
            toastRef.current?.show({ severity: "error", summary: "Upload Failed", detail: err.message });
        } finally {
            setUploading(false);
        }
    };

    const handleDelete = async (doc) => {
        try {
            const result = await deleteDocument(doc.id);
            toastRef.current?.show({ severity: "success", summary: "Deleted", detail: result.message });
            setConfirmDelete(null);
            fetchDocuments(selectedResident, true);
        } catch (err) {
            toastRef.current?.show({ severity: "error", summary: "Delete Failed", detail: err.message });
            setConfirmDelete(null);
        }
    };

    const filteredDocuments = documents.filter(doc =>
        (doc.title || "").toLowerCase().includes(searchQuery.toLowerCase())
    );

    const detectMime = (bytes) => {
        if (bytes[0] === 0x25 && bytes[1] === 0x50 && bytes[2] === 0x44 && bytes[3] === 0x46) return "application/pdf";
        if (bytes[0] === 0x89 && bytes[1] === 0x50 && bytes[2] === 0x4E && bytes[3] === 0x47) return "image/png";
        if (bytes[0] === 0xFF && bytes[1] === 0xD8 && bytes[2] === 0xFF) return "image/jpeg";
        if (bytes[0] === 0x50 && bytes[1] === 0x4B) return "application/zip";
        return "application/pdf";
    };

    const openDocument = async (doc) => {
        try {
            const currentUser = JSON.parse(localStorage.getItem("currentUser"));
            const response = await fetch(`${API_BASE_URL}/documents/file/${doc.id}?userId=${currentUser.id}`, {
                headers: { Authorization: `Bearer ${currentUser.token}` },
            });
            if (!response.ok) throw new Error("Failed to load document");
            const buffer = await response.arrayBuffer();
            const mime = detectMime(new Uint8Array(buffer));

            // docx/xlsx can't render in browser — download instead
            if (mime === "application/zip") {
                const blob = new Blob([buffer]);
                const a = document.createElement("a");
                a.href = URL.createObjectURL(blob);
                a.download = doc.title;
                a.click();
                URL.revokeObjectURL(a.href);
                return;
            }

            const blob = new Blob([buffer], { type: mime });
            setViewerType(mime);
            setViewerTitle(doc.title);
            setViewerUrl(URL.createObjectURL(blob));
        } catch {
            toastRef.current?.show({ severity: "error", summary: "Error", detail: "Could not open document" });
        }
    };

    const closeViewer = () => {
        if (viewerUrl) URL.revokeObjectURL(viewerUrl);
        setViewerUrl(null);
        setViewerType(null);
        setViewerTitle("");
    };

    useEffect(() => {
        const handleEscape = (e) => { if (e.key === "Escape") closeViewer(); };
        if (viewerUrl) window.addEventListener("keydown", handleEscape);
        return () => window.removeEventListener("keydown", handleEscape);
    }, [viewerUrl]);

    return (
        <div className="dashboard-container">
            <Toast ref={toastRef} />
            <main
                className={`admin-documents-content ${
                    sidebarOpen ? "content-with-sidebar" : ""
                }`}
            >
                <h2 className="dashboard-title">Documents</h2>
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
                    <div className="residents-section-label">All Residents</div>
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
                        : !loading && !error && (
                            <>
                                <div className="documents-header">
                                    <button className="back-button" onClick={goBack} aria-label="Back">
                                        <span className="back-arrow">&larr;</span>
                                    </button>
                                    <div className="resident-documents-title">
                                        {selectedResident.firstName} {selectedResident.lastName}
                                    </div>
                                </div>

                                {showUploadForm && (
                                    <div className="upload-section">
                                        <input
                                            type="file"
                                            accept=".pdf,.png,.jpg,.jpeg,.docx,.xlsx"
                                            onChange={(e) => setSelectedFile(e.target.files[0])}
                                        />
                                        <input
                                            type="text"
                                            placeholder="Document Title"
                                            value={docTitle}
                                            onChange={(e) => setDocTitle(e.target.value)}
                                        />
                                        <span className="upload-hint">PDF, PNG, JPG, DOCX, XLSX — Max 10MB</span>
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
                                        <div key={doc.id} className="document-row">
                                            <i className="pi pi-file" style={{ fontSize: "16px", marginRight: "12px" }}></i>
                                            <span
                                                className="document-name"
                                                style={{ cursor: "pointer", flex: 1 }}
                                                onClick={() => openDocument(doc)}
                                            >
                                                {doc.title}
                                            </span>
                                            <button
                                                className="document-delete-btn"
                                                onClick={(e) => { e.stopPropagation(); setConfirmDelete(doc); }}
                                                title="Delete document"
                                            >
                                                <i className="pi pi-trash"></i>
                                            </button>
                                        </div>
                                    ))
                                ) : (
                                    <div className="no-documents">
                                        No documents found for {selectedResident.firstName}.
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

            {confirmDelete && (
                <div className="document-viewer-overlay" onClick={() => setConfirmDelete(null)}>
                    <div className="confirm-dialog" onClick={(e) => e.stopPropagation()}>
                        <p>Delete "<strong>{confirmDelete.title}</strong>"?</p>
                        <div className="confirm-dialog-actions">
                            <button className="cancel-button" onClick={() => setConfirmDelete(null)}>Cancel</button>
                            <button className="delete-confirm-button" onClick={() => handleDelete(confirmDelete)}>Delete</button>
                        </div>
                    </div>
                </div>
            )}

            {viewerUrl && (
                <div className="document-viewer-overlay" onClick={closeViewer}>
                    <div className="document-viewer-container" onClick={(e) => e.stopPropagation()}>
                        <div className="document-viewer-header">
                            <span className="document-viewer-title">{viewerTitle}</span>
                            <div className="document-viewer-actions">
                                <a href={viewerUrl} download={viewerTitle} className="document-viewer-download" title="Download">
                                    <i className="pi pi-download"></i>
                                </a>
                                <button className="document-viewer-close" onClick={closeViewer}>&#10005;</button>
                            </div>
                        </div>
                        <div className="document-viewer-body">
                            {viewerType?.startsWith("image/") ? (
                                <img src={viewerUrl} alt="Document" className="document-viewer-image" />
                            ) : (
                                <iframe src={viewerUrl} title="Document" className="document-viewer-iframe" />
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
