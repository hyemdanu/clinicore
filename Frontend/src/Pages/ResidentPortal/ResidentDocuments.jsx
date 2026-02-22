import React, { useState, useEffect } from "react";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import "./css/ResidentDashboard.css";
import "./css/ResidentDocuments.css";

import searchIcon from "../../assets/icons/magnifying-glass.png";
import documentIcon from "../../assets/icons/documentIcon.png";

const API_BASE_URL = "http://localhost:8080/api/documents";

export default function ResidentDocuments() {
  const [sidebarOpen, setSidebarOpen] = useState(true);
  const [documents, setDocuments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [currentUserId, setCurrentUserId] = useState(null);

  // Upload states
  const [selectedFile, setSelectedFile] = useState(null);
  const [docTitle, setDocTitle] = useState("");
  const [docType, setDocType] = useState("");
  const [uploading, setUploading] = useState(false);

  const toggleSidebar = () => setSidebarOpen((s) => !s);

  useEffect(() => {
    const interval = setInterval(() => {
      const storedUser = JSON.parse(localStorage.getItem("currentUser"));

      if (storedUser && storedUser.id) {
        console.log("User loaded:", storedUser.id);
        setCurrentUserId(storedUser.id);
        clearInterval(interval);
      } else {
        console.log("Waiting for user...");
      }
    }, 500);

    return () => clearInterval(interval);
  }, []);

// Fetch documents on mount / when currentUserId changes
  useEffect(() => {
    if (!currentUserId) return;

    async function fetchDocuments() {
      try {
        const response = await fetch(
            `${API_BASE_URL}/list?userId=${currentUserId}` // <-- use "userId"
        );

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.message || "Failed to fetch documents");
        }

        const data = await response.json();
        setDocuments(data);
      } catch (err) {
        console.error("Error fetching documents:", err);
        setDocuments([]);
      }
    }

    fetchDocuments();
  }, [currentUserId]);

// Refresh after upload
  const handleUpload = async () => {
    const storedUser = JSON.parse(localStorage.getItem("currentUser"));

    if (!storedUser || !storedUser.id) {
      alert("User not logged in");
      return;
    }

    if (!selectedFile || !docTitle || !docType) {
      alert("Please select a file and enter title/type");
      return;
    }

    const formData = new FormData();
    formData.append("currentUserId", storedUser.id); // 🔥 REQUIRED
    formData.append("residentId", storedUser.id);
    formData.append("title", docTitle);
    formData.append("type", docType);
    formData.append("file", selectedFile);

    try {
      const response = await fetch(`${API_BASE_URL}/upload`, {
        method: "POST",
        body: formData
      });

      const result = await response.json();
      if (!response.ok) throw new Error(result.message || "Upload failed");

      alert(result.message);

      const listResponse =
          await fetch(`${API_BASE_URL}/list?userId=${storedUser.id}`);

      const data = await listResponse.json();
      setDocuments(data);

      setSelectedFile(null);
      setDocTitle("");
      setDocType("");

    } catch (err) {
      alert("Upload failed: " + err.message);
    }
  };

  // Filter documents by search
  const filteredDocuments = documents.filter((doc) =>
    doc.title.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="admin-dashboard-container">
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
              {filteredDocuments.length === 0 ? (
                <div className="no-documents">No documents found</div>
              ) : (
                filteredDocuments.map((doc) => (
                  <div key={doc.id} className="document-row">
                    <img src={documentIcon} alt="Document" className="document-icon" />
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

            {/* Add button */}
            <button
              className="documents-fab"
              onClick={() => document.getElementById("fileInput").click()}
            >
              +
            </button>

            {/* Upload form */}
            {selectedFile && (
              <div className="upload-form">
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
                <button onClick={handleUpload} disabled={uploading}>
                  {uploading ? "Uploading..." : "Upload"}
                </button>
                <button onClick={() => setSelectedFile(null)}>Cancel</button>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}







