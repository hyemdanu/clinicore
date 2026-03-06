import { useState, useEffect } from "react";
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

    const [sidebarOpen, setSidebarOpen] = useState(true);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

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

    const allergies = resident?.medicalRecord?.allergyDetails || [];
    const diagnoses = resident?.medicalRecord?.diagnosisDetails || [];

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

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="Dashboard" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
            <main className="dashboard-content">
                <div className="alert-section">
                    <h2 className="dashboard-title">
                        Medical Profile — {resident.firstName || 'Unknown'} {resident.lastName || ''}
                    </h2>
                </div>

                <div className="medical-profile-grid">
                    <div className="profile-column">
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Insurance</h3>
                            </div>
                            <div className="service-item">
                                <span className="service-label">{resident.medicalProfile?.insurance || 'No insurance recorded'}</span>
                            </div>
                        </section>

                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Medical Services</h3>
                            </div>
                            <div className="medical-services-list">
                                <div className="service-item">
                                    <span className="service-label">DNR / POLST</span>
                                    {resident.medicalServices?.dnrPolst ? "Yes" : "No"}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">On Hospice</span>
                                    {resident.medicalServices?.hospice ? "Yes" : "No"}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">Hospice Agency</span>
                                    {resident.medicalServices?.hospiceAgency || 'N/A'}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">Preferred Hospital</span>
                                    {resident.medicalServices?.preferredHospital || 'N/A'}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">Preferred Pharmacy</span>
                                    {resident.medicalServices?.preferredPharmacy || 'N/A'}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">On Home Health</span>
                                    {resident.medicalServices?.homeHealth ? "Yes" : "No"}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">Home Health Agency</span>
                                    {resident.medicalServices?.homeHealthAgency || 'N/A'}
                                </div>
                                <div className="service-item">
                                    <span className="service-label">Mortuary</span>
                                    {resident.medicalServices?.mortuary || 'N/A'}
                                </div>
                            </div>
                        </section>
                    </div>

                    <div className="profile-column">
                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Capabilities</h3>
                            </div>
                            <div className="service-item">
                                <span className="service-label">Mobility Status:</span>
                                {resident.capability?.mobilityStatus || 'Unknown'}
                            </div>
                            <div className="service-item">
                                <span className="service-label">Incontinence Status:</span>
                                {resident.capability?.incontinenceStatus || 'Unknown'}
                            </div>
                            <div className="service-item">
                                <span className="service-label">Self-Medicates:</span>
                                {resident.capability?.selfMedicates ? "Yes" : "No"}
                            </div>
                            <div className="service-item">
                                <span className="service-label">Verbal:</span>
                                {resident.capability?.verbal ? "Yes" : "No"}
                            </div>
                        </section>

                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Diagnoses</h3>
                            </div>
                            {diagnoses.length === 0 ? (
                                <div className="custom-loading"><span>No diagnoses recorded</span></div>
                            ) : (
                                diagnoses.map((d) => (
                                    <div key={d.id} className="inventory-row">
                                        <span>{d.diagnosis || 'Unknown diagnosis'}</span>
                                    </div>
                                ))
                            )}
                        </section>

                        <section className="inventory-section">
                            <div className="inventory-header">
                                <h3>Allergies</h3>
                            </div>
                            {allergies.length === 0 ? (
                                <div className="custom-loading"><span>No allergies recorded</span></div>
                            ) : (
                                allergies.map((a) => (
                                    <div key={a.id} className="inventory-row">
                                        <span>{a.allergyType || 'Unknown allergy'}</span>
                                    </div>
                                ))
                            )}
                        </section>
                    </div>
                </div>
            </main>
        </div>
    );
}
