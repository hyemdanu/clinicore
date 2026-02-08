import React from "react";
import "./css/residentDocuments.css";
import searchIcon from "../../assets/icons/magnifying-glass.png";

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


                </section>
            </div>
        </div>
    );
}




