import React from "react";
import "./css/caregiver.css";

export default function CaregiverDashboard() {
    return (
        <div className="caregiver-page">
            <div className="box">
                {/* Top blue header */}
                <div className="rectangle">
                    <div className="caregiver-dashboard-title">Dashboard</div>
                </div>
                {/* Medication Tasks section */}
                <section className="medication-section">
                    <div className="medication-header">
                        <div>
                            <h2 className="medication-label">Medication Tasks</h2>
                        </div>
                    </div>
                </section>
            </div>
        </div>
    );
}

