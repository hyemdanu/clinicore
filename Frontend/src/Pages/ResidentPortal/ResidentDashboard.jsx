import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";

import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";
import "./css/ResidentDashboard.css";

// PNG ICONS (paths are from /src/Pages/ResidentPortal/)
import userIcon from "../../assets/icons/usericon.png";
import medProfileIcon from "../../assets/icons/residenticon.png";   // using resident icon for Medical Profile
import medicationIcon from "../../assets/icons/inventoryicon.png"; // pill-ish icon
import messagesIcon from "../../assets/icons/Messageicon.png";
import documentsIcon from "../../assets/icons/dashboardicon.png";   // placeholder for Documents

export default function ResidentDashboard() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);

    const [resident, setResident] = useState(null);
    const [unreadCount, setUnreadCount] = useState(0);
    const [activeMedCount, setActiveMedCount] = useState(0);
    const [recentDocs, setRecentDocs] = useState([]);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

    useEffect(() => {
        (async () => {
            setLoading(true);
            setError(null);

            try {
                const currentUserStr = localStorage.getItem("currentUser");
                if (!currentUserStr) { navigate("/"); return; }

                const currentUser = JSON.parse(currentUserStr);
                const currentUserId = currentUser.id;

                // Optional role gate
                if (currentUser.role && currentUser.role !== "RESIDENT") {
                    navigate("/not-authorized");
                    return;
                }

                // fire everything, don't block on failures
                const results = await Promise.allSettled([
                    get(`/user/${currentUserId}/profile?currentUserId=${currentUserId}`),
                    get(`/residents/full/${currentUserId}?currentUserId=${currentUserId}`),
                    get(`/messages/chat/unread-count?userId=${currentUserId}`),
                    get(`/documents/resident/${currentUserId}?userId=${currentUserId}`),
                ]);

                const val = (i, fb) => (results[i].status === "fulfilled" ? results[i].value : fb);

                const profile      = val(0, currentUser);
                const residentData = val(1, null);
                const unread       = val(2, null);
                const docs         = val(3, []);

                setResident(profile);

                // med count comes from the full resident object
                const meds = residentData?.medications;
                setActiveMedCount(Array.isArray(meds) ? meds.length : 0);

                setUnreadCount(
                    typeof unread === "number"
                        ? unread
                        : unread?.unreadCount ?? 0
                );

                // sort newest first so "recent 3" actually means recent
                const sortedDocs = Array.isArray(docs)
                    ? [...docs].sort((a, b) =>
                        new Date(b.updatedAt || b.createdAt || 0) -
                        new Date(a.updatedAt || a.createdAt || 0)
                      )
                    : [];
                setRecentDocs(sortedDocs);

                // Only show the red banner in PRODUCTION and only if ALL failed
                const inProd = (import.meta?.env?.MODE === "production");
                const allFailed = results.every(r => r.status === "rejected");
                setError(inProd && allFailed ? "We couldn’t load your dashboard. Please try again." : null);
            } catch (e) {
                console.error(e);
                // Keep UI clean if something unexpected throws
                setError(null);
            } finally {
                setLoading(false);
            }
        })();
    }, [navigate]);


    const cards = [
        { label: "User",            img: userIcon,       description: "View your profile and account details", to: "/resident/profile" },
        { label: "Medical Profile", img: medProfileIcon, description: "Conditions, allergies, care plan",      to: "/resident/medical-profile" },
        { label: "Medication",      img: medicationIcon, description: `Active medications${activeMedCount ? ` • ${activeMedCount}` : ""}`, to: "/resident/medications" },
        { label: "Messages",        img: messagesIcon,   description: unreadCount > 0 ? `Unread • ${unreadCount}` : "Messages with caregivers", to: "/resident/messages", badge: unreadCount > 0 ? unreadCount : null },
        { label: "Documents",       img: documentsIcon,  description: "Care notes, forms, uploads",            to: "/resident/documents" },
    ];

    return (
        <div className="admin-dashboard-container">{/* reuse admin layout css */}
            <Header onToggleSidebar={toggleSidebar} title="Dashboard" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                <div className="alert-section">
                    <h2 className="dashboard-title">
                        Hi {resident?.firstName ? `${resident.firstName} ${resident.lastName || ""}`.trim() : "there"}!
                    </h2>
                </div>

                {/* Only show this if literally everything failed */}
                {error && <div className="error-message">{error}</div>}

                {/* Primary cards */}
                <section className="rd-card-grid">
                    {cards.map((c) => (
                        <button
                            key={c.label}
                            className="rd-action-card"
                            onClick={() => navigate(c.to)}
                            disabled={loading}
                        >
                            <img src={c.img} alt="" className="rd-card-img" />
                            <div className="rd-card-text">
                                <div className="rd-card-title-row">
                                    <span className="rd-card-title">{c.label}</span>
                                    {c.badge ? <span className="rd-badge">{c.badge}</span> : null}
                                </div>
                                <span className="rd-card-desc">{c.description}</span>
                            </div>
                            <span className="pi pi-angle-right rd-card-caret" aria-hidden />
                        </button>
                    ))}
                </section>

                {/* Secondary panels */}
                <section className="rd-secondary">
                    <div className="rd-panel">
                        <div className="rd-panel-head">
                            <h3>Recent Documents</h3>
                            <button className="rd-link-button" onClick={() => navigate("/resident/documents")}>
                                View all
                            </button>
                        </div>

                        {loading ? (
                            <div className="rd-skeleton-list">
                                <div className="rd-skel-line" />
                                <div className="rd-skel-line" />
                                <div className="rd-skel-line" />
                            </div>
                        ) : recentDocs.length === 0 ? (
                            <div className="rd-empty">No documents yet</div>
                        ) : (
                            <ul className="rd-doc-list">
                                {recentDocs.slice(0, 3).map((d) => (
                                    <li
                                        key={d.id || d.title}
                                        className="rd-doc-item"
                                        onClick={() => navigate(`/resident/documents/${d.id}`)}
                                    >
                                        <span className="pi pi-file rd-doc-icon" aria-hidden />
                                        <div className="rd-doc-meta">
                                            <span className="rd-doc-title">{d.title || "Untitled"}</span>
                                            <span className="rd-doc-sub">
                        {d.type || "Document"} • {d.updatedAt ? new Date(d.updatedAt).toLocaleDateString() : "Recently updated"}
                      </span>
                                        </div>
                                        <span className="pi pi-angle-right rd-doc-caret" aria-hidden />
                                    </li>
                                ))}
                            </ul>
                        )}
                    </div>

                    <div className="rd-panel">
                        <div className="rd-panel-head">
                            <h3>Medication Summary</h3>
                            <button className="rd-link-button" onClick={() => navigate("/resident/medications")}>
                                Manage
                            </button>
                        </div>
                        {loading ? (
                            <div className="rd-skeleton-list">
                                <div className="rd-skel-line" />
                                <div className="rd-skel-line" />
                            </div>
                        ) : (
                            <div className="rd-kpis">
                                <div className="rd-kpi">
                                    <span className="rd-kpi-value">{activeMedCount}</span>
                                    <span className="rd-kpi-label">Active meds</span>
                                </div>
                                <div className="rd-kpi">
                                    <span className="rd-kpi-value">{unreadCount}</span>
                                    <span className="rd-kpi-label">Unread msgs</span>
                                </div>
                            </div>
                        )}
                    </div>
                </section>
            </main>
        </div>
    );
}
