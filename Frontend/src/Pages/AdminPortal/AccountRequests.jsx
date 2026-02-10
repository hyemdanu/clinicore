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
import 'primereact/resources/primereact.min.css'; // Add this if missing


// filters for status dropdown
const STATUS_OPTIONS = [
    { label: 'All Statuses', value: '' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Approved', value: 'APPROVED' },
    { label: 'Denied', value: 'DENIED' },
    { label: 'Completed', value: 'COMPLETED' }
];

// empty form --> initial state for edit fields
const INITIAL_FORM_STATE = {
    firstName: '',
    lastName: '',
    email: ''
};

// retrieve user data from localStorage
// we'll have to change this cus we're not storing the user in localStorage in prod...
const getCurrentUserData = () => {

    // grab user
    const userStr = localStorage.getItem('currentUser');

    // Return null if no user is stored in localStorage
    if (!userStr) return null;

    // json for user object {id, role}
    const user = JSON.parse(userStr);
    return {
        id: user.id,
        role: user.role
    };
};

// start of function
// defining state variables
export default function AccountRequests() {

    // page itself //

    // react hook & toast component for notifcations
    const navigate = useNavigate();
    const toastRef = useRef(null);

    // state to control sidebar visibility (true = open, false = closed)
    const [sidebarOpen, setSidebarOpen] = useState(true);

    // we need state variables cus these shit changes

    // state to hold array of account requests from backend
    const [requests, setRequests] = useState([]);

    // state to show loading spinner while fetching data
    const [loading, setLoading] = useState(true);

    // state to store error messages if API calls fail
    const [error, setError] = useState(null);

    // state to store selected status filter (default is PENDING)
    const [selectedStatus, setSelectedStatus] = useState('PENDING');

    // user shit //

    // state to store the currently selected requested object (requesting user selected)
    const [selectedRequest, setSelectedRequest] = useState(null);
    // state to store current admin's user ID for API calls (user calling the endpoint)
    const [currentUserId, setCurrentUserId] = useState(null);

    // edit shit //

    // state to show/hide edit dialog form
    const [editDialogVisible, setEditDialogVisible] = useState(false);
    // state to store form input values (firstName, lastName, email) while editing
    const [editedData, setEditedData] = useState(INITIAL_FORM_STATE);

    // deny shit //

    // state to show/hide deny confirmation dialog
    const [denyDialogVisible, setDenyDialogVisible] = useState(false);

    // activation code shit //

    // state to store activation code received from backend --> code changes
    const [activationCode, setActivationCode] = useState('');
    // state to show/hide activation code display dialog
    const [activationCodeDialogVisible, setActivationCodeDialogVisible] = useState(false);


    // hook that runs once when component mounts to initialize and fetch data
    useEffect(() => {
        initializeAndFetch();
    }, []);

    // initialize component by checking admin status and call fetch requests
    const initializeAndFetch = async () => {

        // get user
        const user = getCurrentUserData();

        // check if user exists and has ADMIN role
        if (!user || user.role !== 'ADMIN') {
            // redirect to login if not admin
            navigate('/');
            return;
        }

        // store admin ID in state for later use (like appproving, denying, etc._
        setCurrentUserId(user.id);

        // call function to fetch account requests
        await fetchRequests(user.id);
    };

    // fetch all account requests from backend
    // its own method cus we go back to this whenever a dialogue form is closed
    const fetchRequests = async (adminId) => {

        // load spinner --> to let user know we're fetching data
        setLoading(true);

        // clear any previous error messages
        setError(null);


        try {
            // call GET endpoint to fetch account requests (passes adminId as param since need admin access
            const data = await get(`/accountCredential/account-requests?adminId=${adminId}`);

            // store fetched requests in state (default to empty array if no data) --> this data changes since if we approve, it goes away
            setRequests(data || []);


        } catch (err) {
            // Log error to browser console for debugging
            console.error('Error fetching account requests:', err);

            toastRef.current?.clear();

            // Show error toast notification to user
            toastRef.current?.show({
                summary: 'Error',
                detail: 'Failed to load account requests'
            });
        } finally {
            // Stop loading spinner regardless of success or error
            setLoading(false);
        }
    };

    // filter requests based on selected status filter
    const filteredRequests = selectedStatus

        // if status selected, filter requests to only show matching status
        ? requests.filter(req => req.status === selectedStatus)
        // if no status selected (empty string = "All"), return all requests
        : requests;

    // handle opening and closing of edit form
    const handleEditRequest = (request, isClosing = false) => {

        // if closing flag is true, close edit form and reset state
        if (isClosing) {
            // hide the edit form
            setEditDialogVisible(false);

            // clear selected request
            setSelectedRequest(null);

            // reset form to initial empty state
            setEditedData(INITIAL_FORM_STATE);
            return;
        }

        // if user is inputting editted data (form is open)
        setSelectedRequest(request);

        // populate form with current request data so user can see existing values
        setEditedData({
            firstName: request.firstName,
            lastName: request.lastName,
            email: request.email
        });

        // show the edit dialog form
        setEditDialogVisible(true);
    };

    // save edited request data to backend
    const handleSaveEditedRequest = async () => {
        // Guard clause: only proceed if request and user ID are set
        if (!selectedRequest || !currentUserId) return;

        try {
            // call PUT endpoint to update request data (passes requestId and adminId in URL)
            await put(
                `/accountCredential/account-requests/${selectedRequest.id}?adminId=${currentUserId}`,
                // send edited data in request body
                editedData
            );
            // Show success notification
            toastRef.current?.show({
                summary: 'Success',
                detail: 'Account request updated successfully'
            });


            // close the edit dialog
            setEditDialogVisible(false);

            // refresh requests list to show updated data
            await fetchRequests(currentUserId);
        } catch (err) {
            // Show error notification if save fails
            toastRef.current?.show({
                summary: 'Error',
                detail: err.message || 'Failed to update request'
            });
        }
    };

    // approve an account request and display activation code
    const handleApproveRequest = async (requestId) => {
        // Guard clause: only proceed if user ID is set
        if (!currentUserId) return;

        try {
            // call POST endpoint to approve request (passes requestId and adminId in URL)
            // then grab activation code from response
            const response = await post(
                `/accountCredential/account-requests/${requestId}/approve?adminId=${currentUserId}`,
                // send empty object as body (no data needed)
                {}
            );

            // store activation code received from backend
            setActivationCode(response.activationCode);

            // show dialog to display activation code to admin
            setActivationCodeDialogVisible(true);

            // show success notification
            toastRef.current?.show({
                summary: 'Approved',
                detail: 'Account request approved and email sent to the user.'
            });

            // refresh requests list to update request status
            await fetchRequests(currentUserId);

        } catch (err) {
            // Show error notification if approval fails
            toastRef.current?.show({
                summary: 'Error',
                detail: err.message || 'Failed to approve request'
            });
        }
    };

    // open deny confirmation dialog
    const handleOpenDenyDialog = (request) => {
        // store which request will be denied (needed for confirmation)
        setSelectedRequest(request);
        // show confirmation dialog
        setDenyDialogVisible(true);
    };

    // deny an account request after user confirms
    const handleDenyRequest = async () => {
        // Guard clause: only proceed if request and user ID are set
        if (!selectedRequest || !currentUserId) return;


        try {
            // call POST endpoint to deny request (passes requestId and adminId in URL)
            await post(
                `/accountCredential/account-requests/${selectedRequest.id}/deny?adminId=${currentUserId}`,
                // send empty object as body (no data needed)
                {}
            );

            // show success notification
            toastRef.current?.show({
                summary: 'Denied',
                detail: 'Account request has been denied.'
            });

            // close the confirmation dialog
            setDenyDialogVisible(false);
            // refresh requests list to update request status
            await fetchRequests(currentUserId);

        } catch (err) {
            // Show error notification if deny fails
            toastRef.current?.show({
                summary: 'Error',
                detail: err.message || 'Failed to deny request'
            });
        }
    };

    // close deny confirmation dialog
    const handleCloseDenyDialog = () => {
        // hide the confirmation dialog
        setDenyDialogVisible(false);
        // clear the selected request
        setSelectedRequest(null);
    };

    // resend activation code to an approved request
    const handleResendActivationCode = async (requestId) => {
        // Guard clause: only proceed if user ID is set
        if (!currentUserId) return;

        try {
            // call POST endpoint to generate and resend activation code (passes requestId and adminId in URL)
            // then grab new activation code from response
            const response = await post(
                `/accountCredential/account-requests/${requestId}/resend-activation-code?adminId=${currentUserId}`,
                // send empty object as body (no data needed)
                {}
            );

            // store new activation code received from backend
            setActivationCode(response.activationCode);
            // show dialog to display new activation code to admin
            setActivationCodeDialogVisible(true);
            // show success notification
            toastRef.current?.show({
                summary: 'Code Resent',
                detail: 'Activation code resent and email sent to the user.'
            });
            // refresh requests list
            await fetchRequests(currentUserId);
        } catch (err) {
            // Show error notification if resend fails
            toastRef.current?.show({
                summary: 'Error',
                detail: err.message || 'Failed to resend activation code'
            });
        }
    };

    // copy activation code to clipboard
    const handleCopyActivationCode = () => {
        // copy activation code text to clipboard using browser API
        navigator.clipboard.writeText(activationCode);
        // show confirmation toast
        toastRef.current?.show({
            summary: 'Copied',
            detail: 'Activation code copied to clipboard'
        });
    };

    // close activation code dialog and clear the code
    const handleCloseActivationCodeDialog = () => {
        // hide the dialog
        setActivationCodeDialogVisible(false);
        // clear the activation code from state
        setActivationCode('');
    };

    // render action buttons based on request status (Edit, Approve, Deny, Resend Code)
    const actionTemplate = (rowData) => {

        // check if request is completed, denied, approved (disable edit buttons for these)
        const isCompleteOrDenied = rowData.status === 'COMPLETED' || rowData.status === 'DENIED';

        // check if request is pending (show approve/deny/edit buttons)
        const isPending = rowData.status === 'PENDING';

        // check if request is approved (show resend code button)
        const isApproved = rowData.status === 'APPROVED';

        return (
            <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>

                {/* Edit button - disabled for completed/denied/approved requests */}
                <Button
                    icon="pi pi-pencil"
                    className="p-button-rounded p-button-warning"

                    // when button clicked, call edit handler with current row data
                    onClick={() => handleEditRequest(rowData)}

                    // hide button if request is completed or denied
                    disabled={isCompleteOrDenied}
                    title="Edit request"
                />

                {/* Approve and Deny buttons - only show for PENDING requests */}
                {isPending && (
                    <>
                        {/* Approve button */}
                        <Button
                            icon="pi pi-check"
                            className="p-button-rounded p-button-success"
                            // Call approve handler with request ID
                            onClick={() => handleApproveRequest(rowData.id)}
                            title="Approve request"
                        />

                        {/* Deny button */}
                        <Button
                            icon="pi pi-times"
                            className="p-button-rounded p-button-danger"
                            // Call deny handler with request object
                            onClick={() => handleOpenDenyDialog(rowData)}
                            title="Deny request"
                        />
                    </>
                )}

                {/* Resend activation code button - only show for APPROVED requests */}
                {isApproved && (
                    <Button
                        icon="pi pi-envelope"
                        className="p-button-rounded p-button-info"
                        // Call resend handler with request ID
                        onClick={() => handleResendActivationCode(rowData.id)}
                        title="Resend activation code"
                    />
                )}
            </div>
        );
    };

    // toggle sidebar between open and closed states
    const toggleSidebar = () => {
        // Flip sidebar state (open becomes closed, closed becomes open)
        setSidebarOpen(!sidebarOpen);
    };

    return (
        <div className="admin-dashboard-container">
            {/* Toast component - displays notification messages */}
            <Toast ref={toastRef} />

            {/* Header with sidebar toggle button */}
            <Header onToggleSidebar={toggleSidebar} title="Account Requests" />

            {/* Sidebar navigation menu */}
            <AdminSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            {/* Main content area */}
            <main className={`dashboard-content ${sidebarOpen ? 'content-with-sidebar' : ''}`}>
                {/* Page header and description */}
                <div className="alert-section">
                    <h2 className="dashboard-title">Account Creation Requests</h2>
                    <p className="dashboard-subtitle">
                        Review and manage pending account creation requests
                    </p>
                </div>

                {/* Display error message if API call fails */}
                {error && <div className="error-message">{error}</div>}

                {/* Requests data table section */}
                <section className="inventory-section" style={{ marginTop: '2rem' }}>
                    <div className="inventory-header">
                        <h3>Requests</h3>
                        {/* Status filter dropdown menu */}
                        <div className="sort-dropdown-wrapper">
                            <label>Filter by Status:</label>
                            <Dropdown
                                // Current selected status value
                                value={selectedStatus}
                                // Dropdown options (All Statuses, Pending, Approved, etc.)
                                options={STATUS_OPTIONS}
                                // Update state when user selects a status
                                onChange={(e) => setSelectedStatus(e.value)}
                                className="sort-dropdown"
                            />
                        </div>
                    </div>

                    {/* DataTable displaying filtered account requests */}
                    <DataTable
                        // Pass filtered requests to table
                        value={filteredRequests}
                        // Show loading spinner while fetching
                        loading={loading}
                        className="inventory-table modern-table"
                        emptyMessage="No account requests found"
                        // Enable pagination
                        paginator
                        // Show 10 rows per page
                        rows={10}

                        responsiveLayout="scroll"
                        style={{ fontSize: '0.95rem' }}
                    >
                        {/* Column for first name */}
                        <Column field="firstName" header="First Name" style={{ width: '12%' }} />
                        {/* Column for last name */}
                        <Column field="lastName" header="Last Name" style={{ width: '12%' }} />
                        {/* Column for email */}
                        <Column field="email" header="Email" style={{ width: '20%' }} />
                        {/* Column for role (resident, caregiver, admin) */}
                        <Column field="role" header="Role" style={{ width: '10%' }} />
                        {/* Column for status (pending, approved, denied, completed) */}
                        <Column field="status" header="Status" style={{ width: '12%' }} />
                        {/* Column for creation date */}
                        <Column field="createdAt" header="Created" style={{ width: '12%' }} />
                        {/* Column for expiration date */}
                        <Column field="expiresAt" header="Expires" style={{ width: '12%' }} />

                        {/* Column for action buttons (edit, approve, deny, resend code) */}
                        <Column body={actionTemplate} header="Actions" style={{ width: '10%' }} />
                    </DataTable>
                </section>
            </main>

            {/* Edit dialog form for updating account request details */}
            <Dialog
                // Show/hide dialog based on state
                visible={editDialogVisible}
                // Close dialog when X button clicked
                onHide={() => handleEditRequest(null, true)}
                header="Edit Account Request"
                // Make dialog modal (blocks background interactions)
                modal
                closable={false}
                className="modern-dialog"
                // Make dialog responsive on different screen sizes
                breakpoints={{ '960px': '80vw', '640px': '95vw' }}
                // Set dialog width to 40% of viewport
                style={{ width: '40vw' }}
            >
                <div className="dialog-form">
                    {/* First name text entry field */}
                    <div className="dialog-field">
                        <label htmlFor="firstName">First Name</label>
                        <InputText
                            id="firstName"
                            // Show current firstName value or empty string
                            value={editedData.firstName || ''}
                            // Update state as user types
                            onChange={(e) =>
                                setEditedData({ ...editedData, firstName: e.target.value })
                            }
                            className="p-inputtext-lg"
                        />
                    </div>

                    {/* Last name text entry field */}
                    <div className="dialog-field">
                        <label htmlFor="lastName">Last Name</label>
                        <InputText
                            id="lastName"
                            // Show current lastName value or empty string
                            value={editedData.lastName || ''}
                            // Update state as user types
                            onChange={(e) =>
                                setEditedData({ ...editedData, lastName: e.target.value })
                            }
                            className="p-inputtext-lg"
                        />
                    </div>

                    {/* Email text entry field */}
                    <div className="dialog-field">
                        <label htmlFor="email">Email</label>
                        <InputText
                            id="email"
                            type="email"
                            // Show current email value or empty string
                            value={editedData.email || ''}
                            // Update state as user types
                            onChange={(e) =>
                                setEditedData({ ...editedData, email: e.target.value })
                            }
                            className="p-inputtext-lg"
                        />
                    </div>

                    {/* Cancel and Save buttons */}
                    <div className="dialog-actions dialog-actions--right">
                        <Button
                            label="Cancel"
                            onClick={() => handleEditRequest(null, true)}
                            className="p-button-secondary"
                        />

                        <Button
                            label="Save Changes"
                            onClick={handleSaveEditedRequest}
                            className="p-button-success"
                        />
                    </div>

                </div>
            </Dialog>

            {/* Deny request confirmation dialog */}
            <Dialog
                // Show/hide dialog based on state
                visible={denyDialogVisible}
                // Close dialog when X button clicked
                onHide={handleCloseDenyDialog}
                header="Confirm Deny Request"
                // Make dialog modal (blocks background interactions)
                modal
                closable={false}
                className="modern-dialog"
                // Make dialog responsive on different screen sizes
                breakpoints={{ '960px': '80vw', '640px': '95vw' }}
                // Set dialog width to 32% of viewport
                style={{ width: '32vw' }}
            >
                <div className="dialog-confirm">
                    {/* Confirmation message showing email of request to deny */}
                    <p>
                        Are you sure you want to <strong>deny</strong> the account request for{' '}
                        <strong>{selectedRequest?.email}</strong>? This action cannot be undone.
                    </p>

                    {/* Cancel and Deny buttons */}
                    <div className="dialog-actions dialog-actions--right">
                        <Button
                            label="Cancel"
                            onClick={handleCloseDenyDialog}
                            className="p-button-secondary"
                        />

                        <Button
                            label="Deny Request"
                            onClick={handleDenyRequest}
                            className="p-button-danger"
                        />
                    </div>
                </div>
            </Dialog>

            {/* Display activation code dialog showing code to share with user */}
            <Dialog
                // Show/hide dialog based on state
                visible={activationCodeDialogVisible}
                // Close dialog when X button clicked
                onHide={handleCloseActivationCodeDialog}
                header="Activation Code"
                // Make dialog modal (blocks background interactions)
                modal
                closable={false}
                className="modern-dialog"
                // Make dialog responsive on different screen sizes
                breakpoints={{ '960px': '80vw', '640px': '95vw' }}
                // Set dialog width to 40% of viewport
                style={{ width: '40vw' }}
            >
                <div className="dialog-form">
                    {/* Info message explaining activation code usage and expiration */}
                    <p className="dialog-subtitle">
                        An activation email has been sent to the user. You can also share this code
                        directly if needed. The code will expire in 7 days.
                    </p>

                    {/* Activation code display card */}
                    <div className="activation-code-card">
                        <p className="activation-code-label">Activation Code</p>
                        {/* Display the actual activation code value */}
                        <p className="activation-code-value">{activationCode}</p>
                    </div>

                    <div className="activation-buttons">
                        <Button
                            label="Copy to Clipboard"
                            onClick={handleCopyActivationCode}
                            className="p-button-info"
                        />
                        <Button
                            label="Done"
                            onClick={handleCloseActivationCodeDialog}
                        />
                    </div>

                </div>
            </Dialog>
        </div>
    );
}