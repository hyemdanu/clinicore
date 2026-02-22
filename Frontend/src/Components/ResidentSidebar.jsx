import PropTypes from "prop-types";
import { useLocation, useNavigate } from "react-router-dom";
import "./css/ResidentSidebar.css";

// PNG ICONS (paths are from /src/Components/)
import dashboardIcon from "../assets/icons/dashboardicon.png";
import userIcon from "../assets/icons/usericon.png";
import residentIcon from "../assets/icons/residenticon.png";
import messagesIcon from "../assets/icons/Messageicon.png";
import inventoryIcon from "../assets/icons/inventoryicon.png";

export default function ResidentSidebar({ isOpen, onToggle }) {
    const navigate = useNavigate();
    const { pathname } = useLocation();

    const items = [
        { label: "Dashboard",       icon: dashboardIcon, to: "/resident" },
        { label: "User",            icon: userIcon,      to: "/resident/profile" },
        { label: "Medical Profile", icon: residentIcon,  to: "/resident/medical-profile" },
        { label: "Medication",      icon: inventoryIcon, to: "/resident/medications" },
        { label: "Messages",        icon: messagesIcon,  to: "/resident/messages" },
        { label: "Documents",       icon: dashboardIcon, to: "/resident/documents" }, // placeholder
    ];

    // logout handler - clears user data and sends back to login
    const handleLogout = () => {
        localStorage.removeItem('currentUser');
        navigate('/');
    };

    return (
        <aside className={`resident-sidebar ${isOpen ? "resident-sidebar-open" : "resident-sidebar-closed"}`}>
            <div className="resident-sidebar-nav">
                {/* Toggle row */}
                <div className="resident-sidebar-item" onClick={onToggle}>
                    <div className="resident-sidebar-icon"><i className="pi pi-bars" /></div>
                    <span className="resident-sidebar-label">Resident</span>
                </div>

                {items.map((it) => {
                    const active = pathname === it.to;
                    return (
                        <div
                            key={it.to}
                            className={`resident-sidebar-item ${active ? "resident-sidebar-item-active" : ""}`}
                            onClick={() => navigate(it.to)}
                        >
                            <div className="resident-sidebar-icon">
                                <img src={it.icon} alt="" />
                            </div>
                            <span className="resident-sidebar-label">{it.label}</span>
                        </div>
                    );
                })}
            </div>

            {/* logout button at the bottom */}
            <div className="sidebar-logout" onClick={handleLogout}>
                <div className="resident-sidebar-icon">
                    <i className="pi pi-sign-out" style={{ fontSize: '24px', color: '#e74c3c' }}></i>
                </div>
                <span className="resident-sidebar-label" style={{ color: '#e74c3c' }}>Logout</span>
            </div>
        </aside>
    );
}

ResidentSidebar.propTypes = {
    isOpen: PropTypes.bool.isRequired,
    onToggle: PropTypes.func.isRequired,
};
