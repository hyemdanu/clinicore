import { useState, useEffect, useCallback, useRef, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Toast } from 'primereact/toast';
import { Dropdown } from 'primereact/dropdown';
import { get, post, del, put } from '../../services/api';
import '../Shared/css/residents.css';

/*
 * Caregiver list tab in the admin portal.
 * Shows all caregivers and the residents they are assigned to.
 * Admins can add, remove, or switch residents between caregivers.
 */
export default function CaregiverResidentList() {
    const navigate = useNavigate();
    const toastRef = useRef(null);

    const [caregivers, setCaregivers] = useState([]);
    const [allResidents, setAllResidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState('firstName');
    const [expandedIds, setExpandedIds] = useState(new Set());

    // add modal state
    const [showAddModal, setShowAddModal] = useState(false);
    const [addTargetCaregiver, setAddTargetCaregiver] = useState(null);
    const [selectedResidentId, setSelectedResidentId] = useState('');
    const [addLoading, setAddLoading] = useState(false);
    const [addError, setAddError] = useState(null);

    // switch modal state
    const [showSwitchModal, setShowSwitchModal] = useState(false);
    const [switchTargetCaregiver, setSwitchTargetCaregiver] = useState(null);
    const [switchTargetResident, setSwitchTargetResident] = useState(null);
    const [selectedCaregiverId, setSelectedCaregiverId] = useState('');
    const [switchLoading, setSwitchLoading] = useState(false);
    const [switchError, setSwitchError] = useState(null);

    const getCurrentUser = () => {
        const currentUserStr = localStorage.getItem('currentUser');
        if (!currentUserStr) { navigate('/'); return null; }
        return JSON.parse(currentUserStr);
    };

    const fetchCaregivers = useCallback(async () => {
        setLoading(true);
        setError(null);
        try {
            const currentUser = getCurrentUser();
            if (!currentUser) return;
            const data = await get(`/caregivers?currentUserId=${currentUser.id}`);
            setCaregivers(data);
        } catch (err) {
            console.error('Error fetching caregivers:', err);
            setError('Failed to load caregivers. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    const fetchAllResidents = useCallback(async () => {
        try {
            const currentUser = getCurrentUser();
            if (!currentUser) return;
            const data = await get(`/caregivers/residents?currentUserId=${currentUser.id}`);
            setAllResidents(data);
        } catch (err) {
            console.error('Error fetching residents:', err);
        }
    }, [navigate]);

    useEffect(() => {
        fetchCaregivers();
        fetchAllResidents();
    }, [fetchCaregivers, fetchAllResidents]);

    const filteredCaregivers = useMemo(() => {
        let filtered = [...caregivers];

        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(c =>
                `${c.firstName} ${c.lastName}`.toLowerCase().includes(query)
            );
        }

        filtered.sort((a, b) => {
            if (sortBy === 'firstName') return a.firstName.localeCompare(b.firstName);
            return a.lastName.localeCompare(b.lastName);
        });

        return filtered;
    }, [caregivers, searchQuery, sortBy]);

    const toggleExpanded = (id) => {
        setExpandedIds(prev => {
            const next = new Set(prev);
            if (next.has(id)) {
                next.delete(id);
            } else {
                next.add(id);
            }
            return next;
        });
    };

    const toggleSort = () => {
        setSortBy(prev => prev === 'firstName' ? 'lastName' : 'firstName');
    };

    // keyboard handler for accessibility
    const handleCardHeaderKeyDown = (e, id) => {
        if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault();
            toggleExpanded(id);
        }
    };

    // ── Remove ────────────────────────────────────────────────────────────────
    const handleRemoveResident = async (caregiverId, residentId) => {
        if (!window.confirm('Remove this resident from the caregiver?')) return;
        const currentUser = getCurrentUser();
        if (!currentUser) return;
        try {
            await del(`/caregivers/${caregiverId}/residents/${residentId}?currentUserId=${currentUser.id}`);
            fetchCaregivers();
        } catch (err) {
            toastRef.current?.show({ severity: 'error', summary: 'Error', detail: err.message || 'Failed to remove resident.' });
        }
    };

    // ── Add modal ─────────────────────────────────────────────────────────────
    const handleOpenAddModal = (caregiver) => {
        setAddTargetCaregiver(caregiver);
        setSelectedResidentId('');
        setAddError(null);
        setShowAddModal(true);
    };

    const handleCloseAddModal = () => {
        setShowAddModal(false);
        setAddTargetCaregiver(null);
        setAddError(null);
    };

    const handleConfirmAdd = async () => {
        if (!selectedResidentId) {
            setAddError('Please select a resident.');
            return;
        }
        const currentUser = getCurrentUser();
        if (!currentUser) return;
        setAddLoading(true);
        setAddError(null);
        try {
            await post(`/caregivers/${addTargetCaregiver.id}/residents/${selectedResidentId}?currentUserId=${currentUser.id}`);
            handleCloseAddModal();
            fetchCaregivers();
        } catch (err) {
            setAddError(err.message || 'Failed to assign resident.');
        } finally {
            setAddLoading(false);
        }
    };

    // ── Switch modal ──────────────────────────────────────────────────────────
    const handleOpenSwitchModal = (caregiver, resident) => {
        setSwitchTargetCaregiver(caregiver);
        setSwitchTargetResident(resident);
        setSelectedCaregiverId('');
        setSwitchError(null);
        setShowSwitchModal(true);
    };

    const handleCloseSwitchModal = () => {
        setShowSwitchModal(false);
        setSwitchTargetCaregiver(null);
        setSwitchTargetResident(null);
        setSwitchError(null);
    };

    const handleConfirmSwitch = async () => {
        if (!selectedCaregiverId) {
            setSwitchError('Please select a caregiver.');
            return;
        }
        const currentUser = getCurrentUser();
        if (!currentUser) return;
        setSwitchLoading(true);
        setSwitchError(null);
        try {
            await put(
                `/caregivers/${switchTargetCaregiver.id}/residents/${switchTargetResident.id}/switch?toCaregiverId=${selectedCaregiverId}&currentUserId=${currentUser.id}`
            );
            handleCloseSwitchModal();
            fetchCaregivers();
        } catch (err) {
            setSwitchError(err.message || 'Failed to switch resident.');
        } finally {
            setSwitchLoading(false);
        }
    };

    // handle escape key for modals
    const handleModalKeyDown = (e, closeHandler) => {
        if (e.key === 'Escape') {
            closeHandler();
        }
    };

    // residents not yet assigned to the target caregiver (for add dropdown)
    const unassignedResidents = addTargetCaregiver
        ? allResidents.filter(r => !addTargetCaregiver.residents?.some(ar => ar.id === r.id))
        : allResidents;

    // caregivers other than the current one (for switch dropdown)
    const otherCaregivers = switchTargetCaregiver
        ? caregivers.filter(c => c.id !== switchTargetCaregiver.id)
        : caregivers;

    return (
        <div className="residents-tab">
            <Toast ref={toastRef} />
            <div className="residents-actions">
                <button className="action-btn sort-btn" onClick={toggleSort}>
                    <i className="pi pi-sort-alt"></i>
                    SORT: {sortBy === 'firstName' ? 'FIRST NAME' : 'LAST NAME'}
                </button>
            </div>

            <div className="search-container">
                <i className="pi pi-search search-icon"></i>
                <input
                    type="text"
                    className="search-input"
                    placeholder="Search caregivers..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </div>

            {error && <div className="error-message">{error}</div>}

            {loading ? (
                <div className="residents-loading">
                    <i className="pi pi-spin pi-spinner"></i>
                    <span>Loading caregivers...</span>
                </div>
            ) : (
                <div className="residents-list">
                    {filteredCaregivers.length === 0 ? (
                        <div className="no-residents">No caregivers found</div>
                    ) : (
                        filteredCaregivers.map((caregiver) => {
                            const isExpanded = expandedIds.has(caregiver.id);
                            const residentCount = caregiver.residents?.length || 0;

                            return (
                                <div key={caregiver.id} className="caregiver-card">
                                    {/* Caregiver header row - converted from div to button */}
                                    <button
                                        className="caregiver-card-header btn-reset"
                                        onClick={() => toggleExpanded(caregiver.id)}
                                        onKeyDown={(e) => handleCardHeaderKeyDown(e, caregiver.id)}
                                        aria-expanded={isExpanded}
                                        type="button"
                                    >
                                        <div className="caregiver-avatar">
                                            <span>{caregiver.firstName?.[0]}</span>
                                        </div>
                                        <div className="caregiver-item-info">
                                            <span className="resident-name">
                                                {caregiver.firstName} {caregiver.lastName}
                                            </span>
                                            <span className="caregiver-email">{caregiver.email}</span>
                                        </div>
                                        <div className="caregiver-item-pills">
                                            <span className={`stat-pill ${residentCount > 0 ? 'resident-count-pill' : 'no-resident-pill'}`}>
                                                <i className="pi pi-users"></i>
                                                {residentCount} resident{residentCount !== 1 ? 's' : ''}
                                            </span>
                                        </div>
                                        <i className={`pi ${isExpanded ? 'pi-chevron-up' : 'pi-chevron-down'} caregiver-expand-icon`}></i>
                                    </button>

                                    {/* Expanded resident panel */}
                                    {isExpanded && (
                                        <div className="caregiver-residents-panel">
                                            {/* Panel header with Add button */}
                                            <div className="caregiver-panel-header">
                                                <span className="caregiver-panel-label">Assigned Residents</span>
                                                <button
                                                    className="action-btn add-resident-btn"
                                                    onClick={() => handleOpenAddModal(caregiver)}
                                                    type="button"
                                                >
                                                    <i className="pi pi-plus"></i>
                                                    Add Resident
                                                </button>
                                            </div>

                                            {residentCount === 0 ? (
                                                <div className="caregiver-no-residents">
                                                    <i className="pi pi-info-circle"></i>
                                                    No residents assigned yet
                                                </div>
                                            ) : (
                                                caregiver.residents.map((resident) => (
                                                    <div key={resident.id} className="caregiver-resident-row">
                                                        <div className="caregiver-resident-avatar">
                                                            <span>{resident.firstName?.[0]}</span>
                                                        </div>
                                                        <span className="caregiver-resident-name">
                                                            {resident.firstName} {resident.lastName}
                                                        </span>
                                                        {resident.assignedAt && (
                                                            <span className="caregiver-assigned-at">
                                                                Assigned {new Date(resident.assignedAt).toLocaleDateString()}
                                                            </span>
                                                        )}
                                                        <div className="resident-row-actions">
                                                            <button
                                                                className="resident-action-btn switch-btn"
                                                                title="Switch caregiver"
                                                                onClick={() => handleOpenSwitchModal(caregiver, resident)}
                                                                type="button"
                                                            >
                                                                <i className="pi pi-arrow-right-arrow-left"></i>
                                                                Switch
                                                            </button>
                                                            <button
                                                                className="resident-action-btn remove-btn"
                                                                title="Remove from caregiver"
                                                                onClick={() => handleRemoveResident(caregiver.id, resident.id)}
                                                                type="button"
                                                            >
                                                                <i className="pi pi-times"></i>
                                                                Remove
                                                            </button>
                                                        </div>
                                                    </div>
                                                ))
                                            )}
                                        </div>
                                    )}
                                </div>
                            );
                        })
                    )}
                </div>
            )}

            {/* ── Add Resident Modal ─────────────────────────────────────────── */}
            {showAddModal && (
                <div
                    className="edit-modal-backdrop"
                    onClick={handleCloseAddModal}
                    role="presentation"
                    onKeyDown={(e) => handleModalKeyDown(e, handleCloseAddModal)}
                >
                    <div className="edit-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="edit-modal-header">
                            <h3>Add Resident to {addTargetCaregiver?.firstName} {addTargetCaregiver?.lastName}</h3>
                            <button className="sidebar-close-btn" onClick={handleCloseAddModal} aria-label="Close modal">
                                <i className="pi pi-times"></i>
                            </button>
                        </div>
                        <div className="edit-modal-content">
                            {addError && <div className="error-message form-error">{addError}</div>}
                            <div className="form-group">
                                <label>Select Resident <span className="required">*</span></label>
                                <Dropdown
                                    value={selectedResidentId}
                                    onChange={(e) => setSelectedResidentId(e.value)}
                                    options={unassignedResidents.map(r => ({
                                        label: `${r.firstName} ${r.lastName}`,
                                        value: r.id
                                    }))}
                                    placeholder="-- Choose a resident --"
                                    className="w-full"
                                    appendTo="self"
                                />
                                {unassignedResidents.length === 0 && (
                                    <p className="form-info">
                                        All residents are already assigned to this caregiver.
                                    </p>
                                )}
                            </div>
                        </div>
                        <div className="edit-modal-footer">
                            <button className="btn btn-secondary" onClick={handleCloseAddModal}>Cancel</button>
                            <button
                                className="btn btn-primary"
                                onClick={handleConfirmAdd}
                                disabled={addLoading || !selectedResidentId}
                            >
                                {addLoading ? 'Assigning...' : 'Assign'}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ── Switch Caregiver Modal ─────────────────────────────────────── */}
            {showSwitchModal && (
                <div
                    className="edit-modal-backdrop"
                    onClick={handleCloseSwitchModal}
                    role="presentation"
                    onKeyDown={(e) => handleModalKeyDown(e, handleCloseSwitchModal)}
                >
                    <div className="edit-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="edit-modal-header">
                            <h3>Switch Caregiver for {switchTargetResident?.firstName} {switchTargetResident?.lastName}</h3>
                            <button className="sidebar-close-btn" onClick={handleCloseSwitchModal} aria-label="Close modal">
                                <i className="pi pi-times"></i>
                            </button>
                        </div>
                        <div className="edit-modal-content">
                            {switchError && <div className="error-message form-error">{switchError}</div>}
                            <div className="form-group">
                                <label>Current Caregiver</label>
                                <p className="info-value">
                                    {switchTargetCaregiver?.firstName} {switchTargetCaregiver?.lastName}
                                </p>
                                <label>Move to <span className="required">*</span></label>
                                <Dropdown
                                    value={selectedCaregiverId}
                                    onChange={(e) => setSelectedCaregiverId(e.value)}
                                    options={otherCaregivers.map(c => ({
                                        label: `${c.firstName} ${c.lastName}`,
                                        value: c.id
                                    }))}
                                    placeholder="-- Choose a caregiver --"
                                    className="w-full"
                                    appendTo="self"
                                />
                            </div>
                        </div>
                        <div className="edit-modal-footer">
                            <button className="btn btn-secondary" onClick={handleCloseSwitchModal}>Cancel</button>
                            <button
                                className="btn btn-primary"
                                onClick={handleConfirmSwitch}
                                disabled={switchLoading || !selectedCaregiverId}
                            >
                                {switchLoading ? 'Switching...' : 'Switch'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}