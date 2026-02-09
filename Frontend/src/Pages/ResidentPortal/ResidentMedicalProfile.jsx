import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { get, post, patch, del } from "../../services/api";
import './css/ResidentMedicalProfile.css';

export default function ResidentMedicalProfile() {
    const navigate = useNavigate();

    const [resident, setResident] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showAddAllergy, setShowAddAllergy] = useState(false);
    const [newAllergy, setNewAllergy] = useState("");

    const [showAddDiagnosis, setShowAddDiagnosis] = useState(false);
    const [newDiagnosis, setNewDiagnosis] = useState("");

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

            <main className="dashboard-content">

                <div className="alert-section">
                    <h2 className="dashboard-title">
                        Medical Profile — {resident.firstName} {resident.lastName}
                    </h2>
                </div>

                {error && <div className="error-message">{error}</div>}

                {/* ALLERGIES */}
                <section className="inventory-section">
                    <div className="inventory-header">
                        <h3>Allergies</h3>
                        <button className="action-btn" onClick={() => setShowAddAllergy(true)}>
                            <i className="pi pi-plus"></i> Add
                        </button>
                    </div>

                    {allergies.length === 0 && (
                        <div className="custom-loading">
                            <span>No allergies recorded</span>
                        </div>
                    )}

                    {allergies.map((a) => (
                        <div key={a.id} className="inventory-row">
                            <span>{a.allergyType}</span>

                            <div className="row-actions">
                                <button
                                    className="pi pi-pencil"
                                    onClick={() => {
                                        const value = prompt("Rename allergy:", a.allergyType);
                                        if (value) handleRenameItem("allergy", a.id, value);
                                    }}
                                />
                                <button
                                    className="pi pi-trash"
                                    onClick={() => handleDeleteAllergy(a.id)}
                                />
                            </div>
                        </div>
                    ))}

                    {showAddAllergy && (
                        <div className="add-form">
                            <input
                                value={newAllergy}
                                onChange={(e) => setNewAllergy(e.target.value)}
                                placeholder="Enter allergy"
                            />
                            <button onClick={handleAddAllergy}>Add</button>
                            <button onClick={() => setShowAddAllergy(false)}>Cancel</button>
                        </div>
                    )}
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
                        <div className="custom-loading">
                            <span>No diagnoses recorded</span>
                        </div>
                    )}

                    {diagnoses.map((d) => (
                        <div key={d.id} className="inventory-row">
                            <span>{d.diagnosis}</span>

                            <div className="row-actions">
                                <button
                                    className="pi pi-pencil"
                                    onClick={() => {
                                        const value = prompt("Rename diagnosis:", d.diagnosis);
                                        if (value) handleRenameItem("diagnosis", d.id, value);
                                    }}
                                />
                            </div>
                        </div>
                    ))}

                    {showAddDiagnosis && (
                        <div className="add-form">
                            <input
                                value={newDiagnosis}
                                onChange={(e) => setNewDiagnosis(e.target.value)}
                                placeholder="Enter diagnosis"
                            />
                            <button onClick={handleAddDiagnosis}>Add</button>
                            <button onClick={() => setShowAddDiagnosis(false)}>Cancel</button>
                        </div>
                    )}
                </section>

            </main>
        </div>
    );
}
