import React from "react";
import "./css/caregiver.css";

export default function CaregiverDashboard() {
    const progressPercent = 65;
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

                        <div className="progress-summary">
                            <span className="progress-title">Progress</span>
                            <span className="progress-value">
                                {progressPercent}%
                            </span>
                        </div>
                    </div>

                    {/* Progress bar */}
                    <div className="medication-progress-container">
                        <div
                            className="medication-progress"
                            style={{ width: `${progressPercent}%` }}
                        />
                    </div>
                </section>
            </div>
        </div>
    );
}

