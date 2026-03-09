import React from "react";
import "./css/caregiver.css";
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { get } from '../../services/api';
import Header from '../../Components/Header';
import CaregiverSidebar from "../../Components/CaregiverSidebar.jsx";
import CaregiverResidentsTab from './CResidentsTab.jsx';
import CaregiverProfile from "./CaregiverProfile";
import CaregiverDocument from "./CaregiverDocument";
import MessagesTab from '../Shared/MessagesTab';
import InventoryLanding from '../Shared/InventoryLanding.jsx';
import MedicationInventory from '../Shared/CaregiverMedicationInventory.jsx';
import ConsumablesInventory from '../Shared/ConsumablesInventory.jsx';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';

export default function CaregiverDashboard() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
    const [activeTab, setActiveTab] = useState("dashboard");
    const [residents, setResidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const tabTitles = {
        dashboard: "Dashboard",
        residents: "Residents",
        profile: "User",
        messages: "Messages",
        documents: "Documents",
        inventory: "Inventory",
        'medication-inventory': "Medication Inventory",
        'consumables-inventory': "Medical Consumables Inventory"
    };

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const MedicationProgressCard = ({percent}) => {
        return (
            <div className="progress-card">
                <h3>Medication Tasks</h3>

                <div className="progress-header">
                    <span>Progress</span>
                    <span>{percent}%</span>
                </div>

                <div className="progress-bar">
                    <div
                        className="progress-fill"
                        style={{ width: `${percent}%` }}
                    />
                </div>

                <div className="date">October 1, 2025</div>
            </div>
        );
    };

    const TaskBadge = ({ label, color }) => {
        return (
            <div className="task-badge" style={{ backgroundColor: color }}>
                {label}
            </div>
        );
    };

    const ResidentRow = ({ resident }) => {
        const tasks = resident.tasks || [];
        return (
            <div className="resident-row">
                <div className="resident-info">
                    <div className="avatar" />
                    <span>{resident.name}</span>
                </div>

                <div className="task-container">
                    {tasks.map((task, index) => (
                        <TaskBadge
                            key={index}
                            label={task.label}
                            color={task.color}
                        />
                    ))}
                </div>
            </div>
        );
    };

    const fetchResidents = useCallback(async () => {
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

            const data = await get(`/user/residents/list?currentUserId=${currentUserId}`);

            const residentsWithTasks = await Promise.all(
                (data || []).map(async (r) => {
                    const medications = await get(
                        `/user/residents/medication/list?currentUserId=${currentUserId}&residentId=${r.id}`
                    );

                    const counts = {
                        ADMINISTERED: 0,
                        PENDING: 0,
                        MISSED: 0,
                        WITHHELD: 0
                    };

                    (medications || []).forEach((med) => {
                        const status = med.intakeStatus || "PENDING";
                        if (counts[status] !== undefined) {
                            counts[status] += 1;
                        }
                    });

                    return {
                        id: r.id,
                        name: `${r.firstName} ${r.lastName}`.trim(),
                        counts,
                        tasks: [
                            { label: `Taken ${counts.ADMINISTERED}`, color: "#2ecc71" },
                            { label: `Pending ${counts.PENDING}`, color: "#f1c40f" },
                            { label: `Missed ${counts.MISSED}`, color: "#e74c3c" },
                            { label: `Withheld ${counts.WITHHELD}`, color: "#8e44ad" }
                        ]
                    };
                })
            );

            setResidents(residentsWithTasks);
        } catch (err) {
            console.error("Error fetching residents:", err);
            setError("Failed to load residents. Please try again.");
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchResidents();
    }, [fetchResidents]);

    const totalTasks = residents.reduce((sum, r) => {
        if (!r.counts) return sum;
        return sum + r.counts.ADMINISTERED + r.counts.PENDING + r.counts.MISSED + r.counts.WITHHELD;
    }, 0);
    const completedTasks = residents.reduce((sum, r) => {
        if (!r.counts) return sum;
        return sum + r.counts.ADMINISTERED;
    }, 0);
    const progressPercent = totalTasks === 0 ? 0 : Math.round((completedTasks / totalTasks) * 100);

    const renderDashboardContent = () => (
        <div className="caregiver-dashboard-content">
            <MedicationProgressCard percent={progressPercent} />

            {loading && (
                <div className="caregiver-loading">
                    <i className="pi pi-spin pi-spinner caregiver-spinner"></i>
                    <span>Loading dashboard...</span>
                </div>
            )}

            {error && (
                <div className="caregiver-error">
                    {error}
                </div>
            )}

            {!loading && !error && (
                <>
                    <section className="residents-section">
                        <div className="section-header">
                            <h3>Residents</h3>
                        </div>
                        <DataTable
                            value={residents}
                            className="residents-table"
                            emptyMessage="No residents found"
                        >
                            <Column
                                header="Resident"
                                body={(rowData) => <ResidentRow resident={rowData} />}
                            />
                        </DataTable>
                    </section>
                </>
            )}
        </div>
    );

    const renderContent = () => {
        switch (activeTab) {
            case "residents":
                return <CaregiverResidentsTab />;
            case "profile":
                return <CaregiverProfile />;
            case "messages":
                return <MessagesTab />;
            case "documents":
                return <CaregiverDocument />;
            case "inventory":
                return <InventoryLanding onNavigate={setActiveTab} />;
            case "medication-inventory":
                return <MedicationInventory />;
            case "consumables-inventory":
                return <ConsumablesInventory />;
            case "dashboard":
            default:
                return renderDashboardContent();
        }
    };

    return (
        <div className={`dashboard-container ${activeTab === 'messages' ? 'messages-active' : ''}`}>
            <Header onToggleSidebar={toggleSidebar} title={tabTitles[activeTab] || "Dashboard"} />

            <CaregiverSidebar
                isOpen={sidebarOpen}
                activeTab={activeTab}
                onNavigate={setActiveTab}
            />

            <main className={`main-content ${sidebarOpen ? "content-with-sidebar" : ""} ${activeTab === 'messages' ? 'messages-content' : ''}`}>
                {renderContent()}
            </main>
        </div>
    );
}
