import { useState, useEffect } from "react";
import { Button } from "primereact/button";
import { Dropdown } from "primereact/dropdown";
import { get, post } from "../../services/api";
import "./css/residents.css";

/*
 * this the modal in the sidebar when you wanna
 * add or edit a medication for a resident
 */

// scheudling options
const SCHEDULE_OPTIONS = [
    { label: 'Once daily', value: 'Once daily' },
    { label: 'Twice daily', value: 'Twice daily' },
    { label: 'Three times daily', value: 'Three times daily' },
    { label: 'Four times daily', value: 'Four times daily' },
    { label: 'Every 4 hours', value: 'Every 4 hours' },
    { label: 'Every 6 hours', value: 'Every 6 hours' },
    { label: 'Every 8 hours', value: 'Every 8 hours' },
    { label: 'Every 12 hours', value: 'Every 12 hours' },
    { label: 'Morning only', value: 'Morning only' },
    { label: 'Evening only', value: 'Evening only' },
    { label: 'Bedtime', value: 'Bedtime' },
    { label: 'Before meals', value: 'Before meals' },
    { label: 'After meals', value: 'After meals' },
    { label: 'As needed (PRN)', value: 'As needed (PRN)' }
];

// add medication
export default function AddMedicationForm({ residentId, onCancel, onMedicationAdded }) {
    const [formData, setFormData] = useState({
        medicationInventoryId: null,
        dosage: "",
        schedule: "",
        notes: ""
    });
    const [medications, setMedications] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        fetchMedications();
    }, []);

    const fetchMedications = async () => {
        try {
            const data = await get('/residents/medications/available');
            setMedications(data.map(m => ({
                label: `${m.name} (${m.quantity} available)`,
                value: m.id,
                dosage: m.dosagePerServing,
                ...m
            })));
        } catch (err) {
            console.error('Error fetching medications:', err);
        }
    };

    const handleChange = (name, value) => {
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleMedicationSelect = (medId) => {
        const selectedMed = medications.find(m => m.value === medId);
        if (selectedMed && selectedMed.dosage) {
            const dosageValue = selectedMed.dosage;
            const dosageWithUnit = dosageValue.includes('mg') ? dosageValue : `${dosageValue} mg`;
            setFormData(prev => ({
                ...prev,
                medicationInventoryId: medId,
                dosage: dosageWithUnit
            }));
        } else {
            setFormData(prev => ({ ...prev, medicationInventoryId: medId }));
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        try {
            if (!formData.medicationInventoryId) {
                throw new Error("Please select a medication");
            }
            if (!formData.schedule) {
                throw new Error("Please select a schedule");
            }

            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) throw new Error("User not authenticated");
            const currentUserId = JSON.parse(currentUserStr).id;

            const createdMedication = await post(
                `/residents/${residentId}/medications?currentUserId=${currentUserId}`,
                formData
            );

            if (onMedicationAdded) {
                onMedicationAdded(createdMedication);
            }

            onCancel();

        } catch (err) {
            console.error("Error creating medication:", err);
            setError(err.message || "Failed to create medication. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="add-medication-form">
            <div className="form-header">
                <h4>Add New Medication</h4>
            </div>

            <form onSubmit={handleSubmit}>
                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <div className="form-group">
                    <label>Medication <span className="required">*</span></label>
                    <Dropdown
                        value={formData.medicationInventoryId}
                        options={medications}
                        onChange={(e) => handleMedicationSelect(e.value)}
                        placeholder="Select medication"
                        filter
                        className="w-full"
                    />
                </div>

                <div className="form-group">
                    <label>Dosage</label>
                    <input
                        type="text"
                        value={formData.dosage}
                        onChange={(e) => handleChange('dosage', e.target.value)}
                        placeholder="e.g., 100 mg"
                    />
                </div>

                <div className="form-group">
                    <label>Schedule / Frequency <span className="required">*</span></label>
                    <Dropdown
                        value={formData.schedule}
                        options={SCHEDULE_OPTIONS}
                        onChange={(e) => handleChange('schedule', e.value)}
                        placeholder="Select schedule"
                        className="w-full"
                    />
                </div>

                <div className="form-group">
                    <label>Notes</label>
                    <textarea
                        value={formData.notes}
                        onChange={(e) => handleChange('notes', e.target.value)}
                        placeholder="Additional notes..."
                        rows="3"
                    />
                </div>

                <div className="form-actions">
                    <Button
                        type="button"
                        label="Cancel"
                        className="p-button-secondary p-button-sm"
                        onClick={onCancel}
                        disabled={loading}
                    />
                    <Button
                        type="submit"
                        label={loading ? "Adding..." : "Add Medication"}
                        className="p-button-primary p-button-sm"
                        disabled={loading}
                        icon={loading ? "pi pi-spin pi-spinner" : "pi pi-check"}
                    />
                </div>
            </form>
        </div>
    );
}
