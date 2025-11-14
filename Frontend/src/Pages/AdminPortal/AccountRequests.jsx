import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
import { InputText } from 'primereact/inputtext';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { get, put, post } from '../../services/api';
import Header from '../../Components/Header';
import AdminSidebar from '../../Components/AdminSidebar';
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';
import './css/admin.css';

export default function AccountRequests() {
    const navigate = useNavigate();
    const toastRef = useRef(null);

    // sidebar and auth
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [currentUserId, setCurrentUserId] = useState(null);

    // data and loading
    const [requests, setRequests] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // dialogs
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    const [denyDialogVisible, setDenyDialogVisible] = useState(false);
    const [activationCodeDialogVisible, setActivationCodeDialogVisible] = useState(false);

    // form state
    const [selectedRequest, setSelectedRequest] = useState(null);
    const [editedData, setEditedData] = useState({});
    const [activationCode, setActivationCode] = useState('');

    // status options for filtering
    const statusOptions = [
        { label: 'All Statuses', value: '' },
        { label: 'Pending', value: 'PENDING' },
        { label: 'Approved', value: 'APPROVED' },
        { label: 'Denied', value: 'DENIED' },
        { label: 'Completed', value: 'COMPLETED' }
    ];

    const [selectedStatus, setSelectedStatus] = useState('PENDING');

    // Load requests on mount
    useEffect(() => {
        fetchRequests();
    }, []);

    // Fetch all account requests
    const fetchRequests = async () => {
        setLoading(true);
        setError(null);

        try {
            const userStr = localStorage.getItem('currentUser');
            if (!userStr) {
                navigate('/');
                return;
            }

            const user = JSON.parse(userStr);
            setCurrentUserId(user.id);

            const data = await get(`/accountCredential/account-requests?adminId=${user.id}`);
            setRequests(data || []);
        } catch (err) {
            console.error('Error fetching account requests:', err);
            setError('Failed to load account requests. Please try again.');
            toastRef.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: 'Failed to load account requests'
            });
        } finally {
            setLoading(false);
        }
    };

    // Filter requests by status
    const filteredRequests = selectedStatus
        ? requests.filter(req => req.status === selectedStatus)
        : requests;

    // ===== EDIT DIALOG =====
    const openEditDialog = (request) => {
        setSelectedRequest(request);
        setEditedData({
            firstName: request.firstName,
            lastName: request.lastName,
            email: request.email
        });
        setEditDialogVisible(true);
    };

    const saveEditedRequest = async () => {
        if (!selectedRequest) return;

        try {
            await put(
                `/accountCredential/account-requests/${selectedRequest.id}?adminId=${currentUserId}`,
                editedData
            );

            toastRef.current?.show({
                severity: 'success',
                summary: 'Success',
                detail: 'Account request updated successfully'
            });

            setEditDialogVisible(false);
            fetchRequests(); // Refresh list
        } catch (err) {
            toastRef.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: err.message || 'Failed to update request'
            });
        }
    };

    // ===== APPROVE =====
    const approveRequest = async (requestId) => {
        try {
            const response = await post(
                `/accountCredential/account-requests/${requestId}/approve?adminId=${currentUserId}`,
                {}
            );

            setActivationCode(response.activationCode);
            setActivationCodeDialogVisible(true);

            toastRef.current?.show({
                severity: 'success',
                summary: 'Approved',
                detail: 'Account request approved and email sent to the user.'
            });

            fetchRequests();
        } catch (err) {
            toastRef.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: err.message || 'Failed to approve request'
            });
        }
    };

    // ===== DENY DIALOG =====
    const openDenyDialog = (request) => {
        setSelectedRequest(request);
        setDenyDialogVisible(true);
    };

    const denyRequest = async () => {
        if (!selectedRequest) return;

        try {
            // reason removed from UI; pass an empty string or let backend ignore it
            await post(
                `/accountCredential/account-requests/${selectedRequest.id}/deny?adminId=${currentUserId}&reason=`,
                {}
            );

            toastRef.current?.show({
                severity: 'success',
                summary: 'Denied',
                detail: 'Account request has been denied.'
            });

            setDenyDialogVisible(false);
            fetchRequests();
        } catch (err) {
            toastRef.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: err.message || 'Failed to deny request'
            });
        }
    };

    // ===== RESEND ACTIVATION CODE =====
    const resendActivationCode = async (requestId) => {
        try {
            const response = await post(
                `/accountCredential/account-requests/${requestId}/resend-activation-code?adminId=${currentUserId}`,
                {}
            );

            setActivationCode(response.activationCode);
            setActivationCodeDialogVisible(true);

            toastRef.current?.show({
                severity: 'success',
                summary: 'Code Resent',
                detail: 'Activation code resent and email sent to the user.'
            });

            fetchRequests();
        } catch (err) {
            toastRef.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: err.message || 'Failed to resend activation code'
            });
        }
    };

    const actionTemplate = (rowData) => {
        return (
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                <Button
                    icon="pi pi-pencil"
                    className="p-button-rounded p-button-warning"
                    onClick={() => openEditDialog(rowData)}
                    disabled={rowData.status === 'COMPLETED' || rowData.status === 'DENIED'}
                    title="Edit request"
                />

                {rowData.status === 'PENDING' && (
                    <>
                        <Button
                            icon="pi pi-check"
                            className="p-button-rounded p-button-success"
                            onClick={() => approveRequest(rowData.id)}
                            title="Approve request"
                        />
                        <Button
                            icon="pi pi-times"
                            className="p-button-rounded p-button-danger"
                            onClick={() => openDenyDialog(rowData)}
                            title="Deny request"
                        />
                    </>
                )}

                {rowData.status === 'APPROVED' && (
                    <Button
                        icon="pi pi-envelope"
                        className="p-button-rounded p-button-info"
                        onClick={() => resendActivationCode(rowData.id)}
                        title="Resend activation code"
                    />
                )}
            </div>
        );
    };

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    return (
        <div className="admin-dashboard-container">
            <Toast ref={toastRef} />
            <Header onToggleSidebar={toggleSidebar} title="Account Requests" />
            <AdminSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                <div className="alert-section">
                    <h2 className="dashboard-title">Account Creation Requests</h2>
                    <p className="dashboard-subtitle">
                        Review and manage pending account creation requests
                    </p>
                </div>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <section className="inventory-section" style={{ marginTop: '2rem' }}>
                    <div className="inventory-header">
                        <h3>Requests</h3>
                        <div className="sort-dropdown-wrapper">
                            <label>Filter by Status:</label>
                            <Dropdown
                                value={selectedStatus}
                                options={statusOptions}
                                onChange={(e) => setSelectedStatus(e.value)}
                                className="sort-dropdown"
                            />
                        </div>
                    </div>

                    <DataTable
                        value={filteredRequests}
                        loading={loading}
                        className="inventory-table modern-table"
                        emptyMessage="No account requests found"
                        paginator
                        rows={10}
                        rowsPerPageOptions={[5, 10, 20]}
                        responsiveLayout="scroll"
                        style={{ fontSize: '0.95rem' }} // slightly bigger data font
                    >
                        <Column field="firstName" header="First Name" style={{ width: '12%' }} />
                        <Column field="lastName" header="Last Name" style={{ width: '12%' }} />
                        <Column field="email" header="Email" style={{ width: '20%' }} />
                        <Column field="role" header="Role" style={{ width: '10%' }} />
                        <Column field="status" header="Status" style={{ width: '12%' }} />
                        <Column field="createdAt" header="Created" style={{ width: '12%' }} />
                        <Column field="expiresAt" header="Expires" style={{ width: '12%' }} />
                        <Column body={actionTemplate} header="Actions" style={{ width: '10%' }} />
                    </DataTable>
                </section>
            </main>

            {/* EDIT DIALOG */}
            <Dialog
                visible={editDialogVisible}
                onHide={() => setEditDialogVisible(false)}
                header="Edit Account Request"
                modal
                className="modern-dialog"
                breakpoints={{ '960px': '80vw', '640px': '95vw' }}
                style={{ width: '40vw' }}
            >
                <div className="dialog-form">
                    <div className="dialog-field">
                        <label htmlFor="firstName">First Name</label>
                        <InputText
                            id="firstName"
                            value={editedData.firstName || ''}
                            onChange={(e) => setEditedData({ ...editedData, firstName: e.target.value })}
                            className="p-inputtext-lg"
                        />
                    </div>

                    <div className="dialog-field">
                        <label htmlFor="lastName">Last Name</label>
                        <InputText
                            id="lastName"
                            value={editedData.lastName || ''}
                            onChange={(e) => setEditedData({ ...editedData, lastName: e.target.value })}
                            className="p-inputtext-lg"
                        />
                    </div>

                    <div className="dialog-field">
                        <label htmlFor="email">Email</label>
                        <InputText
                            id="email"
                            type="email"
                            value={editedData.email || ''}
                            onChange={(e) => setEditedData({ ...editedData, email: e.target.value })}
                            className="p-inputtext-lg"
                        />
                    </div>

                    <div className="dialog-actions">
                        <Button
                            label="Cancel"
                            icon="pi pi-times"
                            onClick={() => setEditDialogVisible(false)}
                            className="p-button-text"
                        />
                        <Button
                            label="Save Changes"
                            icon="pi pi-check"
                            onClick={saveEditedRequest}
                            className="p-button-success"
                        />
                    </div>
                </div>
            </Dialog>

            {/* DENY DIALOG */}
            <Dialog
                visible={denyDialogVisible}
                onHide={() => setDenyDialogVisible(false)}
                header="Confirm Deny Request"
                modal
                className="modern-dialog"
                breakpoints={{ '960px': '80vw', '640px': '95vw' }}
                style={{ width: '32vw' }}
            >
                <div className="dialog-confirm">
                    <p>
                        Are you sure you want to <strong>deny</strong> the account request for{' '}
                        <strong>{selectedRequest?.email}</strong>? This action cannot be undone.
                    </p>

                    <div className="dialog-actions">
                        <Button
                            label="Cancel"
                            icon="pi pi-times"
                            onClick={() => setDenyDialogVisible(false)}
                            className="p-button-text"
                        />
                        <Button
                            label="Deny Request"
                            icon="pi pi-ban"
                            onClick={denyRequest}
                            className="p-button-danger"
                        />
                    </div>
                </div>
            </Dialog>

            {/* ACTIVATION CODE DIALOG */}
            <Dialog
                visible={activationCodeDialogVisible}
                onHide={() => setActivationCodeDialogVisible(false)}
                header="Activation Code"
                modal
                className="modern-dialog"
                breakpoints={{ '960px': '80vw', '640px': '95vw' }}
                style={{ width: '40vw' }}
            >
                <div className="dialog-form">
                    <p className="dialog-subtitle">
                        An activation email has been sent to the user. You can also share this code
                        directly if needed. The code will expire in 3 days.
                    </p>

                    <div className="activation-code-card">
                        <p className="activation-code-label">Activation Code</p>
                        <p className="activation-code-value">
                            {activationCode}
                        </p>
                    </div>

                    <Button
                        label="Copy to Clipboard"
                        icon="pi pi-copy"
                        onClick={() => {
                            navigator.clipboard.writeText(activationCode);
                            toastRef.current?.show({
                                severity: 'info',
                                summary: 'Copied',
                                detail: 'Activation code copied to clipboard'
                            });
                        }}
                        className="p-button-info"
                        style={{ width: '100%' }}
                    />

                    <Button
                        label="Done"
                        onClick={() => setActivationCodeDialogVisible(false)}
                        style={{ width: '100%' }}
                    />
                </div>
            </Dialog>
        </div>
    );
}
