import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { get, post, patch, del } from "../../services/api";
import './css/ResidentMedicalProfile.css';
import ResidentSidebar from "../../Components/ResidentSidebar";
import Header from "../../Components/Header";

export default function ResidentMedicalProfile() {
    const navigate = useNavigate();

    const [resident, setResident] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showAddAllergy, setShowAddAllergy] = useState(false);
    const [newAllergy, setNewAllergy] = useState("");

    const [showAddDiagnosis, setShowAddDiagnosis] = useState(false);
    const [newDiagnosis, setNewDiagnosis] = useState("");

    const [sidebarOpen, setSidebarOpen] = useState(true);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

    // Load resident
    useEffect(() => {
        loadResident();
    }, []);

    const loadResident = async () => {
        setLoading(true);
        setError(null);

        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) {
                navigate("/");
                return;
            }

            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // If admin opened resident
            const openResidentId = localStorage.getItem("openResidentId");

            const residentId = openResidentId || currentUserId;

            const data = await get(`/residents/full/${residentId}?currentUserId=${currentUserId}`);
            setResident(data);
        } catch (err) {
            console.error("Failed to load resident:", err);
            setError("Failed to load medical profile.");
        } finally {
            setLoading(false);
        }
    };

    const refreshResident = async () => {
        const currentUserId = JSON.parse(localStorage.getItem("currentUser")).id;
        const data = await get(`/residents/full/${resident.id}?currentUserId=${currentUserId}`);
        setResident(data);
    };

    // -----------------------------
    // DATA
    // -----------------------------
    const allergies = resident?.medicalRecord?.allergyDetails || [];
    const diagnoses = resident?.medicalRecord?.diagnosisDetails || [];

    // -----------------------------
    // ADD ALLERGY
    // -----------------------------
    const handleAddAllergy = async () => {
        if (!newAllergy.trim()) return;

        try {
            const currentUserId = JSON.parse(localStorage.getItem("currentUser")).id;

            await post(`/residents/${resident.id}/allergies?currentUserId=${currentUserId}`, {
                allergyType: newAllergy,
                severity: 1,
                notes: ""
            });

            setNewAllergy("");
            setShowAddAllergy(false);
            refreshResident();
        } catch (err) {
            console.error("Add allergy failed:", err);
            alert("Failed to add allergy");
        }
    };

    // -----------------------------
    // DELETE ALLERGY
    // -----------------------------
    const handleDeleteAllergy = async (id) => {
        if (!confirm("Delete allergy?")) return;

        try {
            const currentUserId = JSON.parse(localStorage.getItem("currentUser")).id;

            await del(`/residents/allergies/${id}?currentUserId=${currentUserId}`);

            refreshResident();
        } catch (err) {
            console.error("Delete failed:", err);
        }
    };

    // -----------------------------
    // ADD DIAGNOSIS
    // -----------------------------
    const handleAddDiagnosis = async () => {
        if (!newDiagnosis.trim()) return;

        try {
            const currentUserId = JSON.parse(localStorage.getItem("currentUser")).id;

            await post(`/residents/${resident.id}/diagnoses?currentUserId=${currentUserId}`, {
                diagnosis: newDiagnosis,
                notes: ""
            });

            setNewDiagnosis("");
            setShowAddDiagnosis(false);
            refreshResident();
        } catch (err) {
            console.error("Add diagnosis failed:", err);
        }
    };

    // -----------------------------
    // RENAME ITEM
    // -----------------------------
    const handleRenameItem = async (type, id, value) => {
        const currentUserId = JSON.parse(localStorage.getItem("currentUser")).id;

        const url = type === "allergy"
            ? `/residents/allergies/${id}`
            : `/residents/diagnoses/${id}`;

        await patch(`${url}?currentUserId=${currentUserId}`, {
            [type === "allergy" ? "allergyType" : "diagnosis"]: value
        });

        refreshResident();
    };

    // -----------------------------
    // UI STATES
    // -----------------------------
    if (loading) {
        return (
            <div className="loading-state">
                <i className="pi pi-spin pi-spinner"></i>
                Loading medical profile...
            </div>
        );
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    if (!resident) {
        return <div>No resident loaded.</div>;
    }

    // -----------------------------
    // UI RENDER
    // -----------------------------
    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="Dashboard" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
            <main className="dashboard-content">
                <div className="alert-section">
                    <h2 className="dashboard-title">
                        Medical Profile — {resident.firstName} {resident.lastName}
                    </h2>
                </div>

                {error && <div className="error-message">{error}</div>}

                {/* TWO COLUMN LAYOUT */}
                <div className="medical-profile-grid">

                    {/* LEFT COLUMN */}
                    <div className="profile-column">
                        {/* INSURANCE */}
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Insurance</h3>
                            </div>
                        </section>

                        {/* MEDICAL SERVICES */}
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Medical Services</h3>
                            </div>

                            <div className="medical-services-list">
                                <div className="service-item">
                                    <span className="service-label">DNR / POLST</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">On Hospice</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Hospice Agency</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Preferred Hospital</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Preferred Pharmacy</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">On Home Health</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Home Health Agency</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Mortuary</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Mortuary Health</span>
                                </div>
                            </div>
                        </section>

                        {/* MEDICAL HISTORY */}
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Medical Services</h3>
                            </div>

                            <div className="medical-services-list">
                                <div className="service-item">
                                    <span className="service-label">Stroke</span>
                                </div>

                                <div className="service-item">
                                    <span className="service-label">Heart Attack</span>
                                </div>
                            </div>
                        </section>
                    </div>

                    {/* RIGHT COLUMN */}
                    <div className="profile-column">
                        {/* CAPABILITIES */}
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Capabilities</h3>
                            </div>
                        </section>

                        {/* DIAGNOSES */}
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Diagnoses</h3>
                                <button className="action-btn" onClick={() => setShowAddDiagnosis(true)}>
                                    <i className="pi pi-plus"></i> Add
                                </button>
                            </div>
                            {diagnoses.length === 0 && (
                                <div className="custom-loading"><span>No diagnoses recorded</span></div>
                            )}
                            {diagnoses.map((d) => (
                                <div key={d.id} className="inventory-row">
                                    <span>{d.diagnosis}</span>
                                    <div className="row-actions">
                                        <button className="pi pi-pencil" onClick={() => {
                                            const value = prompt("Rename diagnosis:", d.diagnosis);
                                            if (value) handleRenameItem("diagnosis", d.id, value);
                                        }} />
                                    </div>
                                </div>
                            ))}
                        </section>

                        {/* ALLERGIES (Moved to right as requested) */}
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Allergies</h3>
                                <button className="action-btn" onClick={() => setShowAddAllergy(true)}>
                                    <i className="pi pi-plus"></i> Add
                                </button>
                            </div>
                            {allergies.length === 0 && (
                                <div className="custom-loading"><span>No allergies recorded</span></div>
                            )}
                            {allergies.map((a) => (
                                <div key={a.id} className="inventory-row">
                                    <span>{a.allergyType}</span>
                                    <div className="row-actions">
                                        <button className="pi pi-trash" onClick={() => handleDeleteAllergy(a.id)} />
                                    </div>
                                </div>
                            ))}
                        </section>
                    </div>
                </div>
                {/* --- END WRAPPER --- */}
            </main>
        </div>
    );
}
