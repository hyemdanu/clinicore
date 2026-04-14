import { useState, useEffect, useRef } from "react";
import { Toast } from "primereact/toast";
import "./css/CaregiverDocument.css";
import "../Shared/css/residents.css";
import "../Shared/css/document-shared.css";
import { get, uploadDocument, API_BASE_URL } from "../../services/api";

const ALLOWED_EXTENSIONS = ["pdf", "png", "jpg", "jpeg", "docx", "xlsx"];
const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

export default function CaregiverDocument() {
    const toastRef = useRef(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [assignedResidents, setAssignedResidents] = useState([]);
    const [selectedResident, setSelectedResident] = useState(null);
    const [documents, setDocuments] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    const docCache = useRef({});

    const [selectedFile, setSelectedFile] = useState(null);
    const [docTitle, setDocTitle] = useState("");
    const [uploading, setUploading] = useState(false);
    const [showUploadForm, setShowUploadForm] = useState(false);

    const [viewerUrl, setViewerUrl] = useState(null);
    const [viewerType, setViewerType] = useState(null);
    const [viewerTitle, setViewerTitle] = useState("");

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
            const data = await get(`/caregivers/my-residents?currentUserId=${currentUserId}`);
            setAssignedResidents(data.assigned || []);
        } catch {
            setError("Failed to load assigned residents");
        } finally {
            setLoading(false);
        }
    };

    const fetchDocuments = async (residentId, skipCache = false) => {
        setError(null);

        if (!skipCache && docCache.current[residentId]) {
            setDocuments(docCache.current[residentId]);
            return;
        }

        setLoading(true);
        setDocuments([]);
        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) throw new Error("User not logged in");
            const currentUserId = JSON.parse(currentUserStr).id;
            const response = await get(`/documents/resident/${residentId}?userId=${currentUserId}`);
            const result = response || [];
            docCache.current[residentId] = result;
            setDocuments(result);
        } catch {
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
            fetchDocuments(selectedResident.id, true);
            setSelectedFile(null);
            setDocTitle("");
            setShowUploadForm(false);
        } catch (err) {
            toastRef.current?.show({ severity: "error", summary: "Upload Failed", detail: err.message });
        } finally {
            setUploading(false);
        }
    };

    const filteredDocuments = documents.filter((d) =>
        d.title.toLowerCase().includes(searchTerm.toLowerCase())
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
        <>
            <Toast ref={toastRef} />
                <h2 className="dashboard-title">Documents</h2>
                <div className="search-container">
                    <i className="pi pi-search search-icon"></i>
                    <input
                        type="text"
                        className="search-input"
                        placeholder="Search documents..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                    />
                </div>

                {!selectedResident && (
                    <div className="residents-section-label">Assigned to You</div>
                )}

                    {error && <div className="error-message" role="alert">{error}</div>}
                    {loading && (
                        <div className="residents-loading">
                            <i className="pi pi-spin pi-spinner"></i>
                            <span>Loading...</span>
                        </div>
                    )}

                <div className="residents-list">
                    {!selectedResident
                        ? assignedResidents.map((resident) => (
                            <button
                                key={resident.id}
                                type="button"
                                className="document-row btn-reset"
                                onClick={() => handleResidentClick(resident)}
                            >
                                <div className="resident-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <span className="document-name">
                                    {resident.firstName} {resident.lastName}
                                </span>
                            </button>
                        ))
                        : !loading && !error && (
                            <>
                                <div className="documents-header">
                                    <button className="doc-back-btn" onClick={handleBack} aria-label="Back">
                                        <i className="pi pi-arrow-left"></i>
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
                                            {uploading ? "Uploading..." : "Upload"}
                                        </button>
                                        <button className="cancel-button" onClick={() => setShowUploadForm(false)}>
                                            Cancel
                                        </button>
                                    </div>
                                )}

                                {filteredDocuments.length > 0 ? (
                                    filteredDocuments.map((doc) => (
                                        <button
                                            key={doc.id}
                                            type="button"
                                            className="document-row btn-reset"
                                            onClick={() => openDocument(doc)}
                                        >
                                            <i className="pi pi-file doc-row-icon"></i>
                                            <span className="document-name">{doc.title}</span>
                                        </button>
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
                    <button className="documents-fab" onClick={() => setShowUploadForm(true)}>+</button>
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
        </>
    );
}
