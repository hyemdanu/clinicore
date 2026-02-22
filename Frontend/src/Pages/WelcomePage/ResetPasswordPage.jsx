import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { post } from "../../services/api";
import "./css/LoginPage.css";

export default function ResetPasswordPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();

    const [email, setEmail] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    useEffect(() => {
        // grab email from URL: /reset-password?email=user@example.com
        const emailFromUrl = searchParams.get("email");
        if (!emailFromUrl) {
            setMessage("Invalid reset link. Please request a new one.");
        } else {
            setEmail(emailFromUrl);
        }
    }, [searchParams]);

    const handleResetPassword = async (e) => {
        e.preventDefault();
        setMessage("");

        if (newPassword !== confirmPassword) {
            setMessage("Passwords do not match.");
            return;
        }

        setLoading(true);
        try {
            await post("/accountCredential/reset-password", {
                email,
                newPassword
            });

            setSuccess(true);
            setMessage("Password reset successfully! Redirecting to login...");
            setTimeout(() => navigate("/"), 2500);

        } catch (err) {
            setMessage(err.message || "Failed to reset password. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
            <h1 className="title">
                Clini<span className="core">Core</span>
            </h1>

            <div className="container-box">
                <h2>Reset Password</h2>

                {!email ? (
                    <p style={{ color: "red" }}>{message}</p>
                ) : success ? (
                    <p style={{ color: "green" }}>{message}</p>
                ) : (
                    <>
                        <p style={{ color: "#666", marginBottom: "20px", fontSize: "14px" }}>
                            Resetting password for: <strong>{email}</strong>
                        </p>

                        <form onSubmit={handleResetPassword}>
                            <label htmlFor="newPassword">New Password</label>
                            <div className="input-wrapper">
                                <input
                                    id="newPassword"
                                    type="password"
                                    value={newPassword}
                                    onChange={(e) => setNewPassword(e.target.value)}
                                    required
                                />
                            </div>

                            <label htmlFor="confirmPassword">Confirm Password</label>
                            <div className="input-wrapper">
                                <input
                                    id="confirmPassword"
                                    type="password"
                                    value={confirmPassword}
                                    onChange={(e) => setConfirmPassword(e.target.value)}
                                    required
                                />
                            </div>

                            {message && (
                                <p style={{ color: "red", marginTop: 10 }}>{message}</p>
                            )}

                            <button
                                type="submit"
                                className="login-btn"
                                disabled={loading}
                            >
                                {loading ? <span className="spinner"></span> : "Reset Password"}
                            </button>

                            <button
                                type="button"
                                className="create-btn"
                                onClick={() => navigate("/")}
                            >
                                Back to Login
                            </button>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
}