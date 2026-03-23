import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./Pages/WelcomePage/LoginPage";
import ResidentDashboard from "./Pages/ResidentPortal/ResidentDashboard";
import ResidentDocuments from "./Pages/ResidentPortal/ResidentDocuments";
import ResidentUserProfile from "./Pages/ResidentPortal/ResidentUserProfile";
import ResidentMedication from "./Pages/ResidentPortal/ResidentMedication";
import CaregiverDashboard from "./Pages/CaregiverPortal/CaregiverDashboard";
import CaregiverDocument from "./Pages/CaregiverPortal/CaregiverDocument";
import AdminDashboard from "./Pages/AdminPortal/AdminDashboard";
import AccountRequests from "./Pages/AdminPortal/AccountRequests";
import AdminDocuments from "./Pages/AdminPortal/AdminDocument";
import ForgotUserIdPage from "./Pages/WelcomePage/ForgotUserIdPage";
import ForgotPasswordPage from "./Pages/WelcomePage/ForgotPasswordPage";
import CreateAccountPage from "./Pages/WelcomePage/CreateAccountPage";
import RequestSentPage from "./Pages/WelcomePage/RequestSentPage";
import RecoveryEmailSentPage from "./Pages/WelcomePage/RecoveryEmailSentPage.jsx";
import CompleteAccountCreation from "./Pages/WelcomePage/CompleteAccountCreation.jsx";
import ResidentMedicalProfile from "./Pages/ResidentPortal/ResidentMedicalProfile.jsx";
import ResidentMessages from "./Pages/ResidentPortal/ResidentMessages.jsx";
import ResetPasswordPage from "./Pages/WelcomePage/ResetPasswordPage.jsx";
import ErrorBoundary from "./Components/ErrorBoundary.jsx";

// Blocks access if user is not logged in or has the wrong role
function ProtectedRoute({ role, children }) {
    try {
        const currentUser = JSON.parse(localStorage.getItem("currentUser"));
        if (currentUser && currentUser.role === role) {
            return children;
        }
    } catch {
        // invalid JSON in localStorage — treat as not logged in
    }
    return <Navigate to="/" replace />;
}

export default function App() {
    return (
        <ErrorBoundary>
        <BrowserRouter>
            <Routes>
                <Route path="/" element={<LoginPage/>} />

                {/* Resident routes — require RESIDENT role */}
                <Route path="/resident" element={<ProtectedRoute role="RESIDENT"><ResidentDashboard/></ProtectedRoute>} />
                <Route path="/resident/documents" element={<ProtectedRoute role="RESIDENT"><ResidentDocuments/></ProtectedRoute>} />
                <Route path="/resident/profile" element={<ProtectedRoute role="RESIDENT"><ResidentUserProfile/></ProtectedRoute>} />
                <Route path="/resident/medications" element={<ProtectedRoute role="RESIDENT"><ResidentMedication/></ProtectedRoute>} />
                <Route path="/resident/medical-profile" element={<ProtectedRoute role="RESIDENT"><ResidentMedicalProfile/></ProtectedRoute>} />
                <Route path="/resident/messages" element={<ProtectedRoute role="RESIDENT"><ResidentMessages/></ProtectedRoute>} />

                {/* Caregiver routes — require CAREGIVER role */}
                <Route path="/caregiver" element={<ProtectedRoute role="CAREGIVER"><CaregiverDashboard/></ProtectedRoute>} />
                <Route path="/caregiver/caregiver-documents" element={<ProtectedRoute role="CAREGIVER"><CaregiverDocument/></ProtectedRoute>} />

                {/* Admin routes — require ADMIN role */}
                <Route path="/admin" element={<ProtectedRoute role="ADMIN"><AdminDashboard/></ProtectedRoute>} />
                <Route path="/admin/account-requests" element={<ProtectedRoute role="ADMIN"><AccountRequests/></ProtectedRoute>} />
                <Route path="/admin/documents" element={<ProtectedRoute role="ADMIN"><AdminDocuments/></ProtectedRoute>} />

                {/* Public routes — no auth needed */}
                <Route path="/forgot-userid" element={<ForgotUserIdPage />} />
                <Route path="/forgot-password" element={<ForgotPasswordPage />} />
                <Route path="/create-account" element={<CreateAccountPage />} />
                <Route path="/request-sent" element={<RequestSentPage />} />
                <Route path="/recovery-email-sent" element={<RecoveryEmailSentPage />} />
                <Route path="/activate-account" element={<CompleteAccountCreation />} />
                <Route path="/reset-password" element={<ResetPasswordPage />} />
            </Routes>
        </BrowserRouter>
        </ErrorBoundary>
    );
}
