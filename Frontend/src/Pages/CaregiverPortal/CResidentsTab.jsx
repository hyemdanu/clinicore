import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { get } from '../../services/api';
import ResidentDetailModal from '../Shared/ResidentDetailModal';
import '../Shared/css/residents.css';

/*
 * Residents tab for the caregiver portal
 * - "Assigned to You": residents assigned to the logged-in caregiver
 * - "Residents List": all other residents with their assigned caregiver(s) shown
 */
export default function CaregiverResidentsTab() {
    const navigate = useNavigate();

    const [assignedResidents, setAssignedResidents] = useState([]);
    const [otherResidents, setOtherResidents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [selectedResident, setSelectedResident] = useState(null);
    const [showModal, setShowModal] = useState(false);
    const [loadingResident, setLoadingResident] = useState(false);

    useEffect(() => {
        if (showModal) {
            document.body.classList.add('sidebar-open');
        } else {
            document.body.classList.remove('sidebar-open');
        }
        return () => document.body.classList.remove('sidebar-open');
    }, [showModal]);

    useEffect(() => {
        fetchResidents();
    }, []);

    const fetchResidents = async () => {
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

            // use the caregiver-specific endpoint that returns pre-split data
            const data = await get(`/caregivers/my-residents?currentUserId=${currentUserId}`);

            setAssignedResidents(data.assigned || []);
            setOtherResidents(data.others || []);
        } catch (err) {
            console.error('Error fetching residents:', err);
            setError('Failed to load residents. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleResidentClick = async (resident) => {
        setLoadingResident(true);
        try {
            const currentUserStr = localStorage.getItem('currentUser');
            const currentUserId = JSON.parse(currentUserStr).id;
            const fullResident = await get(`/residents/full/${resident.id}?currentUserId=${currentUserId}`);
            setSelectedResident(fullResident);
            setShowModal(true);
        } catch (err) {
            console.error('Error fetching resident details:', err);
        } finally {
            setLoadingResident(false);
        }
    };

    const handleCloseModal = () => {
        setShowModal(false);
        setSelectedResident(null);
    };

    if (loading) {
        return (
            <div className="residents-loading">
                <i className="pi pi-spin pi-spinner"></i>
                <span>Loading residents...</span>
            </div>
        );
    }

    if (error) {
        return <div className="error-message">{error}</div>;
    }

    return (
        <div className="residents-tab">
            {loadingResident && (
                <div className="residents-loading">
                    <i className="pi pi-spin pi-spinner"></i>
                    <span>Loading resident details...</span>
                </div>
            )}

            {/* Top — assigned to current caregiver */}
            <div className="residents-section-container">
                <h3 className="residents-section-label">Assigned to You</h3>
                <div className="residents-list">
                    {assignedResidents.length === 0 ? (
                        <div className="no-residents">No residents assigned to you.</div>
                    ) : (
                        assignedResidents.map((resident) => (
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
            </div>

            {/* Bottom — all other residents */}
            <div className="residents-section-container">
                <h3 className="residents-section-label">Residents List</h3>
                <div className="residents-list">
                    {otherResidents.length === 0 ? (
                        <div className="no-residents">No other residents.</div>
                    ) : (
                        otherResidents.map((resident) => (
                            <div
                                key={resident.id}
                                className="resident-item"
                                onClick={() => handleResidentClick(resident)}
                            >
                                <div className="resident-avatar">
                                    <i className="pi pi-user"></i>
                                </div>
                                <div className="resident-item-info">
                                    <span className="resident-name">
                                        {resident.firstName} {resident.lastName}
                                    </span>
                                    {resident.assignedCaregivers?.length > 0 ? (
                                        <span className="resident-assigned-badge">
                                            <i className="pi pi-user-edit"></i>
                                            {resident.assignedCaregivers.join(', ')}
                                        </span>
                                    ) : (
                                        <span className="resident-assigned-badge unassigned">
                                            Unassigned
                                        </span>
                                    )}
                                </div>
                            </div>
                        ))
                    )}
                </div>
            </div>

            {showModal && selectedResident && (
                <ResidentDetailModal
                    resident={selectedResident}
                    onClose={handleCloseModal}
                    onResidentUpdated={fetchResidents}
                />
            )}
        </div>
    );
}