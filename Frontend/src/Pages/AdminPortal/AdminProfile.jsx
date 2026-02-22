import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { get, put } from "../../services/api";
import { Button } from 'primereact/button';
import { InputText } from 'primereact/inputtext';
import { Calendar } from 'primereact/calendar';
import { Dropdown } from 'primereact/dropdown';
import { Toast } from 'primereact/toast';
import { useRef } from 'react';
import "primereact/resources/themes/lara-light-blue/theme.css";
import "primeicons/primeicons.css";
import "./css/AdminProfile.css";

export default function AdminProfile() {
    const navigate = useNavigate();
    const toastRef = useRef(null);

    const [loading, setLoading] = useState(true);
    const [admin, setAdmin] = useState(null);
    const [editMode, setEditMode] = useState(false);

    // fields we can edit
    const [editedData, setEditedData] = useState({
        firstName: '',
        lastName: '',
        email: '',
        contactNumber: '',
        birthday: null,
        gender: ''
    });

    const genderOptions = [
        { label: 'Male', value: 'MALE' },
        { label: 'Female', value: 'FEMALE' },
        { label: 'Other', value: 'OTHER' }
    ];

    useEffect(() => {
        // grab user info when page loads
        (async () => {
            setLoading(true);
            try {
                const currentUserStr = localStorage.getItem("currentUser");
                if (!currentUserStr) {
                    navigate("/");
                    return;
                }

                const currentUser = JSON.parse(currentUserStr);
                const currentUserId = currentUser.id;

                // make sure they have the right role
                if (currentUser.role && currentUser.role !== "ADMIN") {
                    navigate("/not-authorized");
                    return;
                }

                // fetch profile from backend
                const profile = await get(`/user/${currentUserId}/profile?currentUserId=${currentUserId}`);
                console.log("Admin profile loaded:", profile);
                setAdmin(profile);

                // populate the form
                setEditedData({
                    firstName: profile.firstName || '',
                    lastName: profile.lastName || '',
                    email: profile.email || '',
                    contactNumber: profile.contactNumber || profile.phone || '',
                    birthday: profile.birthday ? new Date(profile.birthday) : null,
                    gender: profile.gender || ''
                });

            } catch (e) {
                console.error("Failed to load admin profile:", e);
                setAdmin(null);
            } finally {
                setLoading(false);
            }
        })();
    }, [navigate]);

    const handleEdit = () => {
        setEditMode(true);
    };

    const handleCancel = () => {
        // put it back the way it was
        setEditedData({
            firstName: admin.firstName || '',
            lastName: admin.lastName || '',
            email: admin.email || '',
            contactNumber: admin.contactNumber || admin.phone || '',
            birthday: admin.birthday ? new Date(admin.birthday) : null,
            gender: admin.gender || ''
        });
        setEditMode(false);
    };

    const handleSave = async () => {
        try {
            const currentUserStr = localStorage.getItem("currentUser");
            const currentUser = JSON.parse(currentUserStr);
            const currentUserId = currentUser.id;

            // package up the data
            const updateData = {
                firstName: editedData.firstName,
                lastName: editedData.lastName,
                email: editedData.email,
                contactNumber: editedData.contactNumber,
                birthday: editedData.birthday ? editedData.birthday.toISOString().split('T')[0] : null,
                gender: editedData.gender
            };

            // send it off
            await put(`/user/${currentUserId}/profile?currentUserId=${currentUserId}`, updateData);

            // grab the fresh data
            const updatedProfile = await get(`/user/${currentUserId}/profile?currentUserId=${currentUserId}`);
            setAdmin(updatedProfile);

            setEditMode(false);

            toastRef.current?.show({
                severity: 'success',
                summary: 'Success',
                detail: 'Profile updated successfully!',
                life: 3000
            });

        } catch (error) {
            console.error("Failed to update profile:", error);
            toastRef.current?.show({
                severity: 'error',
                summary: 'Error',
                detail: 'Failed to update profile. Please try again.',
                life: 3000
            });
        }
    };

    // get initials for the avatar
    const getInitials = () => {
        if (!admin) return "?";
        const first = admin.firstName?.charAt(0) || "";
        const last = admin.lastName?.charAt(0) || "";
        return (first + last).toUpperCase() || "?";
    };

    if (loading) {
        return (
            <div className="profile-loading">
                <i className="pi pi-spin pi-spinner" style={{ fontSize: '2rem' }}></i>
                <p>Loading your profile...</p>
            </div>
        );
    }

    if (!admin) {
        return (
            <div className="error-message">
                Couldn't load your profile. Please try again later.
            </div>
        );
    }

    // put together display values
    const fullName = [admin.firstName, admin.lastName].filter(Boolean).join(" ") || "—";
    const email = admin.email || "—";
    const username = admin.username || "—";
    const phone = admin.contactNumber || admin.phone || "—";
    const role = admin.role || "ADMIN";

    // extra details just in case
    const dob = admin.birthday ? new Date(admin.birthday).toLocaleDateString() : "—";
    const gender = admin.gender || "—";

    return (
        <div className="user-profile-page">
            <Toast ref={toastRef} />

            <div className="alert-section">
                <h2 className="dashboard-title">Your Profile</h2>
                <p className="profile-subtitle">Manage your account information</p>
            </div>

            <div className="profile-container">
                {/* top section with avatar and basic info */}
                <div className="profile-header">
                    <div className="profile-avatar-section">
                        <div className="profile-avatar-initials">
                            {getInitials()}
                        </div>
                        <div className="profile-avatar-info">
                            <h2>{fullName}</h2>
                            <p className="profile-role-badge">{role}</p>
                        </div>
                    </div>
                </div>

                {/* main info cards */}
                <div className="profile-info-grid">
                    {/* personal info card */}
                    <div className="profile-info-card">
                        <div className="card-title-row">
                            <h3 className="card-title">
                                <i className="pi pi-user"></i> Personal Information
                            </h3>
                            {!editMode && (
                                <button className="edit-btn" onClick={handleEdit}>
                                    <i className="pi pi-pencil"></i>
                                </button>
                            )}
                        </div>

                        <div className="info-row">
                            <span className="info-label">First Name</span>
                            {editMode ? (
                                <InputText
                                    value={editedData.firstName}
                                    onChange={(e) => setEditedData({...editedData, firstName: e.target.value})}
                                    className="edit-input"
                                />
                            ) : (
                                <span className="info-value">{admin.firstName || "—"}</span>
                            )}
                        </div>

                        <div className="info-row">
                            <span className="info-label">Last Name</span>
                            {editMode ? (
                                <InputText
                                    value={editedData.lastName}
                                    onChange={(e) => setEditedData({...editedData, lastName: e.target.value})}
                                    className="edit-input"
                                />
                            ) : (
                                <span className="info-value">{admin.lastName || "—"}</span>
                            )}
                        </div>

                        <div className="info-row">
                            <span className="info-label">Username</span>
                            <span className="info-value">{username}</span>
                        </div>

                        <div className="info-row">
                            <span className="info-label">Email</span>
                            {editMode ? (
                                <InputText
                                    value={editedData.email}
                                    onChange={(e) => setEditedData({...editedData, email: e.target.value})}
                                    className="edit-input"
                                    type="email"
                                />
                            ) : (
                                <span className="info-value">{email}</span>
                            )}
                        </div>

                        <div className="info-row">
                            <span className="info-label">Phone Number</span>
                            {editMode ? (
                                <InputText
                                    value={editedData.contactNumber}
                                    onChange={(e) => setEditedData({...editedData, contactNumber: e.target.value})}
                                    className="edit-input"
                                />
                            ) : (
                                <span className="info-value">{phone}</span>
                            )}
                        </div>
                    </div>

                    {/* additional info card */}
                    <div className="profile-info-card">
                        <h3 className="card-title">
                            <i className="pi pi-info-circle"></i> Additional Details
                        </h3>

                        <div className="info-row">
                            <span className="info-label">Date of Birth</span>
                            {editMode ? (
                                <Calendar
                                    value={editedData.birthday}
                                    onChange={(e) => setEditedData({...editedData, birthday: e.value})}
                                    dateFormat="mm/dd/yy"
                                    showIcon
                                    className="edit-input"
                                />
                            ) : (
                                <span className="info-value">{dob}</span>
                            )}
                        </div>

                        <div className="info-row">
                            <span className="info-label">Gender</span>
                            {editMode ? (
                                <Dropdown
                                    value={editedData.gender}
                                    options={genderOptions}
                                    onChange={(e) => setEditedData({...editedData, gender: e.value})}
                                    placeholder="Select Gender"
                                    className="edit-input"
                                />
                            ) : (
                                <span className="info-value">{gender}</span>
                            )}
                        </div>

                        <div className="info-row">
                            <span className="info-label">Account Type</span>
                            <span className="info-value">Administrator</span>
                        </div>

                    </div>
                </div>

                {/* edit mode buttons */}
                {editMode && (
                    <div className="edit-actions">
                        <Button
                            label="Cancel"
                            className="p-button-outlined"
                            onClick={handleCancel}
                        />
                        <Button
                            label="Save"
                            onClick={handleSave}
                        />
                    </div>
                )}
            </div>
        </div>
    );
}
