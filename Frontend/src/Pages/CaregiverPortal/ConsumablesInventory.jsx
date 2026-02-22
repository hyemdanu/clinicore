import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { get, post, put, del } from '../../services/api';
import Header from '../../Components/Header';
import AdminSidebar from '../../Components/AdminSidebar';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';
import './css/consumables-inventory.css';
import consumablesIcon from '../../assets/icons/consumablesIcon.png';

export default function ConsumablesInventory() {
    const navigate = useNavigate();

    // Sidebar state
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState('consumables-inventory');

    // Consumables data
    const [consumables, setConsumables] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Search state
    const [globalFilter, setGlobalFilter] = useState('');

    // Add dialog state
    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [addFormData, setAddFormData] = useState({
        name: '',
        quantity: ''
    });

    // Edit dialog state
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [selectedConsumable, setSelectedConsumable] = useState(null);
    const [editFormData, setEditFormData] = useState({
        name: '',
        quantity: ''
    });

    // Load consumables on component mount
    useEffect(() => {
        fetchConsumables();
    }, []);

    const fetchConsumables = async () => {
        setLoading(true);
        setError(null);

        try {
            // Check authentication
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) {
                navigate('/');
                return;
            }

            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // Fetch consumables inventory from backend
            const data = await get(`/inventory/consumables?currentUserId=${currentUserId}`);
            setConsumables(data);
        } catch (error) {
            console.error('Error fetching consumables:', error);
            setError('Failed to load consumables inventory. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    // Handle sidebar navigation
    const handleSidebarNavigate = (tab) => {
        setActiveTab(tab);

        // Navigate to different routes based on tab
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
            case 'consumables-inventory':
                navigate('/consumables-inventory');
                break;
            case 'residents':
                navigate('/residents');
                break;
            case 'caregivers':
                alert('Caregivers page coming soon!');
                break;
            case 'user':
                alert('User page coming soon!');
                break;
            case 'messages':
                alert('Messages page coming soon!');
                break;
            case 'documents':
                alert('Documents page coming soon!');
                break;
            default:
                break;
        }
    };

    // Adjust quantity +/-
    const handleAdjustQuantity = async (consumable, change) => {
        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // Calculate new quantity
            const newQuantity = Math.max(0, consumable.quantity + change);

            // Update consumable with new quantity
            const updatedConsumable = {
                ...consumable,
                quantity: newQuantity
            };

            // Call backend PUT endpoint
            await put(`/inventory/consumables/${consumable.id}?currentUserId=${currentUserId}`, updatedConsumable);

            // Refresh the list
            await fetchConsumables();
        } catch (error) {
            console.error('Error adjusting quantity:', error);
            alert('Failed to adjust quantity. Please try again.');
        }
    };

    // Open edit dialog
    const handleEditConsumable = (consumable) => {
        setSelectedConsumable(consumable);
        setEditFormData({
            name: consumable.name,
            quantity: consumable.quantity.toString()
        });
        setEditDialogVisible(true);
    };

    // Save edited consumable
    const handleSaveEdit = async () => {
        if (!editFormData.name.trim() || !editFormData.quantity) {
            alert('Please fill in all fields');
            return;
        }

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            const updatedConsumable = {
                ...selectedConsumable,
                name: editFormData.name.trim(),
                quantity: parseInt(editFormData.quantity)
            };

            await put(`/inventory/consumables/${selectedConsumable.id}?currentUserId=${currentUserId}`, updatedConsumable);

            // Refresh the list
            await fetchConsumables();

            // Close dialog
            setEditDialogVisible(false);
            setSelectedConsumable(null);
            setEditFormData({ name: '', quantity: '' });
        } catch (error) {
            console.error('Error updating consumable:', error);
            alert('Failed to update consumable. Please try again.');
        }
    };

    // Delete consumable
    const handleDeleteConsumable = async (consumable) => {
        if (!window.confirm(`Are you sure you want to delete ${consumable.name}?`)) {
            return;
        }

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            await del(`/inventory/consumables/${consumable.id}?currentUserId=${currentUserId}`);

            // Refresh the list
            await fetchConsumables();
        } catch (error) {
            console.error('Error deleting consumable:', error);
            alert('Failed to delete consumable. Please try again.');
        }
    };

    // Add new consumable
    const handleAddConsumable = async () => {
        if (!addFormData.name.trim() || !addFormData.quantity) {
            alert('Please fill in all fields');
            return;
        }

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            const newConsumable = {
                name: addFormData.name.trim(),
                quantity: parseInt(addFormData.quantity)
            };

            await post(`/inventory/consumables?currentUserId=${currentUserId}`, newConsumable);

            // Refresh the list
            await fetchConsumables();

            // Close dialog and reset form
            setAddDialogVisible(false);
            setAddFormData({ name: '', quantity: '' });
        } catch (error) {
            console.error('Error adding consumable:', error);
            alert('Failed to add consumable. Please try again.');
        }
    };

    // Action buttons template for DataTable
    const actionBodyTemplate = (rowData) => {
        return (
            <div className="action-buttons">
                <button
                    className="btn btn-add"
                    onClick={() => handleAdjustQuantity(rowData, 1)}
                    title="Increase quantity"
                >
                    +
                </button>
                <button
                    className="btn btn-remove"
                    onClick={() => handleAdjustQuantity(rowData, -1)}
                    title="Decrease quantity"
                    disabled={rowData.quantity === 0}
                >
                    -
                </button>
            </div>
        );
    };

    // Menu options template
    const menuBodyTemplate = (rowData) => {
        return (
            <div className="menu-buttons">
                <button
                    className="menu-button"
                    onClick={() => handleEditConsumable(rowData)}
                    title="Edit"
                >
                    ✏️
                </button>
                <button
                    className="menu-button delete-button"
                    onClick={() => handleDeleteConsumable(rowData)}
                    title="Delete"
                >
                    🗑️
                </button>
            </div>
        );
    };

    // Quantity display with low stock warning
    const quantityBodyTemplate = (rowData) => {
        const isLowStock = rowData.quantity <= 10;
        return (
            <div className={`quantity-cell ${isLowStock ? 'low-stock' : ''}`}>
                {isLowStock && <i className="pi pi-exclamation-triangle warning-icon"></i>}
                <span>{rowData.quantity}</span>
            </div>
        );
    };

    // Header with search and add button
    const renderHeader = () => {
        return (
            <div className="table-header">
                <div className="search-container">
                    <span className="p-input-icon-left">
                        <i className="pi pi-search" />
                        <InputText
                            value={globalFilter}
                            onChange={(e) => setGlobalFilter(e.target.value)}
                            placeholder="Search consumables..."
                            className="search-input"
                        />
                    </span>
                </div>
                <Button
                    label="Add Consumable"
                    icon="pi pi-plus"
                    className="add-consumable-btn"
                    onClick={() => {
                        setAddFormData({ name: '', quantity: '' });
                        setAddDialogVisible(true);
                    }}
                />
            </div>
        );
    };

    const loadingIcon = (
        <div className="custom-loading">
            <i className="pi pi-spin pi-spinner custom-spinner"></i>
            <span>Loading consumables...</span>
        </div>
    );

    const tabTitles = {
        'consumables-inventory': 'Medical Consumables Inventory',
        'medication-inventory': 'Medication Inventory',
        'inventory': 'Inventory',
        'dashboard': 'Dashboard',
        'residents': 'Residents',
        'caregivers': 'Caregivers',
        'user': 'User',
        'messages': 'Messages'
    };

    return (
        <div className="consumables-inventory-container">
            <Header
                onToggleSidebar={toggleSidebar}
                title={tabTitles[activeTab] || 'Medical Consumables Inventory'}
            />

            <AdminSidebar
                isOpen={sidebarOpen}
                onToggle={toggleSidebar}
                activeTab={activeTab}
                onNavigate={handleSidebarNavigate}
            />

            <main className={`inventory-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                <div className="inventory-header-section">
                    <div className="header-icon-wrapper">
                        <img src={consumablesIcon} alt="Medication" className="header-icon" />
                    </div>
                    <h1 className="page-title">Inventory: Medical Consumables</h1>
                </div>

                {error && (
                    <div className="error-message">
                        <i className="pi pi-exclamation-circle"></i>
                        {error}
                    </div>
                )}

                <div className="inventory-table-wrapper">
                    <DataTable
                        value={consumables}
                        loading={loading}
                        loadingIcon={loadingIcon}
                        header={renderHeader()}
                        globalFilter={globalFilter}
                        emptyMessage="No consumables found"
                        className="consumables-table"
                        paginator
                        rows={10}
                        rowsPerPageOptions={[5, 10, 25, 50]}
                    >
                        <Column
                            field="name"
                            header="Item Name"
                            sortable
                            style={{ width: '40%' }}
                        />
                        <Column
                            field="quantity"
                            header="Quantity"
                            body={quantityBodyTemplate}
                            sortable
                            style={{ width: '20%' }}
                        />
                        <Column
                            header="Action"
                            body={actionBodyTemplate}
                            style={{ width: '25%' }}
                        />
                        <Column
                            body={menuBodyTemplate}
                            style={{ width: '15%', textAlign: 'center' }}
                        />
                    </DataTable>
                </div>
            </main>

            {/* Add Consumable Dialog */}
            <Dialog
                header="Add New Consumable"
                visible={addDialogVisible}
                style={{ width: '450px' }}
                onHide={() => {
                    setAddDialogVisible(false);
                    setAddFormData({ name: '', quantity: '' });
                }}
                footer={
                    <div>
                        <Button
                            label="Cancel"
                            icon="pi pi-times"
                            onClick={() => {
                                setAddDialogVisible(false);
                                setAddFormData({ name: '', quantity: '' });
                            }}
                            className="p-button-text"
                        />
                        <Button
                            label="Add"
                            icon="pi pi-check"
                            onClick={handleAddConsumable}
                            autoFocus
                        />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="add-name">Item Name</label>
                        <InputText
                            id="add-name"
                            value={addFormData.name}
                            onChange={(e) => setAddFormData({ ...addFormData, name: e.target.value })}
                            placeholder="Enter consumable item name"
                            className="w-full"
                        />
                    </div>
                    <div className="field">
                        <label htmlFor="add-quantity">Quantity</label>
                        <InputText
                            id="add-quantity"
                            type="number"
                            value={addFormData.quantity}
                            onChange={(e) => setAddFormData({ ...addFormData, quantity: e.target.value })}
                            placeholder="Enter initial quantity"
                            className="w-full"
                            min="0"
                        />
                    </div>
                </div>
            </Dialog>

            {/* Edit Consumable Dialog */}
            <Dialog
                header="Edit Consumable"
                visible={editDialogVisible}
                style={{ width: '450px' }}
                onHide={() => {
                    setEditDialogVisible(false);
                    setSelectedConsumable(null);
                    setEditFormData({ name: '', quantity: '' });
                }}
                footer={
                    <div>
                        <Button
                            label="Cancel"
                            icon="pi pi-times"
                            onClick={() => {
                                setEditDialogVisible(false);
                                setSelectedConsumable(null);
                                setEditFormData({ name: '', quantity: '' });
                            }}
                            className="p-button-text"
                        />
                        <Button
                            label="Save"
                            icon="pi pi-check"
                            onClick={handleSaveEdit}
                            autoFocus
                        />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="edit-name">Item Name</label>
                        <InputText
                            id="edit-name"
                            value={editFormData.name}
                            onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
                            placeholder="Enter consumable item name"
                            className="w-full"
                        />
                    </div>
                    <div className="field">
                        <label htmlFor="edit-quantity">Quantity</label>
                        <InputText
                            id="edit-quantity"
                            type="number"
                            value={editFormData.quantity}
                            onChange={(e) => setEditFormData({ ...editFormData, quantity: e.target.value })}
                            placeholder="Enter quantity"
                            className="w-full"
                            min="0"
                        />
                    </div>
                </div>
            </Dialog>
        </div>
    );
}