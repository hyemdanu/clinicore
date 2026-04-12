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
import '../CaregiverPortal/css/consumables-inventory.css';
import consumablesIcon from '../../assets/icons/consumablesIcon.png';

export default function ConsumablesInventory() {
    const navigate = useNavigate();
    const toastRef = useRef(null);
    const [currentUser] = useState(() => {
        const str = localStorage.getItem('currentUser');
        if (!str) return null;
        return JSON.parse(str);
    });

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

    useEffect(() => {
        fetchConsumables();
    }, []);

    const fetchConsumables = async () => {
        setLoading(true);
        setError(null);

        try {
            if (!currentUser) { navigate('/'); return; }
            const data = await get(`/inventory/consumables?currentUserId=${currentUser.id}`);
            setConsumables(data);
        } catch {
            setError('Failed to load consumables inventory. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleAdjustQuantity = async (consumable, change) => {
        if (consumable.quantity === 0 && change < 0) {
            toastRef.current?.show({ severity: 'warn', summary: 'Invalid', detail: 'Quantity cannot be less than 0' });
            return;
        }
        try {
            const newQuantity = Math.max(0, consumable.quantity + change);
            await put(`/inventory/consumables/${consumable.id}?currentUserId=${currentUser.id}`, {
                ...consumable,
                quantity: newQuantity
            });
            setConsumables(prev => prev.map(c => c.id === consumable.id ? { ...c, quantity: newQuantity } : c));
        } catch {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to adjust quantity. Please try again.' });
        }
    };

    const handleEditConsumable = (consumable) => {
        setSelectedConsumable(consumable);
        setEditFormData({ name: consumable.name, quantity: consumable.quantity.toString() });
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
            await put(`/inventory/consumables/${selectedConsumable.id}?currentUserId=${currentUser.id}`, {
                ...selectedConsumable,
                name: editFormData.name.trim(),
                quantity
            });
            setConsumables(prev => prev.map(c => c.id === selectedConsumable.id ? { ...c, name: editFormData.name.trim(), quantity } : c));
            setEditDialogVisible(false);
            setSelectedConsumable(null);
            setEditFormData({ name: '', quantity: '' });
        } catch {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to update consumable. Please try again.' });
        }
    };

    const handleDeleteConsumable = async (consumable) => {
        if (!window.confirm(`Are you sure you want to delete ${consumable.name}?`)) return;
        try {
            await del(`/inventory/consumables/${consumable.id}?currentUserId=${currentUser.id}`);
            setConsumables(prev => prev.filter(c => c.id !== consumable.id));
        } catch {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to delete consumable. Please try again.' });
        }
    };

    const handleAddConsumable = async () => {
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
            const created = await post(`/inventory/consumables?currentUserId=${currentUser.id}`, {
                name: addFormData.name.trim(),
                quantity
            });
            setConsumables(prev => [...prev, created]);
            setAddDialogVisible(false);
            setAddFormData({ name: '', quantity: '' });
        } catch {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: 'Failed to add consumable. Please try again.' });
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
            <button className="menu-button" onClick={() => handleEditConsumable(rowData)} aria-label="Edit">✏️</button>
            <button className="menu-button delete-button" onClick={() => handleDeleteConsumable(rowData)} aria-label="Delete">🗑️</button>
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
                        placeholder="Search consumables..."
                        className="search-input"
                    />
                </span>
            </div>
            <Button
                label="Add Consumable"
                icon="pi pi-plus"
                className="add-consumable-btn"
                onClick={() => { setAddFormData({ name: '', quantity: '' }); setAddDialogVisible(true); }}
            />
        </div>
    );

    const loadingIcon = (
        <div className="custom-loading">
            <i className="pi pi-spin pi-spinner custom-spinner"></i>
            <span>Loading consumables...</span>
        </div>
    );

    return (
        <div className="consumables-inventory-content">
            <Toast ref={toastRef} />
            <div className="inventory-header-section">
                <div className="header-icon-wrapper">
                    <img src={consumablesIcon} alt="Consumables" className="header-icon" />
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
                    <Column field="name" header="Item Name" sortable />
                    <Column field="quantity" header="Quantity" body={quantityBodyTemplate} sortable />
                    <Column header="Action" body={actionBodyTemplate} />
                    <Column body={menuBodyTemplate} />
                </DataTable>
            </div>

            <Dialog
                header="Add New Consumable"
                visible={addDialogVisible}
                className="consumable-dialog"
                onHide={() => { setAddDialogVisible(false); setAddFormData({ name: '', quantity: '' }); }}
                footer={
                    <div>
                        <Button label="Cancel" icon="pi pi-times" onClick={() => { setAddDialogVisible(false); setAddFormData({ name: '', quantity: '' }); }} className="p-button-text" />
                        <Button label="Add" icon="pi pi-check" onClick={handleAddConsumable} autoFocus />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="add-name">Item Name</label>
                        <InputText id="add-name" value={addFormData.name} onChange={(e) => setAddFormData({ ...addFormData, name: e.target.value })} placeholder="Enter consumable item name" className="w-full" />
                    </div>
                    <div className="field">
                        <label htmlFor="add-quantity">Quantity</label>
                        <InputText id="add-quantity" type="number" value={addFormData.quantity} onChange={(e) => setAddFormData({ ...addFormData, quantity: e.target.value })} placeholder="Enter initial quantity" className="w-full" min="0" />
                    </div>
                </div>
            </Dialog>

            <Dialog
                header="Edit Consumable"
                visible={editDialogVisible}
                className="consumable-dialog"
                onHide={() => { setEditDialogVisible(false); setSelectedConsumable(null); setEditFormData({ name: '', quantity: '' }); }}
                footer={
                    <div>
                        <Button label="Cancel" icon="pi pi-times" onClick={() => { setEditDialogVisible(false); setSelectedConsumable(null); setEditFormData({ name: '', quantity: '' }); }} className="p-button-text" />
                        <Button label="Save" icon="pi pi-check" onClick={handleSaveEdit} autoFocus />
                    </div>
                }
            >
                <div className="dialog-content">
                    <div className="field">
                        <label htmlFor="edit-name">Item Name</label>
                        <InputText id="edit-name" value={editFormData.name} onChange={(e) => setEditFormData({ ...editFormData, name: e.target.value })} placeholder="Enter consumable item name" className="w-full" />
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