import './css/CaregiverSidebar.css';
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';
import documentIcon from '../assets/icons/documenticon.png';
import userIcon from '../assets/icons/usericon.png';

// Caregiver Sidebar Component
const CaregiverSidebar = ({ isOpen, onToggle, activeTab, onNavigate }) => {
  // menu items configuration for the different tabs
  const menuItems = [
      { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
      { id: 'residents', label: 'Residents', icon: residentIcon },
      { id: 'profile', label: 'Profile', icon: userIcon },
      { id: 'messages', label: 'Messages', icon: messageIcon },
      { id: 'documents', label: 'Documents', icon: documentIcon },
      { id: 'inventory', label: 'Inventory', icon: inventoryIcon }
  ];

    // update active item when a menu item is clicked
    const handleItemClick = (itemId) => {
        if (onNavigate) {
            onNavigate(itemId);
        }
    }

    return (
        // animation and css stuff handled in CaregiverSidebar.css
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
        </div>
    );
};

export default CaregiverSidebar;