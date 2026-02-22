import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { get, post, del, put } from '../../services/api';
import '../Shared/css/residents.css';

/*
 * Caregiver list tab in the admin portal.
 * Shows all caregivers and the residents they are assigned to.
 * Admins can add, remove, or switch residents between caregivers.
 */
export default function CaregiverResidentList() {
    const navigate = useNavigate();

    const [caregivers, setCaregivers] = useState([]);
    const [filteredCaregivers, setFilteredCaregivers] = useState([]);
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

    useEffect(() => {
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

        setFilteredCaregivers(filtered);
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

    // ── Remove ────────────────────────────────────────────────────────────────
    const handleRemoveResident = async (caregiverId, residentId) => {
        if (!window.confirm('Remove this resident from the caregiver?')) return;
        const currentUser = getCurrentUser();
        if (!currentUser) return;
        try {
            await del(`/caregivers/${caregiverId}/residents/${residentId}?currentUserId=${currentUser.id}`);
            fetchCaregivers();
        } catch (err) {
            alert(err.message || 'Failed to remove resident.');
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
                                    {/* Caregiver header row */}
                                    <div
                                        className="caregiver-card-header"
                                        onClick={() => toggleExpanded(caregiver.id)}
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
                                    </div>

                                    {/* Expanded resident panel */}
                                    {isExpanded && (
                                        <div className="caregiver-residents-panel">
                                            {/* Panel header with Add button */}
                                            <div className="caregiver-panel-header">
                                                <span className="caregiver-panel-label">Assigned Residents</span>
                                                <button
                                                    className="action-btn add-resident-btn"
                                                    onClick={(e) => { e.stopPropagation(); handleOpenAddModal(caregiver); }}
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
                                                            >
                                                                <i className="pi pi-arrow-right-arrow-left"></i>
                                                                Switch
                                                            </button>
                                                            <button
                                                                className="resident-action-btn remove-btn"
                                                                title="Remove from caregiver"
                                                                onClick={() => handleRemoveResident(caregiver.id, resident.id)}
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
                <div className="edit-modal-backdrop" onClick={handleCloseAddModal}>
                    <div className="edit-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="edit-modal-header">
                            <h3>Add Resident to {addTargetCaregiver?.firstName} {addTargetCaregiver?.lastName}</h3>
                            <button className="sidebar-close-btn" onClick={handleCloseAddModal}>
                                <i className="pi pi-times"></i>
                            </button>
                        </div>
                        <div className="edit-modal-content">
                            {addError && <div className="error-message" style={{ marginBottom: 16 }}>{addError}</div>}
                            <div className="form-group">
                                <label>Select Resident <span className="required">*</span></label>
                                <select
                                    value={selectedResidentId}
                                    onChange={(e) => setSelectedResidentId(e.target.value)}
                                >
                                    <option value="">-- Choose a resident --</option>
                                    {unassignedResidents.map(r => (
                                        <option key={r.id} value={r.id}>
                                            {r.firstName} {r.lastName}
                                        </option>
                                    ))}
                                </select>
                                {unassignedResidents.length === 0 && (
                                    <p style={{ fontSize: 13, color: '#8b92a7', marginTop: 8 }}>
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
                <div className="edit-modal-backdrop" onClick={handleCloseSwitchModal}>
                    <div className="edit-modal" onClick={(e) => e.stopPropagation()}>
                        <div className="edit-modal-header">
                            <h3>Switch Caregiver for {switchTargetResident?.firstName} {switchTargetResident?.lastName}</h3>
                            <button className="sidebar-close-btn" onClick={handleCloseSwitchModal}>
                                <i className="pi pi-times"></i>
                            </button>
                        </div>
                        <div className="edit-modal-content">
                            {switchError && <div className="error-message" style={{ marginBottom: 16 }}>{switchError}</div>}
                            <div className="form-group">
                                <label>Current Caregiver</label>
                                <p style={{ fontSize: 15, color: '#2d3748', margin: '4px 0 16px 0' }}>
                                    {switchTargetCaregiver?.firstName} {switchTargetCaregiver?.lastName}
                                </p>
                                <label>Move to <span className="required">*</span></label>
                                <select
                                    value={selectedCaregiverId}
                                    onChange={(e) => setSelectedCaregiverId(e.target.value)}
                                >
                                    <option value="">-- Choose a caregiver --</option>
                                    {otherCaregivers.map(c => (
                                        <option key={c.id} value={c.id}>
                                            {c.firstName} {c.lastName}
                                        </option>
                                    ))}
                                </select>
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
