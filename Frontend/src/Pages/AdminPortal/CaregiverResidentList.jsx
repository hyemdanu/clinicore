import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '../../services/api';
import './css/residents.css';

/*
 * Caregiver list tab in the admin portal.
 * Shows all caregivers and the residents they are assigned to.
 */
export default function CaregiverResidentList() {
    const navigate = useNavigate();

    const [caregivers, setCaregivers] = useState([]);
    const [filteredCaregivers, setFilteredCaregivers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState('firstName');
    const [expandedIds, setExpandedIds] = useState(new Set());

    useEffect(() => {
        fetchCaregivers();
    }, []);

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

    const fetchCaregivers = async () => {
        setLoading(true);
        setError(null);
        try {
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) { navigate('/'); return; } // send them back to login
            const currentUser = JSON.parse(currentUserStr);
            const data = await get(`/caregivers?currentUserId=${currentUser.id}`);
            setCaregivers(data);
        } catch (err) {
            console.error('Error fetching caregivers:', err);
            setError('Failed to load caregivers. Please try again.');
        } finally {
            setLoading(false);
        }
    };

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
                                    {/* Caregiver row */}
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

                                                    {/* residents under this caregiver, shown when expanded */}
                                    {isExpanded && (
                                        <div className="caregiver-residents-panel">
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
        </div>
    );
}
