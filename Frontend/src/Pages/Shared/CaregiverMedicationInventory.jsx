import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { InputText } from 'primereact/inputtext';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';
import { Toast } from 'primereact/toast';
import { get, post, put, del } from '../../services/api.js';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';
import '../CaregiverPortal/css/CaregiverInventory.css';
import medicationsIcon from '../../assets/icons/medicationsIcon.png';

export default function MedicationInventory() {
    const navigate = useNavigate();
    const toastRef = useRef(null);
    const [currentUser] = useState(() => {
        const str = localStorage.getItem('currentUser');
        if (!str) return null;
        return JSON.parse(str);
    });

    const [medications, setMedications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [globalFilter, setGlobalFilter] = useState('');

    const [addDialogVisible, setAddDialogVisible] = useState(false);
    const [addFormData, setAddFormData] = useState({ name: '', quantity: '' });

    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [selectedMedication, setSelectedMedication] = useState(null);
    const [editFormData, setEditFormData] = useState({ name: '', quantity: '' });

    useEffect(() => {
        fetchMedications();
    }, []);

    const fetchMedications = async () => {
        setLoading(true);
        setError(null);
        try {
            if (!currentUser) { navigate('/'); return; }
            const data = await get(`/inventory/medication?currentUserId=${currentUser.id}`);
            setMedications(data);
        } catch (error) {
            setError('Failed to load medication inventory. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleAdjustQuantity = async (medication, change) => {
        if (medication.quantity === 0 && change < 0) {
            toastRef.current?.show({ severity: 'warn', summary: 'Invalid', detail: 'Quantity cannot be less than 0' });
            return;
        }
        try {
            const newQuantity = Math.max(0, medication.quantity + change);
            await put(`/inventory/medication/${medication.id}?currentUserId=${currentUser.id}`, {
                ...medication,
                quantity: newQuantity
            });
            setMedications(prev => prev.map(m => m.id === medication.id ? { ...m, quantity: newQuantity } : m));
        } catch (error) {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to adjust quantity. Please try again.' });
        }
    };

    const handleEditMedication = (medication) => {
        setSelectedMedication(medication);
        setEditFormData({ name: medication.name, quantity: medication.quantity.toString() });
        setEditDialogVisible(true);
    };

    const handleSaveEdit = async () => {
        if (!editFormData.name.trim() || !editFormData.quantity) {
            toastRef.current?.show({ severity: 'warn', summary: 'Missing Fields', detail: 'Please fill in all fields' });
            return;
        }
        const quantity = parseInt(editFormData.quantity);
        if (quantity < 0) {
            toastRef.current?.show({ severity: 'warn', summary: 'Invalid', detail: 'Quantity cannot be negative. Please enter a value of 0 or greater.' });
            return;
        }
        try {
            await put(`/inventory/medication/${selectedMedication.id}?currentUserId=${currentUser.id}`, {
                ...selectedMedication,
                name: editFormData.name.trim(),
                quantity
            });
            setMedications(prev => prev.map(m => m.id === selectedMedication.id ? { ...m, name: editFormData.name.trim(), quantity } : m));
            setEditDialogVisible(false);
            setSelectedMedication(null);
            setEditFormData({ name: '', quantity: '' });
        } catch (error) {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to update medication. Please try again.' });
        }
    };

    const handleDeleteMedication = async (medication) => {
        if (!window.confirm(`Are you sure you want to delete ${medication.name}?`)) return;
        try {
            await del(`/inventory/medication/${medication.id}?currentUserId=${currentUser.id}`);
            setMedications(prev => prev.filter(m => m.id !== medication.id));
        } catch (error) {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to delete medication. Please try again.' });
        }
    };

    const handleAddMedication = async () => {
        if (!addFormData.name.trim() || !addFormData.quantity) {
            toastRef.current?.show({ severity: 'warn', summary: 'Missing Fields', detail: 'Please fill in all fields' });
            return;
        }
        const quantity = parseInt(addFormData.quantity);
        if (quantity < 0) {
            toastRef.current?.show({ severity: 'warn', summary: 'Invalid', detail: 'Quantity cannot be negative. Please enter a value of 0 or greater.' });
            return;
        }
        try {
            const created = await post(`/inventory/medication?currentUserId=${currentUser.id}`, {
                name: addFormData.name.trim(),
                quantity
            });
            setMedications(prev => [...prev, created]);
            setAddDialogVisible(false);
            setAddFormData({ name: '', quantity: '' });
        } catch (error) {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to add medication. Please try again.' });
        }
    };

    const actionBodyTemplate = (rowData) => (
        <div className="action-buttons">
            <button className="btn btn-add" onClick={() => handleAdjustQuantity(rowData, 1)} aria-label="Increase quantity">+</button>
            <button className="btn btn-remove" onClick={() => handleAdjustQuantity(rowData, -1)} aria-label="Decrease quantity" disabled={rowData.quantity === 0}>-</button>
        </div>
    );

    const menuBodyTemplate = (rowData) => (
        <div className="menu-buttons">
            <button className="menu-button" onClick={() => handleEditMedication(rowData)} aria-label="Edit">✏️</button>
            <button className="menu-button delete-button" onClick={() => handleDeleteMedication(rowData)} aria-label="Delete">🗑️</button>
        </div>
    );

    const quantityBodyTemplate = (rowData) => {
        const isLowStock = rowData.quantity <= 10;
        return (
            <div className={`quantity-cell ${isLowStock ? 'low-stock' : ''}`}>
                {isLowStock && <i className="pi pi-exclamation-triangle warning-icon"></i>}
                <span>{rowData.quantity}</span>
            </div>
        );
    };

    const renderHeader = () => (
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
                onClick={() => { setAddFormData({ name: '', quantity: '' }); setAddDialogVisible(true); }}
            />
        </div>
    );

    const loadingIcon = (
        <div className="custom-loading">
            <i className="pi pi-spin pi-spinner custom-spinner"></i>
            <span>Loading medications...</span>
        </div>
    );

    return (
        <div className="medication-inventory-content">
            <Toast ref={toastRef} />
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
                    <Column field="name" header="Medication" sortable />
                    <Column field="quantity" header="Quantity" body={quantityBodyTemplate} sortable />
                    <Column header="Action" body={actionBodyTemplate} />
                    <Column body={menuBodyTemplate} />
                </DataTable>
            </div>

            <Dialog
                header="Add New Medication"
                visible={addDialogVisible}
                className="medication-dialog"
                onHide={() => { setAddDialogVisible(false); setAddFormData({ name: '', quantity: '' }); }}
                footer={
                    <div>
                        <Button label="Cancel" icon="pi pi-times" onClick={() => { setAddDialogVisible(false); setAddFormData({ name: '', quantity: '' }); }} className="p-button-text" />
                        <Button label="Add" icon="pi pi-check" onClick={handleAddMedication} autoFocus />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="add-name">Medication Name</label>
                        <InputText id="add-name" value={addFormData.name} onChange={(e) => setAddFormData({ ...addFormData, name: e.target.value })} placeholder="Enter medication name" className="w-full" />
                    </div>
                    <div className="field">
                        <label htmlFor="add-quantity">Quantity</label>
                        <InputText id="add-quantity" type="number" value={addFormData.quantity} onChange={(e) => setAddFormData({ ...addFormData, quantity: e.target.value })} placeholder="Enter initial quantity" className="w-full" min="0" />
                    </div>
                </div>
            </Dialog>

            <Dialog
                header="Edit Medication"
                visible={editDialogVisible}
                className="medication-dialog"
                onHide={() => { setEditDialogVisible(false); setSelectedMedication(null); setEditFormData({ name: '', quantity: '' }); }}
                footer={
                    <div>
                        <Button label="Cancel" icon="pi pi-times" onClick={() => { setEditDialogVisible(false); setSelectedMedication(null); setEditFormData({ name: '', quantity: '' }); }} className="p-button-text" />
                        <Button label="Save" icon="pi pi-check" onClick={handleSaveEdit} autoFocus />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="edit-name">Medication Name</label>
                        <InputText id="edit-name" value={editFormData.name} onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })} placeholder="Enter medication name" className="w-full" />
                    </div>
                    <div className="field">
                        <label htmlFor="edit-quantity">Quantity</label>
                        <InputText id="edit-quantity" type="number" value={editFormData.quantity} onChange={(e) => setEditFormData({ ...editFormData, quantity: e.target.value })} placeholder="Enter quantity" className="w-full" min="0" />
                    </div>
                </div>
            </Dialog>
        </div>
    );
}