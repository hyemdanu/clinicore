import React, { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import { get } from "../../services/api";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";

import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";

import "./css/ResidentDashboard.css"; // reuse layout styles
import "./css/ResidentMedication.css";

export default function ResidentMedication() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(true);
    const toggleSidebar = () => setSidebarOpen((s) => !s);

    const [resident, setResident] = useState(null);

    const [medications, setMedications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [sortField, setSortField] = useState("medication_name");
    const [sortOrder, setSortOrder] = useState("asc");

    const sortOptions = [
        { label: "Name (A-Z)", value: "medication_name", order: "asc" },
        { label: "Name (Z-A)", value: "medication_name", order: "desc" },
        { label: "Scheduled Time", value: "scheduled_time", order: "asc" },
        { label: "Status", value: "intake_status", order: "asc" },
    ];



    const fetchData = async () => {
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

            // Optional role gate (matches Dashboard)
            if (currentUser.role && currentUser.role !== "RESIDENT") {
                navigate("/not-authorized");
                return;
            }

            const results = await Promise.allSettled([
                get(`/residents/profile?currentUserId=${currentUserId}`),
                get(`/residents/medications?currentUserId=${currentUserId}`),
            ]);

            const val = (i, fb) => (results[i].status === "fulfilled" ? results[i].value : fb);

            const profile = val(0, currentUser);
            const meds = val(1, []);

            setResident(profile);
            setMedications(Array.isArray(meds) ? meds : []);

            // Only show a user-facing error in production and only if everything failed
            const inProd = import.meta?.env?.MODE === "production";
            const allFailed = results.every((r) => r.status === "rejected");

            if (inProd && allFailed) {
                setError("We couldn’t load your medications. Showing sample data.");
                loadSampleData();
            }
        } catch (e) {
            console.error(e);
            setError(null);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, [navigate]);

    const handleSortChange = (e) => {
        const value = e.target.value;
        const selectedOption = sortOptions.find((o) => `${o.value}_${o.order}` === value);
        if (selectedOption) {
            setSortField(selectedOption.value);
            setSortOrder(selectedOption.order);
        }
    };

    const getStatusBadge = (status) => {
        const statusConfig = {
            ADMINISTERED: { label: "Administered", class: "status-badge administered" },
            WITHHELD: { label: "Withheld", class: "status-badge withheld" },
            MISSED: { label: "Missed", class: "status-badge missed" },
        };

        const config = statusConfig[status] || {
            label: "Pending",
            class: "status-badge pending",
        };

        return <span className={config.class}>{config.label}</span>;
    };

    const getScheduleDisplay = (frequency, scheduledTime) => `${frequency} • ${scheduledTime}`;

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

            const aNum = Number(aValue);
            const bNum = Number(bValue);
            if (!Number.isNaN(aNum) && !Number.isNaN(bNum)) {
                return sortOrder === "asc" ? aNum - bNum : bNum - aNum;
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
                        <h2 className="dashboard-title">Medication</h2>

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
                                <label htmlFor="sort-dropdown">Sort by:</label>
                                <select
                                    id="sort-dropdown"
                                    className="sort-dropdown"
                                    onChange={handleSortChange}
                                    value={`${sortField}_${sortOrder}`}
                                >
                                    {sortOptions.map((option, index) => (
                                        <option key={index} value={`${option.value}_${option.order}`}>
                                            {option.label}
                                        </option>
                                    ))}
                                </select>

                                <button className="refresh-button" onClick={handleRefresh} disabled={loading}>
                                    Refresh
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
                                            <th>Scheduled Day and Time</th>
                                            <th>Log Intake Status</th>
                                            <th>Notes</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        {sortedMedications.length === 0 ? (
                                            <tr>
                                                <td colSpan="4" className="empty-message">
                                                    No medications found
                                                </td>
                                            </tr>
                                        ) : (
                                            sortedMedications.map((medication) => (
                                                <tr key={medication.id ?? medication.medication_name}>
                                                    <td>{medication.medication_name}</td>
                                                    <td>{getScheduleDisplay(medication.frequency, medication.scheduled_time)}</td>
                                                    <td>{getStatusBadge(medication.intake_status)}</td>
                                                    <td>{medication.notes || "No notes"}</td>
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
