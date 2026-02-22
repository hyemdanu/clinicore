import React from "react";
import "./css/CaregiverDocument.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";
import userIcon from "../../assets/icons/userIcon.png";

const Users = [
    "User 1",
    "User 2",
    "User 3",
    "User 4",
    "User 5",
    "User 6",
];

export default function CaregiverDocument() {
    return (
        <div className="caregiver-documents-page">
            <div className="box">
                <div className="rectangle">
                    <div className="caregiver-documents-title">
                        Documents
                    </div>
                </div>

                <section className="documents-section">
                    {/* Search bar */}
                    <div className="documents-search">
                        <img
                            src={searchIcon}
                            alt="Search"
                            className="search-icon"
                        />
                        <input
                            type="text"
                            placeholder="Search Document Name or Code"
                        />
                    </div>

                    {/* Document list */}
                    <div className="documents-box">
                        {Users.map((doc, index) => (
                            <React.Fragment key={index}>
                                <div className="document-row">
                                    <img
                                        src={userIcon}
                                        alt="Document"
                                        className="document-icon"
                                    />
                                    <span className="document-name">{doc}</span>
                                </div>

                                {index < Users.length - 1 && <div className="line" />}
                            </React.Fragment>
                        ))}
                    </div>

                    {/* Floating Action Button */}
                    <button className="documents-fab">+</button>
                </section>
            </div>
        </div>
    );
}