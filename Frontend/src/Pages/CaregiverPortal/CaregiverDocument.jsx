import React, { useState } from "react";
import "./css/CaregiverDocument.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";
import userIcon from "../../assets/icons/userIcon.png";

const Users = [
    { id: 1, name: "User 1" },
    { id: 2, name: "User 2" },
    { id: 3, name: "User 3" },
    { id: 4, name: "User 4" },
    { id: 5, name: "User 5" },
    { id: 6, name: "User 6" },
];

export default function CaregiverDocument({ sidebarOpen }) {
    const [searchTerm, setSearchTerm] = useState("");

    const filteredUsers = Users.filter((u) =>
        u.name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="dashboard-container">
            <main className={`main-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
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

                {/* Document List */}
                <div className="documents-box">
                    {filteredUsers.map((doc) => (
                        <div key={doc.id} className="document-row">
                            <img src={userIcon} alt="Document" className="document-icon" />
                            <span className="document-name">{doc.name}</span>
                        </div>
                    ))}
                </div>

                {/* Floating Action Button */}
                <button className="documents-fab">+</button>
            </main>
        </div>
    );
}