import { useNavigate } from 'react-router-dom';
import './css/CaregiverSidebar.css';
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';
// todo add a document icon later
import userIcon from '../assets/icons/usericon.png';

// sidebar component
const CaregiverSidebar = ({ isOpen, activeTab, onNavigate }) => {
  const navigate = useNavigate();

  // menu items for the sidebar
  const menuItems = [
      { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
      { id: 'residents', label: 'Residents', icon: residentIcon },
      { id: 'profile', label: 'User', icon: userIcon },
      { id: 'messages', label: 'Messages', icon: messageIcon },
      { id: 'inventory', label: 'Inventory', icon: inventoryIcon }
  ];

    // handle clicking a menu item
    const handleItemClick = (itemId) => {
        if (onNavigate) {
            onNavigate(itemId);
        }
    }

    // log them out and send them back
    const handleLogout = () => {
        localStorage.removeItem('currentUser');
        navigate('/');
    };

    return (
        // animations and styling handled in css
        <div className={`caregiver-sidebar ${isOpen ? 'caregiver-sidebar-open' : 'caregiver-sidebar-closed'}`}>
            <nav className="caregiver-sidebar-nav">
                {menuItems.map((item) => (
                    <div
                        key={item.id}
                        className={`caregiver-sidebar-item ${activeTab === item.id ? 'caregiver-sidebar-item-active' : ''}`}
                        onClick={() => handleItemClick(item.id)}
                    >
                        <div className="caregiver-sidebar-icon">
                            <img src={item.icon} alt={item.label} />
                        </div>
                        {isOpen && <span className="caregiver-sidebar-label">{item.label}</span>}
                    </div>
                ))}
            </nav>

            {/* logout button at the bottom */}
            <div className="sidebar-logout" onClick={handleLogout}>
                <div className="caregiver-sidebar-icon">
                    <i className="pi pi-sign-out" style={{ fontSize: '24px', color: '#e74c3c' }}></i>
                </div>
                {isOpen && <span className="caregiver-sidebar-label" style={{ color: '#e74c3c' }}>Logout</span>}
            </div>
        </div>
    );
};

export default CaregiverSidebar;