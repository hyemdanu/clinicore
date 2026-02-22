import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { get, post, put, del } from '../../services/api';
import Header from '../../Components/Header';
import CaregiverSidebar from '../../Components/CaregiverSideBar.jsx';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';
import './css/CaregiverInventory.css';
import medicationsIcon from '../../assets/icons/medicationsIcon.png';

export default function MedicationInventory() {
    const navigate = useNavigate();

    // Sidebar state
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState('medication-inventory');

    // Medication data
    const [medications, setMedications] = useState([]);
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
    const [selectedMedication, setSelectedMedication] = useState(null);
    const [editFormData, setEditFormData] = useState({
        name: '',
        quantity: ''
    });

    // Load medications on component mount
    useEffect(() => {
        fetchMedications();
    }, []);

    const fetchMedications = async () => {
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

            // Fetch medication inventory from backend
            const data = await get(`/inventory/medication?currentUserId=${currentUserId}`);
            setMedications(data);
        } catch (error) {
            console.error('Error fetching medications:', error);
            setError('Failed to load medication inventory. Please try again.');
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
        //add links later
        switch(tab) {
            case 'dashboard':
                navigate('/caregiver-dashboard');
                break;
            case 'inventory':
                navigate('/caregiver/inventory');
                break;
            case 'medication-inventory':
                navigate('/caregiver/medication-inventory');
                break;
            case 'residents':
                navigate('/residents');
                break;
            case 'user':
                alert('User page coming soon!');
                // navigate('/user');
                break;
            case 'messages':
                alert('Messages page coming soon!');
                // navigate('/messages');
                break;
            case 'documents':
                alert('Documents page coming soon!');
                // navigate('/documents');
                break;
            default:
                break;
        }
    };

    // Adjust quantity +/-
    const handleAdjustQuantity = async (medication, change) => {
        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // Calculate new quantity
            const newQuantity = Math.max(0, medication.quantity + change);

            // If trying to decrease below 0, show alert and return early
            if (medication.quantity === 0 && change < 0) {
                alert('Quantity cannot be less than 0');
                return;
            }

            // Update medication with new quantity
            const updatedMedication = {
                ...medication,
                quantity: newQuantity
            };

            // Call backend PUT endpoint
            await put(`/inventory/medication/${medication.id}?currentUserId=${currentUserId}`, updatedMedication);

            // Refresh the list
            await fetchMedications();
        } catch (error) {
            console.error('Error adjusting quantity:', error);
            alert('Failed to adjust quantity. Please try again.');
        }
    };

    // Open edit dialog
    const handleEditMedication = (medication) => {
        setSelectedMedication(medication);
        setEditFormData({
            name: medication.name,
            quantity: medication.quantity.toString()
        });
        setEditDialogVisible(true);
    };

    // Save edited medication
    const handleSaveEdit = async () => {
        if (!editFormData.name.trim() || !editFormData.quantity) {
            alert('Please fill in all fields');
            return;
        }
        const quantity = parseInt(editFormData.quantity);
        if (quantity < 0) {
            alert('Quantity cannot be negative. Please enter a value of 0 or greater.');
            return;
        }

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            const updatedMedication = {
                ...selectedMedication,
                name: editFormData.name.trim(),
                quantity: parseInt(editFormData.quantity)
            };

            await put(`/inventory/medication/${selectedMedication.id}?currentUserId=${currentUserId}`, updatedMedication);

            // Refresh the list
            await fetchMedications();

            // Close dialog
            setEditDialogVisible(false);
            setSelectedMedication(null);
            setEditFormData({ name: '', quantity: '' });
        } catch (error) {
            console.error('Error updating medication:', error);
            alert('Failed to update medication. Please try again.');
        }
    };

    // Delete medication
    const handleDeleteMedication = async (medication) => {
        if (!window.confirm(`Are you sure you want to delete ${medication.name}?`)) {
            return;
        }

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            await del(`/inventory/medication/${medication.id}?currentUserId=${currentUserId}`);

            // Refresh the list
            await fetchMedications();
        } catch (error) {
            console.error('Error deleting medication:', error);
            alert('Failed to delete medication. Please try again.');
        }
    };

    // Add new medication
    const handleAddMedication = async () => {
        if (!addFormData.name.trim() || !addFormData.quantity) {
            alert('Please fill in all fields');
            return;
        }
        const quantity = parseInt(addFormData.quantity);
        if (quantity < 0) {
            alert('Quantity cannot be negative. Please enter a value of 0 or greater.');
            return;
        }

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            const newMedication = {
                name: addFormData.name.trim(),
                quantity: parseInt(addFormData.quantity)
            };

            await post(`/inventory/medication?currentUserId=${currentUserId}`, newMedication);

            // Refresh the list
            await fetchMedications();

            // Close dialog and reset form
            setAddDialogVisible(false);
            setAddFormData({ name: '', quantity: '' });
        } catch (error) {
            console.error('Error adding medication:', error);
            alert('Failed to add medication. Please try again.');
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
    //replace icons later after team meeting
    const menuBodyTemplate = (rowData) => {
        return (
            <div className="menu-buttons">
                <button
                    className="menu-button"
                    onClick={() => handleEditMedication(rowData)}
                    title="Edit"
                >
                    ✏️
                </button>
                <button
                    className="menu-button delete-button"
                    onClick={() => handleDeleteMedication(rowData)}
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
                            placeholder="Search medications..."
                            className="search-input"
                        />
                    </span>
                </div>
                <Button
                    label="Add Medication"
                    icon="pi pi-plus"
                    className="add-medication-btn"
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
            <span>Loading medications...</span>
        </div>
    );

    const tabTitles = {
        'medication-inventory': 'Medication Inventory',
        'dashboard': 'Dashboard',
        'residents': 'Residents',
        'user': 'User',
        'messages': 'Messages'
    };

    return (
        <div className="medication-inventory-container">
            <Header
                onToggleSidebar={toggleSidebar}
                title={tabTitles[activeTab] || 'Medication Inventory'}
            />

            <CaregiverSidebar
                isOpen={sidebarOpen}
                onToggle={toggleSidebar}
                activeTab={activeTab}
                onNavigate={handleSidebarNavigate}
            />

            <main className={`inventory-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                <div className="inventory-header-section">
                    <div className="header-icon-wrapper">
                        <img src={medicationsIcon} alt="Medication" className="header-icon" />
                    </div>
                    <h1 className="page-title">Inventory: Medication</h1>
                </div>

                {error && (
                    <div className="error-message">
                        <i className="pi pi-exclamation-circle"></i>
                        {error}
                    </div>
                )}

                <div className="inventory-table-wrapper">
                    <DataTable
                        value={medications}
                        loading={loading}
                        loadingIcon={loadingIcon}
                        header={renderHeader()}
                        globalFilter={globalFilter}
                        emptyMessage="No medications found"
                        className="medication-table"
                        paginator
                        rows={10}
                        rowsPerPageOptions={[5, 10, 25, 50]}
                    >
                        <Column
                            field="name"
                            header="Medication"
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

            {/* Add Medication Dialog */}
            <Dialog
                header="Add New Medication"
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
                            onClick={handleAddMedication}
                            autoFocus
                        />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="add-name">Medication Name</label>
                        <InputText
                            id="add-name"
                            value={addFormData.name}
                            onChange={(e) => setAddFormData({ ...addFormData, name: e.target.value })}
                            placeholder="Enter medication name"
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

            {/* Edit Medication Dialog */}
            <Dialog
                header="Edit Medication"
                visible={editDialogVisible}
                style={{ width: '450px' }}
                onHide={() => {
                    setEditDialogVisible(false);
                    setSelectedMedication(null);
                    setEditFormData({ name: '', quantity: '' });
                }}
                footer={
                    <div>
                        <Button
                            label="Cancel"
                            icon="pi pi-times"
                            onClick={() => {
                                setEditDialogVisible(false);
                                setSelectedMedication(null);
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
                        <label htmlFor="edit-name">Medication Name</label>
                        <InputText
                            id="edit-name"
                            value={editFormData.name}
                            onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })}
                            placeholder="Enter medication name"
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