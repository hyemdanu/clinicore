import React, { useState, useEffect, useRef } from "react";
import { Toast } from "primereact/toast";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import { get, API_BASE_URL } from "../../services/api";
import "./css/ResidentDashboard.css";
import "./css/ResidentDocuments.css";

import searchIcon from "../../assets/icons/magnifying-glass.png";

export default function ResidentDocuments() {
  const toastRef = useRef(null);
  const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
  const [documents, setDocuments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentUserId, setCurrentUserId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Upload states
  const [selectedFile, setSelectedFile] = useState(null);
  const [docTitle, setDocTitle] = useState("");
  const [docType, setDocType] = useState("");
  const [uploading, setUploading] = useState(false);

  const toggleSidebar = () => setSidebarOpen((s) => !s);

  useEffect(() => {
    const storedUser = JSON.parse(localStorage.getItem("currentUser"));
    if (storedUser && storedUser.id) {
      setCurrentUserId(storedUser.id);
    }
  }, []);

// Fetch documents on mount / when currentUserId changes
  useEffect(() => {
    if (!currentUserId) return;

    async function fetchDocuments() {
      setLoading(true);
      setError(null);
      try {
        const data = await get(`/documents/list?userId=${currentUserId}`);
        setDocuments(data);
      } catch (err) {
        console.error("Error fetching documents:", err);
        setDocuments([]);
        setError("Failed to load documents");
      } finally {
        setLoading(false);
      }
    }

    fetchDocuments();
  }, [currentUserId]);

// Refresh after upload
  const handleUpload = async () => {
    const storedUser = JSON.parse(localStorage.getItem("currentUser"));

    if (!storedUser || !storedUser.id) {
      toastRef.current?.show({ severity: "error", summary: "Error", detail: "User not logged in" });
      return;
    }

    if (!selectedFile || !docTitle || !docType) {
      toastRef.current?.show({ severity: "warn", summary: "Missing Fields", detail: "Please select a file and enter title/type" });
      return;
    }

    const formData = new FormData();
    formData.append("currentUserId", storedUser.id);
    formData.append("residentId", storedUser.id);
    formData.append("title", docTitle);
    formData.append("type", docType);
    formData.append("file", selectedFile);

    setUploading(true);
    try {
      const response = await fetch(`${API_BASE_URL}/documents/upload`, {
        method: "POST",
        credentials: "include",
        body: formData
      });

      const result = await response.json();
      if (!response.ok) {
        toastRef.current?.show({ severity: "error", summary: "Upload Failed", detail: result.message || "Unknown error" });
        return;
      }

      toastRef.current?.show({ severity: "success", summary: "Success", detail: result.message });

      // refresh document list after upload
      const data = await get(`/documents/list?userId=${storedUser.id}`);
      setDocuments(data);

      setSelectedFile(null);
      setDocTitle("");
      setDocType("");

    } catch (err) {
      toastRef.current?.show({ severity: "error", summary: "Upload Failed", detail: err.message });
    } finally {
      setUploading(false);
    }
  };

  // Filter documents by search
  const filteredDocuments = documents.filter((doc) =>
    doc.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="admin-dashboard-container">
      <Toast ref={toastRef} />
      <Header onToggleSidebar={toggleSidebar} title="Documents" />
      <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

      <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
        <div className="resident-documents-component">
          <h2 className="dashboard-title">Documents</h2>

          <div className="inventory-section">
            {/* Search */}
            <div className="documents-search">
              <img src={searchIcon} alt="Search" className="search-icon" />
              <input
                type="text"
                placeholder="Search Document Name or Code"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
              />
            </div>

            {/* Document list */}
            <div className="documents-box">
              {loading ? (
                <div className="documents-loading">
                  <i className="pi pi-spin pi-spinner"></i>
                  <span>Loading...</span>
                </div>
              ) : error ? (
                <div className="no-documents">{error}</div>
              ) : filteredDocuments.length === 0 ? (
                <div className="no-documents">No documents found</div>
              ) : (
                filteredDocuments.map((doc) => (
                  <div key={doc.id} className="document-row">
                    <i
                        className="pi pi-file"
                        style={{ fontSize: "16px", marginRight: "12px" }}
                    ></i>
                    <span className="document-name">{doc.title}</span>
                  </div>
                ))
              )}
            </div>

            {/* Hidden file input */}
            <input
              type="file"
              id="fileInput"
              style={{ display: "none" }}
              onChange={(e) => setSelectedFile(e.target.files[0])}
            />
          </div>
        </div>
      </main>
    </div>
  );
}







