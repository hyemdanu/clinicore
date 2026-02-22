import { useNavigate } from 'react-router-dom';
import './css/AdminSidebar.css';
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import caregiverIcon from '../assets/icons/caregivericon.png';
import userIcon from '../assets/icons/usericon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';
import documentIcon from '../assets/icons/documentIcon.png';

// Admin Sidebar Component
// this sidebar is specific to admin users
// caregiver and resident would have their own sidebar cus they different
const AdminSidebar = ({ isOpen, activeTab, onNavigate }) => {
  const navigate = useNavigate();

  // activeTab is now controlled by parent component

  // menu items configuration for the different tabs
  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
    { id: 'residents', label: 'Residents', icon: residentIcon },
    { id: 'caregivers', label: 'Caregivers', icon: caregiverIcon },
    { id: 'account-requests', label: 'Account Requests', icon: documentIcon },
    { id: 'user', label: 'User', icon: userIcon },
    { id: 'messages', label: 'Messages', icon: messageIcon },
    { id: 'inventory', label: 'Inventory', icon: inventoryIcon }
  ];

  // update active item when a menu item is clicked
  const handleItemClick = (itemId) => {
    if (onNavigate) {
      onNavigate(itemId);
    }
  };

  // logout handler - clears user data and sends back to login
  const handleLogout = () => {
    localStorage.removeItem('currentUser');
    navigate('/');
  };

  return (
    // animation and css stuff handled in AdminSidebar.css
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

      {/* logout button at the bottom */}
      <div className="sidebar-logout" onClick={handleLogout}>
        <div className="admin-sidebar-icon">
          <i className="pi pi-sign-out" style={{ fontSize: '24px', color: '#e74c3c' }}></i>
        </div>
        {isOpen && <span className="admin-sidebar-label" style={{ color: '#e74c3c' }}>Logout</span>}
      </div>
    </div>
  );
};

export default AdminSidebar;
