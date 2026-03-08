import { useNavigate } from 'react-router-dom';
import './css/AdminSidebar.css';
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import userIcon from '../assets/icons/usericon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import documentsIcon from '../assets/icons/documentsicon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';

const CaregiverSidebar = ({ isOpen, activeTab, onNavigate, onToggle }) => {
    const navigate = useNavigate();

    const menuItems = [
        { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
        { id: 'residents', label: 'Residents', icon: residentIcon },
        { id: 'profile', label: 'User', icon: userIcon },
        { id: 'messages', label: 'Messages', icon: messageIcon },
        { id: 'documents', label: 'Documents', icon: documentsIcon },
        { id: 'inventory', label: 'Inventory', icon: inventoryIcon }
    ];

    const handleItemClick = (itemId) => {
        if (onNavigate) {
            onNavigate(itemId);
        }
        if (window.innerWidth <= 768 && onToggle) {
            onToggle();
        }
    };

    const handleLogout = () => {
        localStorage.removeItem('currentUser');
        navigate('/');
    };

    return (
        <>
            {isOpen && <div className="sidebar-backdrop" onClick={onToggle} />}
            <div className={`admin-sidebar ${isOpen ? 'admin-sidebar-open' : 'admin-sidebar-closed'}`}>
                <nav className="admin-sidebar-nav">
                    {menuItems.map((item) => (
                        <div
                            key={item.id}
                            className={`admin-sidebar-item ${activeTab === item.id ? 'admin-sidebar-item-active' : ''}`}
                            onClick={() => handleItemClick(item.id)}
                        >
                            <div className="admin-sidebar-icon">
                                <img src={item.icon} alt={item.label} />
                            </div>
                            {isOpen && <span className="admin-sidebar-label">{item.label}</span>}
                        </div>
                    ))}
                </nav>

                <div className="sidebar-logout" onClick={handleLogout}>
                    <div className="admin-sidebar-icon">
                        <i className="pi pi-sign-out" style={{ fontSize: '24px', color: '#e74c3c' }}></i>
                    </div>
                    {isOpen && <span className="admin-sidebar-label" style={{ color: '#e74c3c' }}>Logout</span>}
                </div>
            </div>
        </>
    );
};

export default CaregiverSidebar;