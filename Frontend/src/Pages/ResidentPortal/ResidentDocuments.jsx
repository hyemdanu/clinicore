import React from "react";
import "./css/residentDocuments.css";
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
    return (
        <div className="resident-documents-page">
            <div className="box">
                <div className="rectangle">
                    <div className="resident-documents-title">
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
                        {documents.map((doc, index) => (
                            <React.Fragment key={index}>
                                <div className="document-row">
                                    <img
                                        src={documentIcon}
                                        alt="Document"
                                        className="document-icon"
                                    />
                                    <span className="document-name">{doc}</span>
                                </div>

                                {index < documents.length - 1 && <div className="line" />}
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




