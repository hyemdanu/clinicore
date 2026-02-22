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


}

