import React, { useState } from "react";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import "./css/ResidentDashboard.css";
import "./css/ResidentDocuments.css";

import searchIcon from "../../assets/icons/magnifying-glass.png";
import documentIcon from "../../assets/icons/documentIcon.png";

const documents = [
    "Insurance Form",
    "Medical History",
    "Consent Form",
    "Care Plan",
    "Emergency Contacts",
    "Billing Statement",
];

export default function ResidentDocuments() {
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const toggleSidebar = () => setSidebarOpen((s) => !s);

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
                            />
                        </div>

                        <div className="documents-box">
                            {documents.map((doc, index) => (
                                <div key={index} className="document-row">
                                    <img
                                        src={documentIcon}
                                        alt="Document"
                                        className="document-icon"
                                    />
                                    <span className="document-name">{doc}</span>
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







