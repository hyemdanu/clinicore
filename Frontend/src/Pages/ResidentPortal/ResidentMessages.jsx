import { useState } from "react";
import Header from "../../Components/Header";
import ResidentSidebar from "../../Components/ResidentSidebar";
import MessagesTab from "../Shared/MessagesTab";
import "./css/ResidentDashboard.css";

export default function ResidentMessages() {
    const [sidebarOpen, setSidebarOpen] = useState(() => window.innerWidth > 768);
    const toggleSidebar = () => setSidebarOpen(s => !s);

    return (
        <div className="admin-dashboard-container messages-active">
            <Header onToggleSidebar={toggleSidebar} title="Messages" />
            <ResidentSidebar isOpen={sidebarOpen} onToggle={toggleSidebar} />

            <main className={`dashboard-content ${sidebarOpen ? "content-with-sidebar" : ""} messages-content`}>
                <MessagesTab />
            </main>
        </div>
    );
}
