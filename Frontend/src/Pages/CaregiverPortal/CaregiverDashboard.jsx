import React, { useState } from "react";
import "./css/caregiver.css";

import DashboardIcon from '../../../assets/icons/dashboardicon.png';
import MessageIcon from '../../../assets/icons/Messageicon.png';
import ResidentIcon from '../../../assets/icons/residenticon.png';
import UserIcon from '../../../assets/icons/usericon.png';
import InventoryIcon from '../../../assets/icons/inventoryicon.png';
import CaregiverIcon from '../../../assets/icons/caregivericon.png';

export default function CaregiverDashboard() {
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const taskColors = {
        'Task 1': '#E26780', // Red
        'Task 2': '#67E2BD', // Green
        'Task 3': '#F5D086', // Yellow
        'Task 4': '#3BCEFF'  // Blue
    };

    // Initialize tasks with individual task completion state
    const [tasks, setTasks] = useState([
        {
            person: "Person 1",
            tasks: [
                { name: "Task 1", completed: false },
                { name: "Task 2", completed: false },
                { name: "Task 3", completed: false }
            ]
        },
        {
            person: "Person 2",
            tasks: [
                { name: "Task 1", completed: false },
                { name: "Task 2", completed: false },
                { name: "Task 3", completed: false }
            ]
        },
        {
            person: "Person 3",
            tasks: [
                { name: "Task 4", completed: false }
            ]
        },
        {
            person: "Person 4",
            tasks: [
                { name: "Task 1", completed: false },
                { name: "Task 2", completed: false }
            ]
        },
        {
            person: "Person 5",
            tasks: [
                { name: "Task 4", completed: false }
            ]
        },
        {
            person: "Person 6",
            tasks: [
                { name: "Task 3", completed: false }
            ]
        }
    ]);

    const sidebarItems = [
        {
            items: [
                { name: 'Dashboard', icon: DashboardIcon, active: true },
                { name: 'Residents', icon: ResidentIcon },
                { name: 'User', icon: UserIcon },
                { name: 'Messages', icon: MessageIcon },
                { name: 'Documents', icon: DashboardIcon },
                { name: 'Inventory', icon: InventoryIcon }
            ]
        }
    ];

    // Calculate progress based on completed individual tasks
    const calculateProgress = () => {
        let totalTasks = 0;
        let completedTasks = 0;

        tasks.forEach(person => {
            person.tasks.forEach(task => {
                totalTasks++;
                if (task.completed) {
                    completedTasks++;
                }
            });
        });

        return totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
    };

    const progressPercent = calculateProgress();

    // Toggle individual task completion
    const toggleTaskCompletion = (personIndex, taskIndex) => {
        const updatedTasks = tasks.map((person, pIndex) => {
            if (pIndex === personIndex) {
                const updatedPersonTasks = person.tasks.map((task, tIndex) => {
                    if (tIndex === taskIndex) {
                        return { ...task, completed: !task.completed };
                    }
                    return task;
                });
                return { ...person, tasks: updatedPersonTasks };
            }
            return person;
        });
        setTasks(updatedTasks);
    };

    // Get task color based on task name
    const getTaskColor = (taskName) => {
        return taskColors[taskName] || '#369df7'; // Default blue if task not found
    };

    // Current full date
    const getFullDate = () => {
        return new Date().toLocaleDateString('en-US', {
            month: 'long',
            day: 'numeric',
            year: 'numeric'
        });
    };

    return (
        <div className={`caregiver-page ${sidebarOpen ? 'sidebar-open' : ''}`}>
            {/* Sidebar */}
            <div className="sidebar">
                <div className="sidebar-content">
                    {sidebarItems.map((section, index) => (
                        <div key={index} className="sidebar-section">
                            <ul className="sidebar-list">
                                {section.items.map((item, itemIndex) => (
                                    <li key={itemIndex} className={`sidebar-item ${item.active ? 'active' : ''}`}>
                                        <div className="sidebar-item-content">
                                            <img
                                                src={item.icon}
                                                alt={item.name}
                                                className="sidebar-icon"
                                            />
                                            <span className="sidebar-item-text">{item.name}</span>
                                        </div>
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ))}
                </div>
            </div>

            {/* Dashboard/Main Content */}
            <div className="main-content">
                <div className="box">
                    {/* Top blue header */}
                    <div className="rectangle">
                        <button
                            className="sidebar-toggle"
                            onClick={() => setSidebarOpen(!sidebarOpen)}
                        >
                            ☰
                        </button>
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

                        {/* Current full date */}
                        <div className="progress-date">{getFullDate()}</div>

                        {/* Task list card */}
                        <div className="medication-box">
                            {tasks.map((person, personIndex) => (
                                <React.Fragment key={personIndex}>
                                    <div className="row">
                                        <div className="person-task-group">
                                            <div className="person-name">
                                                {person.person}
                                            </div>
                                        </div>
                                        <div className="tasks-container">
                                            {person.tasks.map((task, taskIndex) => (
                                                <div
                                                    key={taskIndex}
                                                    className={`task-rectangle ${task.completed ? 'completed' : ''}`}
                                                    style={{
                                                        backgroundColor: getTaskColor(task.name),
                                                        borderColor: getTaskColor(task.name)
                                                    }}
                                                >
                                                    <input
                                                        type="checkbox"
                                                        className="task-checkbox"
                                                        checked={task.completed}
                                                        onChange={() => toggleTaskCompletion(personIndex, taskIndex)}
                                                    />
                                                    <span className="task-text">{task.name}</span>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                    {personIndex < tasks.length - 1 && <div className="line" />}
                                </React.Fragment>
                            ))}
                        </div>
                    </section>
                </div>
            </div>
        </div>
    );
}