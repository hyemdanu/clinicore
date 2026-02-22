import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";
import "./css/ResidentDashboard.css";
import "./css/ResidentUserProfile.css";

export default function ResidentUserProfile() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [loading, setLoading] = useState(true);
    const [resident, setResident] = useState(null);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

    useEffect(() => {
        // grab user info when page loads
        (async () => {
            setLoading(true);
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

                const profile = await get(`/user/${currentUserId}/profile?currentUserId=${currentUserId}`);
                console.log("PROFILE RETURNED:", profile);
                setResident(profile);

            } catch (e) {
                console.error(e);
                setResident(null);
            } finally {
                setLoading(false);
            }
        })();
    }, [navigate]);

    // get initials for the avatar
    const getInitials = () => {
        if (!resident) return "?";
        const first = resident.firstName?.charAt(0) || "";
        const last = resident.lastName?.charAt(0) || "";
        return (first + last).toUpperCase() || "?";
    };

    if (loading) {
        return (
            <div className="admin-dashboard-container">
                <Header onToggleSidebar={toggleSidebar} title="User Profile" />
                <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
                <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                    <div className="profile-loading">
                        <i className="pi pi-spin pi-spinner" style={{ fontSize: '2rem' }}></i>
                        <p>Loading your profile...</p>
                    </div>
                </main>
            </div>
        );
    }

    if (!resident) {
        return (
            <div className="admin-dashboard-container">
                <Header onToggleSidebar={toggleSidebar} title="User Profile" />
                <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />
                <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                    <div className="error-message">
                        We couldn't load your profile. Please try again.
                    </div>
                </main>
            </div>
        );
    }

    const fullName = [resident.firstName, resident.lastName].filter(Boolean).join(" ");
    const firstName = resident.firstName || "—";
    const lastName = resident.lastName || "—";
    const dob = resident.birthday ? new Date(resident.birthday).toLocaleDateString() : "—";
    const emergencyName = resident.emergencyContactName ?? resident.emergency_contact_name;
    const emergencyPhone = resident.emergencyContactNumber ?? resident.emergency_contact_number;
    const emergency = emergencyName || emergencyPhone
        ? `${emergencyName || ""}${emergencyName && emergencyPhone ? " — " : ""}${emergencyPhone || ""}`
        : "—";
    const phone = resident.contactNumber ?? resident.phone ?? "—";
    const email = resident.email ?? "—";
    const gender = resident.gender ?? "—";
    const username = resident.username ?? "—";

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="User Profile" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                <div className="user-profile-page">
                    <div className="alert-section">
                        <h2 className="dashboard-title">Your Profile</h2>
                        <p className="profile-subtitle">View your account information</p>
                    </div>

                    <div className="profile-container">
                        {/* top section with avatar and basic info */}
                        <div className="profile-header">
                            <div className="profile-avatar-section">
                                <div className="profile-avatar-initials">
                                    {getInitials()}
                                </div>
                                <div className="profile-avatar-info">
                                    <h2>{fullName || "—"}</h2>
                                    <p className="profile-role-badge">RESIDENT</p>
                                </div>
                            </div>
                        </div>

                        {/* main info cards */}
                        <div className="profile-info-grid">
                            {/* personal info card */}
                            <div className="profile-info-card">
                                <div className="card-title-row">
                                    <h3 className="card-title">
                                        <i className="pi pi-user"></i> Personal Information
                                    </h3>
                                </div>

                                <div className="info-row">
                                    <span className="info-label">First Name</span>
                                    <span className="info-value">{firstName}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Last Name</span>
                                    <span className="info-value">{lastName}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Username</span>
                                    <span className="info-value">{username}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Email</span>
                                    <span className="info-value">{email}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Phone Number</span>
                                    <span className="info-value">{phone}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Emergency Contact</span>
                                    <span className="info-value">{emergency}</span>
                                </div>
                            </div>

                            {/* additional info card */}
                            <div className="profile-info-card">
                                <h3 className="card-title">
                                    <i className="pi pi-info-circle"></i> Additional Details
                                </h3>

                                <div className="info-row">
                                    <span className="info-label">Date of Birth</span>
                                    <span className="info-value">{dob}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Gender</span>
                                    <span className="info-value">{gender}</span>
                                </div>
                                <div className="info-row">
                                    <span className="info-label">Account Type</span>
                                    <span className="info-value">Resident</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
