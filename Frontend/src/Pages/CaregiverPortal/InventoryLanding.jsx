import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../Components/Header';
import AdminSidebar from '../../Components/AdminSidebar';
import './css/inventory-landing.css';
import consumablesIcon from '../../assets/icons/consumablesIcon.png';
import medicationsIcon from '../../assets/icons/medicationsIcon.png';

export default function InventoryLanding() {
    const navigate = useNavigate();
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState('inventory');

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    //Links title cards to their respective inventory pages
    const handleMedicationsClick = () => {
        navigate('/caregiver/medication-inventory');
    };

    const handleConsumablesClick = () => {
        navigate('/caregiver/consumables-inventory');
    };

    // Handle sidebar navigation
    const handleSidebarNavigate = (tab) => {
        setActiveTab(tab);

        // Navigate to different routes based on tab (update later)
        switch(tab) {
            case 'dashboard':
                navigate('/admin-dashboard');
                break;
            case 'inventory':
                navigate('/caregiver/inventory');
                break;
            case 'medication-inventory':
                navigate('/medication-inventory');
                break;
            case 'medication-consumables-inventory':
                navigate('/caregiver/consumables-inventory');
                break;
            case 'residents':
                // Add link later
                navigate('/residents');
                break;
            case 'caregivers':
                // Add  later
                alert('Caregivers page coming soon!');
                // navigate('/caregivers');
                break;
            case 'user':

                alert('User page coming soon!');
                // navigate('/user');
                break;
            case 'messages':
                // Add  messages route
                alert('Messages page coming soon!');
                // navigate('/messages');
                break;
            case 'documents':
                // Add  documents route
                alert('Documents page coming soon!');
                // navigate('/documents');
                break;
            default:
                break;
        }
    };

    const tabTitles = {
        'inventory': 'Inventory',
        'dashboard': 'Dashboard',
        'residents': 'Residents',
        'user': 'User',
        'messages': 'Messages',
        'documents': 'Documents'
    };

    return (
        <div className="inventory-landing-container">
            <Header
                onToggleSidebar={toggleSidebar}
                title={tabTitles[activeTab] || 'Inventory'}
            />

            <AdminSidebar
                isOpen={sidebarOpen}
                onToggle={toggleSidebar}
                activeTab={activeTab}
                onNavigate={handleSidebarNavigate}
            />

            <main className={`landing-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                <div className="inventory-cards-container">

                    {/* Medical Consumables Card */}
                    <div className="inventory-card" onClick={handleConsumablesClick}>
                        <div className="card-icon-wrapper purple">
                            <img
                                src={consumablesIcon}
                                alt="Medical Consumables"
                                className="card-icon"
                            />
                                <path d="M21.71 2.29a1 1 0 0 0-1.42 0L18 4.59l-2.29-2.3a1 1 0 0 0-1.42 1.42L16.59 6l-4.3 4.29-1.58-1.58a1 1 0 0 0-1.42 0l-8 8a1 1 0 0 0 0 1.42l4.17 4.17a1 1 0 0 0 1.42 0l8-8a1 1 0 0 0 0-1.42L13.3 11.3 17.59 7l2.3 2.29a1 1 0 0 0 1.42 0 1 1 0 0 0 0-1.42L19.41 5.59l2.3-2.3a1 1 0 0 0 0-1.42zM9.88 17.12L5.41 12.65l6.59-6.59 4.47 4.47z" fill="currentColor"/>

                        </div>
                        <h2 className="card-title">Medical<br/>Consumables</h2>
                    </div>

                    {/* Medications Card */}
                    <div className="inventory-card" onClick={handleMedicationsClick}>
                        <div className="card-icon-wrapper pink">
                            <img
                                src={medicationsIcon}
                                alt="Medical Consumables"
                                className="card-icon"
                            />
                                <path d="M4.22 11.29l7.07-7.07a2 2 0 0 1 2.83 0l5.66 5.66a2 2 0 0 1 0 2.83l-7.07 7.07a2 2 0 0 1-2.83 0L4.22 14.12a2 2 0 0 1 0-2.83zM8.5 11a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zm6 6a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z" fill="currentColor"/>
                                <path d="M12 9a1 1 0 0 1 1 1v4a1 1 0 0 1-2 0v-4a1 1 0 0 1 1-1z" fill="currentColor"/>

                        </div>
                        <h2 className="card-title">Medications</h2>
                    </div>

                </div>
            </main>
        </div>
    );
}