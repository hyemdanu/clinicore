import React from "react";
import "./css/caregiver.css";
import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '../../services/api';
import Header from '../../Components/Header';
import CaregiverSidebar from "../../Components/CaregiverSidebar.jsx";
import CaregiverResidentsTab from './CResidentsTab.jsx';
import CaregiverProfile from "./CaregiverProfile";
import CaregiverDocument from "./CaregiverDocument";
import MessagesTab from '../Shared/MessagesTab';
import InventoryLanding from '../Shared/InventoryLanding.jsx';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';

export default function CaregiverDashboard() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
    const [activeTab, setActiveTab] = useState("dashboard");
    const [selectedResidentId, setSelectedResidentId] = useState(null);
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

    const handleNavigate = (tab, residentId = null) => {
        setActiveTab(tab);
        setSelectedResidentId(residentId);
        window.history.pushState({ tab }, '', `#${tab}`);
    };

    // Handle browser back/forward buttons
    useEffect(() => {
        const handlePopState = (e) => {
            const tab = e.state?.tab || 'dashboard';
            setActiveTab(tab);
        };
        window.addEventListener('popstate', handlePopState);
        if (!window.history.state?.tab) {
            window.history.replaceState({ tab: activeTab }, '', `#${activeTab}`);
        }
        return () => window.removeEventListener('popstate', handlePopState);
    }, []);

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

                <div className="date">{new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</div>
            </div>
        );
    };

    const ResidentRow = ({ resident }) => {
        const counts = resident.counts || {};
        return (
            <button
                type="button"
                className="dash-resident-card btn-reset"
                onClick={() => handleNavigate('residents', resident.id)}
            >
                <div className="dash-resident-info">
                    <div className="resident-avatar">
                        <i className="pi pi-user"></i>
                    </div>
                    <span className="resident-name">{resident.name}</span>
                </div>
                <div className="dash-badges">
                    <span className="dash-badge taken"><span className="badge-label">Taken </span>{counts.ADMINISTERED}</span>
                    <span className="dash-badge pending"><span className="badge-label">Pending </span>{counts.PENDING}</span>
                    <span className="dash-badge missed"><span className="badge-label">Missed </span>{counts.MISSED}</span>
                    <span className="dash-badge withheld"><span className="badge-label">Withheld </span>{counts.WITHHELD}</span>
                </div>
                <i className="pi pi-chevron-right dash-chevron"></i>
            </button>
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

            // lightweight endpoint — returns only names + medication status counts
            const data = await get(`/residents/medication-summary?currentUserId=${currentUserId}`);

            const residentsWithTasks = (data || []).map((r) => {
                const counts = r.medicationCounts || {
                    ADMINISTERED: 0, PENDING: 0, MISSED: 0, WITHHELD: 0
                };

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
            });

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

    const { progressPercent } = useMemo(() => {
        const total = residents.reduce((sum, r) => {
            if (!r.counts) return sum;
            return sum + r.counts.ADMINISTERED + r.counts.PENDING + r.counts.MISSED + r.counts.WITHHELD;
        }, 0);
        const completed = residents.reduce((sum, r) => {
            if (!r.counts) return sum;
            return sum + r.counts.ADMINISTERED;
        }, 0);
        const percent = total === 0 ? 0 : Math.round((completed / total) * 100);
        return { totalTasks: total, completedTasks: completed, progressPercent: percent };
    }, [residents]);

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
                        <div className="dash-residents-list">
                            {residents.length === 0 ? (
                                <div className="no-residents">No residents found</div>
                            ) : (
                                residents.map((r) => <ResidentRow key={r.id} resident={r} />)
                            )}
                        </div>
                    </section>
                </>
            )}
        </div>
    );

    const renderContent = () => {
        switch (activeTab) {
            case "residents":
                return <CaregiverResidentsTab initialResidentId={selectedResidentId} />;
            case "profile":
                return <CaregiverProfile />;
            case "messages":
                return <MessagesTab />;
            case "documents":
                return <CaregiverDocument />;
            case "inventory":
                return <InventoryLanding />;
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
                onNavigate={handleNavigate}
            />

            <main className={`main-content ${sidebarOpen ? "content-with-sidebar" : ""} ${activeTab === 'messages' ? 'messages-content' : ''}`}>
                {renderContent()}
            </main>
        </div>
    );
}
