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
      try {
        const response = await fetch(
            `${API_BASE_URL}/list?userId=${currentUserId}`
        );
        const data = await response.json();
        setDocuments(data);
      } catch (err) {
        console.error("Error fetching documents:", err);
      }
    }

    fetchDocuments();
  }, [currentUserId]);

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
              <div className="documents-search">
                <img src={searchIcon} alt="Search" className="search-icon" />
                <input
                    type="text"
                    placeholder="Search Document Name or Code"
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                />
              </div>

              <div className="documents-box">
                {filteredDocuments.map((doc) => (
                    <div key={doc.id} className="document-row">
                      <img src={documentIcon} alt="Document" className="document-icon" />
                      <span className="document-name">{doc.title}</span>
                    </div>
                ))}
              </div>

              <button className="documents-fab">+</button>
            </div>
          </div>
        </main>
      </div>
  );
}







