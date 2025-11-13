import React from "react";
import "./css/caregiver.css";

export default function CaregiverDashboard() {
    const tasks = [
        { person: "Person 1", task: "Task 1" },
        { person: "Person 2", task: "Task 1" },
        { person: "Person 3", task: "Task 1" },
        { person: "Person 4", task: "Task 1" },
        { person: "Person 5", task: "Task 1" },
        { person: "Person 6", task: "Task 1" },
    ];
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
                            <span className="progress-value">{progressPercent}%</span>
                        </div>
                    </div>

                    {/* Progress bar */}
                    <div className="medication-progress-container">
                        <div
                            className="medication-progress"
                            style={{ width: `${progressPercent}%` }}
                        />
                    </div>
                    {/* Date */}
                    <div className="progress-date">12/03/2025</div>
                    {/* Task list card */}
                    <div className="medication-box">
                        {tasks.map((item, index) => (
                            <React.Fragment key={index}>
                                <div className="row">
                                    <div className="person-name">{item.person}</div>
                                    <div className="task-rectangle">{item.task}</div>
                                </div>
                                {index < tasks.length - 1 && <div className="line" />}
                            </React.Fragment>
                        ))}
                    </div>
                </section>
            </div>
        </div>
    );
}

