import React from "react";
import "./css/ResidentUserProfile.css";

export default function ResidentUserProfile() {
    // Sample user data
    const user = {
        name: "John Smith",
        dob: "01/01/1980",
        contactInfo: "123-456-7890",
        admissionDate: "01/01/2020",
        gender: "Male",
        emergencyContact: "Jane Smith - 123-456-7890",
        facility: "Sacramento",
        roomNumber: "101A",
        picture: "picture.jpg" // Replace with actual image path
    };

    return (
        <div className="resident-user-profile-page">
            <div className="box">
                <div className="profile-left">
                    <img src={user.picture} alt="Profile" className="profile-picture" />
                </div>
                <div className="profile-middle">
                    <div className="profile-info">
                        <p><strong>Name:</strong></p>
                        <p>{user.name}</p>
                    </div>
                    <div className="profile-info">
                        <p><strong>Date of Birth:</strong></p>
                        <p>{user.dob}</p>
                    </div>
                    <div className="profile-info">
                        <p><strong>Contact Info:</strong></p>
                        <p>{user.contactInfo}</p>
                    </div>
                    <div className="profile-info">
                        <p><strong>Admission Date:</strong></p>
                        <p>{user.admissionDate}</p>
                    </div>
                </div>
                <div className="profile-right">
                    <div className="profile-info">
                        <p><strong>Gender:</strong></p>
                        <p>{user.gender}</p>
                    </div>
                    <div className="profile-info">
                        <p><strong>Emergency Contact:</strong></p>
                        <p>{user.emergencyContact}</p>
                    </div>
                    <div className="profile-info">
                        <p><strong>Facility:</strong></p>
                        <p>{user.facility}</p>
                    </div>
                    <div className="profile-info">
                        <p><strong>Room Number:</strong></p>
                        <p>{user.roomNumber}</p>
                    </div>
                </div>
            </div>
        </div>
    );
}
