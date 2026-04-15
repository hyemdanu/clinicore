import React, { useState, useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import { Toast } from "primereact/toast";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import { get, API_BASE_URL } from "../../services/api";
import "./css/ResidentDashboard.css";
import "./css/ResidentDocuments.css";
import "../Shared/css/residents.css";
import "../Shared/css/document-shared.css";


export default function ResidentDocuments() {
  const location = useLocation();
  const toastRef = useRef(null);
  const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
  const [documents, setDocuments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentUserId, setCurrentUserId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [viewerUrl, setViewerUrl] = useState(null);
  const [viewerType, setViewerType] = useState(null);
  const [viewerTitle, setViewerTitle] = useState("");
  const autoOpenedRef = useRef(false);

  const toggleSidebar = () => setSidebarOpen((s) => !s);

  useEffect(() => {
    const storedUser = JSON.parse(localStorage.getItem("currentUser"));
    if (storedUser && storedUser.id) {
      setCurrentUserId(storedUser.id);
    }
  }, []);

  useEffect(() => {
    if (!currentUserId) return;

    async function fetchDocuments() {
      setLoading(true);
      setError(null);
      try {
        const data = await get(`/documents/list?userId=${currentUserId}`);
        setDocuments(data);
      } catch {
        setDocuments([]);
        setError("Failed to load documents");
      } finally {
        setLoading(false);
      }
    }

    fetchDocuments();
  }, [currentUserId]);

  const filteredDocuments = documents.filter((doc) =>
    doc.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // auto-open a document if one was passed via navigation state (e.g. from dashboard recents)
  useEffect(() => {
    const openDocId = location.state?.openDocId;
    if (!openDocId || autoOpenedRef.current || documents.length === 0) return;
    const doc = documents.find((d) => d.id === openDocId);
    if (doc) {
      autoOpenedRef.current = true;
      openDocument(doc);
    }
  }, [documents, location.state]);

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
    <div className="admin-dashboard-container">
      <Toast ref={toastRef} />
      <Header onToggleSidebar={toggleSidebar} title="Documents" />
      <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
        <div className="resident-documents-component">
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

          {loading ? (
            <div className="residents-loading">
              <i className="pi pi-spin pi-spinner"></i>
              <span>Loading documents...</span>
            </div>
          ) : error ? (
            <div className="error-message">{error}</div>
          ) : filteredDocuments.length === 0 ? (
            <div className="no-documents">No documents found</div>
          ) : (
            <div className="residents-list">
              {filteredDocuments.map((doc) => (
                <button
                  key={doc.id}
                  type="button"
                  className="document-row btn-reset"
                  onClick={() => openDocument(doc)}
                >
                  <i className="pi pi-file doc-row-icon"></i>
                  <span className="document-name">{doc.title}</span>
                </button>
              ))}
            </div>
          )}
        </div>
      </main>

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
