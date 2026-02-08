import React, { useState, useEffect } from 'react';
import './css/ResidentMedication.css';

const ResidentMedication = ({ currentUserId }) => {
    const [medications, setMedications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [sortField, setSortField] = useState('medication_name');
    const [sortOrder, setSortOrder] = useState('asc');

    // Sorting options
    const sortOptions = [
        { label: 'Name (A-Z)', value: 'medication_name', order: 'asc' },
        { label: 'Name (Z-A)', value: 'medication_name', order: 'desc' },
        { label: 'Scheduled Time', value: 'scheduled_time', order: 'asc' },
        { label: 'Status', value: 'intake_status', order: 'asc' }
    ];

    // Fetch medications from database
    useEffect(() => {
        if (currentUserId) {
            fetchMedications();
        }
    }, [currentUserId]);

    const fetchMedications = async () => {
        setLoading(true);
        setError(null);

        try {
            const response = await fetch(`/api/residents/medications?currentUserId=${currentUserId}`);
            if (!response.ok) {
                throw new Error(`Failed to fetch medications: ${response.status}`);
            }
            const data = await response.json();

            setMedications(data);
        } catch (err) {
            console.error('Error fetching medications:', err);
            setError(err.message);
            loadSampleData();
        } finally {
            setLoading(false);
        }
    };

    // Sample data
    const loadSampleData = () => {
        const sampleData = [
            {
                id: 1,
                medication_name: 'Medication 1',
                frequency: 'M, W, F',
                scheduled_time: 'AM',
                intake_status: 'ADMINISTERED',
                notes: 'Taken and went well'
            },
            {
                id: 2,
                medication_name: 'Medication 2',
                frequency: 'W, F',
                scheduled_time: 'PM',
                intake_status: 'WITHHELD',
                notes: 'Some kind of description here...'
            },
            {
                id: 3,
                medication_name: 'Medication 2',
                frequency: 'Th, S',
                scheduled_time: 'AM',
                intake_status: 'MISSED',
                notes: 'Some kind of description here...'
            },
            {
                id: 4,
                medication_name: 'Medication 1',
                frequency: 'Everyday',
                scheduled_time: 'AM',
                intake_status: 'ADMINISTERED',
                notes: 'Taken and went well'
            },
            {
                id: 5,
                medication_name: 'Medication 1',
                frequency: 'M, W, F, S',
                scheduled_time: 'PM',
                intake_status: 'ADMINISTERED',
                notes: 'Taken and went well'
            },
            {
                id: 6,
                medication_name: 'Medication 1',
                frequency: 'Sa, M, F',
                scheduled_time: 'AM',
                intake_status: 'ADMINISTERED',
                notes: 'Taken and went well'
            }
        ];
        setMedications(sampleData);
    };

    const handleSortChange = (e) => {
        const value = e.target.value;
        const selectedOption = sortOptions.find(option =>
            `${option.value}_${option.order}` === value
        );

        if (selectedOption) {
            setSortField(selectedOption.value);
            setSortOrder(selectedOption.order);
        }
    };

    const getStatusBadge = (status) => {
        const statusConfig = {
            ADMINISTERED: {
                label: 'Administered',
                class: 'status-badge administered',
                color: '#198754'
            },
            WITHHELD: {
                label: 'Withheld',
                class: 'status-badge withheld',
                color: '#ffc107'
            },
            MISSED: {
                label: 'Missed',
                class: 'status-badge missed',
                color: '#dc3545'
            }
        };

        const config = statusConfig[status] || {
            label: 'Pending',
            class: 'status-badge pending',
            color: '#6c757d'
        };

        return (
            <span className={config.class}>
                {config.label}
            </span>
        );
    };

    // Combine frequency and scheduled_time for display
    const getScheduleDisplay = (frequency, scheduledTime) => {
        return `${frequency} • ${scheduledTime}`;
    };

    // Sort medications
    const sortedMedications = [...medications].sort((a, b) => {
        let aValue = a[sortField];
        let bValue = b[sortField];

        if (aValue === null || aValue === undefined) aValue = '';
        if (bValue === null || bValue === undefined) bValue = '';

        if (typeof aValue === 'string' && typeof bValue === 'string') {
            const comparison = aValue.localeCompare(bValue);
            return sortOrder === 'asc' ? comparison : -comparison;
        }

        return sortOrder === 'asc' ? aValue - bValue : bValue - aValue;
    });

    const handleRefresh = () => {
        fetchMedications();
    };

    const handleAddMedication = () => {
        // Add medication functionality
        console.log('Add medication clicked');
        // You would typically navigate to an add medication form here
        // or show a modal
    };

    return (
        <div className="resident-medication-component">
            <div className="alert-section">
                <h2 className="dashboard-title">
                    Medication
                </h2>

                {error && (
                    <div className="error-message">
                        <svg className="error-icon" width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                            <path d="M8 16C12.4183 16 16 12.4183 16 8C16 3.58172 12.4183 0 8 0C3.58172 0 0 3.58172 0 8C0 12.4183 3.58172 16 8 16Z" fill="#DC3545"/>
                            <path d="M8 4C8.55228 4 9 4.44772 9 5V9C9 9.55228 8.55228 10 8 10C7.44772 10 7 9.55228 7 9V5C7 4.44772 7.44772 4 8 4Z" fill="white"/>
                            <path d="M8 12C8.55228 12 9 11.5523 9 11C9 10.4477 8.55228 10 8 10C7.44772 10 7 10.4477 7 11C7 11.5523 7.44772 12 8 12Z" fill="white"/>
                        </svg>
                        <span>Error: {error}. Showing sample data.</span>
                    </div>
                )}
            </div>

            <div className="inventory-section">
                <div className="inventory-header">
                    <h3>Current Medications</h3>
                    <div className="sort-dropdown-wrapper">
                        <label htmlFor="sort-dropdown">Sort by:</label>
                        <select
                            id="sort-dropdown"
                            className="sort-dropdown"
                            onChange={handleSortChange}
                            value={`${sortField}_${sortOrder}`}
                        >
                            {sortOptions.map((option, index) => (
                                <option key={index} value={`${option.value}_${option.order}`}>
                                    {option.label}
                                </option>
                            ))}
                        </select>

                        <button
                            className="refresh-button"
                            onClick={handleRefresh}
                            disabled={loading}
                        >
                            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                                <path d="M13.65 2.35C12.2 0.9 10.21 0 8 0C3.58 0 0 3.58 0 8C0 12.42 3.58 16 8 16C11.73 16 14.84 13.45 15.73 10H13.65C12.83 12.33 10.61 14 8 14C4.69 14 2 11.31 2 8C2 4.69 4.69 2 8 2C9.66 2 11.14 2.69 12.22 3.78L9 7H16V0L13.65 2.35Z" fill="#369DF7"/>
                            </svg>
                            Refresh
                        </button>
                    </div>
                </div>

                <div className="inventory-table">
                    {loading ? (
                        <div className="custom-loading">
                            <div className="custom-spinner">
                                <svg width="50" height="50" viewBox="0 0 50 50" xmlns="http://www.w3.org/2000/svg">
                                    <circle cx="25" cy="25" r="20" fill="none" stroke="#369DF7" strokeWidth="4" strokeLinecap="round">
                                        <animateTransform
                                            attributeName="transform"
                                            type="rotate"
                                            from="0 25 25"
                                            to="360 25 25"
                                            dur="1s"
                                            repeatCount="indefinite"
                                        />
                                        <animate
                                            attributeName="stroke-dasharray"
                                            values="1,200;90,200;1,200"
                                            dur="1.5s"
                                            repeatCount="indefinite"
                                        />
                                        <animate
                                            attributeName="stroke-dashoffset"
                                            values="0;-35;-124"
                                            dur="1.5s"
                                            repeatCount="indefinite"
                                        />
                                    </circle>
                                </svg>
                            </div>
                            <span>Loading medications...</span>
                        </div>
                    ) : (
                        <div className="table-container">
                            <table className="medications-table">
                                <thead>
                                <tr>
                                    <th>Medication</th>
                                    <th>Scheduled Day and Time</th>
                                    <th>Log Intake Status</th>
                                    <th>Notes</th>
                                </tr>
                                </thead>
                                <tbody>
                                {sortedMedications.length === 0 ? (
                                    <tr>
                                        <td colSpan="4" className="empty-message">
                                            No medications found
                                        </td>
                                    </tr>
                                ) : (
                                    sortedMedications.map((medication) => (
                                        <tr key={medication.id}>
                                            <td>
                                                <div className="medication-name-cell">
                                                    <div className="medication-icon">
                                                        <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                            <path d="M21 5H3C1.9 5 1 5.9 1 7V17C1 18.1 1.9 19 3 19H21C22.1 19 23 18.1 23 17V7C23 5.9 22.1 5 21 5ZM21 17H3V7H21V17Z" fill="#369DF7"/>
                                                            <path d="M6 10H8V14H6V10Z" fill="#369DF7"/>
                                                            <path d="M10 10H18V12H10V10Z" fill="#369DF7"/>
                                                            <path d="M10 14H16V16H10V14Z" fill="#369DF7"/>
                                                        </svg>
                                                    </div>
                                                    <span className="medication-name">{medication.medication_name}</span>
                                                </div>
                                            </td>
                                            <td>
                                                <span className="schedule">
                                                    {getScheduleDisplay(medication.frequency, medication.scheduled_time)}
                                                </span>
                                            </td>
                                            <td>
                                                {getStatusBadge(medication.intake_status)}
                                            </td>
                                            <td>
                                                <div className="notes-cell">
                                                    {medication.notes || 'No notes'}
                                                </div>
                                            </td>
                                        </tr>
                                    ))
                                )}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>

                // Add Medication Button
                <div className="add-medication-section">
                    <button
                        className="add-medication-button"
                        onClick={handleAddMedication}
                    >
                        <span className="plus-icon">+</span>
                        Add Medication
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ResidentMedication;