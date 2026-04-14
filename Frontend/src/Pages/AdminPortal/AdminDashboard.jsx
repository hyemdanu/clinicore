import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { get } from '../../services/api';
import Header from '../../Components/Header';
import AdminSidebar from '../../Components/AdminSidebar';
import ResidentsTab from './ResidentsTab';
import CaregiverResidentList from './CaregiverResidentList';
import MessagesTab from '../Shared/MessagesTab';
import AccountRequests from './AccountRequests';
import AdminProfile from './AdminProfile';
import AdminDocuments from './AdminDocument';
import InventoryLanding from '../Shared/InventoryLanding.jsx';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';
import "./css/admin.css";

export default function AdminDashboard() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
    const [activeTab, setActiveTab] = useState(
        () => localStorage.getItem('adminActiveTab') || 'dashboard'
    );

    const [medicationInventory, setMedicationInventory] = useState([]);
    const [consumablesInventory, setConsumablesInventory] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [medicationSort, setMedicationSort] = useState('quantity-asc');
    const [consumablesSort, setConsumablesSort] = useState('quantity-asc');

    const sortOptions = [
        { label: 'Quantity: Low to High', value: 'quantity-asc' },
        { label: 'Quantity: High to Low', value: 'quantity-desc' },
        { label: 'Name: A to Z', value: 'name-asc' },
        { label: 'Name: Z to A', value: 'name-desc' }
    ];

    const fetchInventoryData = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) {
                navigate('/');
                return;
            }
            const currentUser = JSON.parse(currentUserStr);
            const [medications, consumables] = await Promise.all([
                get(`/inventory/medication?currentUserId=${currentUser.id}`),
                get(`/inventory/consumables?currentUserId=${currentUser.id}`)
            ]);
            setMedicationInventory(medications);
            setConsumablesInventory(consumables);
        } catch {
            setError('Failed to load inventory data. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        if (activeTab === 'dashboard') {
            fetchInventoryData();
        }
    }, [activeTab, fetchInventoryData]);

    const toggleSidebar = () => setSidebarOpen(!sidebarOpen);

    const handleNavigate = (tab) => {
        setActiveTab(tab);
        localStorage.setItem('adminActiveTab', tab);
        window.history.pushState({ tab }, '', `#${tab}`);
    };

    // Handle browser back/forward buttons
    useEffect(() => {
        const handlePopState = (e) => {
            const tab = e.state?.tab || 'dashboard';
            setActiveTab(tab);
            localStorage.setItem('adminActiveTab', tab);
        };
        window.addEventListener('popstate', handlePopState);
        // Set initial history state
        if (!window.history.state?.tab) {
            window.history.replaceState({ tab: activeTab }, '', `#${activeTab}`);
        }
        return () => window.removeEventListener('popstate', handlePopState);
    }, []);

    const tabTitles = {
        dashboard: 'Dashboard',
        inventory: 'Inventory',
        residents: 'Residents',
        caregivers: 'Caregivers',
        'account-requests': 'Account Requests',
        user: 'User',
        documents: 'Documents',
        messages: 'Messages',
        'medication-inventory': 'Medication Inventory',
        'consumables-inventory': 'Medical Consumables Inventory'
    };

    const sortData = (data, sortKey) => {
        const [field, order] = sortKey.split('-');
        return [...data].sort((a, b) => {
            if (field === 'quantity') {
                return order === 'asc' ? a.quantity - b.quantity : b.quantity - a.quantity;
            }
            const nameA = a.name.toLowerCase();
            const nameB = b.name.toLowerCase();
            return order === 'asc' ? nameA.localeCompare(nameB) : nameB.localeCompare(nameA);
        });
    };

    const sortedMedications = useMemo(() => sortData(medicationInventory, medicationSort), [medicationInventory, medicationSort]);
    const sortedConsumables = useMemo(() => sortData(consumablesInventory, consumablesSort), [consumablesInventory, consumablesSort]);

    const lowStockItems = useMemo(() => [
        ...medicationInventory.map(item => ({ ...item, category: 'Medication' })),
        ...consumablesInventory.map(item => ({ ...item, category: 'Consumable' }))
    ].filter(item => Number(item.quantity) <= 10), [medicationInventory, consumablesInventory]);

    const lowStockCount = lowStockItems.length;

    const quantityTemplate = (rowData) => {
        const isLowStock = rowData.quantity <= 10;
        return (
            <div className={`quantity-cell ${isLowStock ? 'low-stock' : ''}`}>
                {isLowStock && <i className="pi pi-exclamation-triangle warning-icon"></i>}
                <span>{rowData.quantity}</span>
            </div>
        );
    };

    const renderInventoryContent = () => (
        <>
            <div className="alert-section">
                <h2 className="dashboard-title">Inventory Dashboard</h2>
            </div>

            {loading && (
                <div className="caregiver-loading">
                    <i className="pi pi-spin pi-spinner caregiver-spinner"></i>
                    <span>Loading inventory...</span>
                </div>
            )}

            {!loading && error && <div className="error-message">{error}</div>}

            {!loading && !error && <>
                <div className="alert-warning-inline" role="alert">
                    <i className="pi pi-exclamation-triangle alert-warning-icon"></i>
                    <div className="alert-warning-body">
                        <strong>{lowStockCount} low stock item{lowStockCount > 1 ? 's' : ''}</strong>
                        <div className="alert-warning-detail">
                            {lowStockItems.slice(0, 3).map(i => i.name).join(', ')}{lowStockItems.length > 3 ? ` and ${lowStockItems.length - 3} more` : ''}
                        </div>
                    </div>
                </div>

                <section className="inventory-section">
                    <div className="inventory-header">
                        <h3>Medication Inventory</h3>
                        <div className="sort-dropdown-wrapper">
                            <label>Sort by:</label>
                            <Dropdown
                                value={medicationSort}
                                options={sortOptions}
                                onChange={(e) => setMedicationSort(e.value)}
                                className="sort-dropdown"
                            />
                        </div>
                    </div>
                    <DataTable
                        value={sortedMedications}
                        className="inventory-table"
                        emptyMessage="No medications found"
                    >
                        <Column field="name" header="Medication" className="col-name" />
                        <Column field="quantity" header="Quantity" body={quantityTemplate} className="col-quantity" />
                    </DataTable>
                </section>

                <section className="inventory-section">
                    <div className="inventory-header">
                        <h3>Medical Consumables Inventory</h3>
                        <div className="sort-dropdown-wrapper">
                            <label>Sort by:</label>
                            <Dropdown
                                value={consumablesSort}
                                options={sortOptions}
                                onChange={(e) => setConsumablesSort(e.value)}
                                className="sort-dropdown"
                            />
                        </div>
                    </div>
                    <DataTable
                        value={sortedConsumables}
                        className="inventory-table"
                        emptyMessage="No consumables found"
                    >
                        <Column field="name" header="Items" className="col-name" />
                        <Column field="quantity" header="Quantity" body={quantityTemplate} className="col-quantity" />
                    </DataTable>
                </section>
            </>}
        </>
    );

    const renderContent = () => {
        switch (activeTab) {
            case 'residents':
                return <ResidentsTab />;
            case 'caregivers':
                return <CaregiverResidentList />;
            case 'account-requests':
                return <AccountRequests embedded={true} />;
            case 'user':
                return <AdminProfile />;
            case 'documents':
                return <AdminDocuments />;
            case 'messages':
                return <MessagesTab />;
            case 'inventory':
                return <InventoryLanding />;
            case 'dashboard':
            default:
                return renderInventoryContent();
        }
    };

    return (
        <div className={`admin-dashboard-container ${activeTab === 'messages' ? 'messages-active' : ''}`}>
            <Header onToggleSidebar={toggleSidebar} title={tabTitles[activeTab] || 'Dashboard'} />

            <AdminSidebar
                isOpen={sidebarOpen}
                onToggle={toggleSidebar}
                activeTab={activeTab}
                onNavigate={handleNavigate}
            />

            <main className={`dashboard-content ${sidebarOpen ? 'content-with-sidebar' : ''} ${activeTab === 'messages' ? 'messages-content' : ''}`}>
                {renderContent()}
            </main>
        </div>
    );
}