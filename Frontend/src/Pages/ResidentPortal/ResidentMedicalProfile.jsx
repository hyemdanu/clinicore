import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import { get } from '../../services/api';

import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";
import "./css/ResidentMedicalProfile.css";

export default function ResidentMedicalProfile() {
    const navigate = useNavigate();
    const [sidebarOpen, setSidebarOpen] = useState(true);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

    // State variables to hold fetched data
    const [insurance, setInsurance] = useState("");
    const [medicalServices, setMedicalServices] = useState({});
    const [capabilities, setCapabilities] = useState({});
    const [diagnoses, setDiagnoses] = useState([]);
    const [medicalHistory, setMedicalHistory] = useState([]);
    const [allergies, setAllergies] = useState([]);

    useEffect(() => {
        const fetchData = async () => {
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) {
                return; // Navigate if not logged in
            }

            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            try {
                const residentProfile = await get(`/residents/full/${currentUserId}`);
                const medications = await get(`/residents/medications?currentUserId=${currentUserId}`);
                const allergiesData = await get(`/residents/allergies?currentUserId=${currentUserId}`);
                const diagnosesData = await get(`/residents/diagnoses?currentUserId=${currentUserId}`);
                const insurance = await(get `/residents/insurance?currentUserId=${currentUserId}`);
                const capability = await(get `/residents/capability?currentUserId=${currentUserId}`);
                // Update state with fetched data
                setInsurance(residentProfile.insurance || "N/A");
                setMedicalServices(residentProfile.medicalServices || {});
                setCapabilities(residentProfile.capabilities || {});
                setDiagnoses(diagnosesData || []);
                setMedicalHistory(residentProfile.medicalHistory || []);
                setAllergies(allergiesData || []);
                setInsurance(insurance);
                setCapabilities(capability);
            } catch (error) {
                console.error('Error fetching resident profile data:', error);
            }
        };

        fetchData();
    }, [navigate]);

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
                            <p className="mp-value">{insurance}</p>
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Medical Services</h3>
                                <button className="mp-edit-btn pi pi-pencil" />
                            </div>

                            <div className="mp-row"><span>DNR/POLST</span><span>{medicalServices.dnr ? "Yes" : "No"}</span></div>
                            <div className="mp-row"><span>On Hospice</span><span>{medicalServices.hospice ? "Yes" : "No"}</span></div>
                            <div className="mp-row"><span>Hospice Agency</span><span>{medicalServices.hospiceAgency || "N/A"}</span></div>
                            <div className="mp-row"><span>Preferred Hospital</span><span>{medicalServices.preferredHospital || "N/A"}</span></div>
                            <div className="mp-row"><span>Preferred Pharmacy</span><span>{medicalServices.preferredPharmacy || "N/A"}</span></div>
                            <div className="mp-row"><span>On Home Health</span><span>{medicalServices.homeHealth ? "Yes" : "No"}</span></div>
                            <div className="mp-row"><span>Home Health Agency</span><span>{medicalServices.homeHealthAgency || "N/A"}</span></div>
                            <div className="mp-row"><span>Mortuary</span><span>{medicalServices.mortuary || "N/A"}</span></div>
                            <div className="mp-row"><span>Mortuary Health</span><span>{medicalServices.mortuaryHealth || "N/A"}</span></div>
                        </div>
                    </div>

                    {/* RIGHT COLUMN */}
                    <div className="medical-column">
                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Capabilities</h3>
                                <button className="mp-edit-btn pi pi-pencil" />
                            </div>

                            <div className="mp-row"><span>Is Verbal</span><span>{capabilities.isVerbal ? "Yes" : "No"}</span></div>
                            <div className="mp-row"><span>Self-Medicates</span><span>{capabilities.selfMedicates ? "Yes" : "No"}</span></div>
                            <div className="mp-row"><span>Incontinence Status</span><span>{capabilities.incontinenceStatus || "N/A"}</span></div>
                            <div className="mp-row"><span>Mobility Capability</span><span>{capabilities.mobilityCapability || "N/A"}</span></div>
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Diagnoses</h3>
                                <button className="mp-add-btn pi pi-plus" />
                            </div>

                            {diagnoses.map((diagnosis, index) => (
                                <div className="mp-list-item" key={index}>
                                    {diagnosis.name} <span className="pi pi-ellipsis-v" />
                                </div>
                            ))}
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Medical History</h3>
                                <button className="mp-add-btn pi pi-plus" />
                            </div>

                            {medicalHistory.map((history, index) => (
                                <div className="mp-list-item" key={index}>
                                    {history.name} <span className="pi pi-ellipsis-v" />
                                </div>
                            ))}
                        </div>

                        <div className="mp-card">
                            <div className="mp-header">
                                <h3>Allergies</h3>
                                <button className="mp-add-btn pi pi-plus" />
                            </div>

                            {allergies.map((allergy, index) => (
                                <div className="mp-list-item" key={index}>
                                    {allergy.name} <span className="pi pi-ellipsis-v" />
                                </div>
                            ))}
                        </div>
                    </div>
                </section>
            </main>
        </div>
    );
}
