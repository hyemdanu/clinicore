import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";
import { get } from "../../services/api";

import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";

import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";
import femaleDefaultPfp from "../../assets/icons/femaleDefault.png";
import maleDefaultPfp from "../../assets/icons/maleDefault.png";


// Reuse the dashboard layout shell styles so the header/sidebar match across pages
import "./css/ResidentDashboard.css";
import "./css/ResidentUserProfile.css";

import defaultAvatar from "../../assets/icons/usericon.png";

export default function ResidentUserProfile() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [loading, setLoading] = useState(true);
    const [resident, setResident] = useState(null);

    const toggleSidebar = () => setSidebarOpen((s) => !s);

    useEffect(() => {
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

    const view = useMemo(() => {
        if (!resident) return null;

        const fullName = [resident.firstName, resident.lastName].filter(Boolean).join(" ");
        const dob = resident.birthday ? new Date(resident.birthday).toLocaleDateString() : "—";

        const emergencyName = resident.emergencyContactName ?? resident.emergency_contact_name;
        const emergencyPhone = resident.emergencyContactNumber ?? resident.emergency_contact_number;
        const emergency =
            emergencyName || emergencyPhone
                ? `${emergencyName || ""}${emergencyName && emergencyPhone ? " — " : ""}${emergencyPhone || ""}`
                : "—";

        const phone = resident.contactNumber ?? resident.phone ?? "—";
        const gender = resident.gender ?? "—";
        const username = resident.username ?? "—";

        // If facilities and room ever get added, utilize this, for now ignore or delete this eventually
        const facility = resident.facility ?? resident.facilityName ?? "—";
        const room = resident.roomNumber ?? resident.room ?? "—";

        // Allow backend-provided avatar URL if present; otherwise default icon.
        const genderValue = (resident.gender ?? "").toString().trim().toLowerCase();

        const picture =
            genderValue === "female"
                ? femaleDefaultPfp
                : genderValue === "male"
                    ? maleDefaultPfp
                    : defaultAvatar; // fallback if gender missing/other



        return { fullName: fullName || "—", dob, phone, gender, emergency, facility, room, username, picture };
    }, [resident]);

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title="User Profile" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""}`}>
                <div className="alert-section">
                    <h2 className="dashboard-title">Your Profile</h2>
                </div>

                {loading ? (
                    <div className="rd-skeleton-list" style={{ maxWidth: 720 }}>
                        <div className="rd-skel-line" />
                        <div className="rd-skel-line" />
                        <div className="rd-skel-line" />
                    </div>
                ) : !view ? (
                    <div className="error-message">We couldn’t load your profile. Please try again.</div>
                ) : (
                    <div className="resident-user-profile-page">
                        <div className="box">
                            <div className="profile-left">
                                <img
                                    src={view.picture}
                                    alt="Profile"
                                    className="profile-picture"
                                    onError={(e) => {
                                        e.currentTarget.src = defaultAvatar;
                                    }}
                                />
                            </div>

                            <div className="profile-middle">
                                <div className="profile-info">
                                    <p>
                                        <strong>Name:</strong>
                                    </p>
                                    <p>{view.fullName}</p>
                                </div>
                                <div className="profile-info">
                                    <p>
                                        <strong>Date of Birth:</strong>
                                    </p>
                                    <p>{view.dob}</p>
                                </div>
                                <div className="profile-info">
                                    <p>
                                        <strong>Contact Info:</strong>
                                    </p>
                                    <p>{view.phone}</p>
                                </div>
                                <div className="profile-info">
                                    <p>
                                        <strong>Username:</strong>
                                    </p>
                                    <p>{view.username}</p>
                                </div>
                            </div>

                            <div className="profile-right">
                                <div className="profile-info">
                                    <p>
                                        <strong>Gender:</strong>
                                    </p>
                                    <p>{view.gender}</p>
                                </div>
                                <div className="profile-info">
                                    <p>
                                        <strong>Emergency Contact:</strong>
                                    </p>
                                    <p>{view.emergency}</p>
                                </div>
                                <div className="profile-info">
                                    <p>
                                        <strong>Facility:</strong>
                                    </p>
                                    <p>{view.facility}</p>
                                </div>
                                <div className="profile-info">
                                    <p>
                                        <strong>Room Number:</strong>
                                    </p>
                                    <p>{view.room}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}
