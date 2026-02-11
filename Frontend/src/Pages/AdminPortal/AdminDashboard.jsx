import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { get } from '../../services/api';
import Header from '../../Components/Header';
import AdminSidebar from '../../Components/AdminSidebar';
import ResidentsTab from './ResidentsTab';
import CaregiverResidentList from './CaregiverResidentList';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';
import "./css/admin.css";

// Admin Dashboard Page
export default function AdminDashboard() {
    const navigate = useNavigate();

    // sidebarOpen controls whether the sidebar is visible or hidden
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState('dashboard');

    // these hold the inventory data from the backend
    // we store medications and consumables separately because they different types
    const [medicationInventory, setMedicationInventory] = useState([]);
    const [consumablesInventory, setConsumablesInventory] = useState([]);

    // loading state - shows spinner while fetching data
    // error state - holds error message if API call fails
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // sort states - each table can be sorted independently
    // default is 'quantity-asc' which shows low stock items first
    const [medicationSort, setMedicationSort] = useState('quantity-asc');
    const [consumablesSort, setConsumablesSort] = useState('quantity-asc');

    // dropdown options for sorting
    const sortOptions = [
        { label: 'Quantity: Low to High', value: 'quantity-asc' },
        { label: 'Quantity: High to Low', value: 'quantity-desc' },
        { label: 'Name: A to Z', value: 'name-asc' },
        { label: 'Name: Z to A', value: 'name-desc' }
    ];

    // runs once when component loads, fetches inventory data
    useEffect(() => {
        fetchInventoryData();
    }, []);

    const fetchInventoryData = async () => {
        setLoading(true);
        setError(null);

        try {
            // check if user is logged in by looking at localStorage
            // if no user found, kick them back to login page
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) {
                navigate('/');
                return;
            }

            // parse the user data to get their ID
            // we need this to make authenticated API calls
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // fetch both inventories at the same time using Promise.all
            const [medications, consumables] = await Promise.all([
                get(`/inventory/medication?currentUserId=${currentUserId}`),
                get(`/inventory/consumables?currentUserId=${currentUserId}`)
            ]);

            // update state with the fetched data
            setMedicationInventory(medications);
            setConsumablesInventory(consumables);
        } catch (error) {
            console.error('Error fetching inventory data:', error);
            setError('Failed to load inventory data. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    // toggle sidebar open/closed when hamburger is clicked
    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const tabTitles = {
        dashboard: 'Dashboard',
        inventory: 'Inventory',
        residents: 'Residents',
        caregivers: 'Caregivers',
        user: 'User',
        messages: 'Messages'
    };

    // sorting function
    const sortData = (data, sortKey) => {
        const [field, order] = sortKey.split('-'); // split into field and order

        const sorted = [...data].sort((a, b) => {
            if (field === 'quantity') {
                return order === 'asc' ? a.quantity - b.quantity : b.quantity - a.quantity;
            } else {
                const nameA = a.name.toLowerCase();
                const nameB = b.name.toLowerCase();
                if (order === 'asc') {
                    return nameA.localeCompare(nameB); // A to Z
                } else {
                    return nameB.localeCompare(nameA); // Z to A
                }
            }
        });
        return sorted;
    };

    // get sorted data based on current sort selections
    const sortedMedications = sortData(medicationInventory, medicationSort);
    const sortedConsumables = sortData(consumablesInventory, consumablesSort);

    // compute low-stock items for alert banner (quantity <= 10)
    const lowStockItems = [
        ...medicationInventory.map(item => ({ ...item, category: 'Medication' })),
        ...consumablesInventory.map(item => ({ ...item, category: 'Consumable' }))
    ].filter(item => Number(item.quantity) <= 10);

    const lowStockCount = lowStockItems.length;

    // shows a warning icon and red text if stock is low (≤ 10)
    const quantityTemplate = (rowData) => {
        const isLowStock = rowData.quantity <= 10; // threshold is 10
        return (
            <div className={`quantity-cell ${isLowStock ? 'low-stock' : ''}`}>
                {/* show warning triangle if low stock */}
                {isLowStock && <i className="pi pi-exclamation-triangle warning-icon"></i>}
                <span>{rowData.quantity}</span>
            </div>
        );
    };

    // looooooaadddinnnngggg
    const loadingIcon = (
        <div className="custom-loading">
            <i className="pi pi-spin pi-spinner custom-spinner"></i>
            <span>Loading inventory...</span>
        </div>
    );

    const renderPlaceholder = (label) => (
        <div className="placeholder-content">
            <h2>{label}</h2>
            <p>Content coming soon.</p>
        </div>
    );

    const renderInventoryContent = () => (
        <>
            <div className="alert-section">
                <h2 className="dashboard-title">Inventory Dashboard</h2>
            </div>

            {/* Low stock alert banner shown above the tables when any item is low */}
            {lowStockCount > 0 && (
                <div
                    className="low-stock-alert"
                    role="alert"
                    style={{
                        display: 'flex',
                        alignItems: 'center',
                        backgroundColor: '#fff6f6',
                        border: '1px solid #ffcccc',
                        color: '#a94442',
                        padding: '0.75rem 1rem',
                        borderRadius: 4,
                        marginBottom: '1rem'
                    }}
                >
                    <i className="pi pi-exclamation-triangle" style={{ fontSize: '1.2rem', marginRight: '0.5rem' }}></i>
                    <div style={{ flex: 1 }}>
                        <strong>{lowStockCount} low stock item{lowStockCount > 1 ? 's' : ''}</strong>
                        <div style={{ fontSize: '0.9rem', opacity: 0.9 }}>
                            {lowStockItems.slice(0, 3).map(i => i.name).join(', ')}{lowStockItems.length > 3 ? ` and ${lowStockItems.length - 3} more` : ''}
                        </div>
                    </div>
                </div>
            )}

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

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
                    loading={loading}
                    loadingIcon={loadingIcon}
                    className="inventory-table"
                    emptyMessage="No medications found"
                >
                    <Column field="name" header="Medication" style={{ width: '60%' }} />
                    <Column
                        field="quantity"
                        header="Quantity"
                        body={quantityTemplate}
                        style={{ width: '40%' }}
                    />
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
                    loading={loading}
                    loadingIcon={loadingIcon}
                    className="inventory-table"
                    emptyMessage="No consumables found"
                >
                    <Column field="name" header="Items" style={{ width: '60%' }} />
                    <Column
                        field="quantity"
                        header="Quantity"
                        body={quantityTemplate}
                        style={{ width: '40%' }}
                    />
                </DataTable>
            </section>
        </>
    );

    const renderContent = () => {
        switch (activeTab) {
            case 'residents':
                return <ResidentsTab />;
            case 'caregivers':
                return <CaregiverResidentList />;
            case 'user':
                return renderPlaceholder('User Management');
            case 'messages':
                return renderPlaceholder('Messages');
            case 'inventory':
            case 'dashboard':
            default:
                return renderInventoryContent();
        }
    };

    return (
        <div className="admin-dashboard-container">
            <Header onToggleSidebar={toggleSidebar} title={tabTitles[activeTab] || 'Dashboard'} />

            <AdminSidebar
                isOpen={sidebarOpen}
                onToggle={toggleSidebar}
                activeTab={activeTab}
                onNavigate={setActiveTab}
            />

            <main className={`dashboard-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                {renderContent()}
            </main>
        </div>
    );
}

