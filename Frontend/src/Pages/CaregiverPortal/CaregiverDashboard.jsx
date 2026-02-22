import React from "react";
import "./css/caregiver.css";
import { useState, useEffect, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { DataTable } from 'primereact/datatable';
import { Column } from 'primereact/column';
import { Dropdown } from 'primereact/dropdown';
import { get } from '../../services/api';
import Header from '../../Components/Header';
import CaregiverSidebar from "../../Components/CaregiverSidebar.jsx";
import 'primereact/resources/themes/lara-light-blue/theme.css';
import 'primeicons/primeicons.css';

export default function CaregiverDashboard() {
    const navigate = useNavigate();

    // State for sidebar visibility and active tab
    const [sidebarOpen, setSidebarOpen] = useState(true);
    const [activeTab, setActiveTab] = useState("dashboard");

    //TODO: fetch patient info and display in table
    //TODO: fetch patient medication tasks and display in table

    // store patient medication tasks, and patient info for display in table
    const [patientTasks, setPatientTasks] = useState([]);
    const [patientInfo, setPatientInfo] = useState({});

    // loading and error states for data fetching
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // medication progression card to track how many medications are pending, taken, or missed
    const MedicationProgressCard = ({percent}) => {
        return (
            <div className="progress-card">
                <h3>Medication Tasks</h3>

                <div className="progress-header">
                    <span>Progress</span>
                    <span>{percent}%</span>
                </div>

                <div className="progress-bar">
                    <div
                        className="progress-fill"
                        style={{ width: `${percent}%` }}
                    />
                </div>

                <div className="date">October 1, 2025</div>
            </div>
        );
    };

    // resident task cards to show their medication progress
    const TaskBadge = ({ label, color }) => {
        return (
            <div className="task-badge" style={{ backgroundColor: color }}>
                {label}
            </div>
        );
    };

    // resident rows in the table
    const ResidentRow = ({ resident }) => {
        return (
            <div className="resident-row">
                <div className="resident-info">
                    <div className="avatar" />
                    <span>{resident.name}</span>
                </div>

                <div className="task-container">
                    {resident.tasks.map((task, index) => (
                        <TaskBadge
                            key={index}
                            label={task.label}
                            color={task.color}
                        />
                    ))}
                </div>
            </div>
        );
    };

}

