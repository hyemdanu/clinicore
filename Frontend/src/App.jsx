import { BrowserRouter, Routes, Route } from "react-router-dom";
import LoginPage from "./Pages/WelcomePage/LoginPage";
import ResidentDashboard from "./Pages/ResidentPortal/ResidentDashboard";
import ResidentMedication from "./Pages/ResidentPortal/ResidentMedication";
import CaregiverDashboard from "./Pages/CaregiverPortal/CaregiverDashboard";
import AdminDashboard from "./Pages/AdminPortal/AdminDashboard";
import AccountRequests from "./Pages/AdminPortal/AccountRequests";
import ForgotUserIdPage from "./Pages/WelcomePage/ForgotUserIdPage";
import ForgotPasswordPage from "./Pages/WelcomePage/ForgotPasswordPage";
import CreateAccountPage from "./Pages/WelcomePage/CreateAccountPage";
import RequestSentPage from "./Pages/WelcomePage/RequestSentPage";
import RecoveryEmailSentPage from "./Pages/WelcomePage/RecoveryEmailSentPage.jsx"
import CompleteAccountCreation from "./Pages/WelcomePage/CompleteAccountCreation.jsx";

export default function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage/>} />
                <Route path="/resident" element={<ResidentDashboard/>} />
                <Route path="/resident/medications" element={<ResidentMedication/>} />
                <Route path="/resident/medical-profile" element={<ResidentMedicalProfile/>} />
                <Route path="/caregiver" element={<CaregiverDashboard/>} />
                <Route path="/admin" element={<AdminDashboard/>} />
                <Route path="/admin/account-requests" element={<AccountRequests/>} />
                <Route path="/forgot-userid" element={<ForgotUserIdPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/create-account" element={<CreateAccountPage />} />
                <Route path="/request-sent" element={<RequestSentPage />} />
                <Route path="/recovery-email-sent" element={<RecoveryEmailSentPage />} />
                <Route path="/activate-account" element={<CompleteAccountCreation />} />
            </Routes>
        </BrowserRouter>
    );
}
