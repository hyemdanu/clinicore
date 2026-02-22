import './css/AdminSidebar.css';  // Reuse the same CSS
import dashboardIcon from '../assets/icons/dashboardicon.png';
import residentIcon from '../assets/icons/residenticon.png';
import userIcon from '../assets/icons/usericon.png';
import messageIcon from '../assets/icons/Messageicon.png';
import documentsIcon from '../assets/icons/documentsicon.png';
import inventoryIcon from '../assets/icons/inventoryicon.png';

// Caregiver Sidebar Component
const CaregiverSidebar = ({ isOpen, onToggle, activeTab, onNavigate }) => {
    // activeTab is now controlled by parent component

    // menu items configuration for caregivers
    // Includes Documents tab
    const menuItems = [
        { id: 'dashboard', label: 'Dashboard', icon: dashboardIcon },
        { id: 'residents', label: 'Residents', icon: residentIcon },
        { id: 'user', label: 'User', icon: userIcon },
        { id: 'messages', label: 'Messages', icon: messageIcon },
        { id: 'documents', label: 'Documents', icon: documentsIcon },
        { id: 'inventory', label: 'Inventory', icon: inventoryIcon }
    ];

    // update active item when a menu item is clicked
    const handleItemClick = (itemId) => {
        if (onNavigate) {

            onNavigate(itemId);
        }
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
        </div>
    );
};

export default CaregiverSidebar;