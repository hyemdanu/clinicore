import { useNavigate } from 'react-router-dom';
import './css/AdminSidebar.css';
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import caregiverIcon from '../assets/icons/caregivericon.png';
import userIcon from '../assets/icons/usericon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';
import documentIcon from '../assets/icons/documentsIcon.png';
import accountRequestIcon from '../assets/icons/accountRequest.png';


const AdminSidebar = ({ isOpen, activeTab, onNavigate, onToggle }) => {
  const navigate = useNavigate();

  const menuItems = [
    { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
    { id: 'residents', label: 'Residents', icon: residentIcon },
    { id: 'caregivers', label: 'Caregivers', icon: caregiverIcon },
    { id: 'account-requests', label: 'Account Requests', icon: accountRequestIcon },
    { id: 'user', label: 'User', icon: userIcon },
    { id: 'documents', label: 'Documents', icon: documentIcon },
    { id: 'messages', label: 'Messages', icon: messageIcon },
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
        {isOpen && (
            <button
                className="sidebar-backdrop"
                onClick={onToggle}
                aria-label="Close sidebar"
                type="button"
            />
        )}

        <aside className={`admin-sidebar ${isOpen ? 'admin-sidebar-open' : 'admin-sidebar-closed'}`}>
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

                  {isOpen && (
                      <span className="admin-sidebar-label">
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
            <div className="admin-sidebar-icon">
              <i className="pi pi-sign-out sidebar-logout-icon" />
            </div>

            {isOpen && (
                <span className="admin-sidebar-label">
            Logout
          </span>
            )}
          </button>
        </aside>
      </>
  );
};

export default AdminSidebar;
