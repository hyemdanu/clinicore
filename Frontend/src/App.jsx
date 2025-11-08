import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./Pages/WelcomePage/LoginPage";
import ResidentDashboard from "./Pages/ResidentPortal/ResidentDashboard";
import CaregiverDashboard from "./Pages/CaregiverPortal/CaregiverDashboard";
import AdminDashboard from "./Pages/AdminPortal/AdminDashboard";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage/>} />
                <Route path="/resident" element={<ResidentDashboard/>} />
                <Route path="/caregiver" element={<CaregiverDashboard/>} />
                <Route path="/admin" element={<AdminDashboard/>} />
            </Routes>
        </BrowserRouter>
    );
}
