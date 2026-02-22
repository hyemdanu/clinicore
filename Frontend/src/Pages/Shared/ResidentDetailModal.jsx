import { useState, useEffect } from "react";
import dayjs from "dayjs";
import relativeTime from "dayjs/plugin/relativeTime";
import { Button } from "primereact/button";
import { Dropdown } from "primereact/dropdown";
import AddMedicationForm from "../Shared/AddMedicationModal.jsx";
import { patch } from "../../services/api.js";
import "../Shared/css/residents.css";

/*
 * this the modal in the sidebar when you wanna
 * view resident details: general, medical, medication
 */


dayjs.extend(relativeTime);

export default function ResidentDetailModal({ resident, onClose, onResidentUpdated }) {
    const [activeTab, setActiveTab] = useState(() => {
        return localStorage.getItem('residentDetailActiveTab') || "general";
    });
    const [showAddMedicationForm, setShowAddMedicationForm] = useState(false);
    const [medications, setMedications] = useState(resident.medications || []);
    const [updatingMedId, setUpdatingMedId] = useState(null);
    const [editingMedId, setEditingMedId] = useState(null);
    const [editForm, setEditForm] = useState({});

    useEffect(() => {
        localStorage.setItem('residentDetailActiveTab', activeTab);
        localStorage.setItem('openResidentId', resident.id);
    }, [activeTab, resident.id]);

    const handleClose = () => {
        localStorage.removeItem('residentDetailActiveTab');
        localStorage.removeItem('openResidentId');
        onClose();
    };

    useEffect(() => {
        setMedications(resident.medications || []);
        setShowAddMedicationForm(false);
    }, [resident.id]);

    useEffect(() => {
        const interval = setInterval(async () => {
            if (!showAddMedicationForm && resident.id) {
                try {
                    const { get } = await import('../../services/api.js');
                    const currentUserStr = localStorage.getItem('currentUser');
                    if (currentUserStr) {
                        const currentUserId = JSON.parse(currentUserStr).id;
                        const data = await get(`/residents/full/${resident.id}?currentUserId=${currentUserId}`);
                        setMedications(data.medications || []);
                    }
                } catch (error) {
                    console.error('Auto-refresh failed:', error);
                }
            }
        }, 5000);

        return () => clearInterval(interval);
    }, [resident.id, showAddMedicationForm]);

    if (!resident) {
        return null;
    }

    const allergies = resident.medicalRecord?.allergies || [];
    const diagnoses = resident.medicalRecord?.diagnoses || [];
    const allergyDetails = resident.medicalRecord?.allergyDetails || [];
    const diagnosisDetails = resident.medicalRecord?.diagnosisDetails || [];
    const services = resident.medicalServices || {};
    const capability = resident.capability || {};

    const handleMedicationAdded = (newMedication) => {
        setMedications(prev => [...prev, newMedication]);
        setShowAddMedicationForm(false);
        if (onResidentUpdated) {
            onResidentUpdated();
        }
    };

    const handleStatusChange = async (medicationId, newStatus) => {
        setUpdatingMedId(medicationId);
        try {
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            const updatedMedication = await patch(
                `/residents/medications/${medicationId}/status?status=${newStatus}&currentUserId=${currentUserId}`
            );

            setMedications(prev =>
                prev.map(med => med.id === medicationId ? updatedMedication : med)
            );
        } catch (error) {
            console.error("Error updating medication status:", error);
            alert("Failed to update medication status.");
        } finally {
            setUpdatingMedId(null);
        }
    };

    const handleEditMedication = (med) => {
        setEditingMedId(med.id);
        setEditForm({ dosage: med.dosage, schedule: med.schedule, notes: med.notes });
    };

    const handleSaveEdit = async (medicationId) => {
        try {
            const { patch } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await patch(`/residents/medications/${medicationId}?currentUserId=${currentUserId}`, editForm);

            setMedications(prev => prev.map(med =>
                med.id === medicationId
                    ? { ...med, ...editForm }
                    : med
            ));
            setEditingMedId(null);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error updating medication:", error);
            alert("Failed to update medication");
        }
    };

    const handleDeleteMedication = async (medicationId) => {
        if (!confirm("Are you sure you want to delete this medication?")) return;

        try {
            const { del } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await del(`/residents/medications/${medicationId}?currentUserId=${currentUserId}`);
            setMedications(prev => prev.filter(med => med.id !== medicationId));
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error deleting medication:", error);
            alert("Failed to delete medication");
        }
    };

    return (
        <div className="resident-detail-sidebar">
            <button className="sidebar-close-btn" onClick={handleClose}>
                <i className="pi pi-times"></i>
            </button>

            <div className="sidebar-tabs">
                <button
                    className={`sidebar-tab ${activeTab === "general" ? "active" : ""}`}
                    onClick={() => setActiveTab("general")}
                >
                    GENERAL
                </button>
                <button
                    className={`sidebar-tab ${activeTab === "medical" ? "active" : ""}`}
                    onClick={() => setActiveTab("medical")}
                >
                    MEDICAL
                </button>
                <button
                    className={`sidebar-tab ${activeTab === "medication" ? "active" : ""}`}
                    onClick={() => setActiveTab("medication")}
                >
                    MEDICATION
                </button>
            </div>

            <div className="sidebar-content">
                {activeTab === "general" && <GeneralTab details={resident} />}
                {activeTab === "medical" && (
                    <MedicalTab
                        services={services}
                        capability={capability}
                        allergies={allergies}
                        diagnoses={diagnoses}
                        allergyDetails={allergyDetails}
                        diagnosisDetails={diagnosisDetails}
                        medicalProfile={resident.medicalProfile}
                        residentId={resident.id}
                        onResidentUpdated={onResidentUpdated}
                    />
                )}
                {activeTab === "medication" && (
                    <MedicationTab
                        medications={medications}
                        residentId={resident.id}
                        showAddForm={showAddMedicationForm}
                        onAddMedication={() => setShowAddMedicationForm(true)}
                        onCancelAdd={() => setShowAddMedicationForm(false)}
                        onMedicationAdded={handleMedicationAdded}
                        onStatusChange={handleStatusChange}
                        onDelete={handleDeleteMedication}
                        updatingMedId={updatingMedId}
                        editingMedId={editingMedId}
                        editForm={editForm}
                        onEdit={handleEditMedication}
                        onSaveEdit={handleSaveEdit}
                        onCancelEdit={() => setEditingMedId(null)}
                        setEditForm={setEditForm}
                    />
                )}
            </div>
        </div>
    );
}

function GeneralTab({ details }) {
    return (
        <div className="tab-content">
            <div className="general-card">
                <div className="general-header">
                    <div className="avatar-large">
                        {details.firstName?.[0]}
                    </div>
                    <div>
                        <h3>{details.firstName} {details.lastName}</h3>
                        <p className="muted">{details.email}</p>
                    </div>
                </div>

                <div className="general-grid">
                    <InfoRow label="Birthday" value={details.birthday ? dayjs(details.birthday).format("MMM D, YYYY") : "N/A"} />
                    <InfoRow label="Gender" value={details.gender || "N/A"} />
                    <InfoRow label="Contact Number" value={details.contactNumber || "N/A"} />
                    <InfoRow label="Emergency Contact" value={details.emergencyContactName || "N/A"} />
                    <InfoRow label="Emergency Number" value={details.emergencyContactNumber || "N/A"} />
                </div>

                <div className="general-notes">
                    <label>Notes</label>
                    <p>{details.residentNotes || "No notes yet."}</p>
                </div>
            </div>
        </div>
    );
}


// for displaying a label and value pair
function MedicalTab({ services, capability, allergies, diagnoses, allergyDetails, diagnosisDetails, medicalProfile, residentId, onResidentUpdated }) {
    const [showAddAllergy, setShowAddAllergy] = useState(false);
    const [showAddDiagnosis, setShowAddDiagnosis] = useState(false);
    const [allergyForm, setAllergyForm] = useState({ allergyType: '', severity: 1, notes: '' });
    const [diagnosisForm, setDiagnosisForm] = useState({ diagnosis: '', notes: '' });
    const [isSubmitting, setIsSubmitting] = useState(false);

    const [editingInsurance, setEditingInsurance] = useState(false);
    const [editingMedicalNotes, setEditingMedicalNotes] = useState(false);
    const [editingServices, setEditingServices] = useState(false);
    const [editingCapabilities, setEditingCapabilities] = useState(false);

    const [insuranceForm, setInsuranceForm] = useState(medicalProfile?.insurance || '');
    const [medicalNotesForm, setMedicalNotesForm] = useState(medicalProfile?.notes || '');
    const [servicesForm, setServicesForm] = useState(services || {});
    const [capabilityForm, setCapabilityForm] = useState(capability || {});

    const handleAddAllergy = async () => {
        if (!allergyForm.allergyType.trim()) {
            alert("Please enter allergy type");
            return;
        }

        setIsSubmitting(true);
        try {
            const { post } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await post(`/residents/${residentId}/allergies?currentUserId=${currentUserId}`, allergyForm);
            setAllergyForm({ allergyType: '', severity: 1, notes: '' });
            setShowAddAllergy(false);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error adding allergy:", error);
            alert("Failed to add allergy");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeleteAllergy = async (allergyId) => {
        if (!confirm("Are you sure you want to delete this allergy?")) return;

        try {
            const { del } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await del(`/residents/allergies/${allergyId}?currentUserId=${currentUserId}`);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error deleting allergy:", error);
            alert("Failed to delete allergy");
        }
    };

    const handleAddDiagnosis = async () => {
        if (!diagnosisForm.diagnosis.trim()) {
            alert("Please enter diagnosis");
            return;
        }

        setIsSubmitting(true);
        try {
            const { post } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await post(`/residents/${residentId}/diagnoses?currentUserId=${currentUserId}`, diagnosisForm);
            setDiagnosisForm({ diagnosis: '', notes: '' });
            setShowAddDiagnosis(false);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error adding diagnosis:", error);
            alert("Failed to add diagnosis");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleDeleteDiagnosis = async (diagnosisId) => {
        if (!confirm("Are you sure you want to delete this diagnosis?")) return;

        try {
            const { del } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await del(`/residents/diagnoses/${diagnosisId}?currentUserId=${currentUserId}`);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error deleting diagnosis:", error);
            alert("Failed to delete diagnosis");
        }
    };

    const handleSaveInsurance = async () => {
        setIsSubmitting(true);
        try {
            const { patch } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await patch(`/residents/${residentId}/medical-profile?currentUserId=${currentUserId}`, { insurance: insuranceForm });
            setEditingInsurance(false);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error updating insurance:", error);
            alert("Failed to update insurance");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleSaveMedicalNotes = async () => {
        setIsSubmitting(true);
        try {
            const { patch } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await patch(`/residents/${residentId}/medical-profile?currentUserId=${currentUserId}`, { notes: medicalNotesForm });
            setEditingMedicalNotes(false);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error updating medical notes:", error);
            alert("Failed to update medical notes");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleSaveServices = async () => {
        setIsSubmitting(true);
        try {
            const { patch } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await patch(`/residents/${residentId}/medical-services?currentUserId=${currentUserId}`, servicesForm);
            setEditingServices(false);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error updating medical services:", error);
            alert("Failed to update medical services");
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleSaveCapabilities = async () => {
        setIsSubmitting(true);
        try {
            const { patch } = await import('../../services/api.js');
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            await patch(`/residents/${residentId}/capabilities?currentUserId=${currentUserId}`, capabilityForm);
            setEditingCapabilities(false);
            if (onResidentUpdated) onResidentUpdated();
        } catch (error) {
            console.error("Error updating capabilities:", error);
            alert("Failed to update capabilities");
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <div className="tab-content medical-tab">
            <div className="medical-columns">
                <div className="medical-column">
                    <div className="medical-section">
                        <div className="section-header">
                            <label>Insurance</label>
                            {!editingInsurance && (
                                <button className="icon-btn" onClick={() => {
                                    setInsuranceForm(medicalProfile?.insurance || '');
                                    setEditingInsurance(true);
                                }}>
                                    <i className="pi pi-pencil"></i>
                                </button>
                            )}
                        </div>
                        {editingInsurance ? (
                            <div className="edit-inline">
                                <input
                                    type="text"
                                    value={insuranceForm}
                                    onChange={(e) => setInsuranceForm(e.target.value)}
                                    placeholder="Insurance provider"
                                />
                                <div className="form-actions">
                                    <Button label="Cancel" onClick={() => setEditingInsurance(false)} className="p-button-secondary p-button-sm" />
                                    <Button label="Save" onClick={handleSaveInsurance} disabled={isSubmitting} className="p-button-primary p-button-sm" />
                                </div>
                            </div>
                        ) : (
                            <div className="field-value">{medicalProfile?.insurance || "N/A"}</div>
                        )}
                    </div>
                    <div className="medical-section">
                        <div className="section-header">
                            <label>Medical Profile Notes</label>
                            {!editingMedicalNotes && (
                                <button className="icon-btn" onClick={() => {
                                    setMedicalNotesForm(medicalProfile?.notes || '');
                                    setEditingMedicalNotes(true);
                                }}>
                                    <i className="pi pi-pencil"></i>
                                </button>
                            )}
                        </div>
                        {editingMedicalNotes ? (
                            <div className="edit-inline">
                                <textarea
                                    value={medicalNotesForm}
                                    onChange={(e) => setMedicalNotesForm(e.target.value)}
                                    placeholder="Medical profile notes"
                                    rows="4"
                                />
                                <div className="form-actions">
                                    <Button label="Cancel" onClick={() => setEditingMedicalNotes(false)} className="p-button-secondary p-button-sm" />
                                    <Button label="Save" onClick={handleSaveMedicalNotes} disabled={isSubmitting} className="p-button-primary p-button-sm" />
                                </div>
                            </div>
                        ) : (
                            <div className="field-value">{medicalProfile?.notes || "—"}</div>
                        )}
                    </div>

                    <div className="medical-section services-section">
                        <div className="section-header">
                            <label>Medical Services</label>
                            {!editingServices && (
                                <button className="icon-btn" onClick={() => {
                                    setServicesForm(services || {});
                                    setEditingServices(true);
                                }}>
                                    <i className="pi pi-pencil"></i>
                                </button>
                            )}
                        </div>
                        {editingServices ? (
                            <div className="edit-form-grid">
                                <div className="form-group">
                                    <label>DNR/POLST</label>
                                    <select
                                        value={servicesForm.dnrPolst || 'No'}
                                        onChange={(e) => setServicesForm({...servicesForm, dnrPolst: e.target.value})}
                                    >
                                        <option value="No">No</option>
                                        <option value="Yes">Yes</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>On Hospice</label>
                                    <select
                                        value={servicesForm.hospice ? 'true' : 'false'}
                                        onChange={(e) => setServicesForm({...servicesForm, hospice: e.target.value === 'true'})}
                                    >
                                        <option value="false">No</option>
                                        <option value="true">Yes</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Hospice Agency</label>
                                    <input
                                        type="text"
                                        value={servicesForm.hospiceAgency || ''}
                                        onChange={(e) => setServicesForm({...servicesForm, hospiceAgency: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Preferred Hospital</label>
                                    <input
                                        type="text"
                                        value={servicesForm.preferredHospital || ''}
                                        onChange={(e) => setServicesForm({...servicesForm, preferredHospital: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Preferred Pharmacy</label>
                                    <input
                                        type="text"
                                        value={servicesForm.preferredPharmacy || ''}
                                        onChange={(e) => setServicesForm({...servicesForm, preferredPharmacy: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>On Home Health</label>
                                    <select
                                        value={servicesForm.homeHealth ? 'true' : 'false'}
                                        onChange={(e) => setServicesForm({...servicesForm, homeHealth: e.target.value === 'true'})}
                                    >
                                        <option value="false">No</option>
                                        <option value="true">Yes</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Home Health Agency</label>
                                    <input
                                        type="text"
                                        value={servicesForm.homeHealthAgency || ''}
                                        onChange={(e) => setServicesForm({...servicesForm, homeHealthAgency: e.target.value})}
                                    />
                                </div>
                                <div className="form-group">
                                    <label>Mortuary</label>
                                    <input
                                        type="text"
                                        value={servicesForm.mortuary || ''}
                                        onChange={(e) => setServicesForm({...servicesForm, mortuary: e.target.value})}
                                    />
                                </div>
                                <div className="form-actions" style={{gridColumn: '1 / -1'}}>
                                    <Button label="Cancel" onClick={() => setEditingServices(false)} className="p-button-secondary p-button-sm" />
                                    <Button label="Save" onClick={handleSaveServices} disabled={isSubmitting} className="p-button-primary p-button-sm" />
                                </div>
                            </div>
                        ) : (
                            <div className="services-grid">
                                <ServiceRow label="DNR/POLST" value={services.dnrPolst} />
                                <ServiceRow label="On Hospice" value={services.hospice ? "Yes" : "No"} />
                                <ServiceRow label="Hospice Agency" value={services.hospiceAgency} />
                                <ServiceRow label="Preferred Hospital" value={services.preferredHospital} />
                                <ServiceRow label="Preferred Pharmacy" value={services.preferredPharmacy} />
                                <ServiceRow label="On Home Health" value={services.homeHealth ? "Yes" : "No"} />
                                <ServiceRow label="Home Health Agency" value={services.homeHealthAgency} />
                                <ServiceRow label="Mortuary" value={services.mortuary} />
                            </div>
                        )}
                    </div>
                </div>

                <div className="medical-column">
                    <div className="medical-section capabilities-section">
                        <div className="section-header">
                            <label>Capabilities</label>
                            {!editingCapabilities && (
                                <button className="icon-btn" onClick={() => {
                                    setCapabilityForm(capability || {});
                                    setEditingCapabilities(true);
                                }}>
                                    <i className="pi pi-pencil"></i>
                                </button>
                            )}
                        </div>
                        {editingCapabilities ? (
                            <div className="edit-form-grid">
                                <div className="form-group">
                                    <label>Mobility Status</label>
                                    <select
                                        value={capabilityForm.mobilityStatus || ''}
                                        onChange={(e) => setCapabilityForm({...capabilityForm, mobilityStatus: e.target.value})}
                                    >
                                        <option value="">Select...</option>
                                        <option value="Walks Without Assistance">Walks Without Assistance</option>
                                        <option value="Walks With Assistance">Walks With Assistance</option>
                                        <option value="Wheelchair">Wheelchair</option>
                                        <option value="Bedridden">Bedridden</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Incontinence Status</label>
                                    <select
                                        value={capabilityForm.incontinenceStatus || ''}
                                        onChange={(e) => setCapabilityForm({...capabilityForm, incontinenceStatus: e.target.value})}
                                    >
                                        <option value="">Select...</option>
                                        <option value="Continent">Continent</option>
                                        <option value="Incontinent (Urine)">Incontinent (Urine)</option>
                                        <option value="Incontinent (Bowels)">Incontinent (Bowels)</option>
                                        <option value="Incontinent (Both)">Incontinent (Both)</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Self Medicates</label>
                                    <select
                                        value={capabilityForm.selfMedicates ? 'true' : 'false'}
                                        onChange={(e) => setCapabilityForm({...capabilityForm, selfMedicates: e.target.value === 'true'})}
                                    >
                                        <option value="false">No</option>
                                        <option value="true">Yes</option>
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Verbal</label>
                                    <select
                                        value={capabilityForm.verbal ? 'true' : 'false'}
                                        onChange={(e) => setCapabilityForm({...capabilityForm, verbal: e.target.value === 'true'})}
                                    >
                                        <option value="false">No</option>
                                        <option value="true">Yes</option>
                                    </select>
                                </div>
                                <div className="form-actions" style={{gridColumn: '1 / -1'}}>
                                    <Button label="Cancel" onClick={() => setEditingCapabilities(false)} className="p-button-secondary p-button-sm" />
                                    <Button label="Save" onClick={handleSaveCapabilities} disabled={isSubmitting} className="p-button-primary p-button-sm" />
                                </div>
                            </div>
                        ) : (
                            <div className="capabilities-grid">
                                <ServiceRow label="Mobility Status" value={capability.mobilityStatus} />
                                <ServiceRow label="Incontinence Status" value={capability.incontinenceStatus} />
                                <ServiceRow label="Self Medicates" value={capability.selfMedicates ? "Yes" : "No"} />
                                <ServiceRow label="Verbal" value={capability.verbal ? "Yes" : "No"} />
                            </div>
                        )}
                    </div>

                    <div className="medical-section list-section">
                        <div className="section-header">
                            <label>Diagnoses</label>
                            {!showAddDiagnosis && (
                                <Button
                                    icon="pi pi-plus"
                                    className="p-button-sm p-button-text"
                                    onClick={() => setShowAddDiagnosis(true)}
                                />
                            )}
                        </div>
                        {showAddDiagnosis && (
                            <div className="add-form">
                                <input
                                    type="text"
                                    placeholder="Diagnosis"
                                    value={diagnosisForm.diagnosis}
                                    onChange={(e) => setDiagnosisForm({...diagnosisForm, diagnosis: e.target.value})}
                                />
                                <textarea
                                    placeholder="Notes (optional)"
                                    value={diagnosisForm.notes}
                                    onChange={(e) => setDiagnosisForm({...diagnosisForm, notes: e.target.value})}
                                    rows="2"
                                />
                                <div className="form-actions">
                                    <Button label="Cancel" onClick={() => setShowAddDiagnosis(false)} className="p-button-secondary p-button-sm" />
                                    <Button label="Add" onClick={handleAddDiagnosis} disabled={isSubmitting} className="p-button-primary p-button-sm" />
                                </div>
                            </div>
                        )}
                        <DetailedList items={diagnosisDetails} onDelete={handleDeleteDiagnosis} emptyText="No diagnoses recorded." type="diagnosis" />
                    </div>

                    <div className="medical-section list-section">
                        <div className="section-header">
                            <label>Allergies</label>
                            {!showAddAllergy && (
                                <Button
                                    icon="pi pi-plus"
                                    className="p-button-sm p-button-text"
                                    onClick={() => setShowAddAllergy(true)}
                                />
                            )}
                        </div>
                        {showAddAllergy && (
                            <div className="add-form">
                                <input
                                    type="text"
                                    placeholder="Allergy Type"
                                    value={allergyForm.allergyType}
                                    onChange={(e) => setAllergyForm({...allergyForm, allergyType: e.target.value})}
                                />
                                <select
                                    value={allergyForm.severity}
                                    onChange={(e) => setAllergyForm({...allergyForm, severity: parseInt(e.target.value)})}
                                >
                                    <option value={1}>Severity: Mild (1)</option>
                                    <option value={2}>Severity: Moderate (2)</option>
                                    <option value={3}>Severity: Severe (3)</option>
                                </select>
                                <textarea
                                    placeholder="Notes (optional)"
                                    value={allergyForm.notes}
                                    onChange={(e) => setAllergyForm({...allergyForm, notes: e.target.value})}
                                    rows="2"
                                />
                                <div className="form-actions">
                                    <Button label="Cancel" onClick={() => setShowAddAllergy(false)} className="p-button-secondary p-button-sm" />
                                    <Button label="Add" onClick={handleAddAllergy} disabled={isSubmitting} className="p-button-primary p-button-sm" />
                                </div>
                            </div>
                        )}
                        <DetailedList items={allergyDetails} onDelete={handleDeleteAllergy} emptyText="No allergies recorded." type="allergy" />
                    </div>
                </div>
            </div>
        </div>
    );
}

// for displaying and managing medications
function MedicationTab({ medications, residentId, showAddForm, onAddMedication, onCancelAdd, onMedicationAdded, onStatusChange, onDelete, updatingMedId, editingMedId, editForm, onEdit, onSaveEdit, onCancelEdit, setEditForm }) {
    const otherStatusOptions = [
        { label: 'Set as Pending', value: 'PENDING' },
        { label: 'Set as Withheld', value: 'WITHHELD' },
        { label: 'Set as Missed', value: 'MISSED' }
    ];

    const scheduleOptions = [
        { label: 'Once daily', value: 'Once daily' },
        { label: 'Twice daily', value: 'Twice daily' },
        { label: 'Three times daily', value: 'Three times daily' },
        { label: 'Every 4 hours', value: 'Every 4 hours' },
        { label: 'Every 6 hours', value: 'Every 6 hours' },
        { label: 'Every 8 hours', value: 'Every 8 hours' },
        { label: 'As needed (PRN)', value: 'As needed (PRN)' }
    ];

    return (
        <div className="tab-content medication-tab">
            <div className="medication-header">
                <h3>Medications</h3>
                {!showAddForm && (
                    <Button
                        label="Add Medication"
                        onClick={onAddMedication}
                        className="p-button-primary p-button-sm"
                        icon="pi pi-plus"
                    />
                )}
            </div>

            {showAddForm ? (
                <AddMedicationForm
                    residentId={residentId}
                    onCancel={onCancelAdd}
                    onMedicationAdded={onMedicationAdded}
                />
            ) : (
                <div className="medication-list">
                    {medications.length === 0 ? (
                        <div className="empty-note">No medications recorded.</div>
                    ) : (
                        medications.map((med) => {
                            const isEditing = editingMedId === med.id;
                            return (<div className={`medication-item ${med.isOverdue ? 'overdue' : ''}`} key={med.id}>
                                <div className="med-header">
                                    <div className="med-name-row">
                                        <span className="med-name">{med.name}</span>
                                        {med.inventoryQuantity !== undefined && (
                                            <span className={`qty-badge ${med.inventoryQuantity === 0 ? 'qty-zero' : med.inventoryQuantity < 5 ? 'qty-low' : ''}`}>
                                                {med.inventoryQuantity} in stock
                                            </span>
                                        )}
                                    </div>
                                    {!isEditing && (
                                        <div style={{display: 'flex', gap: '8px'}}>
                                            <button className="edit-btn" onClick={() => onEdit(med)}>
                                                <i className="pi pi-pencil"></i>
                                            </button>
                                            <button className="delete-btn-med" onClick={() => onDelete(med.id)}>
                                                <i className="pi pi-trash"></i>
                                            </button>
                                        </div>
                                    )}
                                </div>

                                {isEditing ? (
                                    <div className="edit-form">
                                        <div className="form-group">
                                            <label>Dosage</label>
                                            <input
                                                type="text"
                                                value={editForm.dosage}
                                                onChange={(e) => setEditForm({...editForm, dosage: e.target.value})}
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Schedule</label>
                                            <Dropdown
                                                value={editForm.schedule}
                                                options={scheduleOptions}
                                                onChange={(e) => setEditForm({...editForm, schedule: e.value})}
                                                className="w-full"
                                            />
                                        </div>
                                        <div className="form-group">
                                            <label>Notes</label>
                                            <textarea
                                                value={editForm.notes}
                                                onChange={(e) => setEditForm({...editForm, notes: e.target.value})}
                                                rows="2"
                                            />
                                        </div>
                                        <div className="edit-actions">
                                            <Button label="Cancel" onClick={onCancelEdit} className="p-button-secondary p-button-sm" />
                                            <Button label="Save" onClick={() => onSaveEdit(med.id)} className="p-button-primary p-button-sm" />
                                        </div>
                                    </div>
                                ) : (
                                    <>
                                        <div className="med-detail">
                                            {med.dosage && <span>{med.dosage}</span>}
                                            {med.dosage && med.schedule && <span> • </span>}
                                            {med.schedule && <span>{med.schedule}</span>}
                                        </div>
                                        {med.nextDoseTime && (
                                            <div className={`med-next-dose ${med.isOverdue ? 'overdue-text' : ''}`}>
                                                {med.isOverdue ? '⚠️ Overdue' : 'Next dose'}: {dayjs(med.nextDoseTime).fromNow()}
                                            </div>
                                        )}
                                        {med.lastAdministeredAt && (
                                            <div className="med-last-admin">
                                                Last given: {dayjs(med.lastAdministeredAt).fromNow()}
                                            </div>
                                        )}
                                        {med.notes && (
                                            <div className="med-notes">
                                                <i className="pi pi-info-circle"></i> {med.notes}
                                            </div>
                                        )}
                                        <div className="med-actions">
                                            <Button
                                                label="Administer"
                                                onClick={() => {
                                                    onStatusChange(med.id, 'ADMINISTERED');
                                                }}
                                                disabled={updatingMedId === med.id || med.inventoryQuantity === 0 || med.intakeStatus === 'Administered'}
                                                className="p-button-primary p-button-sm administer-btn"
                                                icon="pi pi-check"
                                            />
                                            <Dropdown
                                                value={null}
                                                options={otherStatusOptions}
                                                onChange={(e) => {
                                                    if (e.value) {
                                                        onStatusChange(med.id, e.value);
                                                    }
                                                }}
                                                disabled={updatingMedId === med.id}
                                                className="other-status-dropdown"
                                                placeholder="Other"
                                                panelClassName="status-dropdown-panel"
                                                showClear={false}
                                            />
                                            <span className={`current-status-text status-${(med.intakeStatus || 'Pending').toLowerCase()}`}>
                                                {med.intakeStatus || 'Pending'}
                                            </span>
                                        </div>
                                    </>
                                )}
                            </div>);
                        })
                    )}

                </div>
            )}
        </div>
    );
}

function InfoRow({ label, value }) {
    return (
        <div className="info-row">
            <span className="label">{label}</span>
            <span className="value">{value || "N/A"}</span>
        </div>
    );
}

function ServiceRow({ label, value }) {
    return (
        <div className="service-item">
            <span>{label}</span>
            <span className="service-value">{value || "N/A"}</span>
        </div>
    );
}

function List({ items, emptyText }) {
    if (!items || items.length === 0) {
        return <div className="empty-note">{emptyText}</div>;
    }
    return (
        <div className="item-list">
            {items.map((item, idx) => (
                <div className="list-item" key={idx}>
                    <span>{item}</span>
                </div>
            ))}
        </div>
    );
}

// for displaying detailed lists like allergies and diagnoses
function DetailedList({ items, onDelete, emptyText, type }) {
    if (!items || items.length === 0) {
        return <div className="empty-note">{emptyText}</div>;
    }

    const getSeverityBadge = (severity) => {
        if (severity === 3) return <span className="severity-badge severe">Severe</span>;
        if (severity === 2) return <span className="severity-badge moderate">Moderate</span>;
        return <span className="severity-badge mild">Mild</span>;
    };

    return (
        <div className="item-list detailed">
            {items.map((item) => (
                <div className="list-item detailed-item" key={item.id}>
                    <div className="item-content">
                        <div className="item-header">
                            <span className="item-name">
                                {type === 'allergy' ? item.allergyType : item.diagnosis}
                            </span>
                            {type === 'allergy' && item.severity && getSeverityBadge(item.severity)}
                        </div>
                        {item.notes && (
                            <div className="item-notes">
                                <i className="pi pi-info-circle"></i> {item.notes}
                            </div>
                        )}
                    </div>
                    <button
                        className="delete-btn"
                        onClick={() => onDelete(item.id)}
                        title="Delete"
                    >
                        <i className="pi pi-trash"></i>
                    </button>
                </div>
            ))}
        </div>
    );
}
