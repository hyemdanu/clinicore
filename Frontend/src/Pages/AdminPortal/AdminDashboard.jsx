import { useState, useEffect, useCallback } from 'react';
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

export default function AdminDashboard() {
    const navigate = useNavigate();

    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState(
        () => localStorage.getItem('adminActiveTab') || 'dashboard'
    );

    const [medicationInventory, setMedicationInventory] = useState([]);
    const [consumablesInventory, setConsumablesInventory] = useState([]);
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
        } catch (error) {
            console.error('Error fetching inventory data:', error);
            setError('Failed to load inventory data. Please try again.');
        }
    }, [navigate]);

    useEffect(() => {
        fetchInventoryData();
    }, [fetchInventoryData]);

    const toggleSidebar = () => setSidebarOpen(!sidebarOpen);

    const handleNavigate = (tab) => {
        setActiveTab(tab);
        localStorage.setItem('adminActiveTab', tab);
    };

    const tabTitles = {
        dashboard: 'Dashboard',
        inventory: 'Inventory',
        residents: 'Residents',
        caregivers: 'Caregivers',
        user: 'User',
        messages: 'Messages'
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

    const sortedMedications = sortData(medicationInventory, medicationSort);
    const sortedConsumables = sortData(consumablesInventory, consumablesSort);

    // items with quantity <= 10
    const lowStockItems = [
        ...medicationInventory.map(item => ({ ...item, category: 'Medication' })),
        ...consumablesInventory.map(item => ({ ...item, category: 'Consumable' }))
    ].filter(item => Number(item.quantity) <= 10);

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

            {error && <div className="error-message">{error}</div>}

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
                    <Column field="name" header="Medication" style={{ width: '60%' }} />
                    <Column field="quantity" header="Quantity" body={quantityTemplate} style={{ width: '40%' }} />
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
                    <Column field="name" header="Items" style={{ width: '60%' }} />
                    <Column field="quantity" header="Quantity" body={quantityTemplate} style={{ width: '40%' }} />
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
                onNavigate={handleNavigate}
            />

            <main className={`dashboard-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                {renderContent()}
            </main>
        </div>
    );
}
