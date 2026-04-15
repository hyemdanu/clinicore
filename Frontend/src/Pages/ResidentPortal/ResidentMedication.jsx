import React, { useCallback, useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { Dropdown } from 'primereact/dropdown';
import { get } from "../../services/api";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";

import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";

import "./css/ResidentDashboard.css";
import "./css/ResidentMedication.css";

export default function ResidentMedication() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
    const toggleSidebar = () => setSidebarOpen((s) => !s);

    const [medications, setMedications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [sortField, setSortField] = useState("medication_name");
    const [sortOrder, setSortOrder] = useState("asc");

    const [sortValue, setSortValue] = useState('name_asc');
    const sortOptions = [
        { label: "Name (A-Z)", value: "name_asc" },
        { label: "Name (Z-A)", value: "name_desc" },
        { label: "Status", value: "status_asc" },
    ];

    const fetchData = useCallback(async () => {
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

            if (currentUser.role && currentUser.role !== "RESIDENT") {
                navigate("/not-authorized");
                return;
            }

            // Fetch full resident details including medications - same pattern as ResidentDetailModal
            const residentData = await get(`/residents/full/${currentUserId}?currentUserId=${currentUserId}`);

            // Format medications from the resident data
            const formattedMeds = (residentData.medications || []).map(med => ({
                id: med.id,
                medication_name: med.name,
                dosage: med.dosage,
                frequency: med.schedule,
                scheduled_time: med.scheduledTime,
                intake_status: med.intakeStatus,
                notes: med.notes || "No notes",
                lastAdministeredAt: med.lastAdministeredAt,
                nextDoseTime: med.nextDoseTime,
                isOverdue: med.isOverdue,
                inventoryQuantity: med.inventoryQuantity
            }));

            setMedications(formattedMeds);
        } catch (e) {
            console.error("Error fetching medications:", e);
            setError("Failed to load medications. Please try again.");
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchData();
    }, [fetchData]);

    const handleSortChange = (val) => {
        setSortValue(val);
        const map = {
            name_asc: { field: 'medication_name', order: 'asc' },
            name_desc: { field: 'medication_name', order: 'desc' },
            status_asc: { field: 'intake_status', order: 'asc' },
        };
        const s = map[val] || map.name_asc;
        setSortField(s.field);
        setSortOrder(s.order);
    };

    const getStatusBadge = (status) => {
        const statusConfig = {
            "Administered": { label: "Administered", class: "status-badge administered" },
            "Withheld": { label: "Withheld", class: "status-badge withheld" },
            "Missed": { label: "Missed", class: "status-badge missed" },
            "Pending": { label: "Pending", class: "status-badge pending" }
        };

        const config = statusConfig[status] || statusConfig["Pending"];
        return <span className={config.class}>{config.label}</span>;
    };

    const getScheduleDisplay = (frequency, scheduledTime) => {
        if (frequency && scheduledTime) {
            return `${frequency} • ${scheduledTime}`;
        }
        return frequency || scheduledTime || "Not scheduled";
    };

    const sortedMedications = useMemo(() => {
        const arr = Array.isArray(medications) ? [...medications] : [];

        return arr.sort((a, b) => {
            let aValue = a?.[sortField];
            let bValue = b?.[sortField];

            if (aValue === null || aValue === undefined) aValue = "";
            if (bValue === null || bValue === undefined) bValue = "";

            if (typeof aValue === "string" && typeof bValue === "string") {
                const comparison = aValue.localeCompare(bValue);
                return sortOrder === "asc" ? comparison : -comparison;
            }

            return 0;
        });
    }, [medications, sortField, sortOrder]);

    const handleRefresh = () => fetchData();

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="Medication" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                <div className="resident-medication-component">
                    <div className="alert-section">
                        <h2 className="dashboard-title">My Medications</h2>

                        {error && (
                            <div className="error-message">
                                <span>{error}</span>
                            </div>
                        )}
                    </div>

                    <div className="inventory-section">
                        <div className="inventory-header">
                            <h3>Current Medications</h3>
                            <div className="sort-dropdown-wrapper">
                                <label>Sort by:</label>
                                <Dropdown
                                    value={sortValue}
                                    options={sortOptions}
                                    onChange={(e) => handleSortChange(e.value)}
                                    className="sort-dropdown"
                                    appendTo="self"
                                />
                                <button className="refresh-button" onClick={handleRefresh} disabled={loading}>
                                    {loading ? "Loading..." : "Refresh"}
                                </button>
                            </div>
                        </div>

                        <div className="inventory-table">
                            {loading ? (
                                <div className="custom-loading">
                                    <span>Loading medications...</span>
                                </div>
                            ) : (
                                <div className="table-container">
                                    <table className="medications-table">
                                        <thead>
                                        <tr>
                                            <th>Medication</th>
                                            <th>Dosage</th>
                                            <th>Schedule</th>
                                            <th>Status</th>
                                            <th>Notes</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {sortedMedications.length === 0 ? (
                                            <tr>
                                                <td colSpan="5" className="empty-message">
                                                    No medications found
                                                </td>
                                            </tr>
                                        ) : (
                                            sortedMedications.map((medication) => (
                                                <tr key={medication.id}>
                                                    <td>{medication.medication_name}</td>
                                                    <td>{medication.dosage || "—"}</td>
                                                    <td>{getScheduleDisplay(medication.frequency, medication.scheduled_time)}</td>
                                                    <td>{getStatusBadge(medication.intake_status)}</td>
                                                    <td>{medication.notes}</td>
                                                </tr>
                                            ))
                                        )}
                                        </tbody>
                                    </table>
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
