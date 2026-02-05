import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";

import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";
import "./css/ResidentMedicalProfile.css";

export default function ResidentMedicalProfile() {
    const navigate = useNavigate();
    const [sidebarOpen, setSidebarOpen] = useState(true);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

    // Allergy state
    const [allergies, setAllergies] = useState([
        "Allergy1",
        "Allergy2"
    ]);
    const [newAllergy, setNewAllergy] = useState("");
    const [showAllergyInput, setShowAllergyInput] = useState(false);

    const handleAddAllergy = () => {
        if (!newAllergy.trim()) return;

        setAllergies([...allergies, newAllergy.trim()]);
        setNewAllergy("");
        setShowAllergyInput(false);
    };

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="Medical Profile" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>

                <section className="medical-profile-grid">

                    {/* LEFT COLUMN */}
                    <div className="medical-column">

                        <div className="mp-card">
                            <h3>Insurance</h3>
                            <p className="mp-value">Medi-Cal</p>
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Medical Services</h3>
                                <button className="mp-edit-btn pi pi-pencil" />
                            </div>

                            <div className="mp-row"><span>DNR/POLST</span><span>Yes</span></div>
                            <div className="mp-row"><span>On Hospice</span><span>Yes</span></div>
                            <div className="mp-row"><span>Hospice Agency</span><span>ABC Hospice</span></div>
                            <div className="mp-row"><span>Preferred Hospital</span><span>ABC Hospital</span></div>
                            <div className="mp-row"><span>Preferred Pharmacy</span><span>ABC Pharmacy</span></div>
                            <div className="mp-row"><span>On Home Health</span><span>No</span></div>
                            <div className="mp-row"><span>Home Health Agency</span><span>N/A</span></div>
                            <div className="mp-row"><span>Mortuary</span><span>ABC Mortuary</span></div>
                            <div className="mp-row"><span>Mortuary Health</span><span>Completed</span></div>
                        </div>

                    </div>

                    {/* RIGHT COLUMN */}
                    <div className="medical-column">

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Capabilities</h3>
                                <button className="mp-edit-btn pi pi-pencil" />
                            </div>

                            <div className="mp-row"><span>Is Verbal</span><span>Yes</span></div>
                            <div className="mp-row"><span>Self-Medicates</span><span>Yes</span></div>
                            <div className="mp-row"><span>Incontinence Status</span><span>Continent</span></div>
                            <div className="mp-row"><span>Mobility Capability</span><span>Walks without assistance</span></div>
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Diagnoses</h3>
                                <button className="mp-add-btn pi pi-plus" />
                            </div>

                            <div className="mp-list-item">Some Disease <span className="pi pi-ellipsis-v" /></div>
                            <div className="mp-list-item">Nausea <span className="pi pi-ellipsis-v" /></div>
                            <div className="mp-list-item">Injury <span className="pi pi-ellipsis-v" /></div>
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Medical History</h3>
                                <button className="mp-add-btn pi pi-plus" />
                            </div>

                            <div className="mp-list-item">Stroke <span className="pi pi-ellipsis-v" /></div>
                            <div className="mp-list-item">Heart Attack <span className="pi pi-ellipsis-v" /></div>
                        </div>

                        {/* ✅ EDITABLE ALLERGIES */}
                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Allergies</h3>
                                <button
                                    className="mp-add-btn pi pi-plus"
                                    onClick={() => setShowAllergyInput(true)}
                                />
                            </div>

                            {allergies.map((allergy, index) => (
                                <div className="mp-list-item" key={index}>
                                    {allergy} <span className="pi pi-ellipsis-v" />
                                </div>
                            ))}

                            {showAllergyInput && (
                                <div className="mp-add-row">
                                    <input
                                        type="text"
                                        placeholder="Enter allergy name"
                                        value={newAllergy}
                                        onChange={(e) => setNewAllergy(e.target.value)}
                                        className="mp-input"
                                    />

                                    <button className="mp-save-btn" onClick={handleAddAllergy}>
                                        Add
                                    </button>

                                    <button
                                        className="mp-cancel-btn"
                                        onClick={() => {
                                            setShowAllergyInput(false);
                                            setNewAllergy("");
                                        }}
                                    >
                                        Cancel
                                    </button>
                                </div>
                            )}
                        </div>

                    </div>

                </section>

            </main>
        </div>
    );
}
