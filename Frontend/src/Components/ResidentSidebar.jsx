import PropTypes from "prop-types";
import { useLocation, useNavigate } from "react-router-dom";
import "./css/ResidentSidebar.css";

import dashboardIcon from "../assets/icons/dashboardicon.png";
import userIcon from "../assets/icons/usericon.png";
import residentIcon from "../assets/icons/residenticon.png";
import messagesIcon from "../assets/icons/Messageicon.png";
import inventoryIcon from "../assets/icons/inventoryicon.png";
import documentIcon from '../assets/icons/documentsIcon.png';

export default function ResidentSidebar({ isOpen, onToggle }) {
    const navigate = useNavigate();
    const { pathname } = useLocation();

    const items = [
        { label: "Dashboard", icon: dashboardIcon, to: "/resident" },
        { label: "User", icon: userIcon, to: "/resident/profile" },
        { label: "Medical Profile", icon: residentIcon, to: "/resident/medical-profile" },
        { label: "Medication", icon: inventoryIcon, to: "/resident/medications" },
        { label: "Messages", icon: messagesIcon, to: "/resident/messages" },
        { label: "Documents", icon: documentIcon, to: "/resident/documents" },
    ];

    const handleLogout = () => {
        localStorage.removeItem("currentUser");
        navigate("/");
    };

    const handleNavClick = (to) => {
        navigate(to);
        if (window.innerWidth <= 768) onToggle();
    };

    return (
        <>
            {isOpen && (
                <button
                    className="sidebar-backdrop"
                    onClick={onToggle}
                    aria-label="Close sidebar"
                    type="button"
                />
            )}

            <aside className={`resident-sidebar ${isOpen ? "resident-sidebar-open" : "resident-sidebar-closed"}`}>
                <div className="resident-sidebar-nav">
                    {items.map((it) => {
                        const active = pathname === it.to;

                        return (
                            <div
                                key={it.to}
                                className={`resident-sidebar-item ${active ? "resident-sidebar-item-active" : ""}`}
                                onClick={() => handleNavClick(it.to)}
                            >
                                <div className="resident-sidebar-icon">
                                    <img src={it.icon} alt="" />
                                </div>

                                {isOpen && (
                                    <span className="resident-sidebar-label">
                                    {it.label}
                                </span>
                                )}
                            </div>
                        );
                    })}
                </div>

                <button
                    className="sidebar-logout"
                    onClick={handleLogout}
                    aria-label="Log out"
                    type="button"
                >
                    <div className="resident-sidebar-icon">
                        <i className="pi pi-sign-out sidebar-logout-icon" />
                    </div>

                    {isOpen && (
                        <span className="resident-sidebar-label">
                        Logout
                    </span>
                    )}
                </button>
            </aside>
        </>
    );
}

ResidentSidebar.propTypes = {
    isOpen: PropTypes.bool.isRequired,
    onToggle: PropTypes.func.isRequired,
};