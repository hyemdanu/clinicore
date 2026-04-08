import { useState, useEffect, useMemo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '../../services/api';
import ResidentDetailModal from '../Shared/ResidentDetailModal';
import '../Shared/css/residents.css';

/*
 * the Residents tab in the admin portal
 */
export default function ResidentsTab() {
    const navigate = useNavigate();

    const [residents, setResidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [searchQuery, setSearchQuery] = useState('');
    const [sortBy, setSortBy] = useState('firstName'); // firstName, lastName

    const [selectedResident, setSelectedResident] = useState(null);
    const [showModal, setShowModal] = useState(false);

    useEffect(() => {
        if (showModal) {
            document.body.classList.add('sidebar-open');
        } else {
            document.body.classList.remove('sidebar-open');
        }
        return () => {
            document.body.classList.remove('sidebar-open');
        };
    }, [showModal]);

    const fetchResidents = useCallback(async () => {
        setLoading(true);
        setError(null);

        try {
            const currentUserStr = localStorage.getItem('currentUser');
            if (!currentUserStr) {
                navigate('/');
                return;
            }

            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // just id/firstName/lastName — full data is fetched on click below
            const residentsData = await get(`/residents/list?currentUserId=${currentUserId}`);
            setResidents(residentsData);
        } catch (error) {
            console.error('Error fetching residents:', error);
            setError('Failed to load residents. Please try again.');
        } finally {
            setLoading(false);
        }
    }, [navigate]);

    useEffect(() => {
        fetchResidents();
    }, [fetchResidents]);

    useEffect(() => {
        const openResidentId = localStorage.getItem('openResidentId');
        if (openResidentId && residents.length > 0) {
            const match = residents.find(r => r.id === parseInt(openResidentId));
            if (match) {
                openResidentModal(match.id);
            }
        }
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [residents]);

    const filteredResidents = useMemo(() => {
        let filtered = [...residents];

        if (searchQuery.trim()) {
            const query = searchQuery.toLowerCase();
            filtered = filtered.filter(resident =>
                `${resident.firstName} ${resident.lastName}`.toLowerCase().includes(query)
            );
        }

        filtered.sort((a, b) => {
            if (sortBy === 'firstName') {
                return a.firstName.localeCompare(b.firstName);
            } else {
                return a.lastName.localeCompare(b.lastName);
            }
        });

        return filtered;
    }, [residents, searchQuery, sortBy]);

    const openResidentModal = async (residentId) => {
        try {
            const currentUser = JSON.parse(localStorage.getItem('currentUser'));
            if (!currentUser) { navigate('/'); return; }
            // modal needs the full hydrated resident (meds, allergies, etc.)
            const fullResident = await get(
                `/residents/full/${residentId}?currentUserId=${currentUser.id}`
            );
            setSelectedResident(fullResident);
            setShowModal(true);
        } catch (err) {
            console.error('Error fetching resident details:', err);
            setError('Failed to load resident details.');
        }
    };

    const handleResidentClick = (resident) => {
        openResidentModal(resident.id);
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedResident(null);
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
                    placeholder="Search...."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                />
            </div>

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {loading ? (
                <div className="residents-loading">
                    <i className="pi pi-spin pi-spinner"></i>
                    <span>Loading residents...</span>
                </div>
            ) : (
                <div className="residents-list">
                    {filteredResidents.length === 0 ? (
                        <div className="no-residents">
                            No residents found
                        </div>
                    ) : (
                        filteredResidents.map((resident) => (
                            <div
                                key={resident.id}
                                className="resident-item"
                                onClick={() => handleResidentClick(resident)}
                            >
                                <div className="resident-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <span className="resident-name">
                                    {resident.firstName} {resident.lastName}
                                </span>
                            </div>
                        ))
                    )}
                </div>
            )}

            {showModal && selectedResident && (
                <>
                    <ResidentDetailModal
                        resident={selectedResident}
                        onClose={handleCloseModal}
                        onResidentUpdated={fetchResidents}
                    />
                </>
            )}
        </div>
    );
}
