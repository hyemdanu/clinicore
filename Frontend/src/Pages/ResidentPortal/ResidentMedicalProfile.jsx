import { useState, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";
import './css/ResidentMedicalProfile.css';
import ResidentSidebar from "../../Components/ResidentSidebar";
import Header from "../../Components/Header";

export default function ResidentMedicalProfile() {
    const navigate = useNavigate();

    const [resident, setResident] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
    const toggleSidebar = () => setSidebarOpen((s) => !s);

    const loadResident = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const currentUserStr = localStorage.getItem("currentUser");
            if (!currentUserStr) { navigate("/"); return; }

            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            const data = await get(`/residents/full/${currentUserId}?currentUserId=${currentUserId}`);
            setResident(data);
        } catch {
            setError("Failed to load medical profile.");
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => { loadResident(); }, [loadResident]);

    const allergies = resident?.medicalRecord?.allergyDetails || [];
    const diagnoses = resident?.medicalRecord?.diagnosisDetails || [];
    const med = resident?.medicalProfile || {};
    const svc = resident?.medicalServices || {};
    const cap = resident?.capability || {};

    const InfoRow = ({ label, value }) => (
        <div className="mp-row">
            <span className="mp-label">{label}</span>
            <span className="mp-value">{value}</span>
        </div>
    );

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="Medical Profile" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                {loading ? (
                    <div className="residents-loading">
                        <i className="pi pi-spin pi-spinner"></i>
                        <span>Loading medical profile...</span>
                    </div>
                ) : error ? (
                    <div className="error-message" role="alert">{error}</div>
                ) : !resident ? (
                    <div className="error-message">No resident loaded.</div>
                ) : (
                    <>
                        <h2 className="dashboard-title">
                            Medical Profile — {resident.firstName || 'Unknown'} {resident.lastName || ''}
                        </h2>

                        <div className="mp-grid">
                            {/* General Info */}
                            <section className="mp-card">
                                <h3>General Information</h3>
                                <InfoRow label="Insurance" value={med.insurance || 'Not recorded'} />
                                <InfoRow label="Mobility Status" value={cap.mobilityStatus || 'Unknown'} />
                                <InfoRow label="Incontinence Status" value={cap.incontinenceStatus || 'Unknown'} />
                                <InfoRow label="Self-Medicates" value={cap.selfMedicates ? 'Yes' : 'No'} />
                                <InfoRow label="Verbal" value={cap.verbal ? 'Yes' : 'No'} />
                            </section>

                            {/* Medical Services */}
                            <section className="mp-card">
                                <h3>Medical Services</h3>
                                <InfoRow label="DNR / POLST" value={svc.dnrPolst ? 'Yes' : 'No'} />
                                <InfoRow label="On Hospice" value={svc.hospice ? 'Yes' : 'No'} />
                                <InfoRow label="Hospice Agency" value={svc.hospiceAgency || 'N/A'} />
                                <InfoRow label="Preferred Hospital" value={svc.preferredHospital || 'N/A'} />
                                <InfoRow label="Preferred Pharmacy" value={svc.preferredPharmacy || 'N/A'} />
                                <InfoRow label="On Home Health" value={svc.homeHealth ? 'Yes' : 'No'} />
                                <InfoRow label="Home Health Agency" value={svc.homeHealthAgency || 'N/A'} />
                                <InfoRow label="Mortuary" value={svc.mortuary || 'N/A'} />
                            </section>

                            {/* Diagnoses */}
                            <section className="mp-card">
                                <h3>Diagnoses</h3>
                                {diagnoses.length === 0 ? (
                                    <p className="mp-empty">No diagnoses recorded</p>
                                ) : (
                                    diagnoses.map((d) => (
                                        <div key={d.id} className="mp-list-item">
                                            {d.diagnosis || 'Unknown diagnosis'}
                                        </div>
                                    ))
                                )}
                            </section>

                            {/* Allergies */}
                            <section className="mp-card">
                                <h3>Allergies</h3>
                                {allergies.length === 0 ? (
                                    <p className="mp-empty">No allergies recorded</p>
                                ) : (
                                    allergies.map((a) => (
                                        <div key={a.id} className="mp-list-item">
                                            {a.allergyType || 'Unknown allergy'}
                                        </div>
                                    ))
                                )}
                            </section>
                        </div>
                    </>
                )}
            </main>
        </div>
    );
}
