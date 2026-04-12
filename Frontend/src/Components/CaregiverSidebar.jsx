import { useNavigate } from 'react-router-dom';
import './css/CaregiverSidebar.css';
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import userIcon from '../assets/icons/usericon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import documentsIcon from '../assets/icons/documentsIcon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';

const CaregiverSidebar = ({ isOpen, activeTab, onNavigate }) => {
    const navigate = useNavigate();

    const menuItems = [
        { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
        { id: 'residents', label: 'Residents', icon: residentIcon },
        { id: 'profile', label: 'User', icon: userIcon },
        { id: 'messages', label: 'Messages', icon: messageIcon },
        { id: 'documents', label: 'Documents', icon: documentsIcon },
        { id: 'inventory', label: 'Inventory', icon: inventoryIcon }
    ];

    const handleLogout = () => {
        localStorage.removeItem('currentUser');
        navigate('/');
    };

    const handleItemClick = (itemId) => {
        if (onNavigate) {
            onNavigate(itemId);
        }
    };

    return (
        <>
            {isOpen && (
                <button
                    className="sidebar-backdrop"
                    onClick={() => {}}
                    aria-label="Close sidebar"
                    type="button"
                />
            )}

            <aside className={`caregiver-sidebar ${isOpen ? 'caregiver-sidebar-open' : 'caregiver-sidebar-closed'}`}>
                <nav className="caregiver-sidebar-nav">
                    {menuItems.map((item) => (
                        <div
                            key={item.id}
                            className={`caregiver-sidebar-item ${
                                activeTab === item.id ? 'caregiver-sidebar-item-active' : ''
                            }`}
                            onClick={() => handleItemClick(item.id)}
                        >
                            <div className="caregiver-sidebar-icon">
                                <img src={item.icon} alt={item.label} />
                            </div>

                            {isOpen && (
                                <span className="caregiver-sidebar-label">
                                    {item.label}
                                </span>
                            )}
                        </div>
                    ))}
                </nav>

                <button
                    className="sidebar-logout"
                    onClick={handleLogout}
                    aria-label="Log out"
                    type="button"
                >
                    <div className="caregiver-sidebar-icon">
                        <i className="pi pi-sign-out sidebar-logout-icon" />
                    </div>

                    {isOpen && (
                        <span className="caregiver-sidebar-label">
                            Logout
                        </span>
                    )}
                </button>
            </aside>
        </>
    );
};

export default CaregiverSidebar;