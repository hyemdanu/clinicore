import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./Pages/WelcomePage/LoginPage";
import ResidentDashboard from "./Pages/ResidentPortal/ResidentDashboard";
import ResidentUserProfile from "./Pages/ResidentPortal/ResidentUserProfile";
import ResidentMedication from "./Pages/ResidentPortal/ResidentMedication";
import CaregiverDashboard from "./Pages/CaregiverPortal/CaregiverDashboard";
import AdminDashboard from "./Pages/AdminPortal/AdminDashboard";
import ForgotUserIdPage from "./Pages/WelcomePage/ForgotUserIdPage";
import ForgotPasswordPage from "./Pages/WelcomePage/ForgotPasswordPage";
import CreateAccountPage from "./Pages/WelcomePage/CreateAccountPage";
import RequestSentPage from "./Pages/WelcomePage/RequestSentPage";
import RecoveryEmailSentPage from "./Pages/WelcomePage/RecoveryEmailSentPage.jsx";
import ResidentMedicalProfile from "./Pages/ResidentPortal/ResidentMedicalProfile.jsx";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage/>} />
                <Route path="/resident" element={<ResidentDashboard/>} />
                <Route path="/resident/profile" element={<ResidentUserProfile/>} />
                <Route path="/resident/medications" element={<ResidentMedication/>} />
                <Route path="/resident/medical-profile" element={<ResidentMedicalProfile/>} />
                <Route path="/caregiver" element={<CaregiverDashboard/>} />
                <Route path="/admin" element={<AdminDashboard/>} />
                <Route path="/forgot-userid" element={<ForgotUserIdPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/create-account" element={<CreateAccountPage />} />
                <Route path="/request-sent" element={<RequestSentPage />} />
                <Route path="/recovery-email-sent" element={<RecoveryEmailSentPage />} />
            </Routes>
        </BrowserRouter>
    );
}
