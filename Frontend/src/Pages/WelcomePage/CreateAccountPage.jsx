import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { post } from "../../services/api";
import "./css/LoginPage.css"; // reuse same CSS

export default function CreateAccountPage() {
    const navigate = useNavigate();

    const [firstName, setFirstName] = useState("");
    const [lastName, setLastName] = useState("");
    const [email, setEmail] = useState("");
    const [role, setRole] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRequestAccess = async (e) => {
        e.preventDefault();
        setMessage("");

        setLoading(true);

        try {
            const response = await post("/accountCredential/request-access", {
                firstName,
                lastName,
                email,
                role
            });

            if (response && response.message) {
                // if request was sent, go to always go to request sent confirmation page + pass message + status
                navigate("/request-sent", {
                    state: {
                        message: response.message,
                        status: response.status, // optional
                    },
                });

            //
            } else {
                setMessage("Unable to submit request. Please try again.");
            }

        } catch (err) {
            console.error("Request access error:", err);
            setMessage("Unable to submit request. Please try again.");
        } finally {
            setLoading(false);
        }
    }


        return (
            <div className="login-container">
                <h1 className="title">
                    Clini<span className="core">Core</span>
                </h1>

                <div className="container-box">
                    <h2>Request an Account</h2>

                    <form onSubmit={handleRequestAccess}>

                        {/* ⭐ First Name */}
                        <label htmlFor="firstName">First Name</label>
                        <div className="input-wrapper">
                            <input
                                id="firstName"
                                type="text"
                                value={firstName}
                                onChange={(e) => setFirstName(e.target.value)}
                                required
                            />
                        </div>

                        {/* ⭐ Last Name */}
                        <label htmlFor="lastName">Last Name</label>
                        <div className="input-wrapper">
                            <input
                                id="lastName"
                                type="text"
                                value={lastName}
                                onChange={(e) => setLastName(e.target.value)}
                                required
                            />
                        </div>

                        {/* ⭐ Role Dropdown */}
                        <label htmlFor="role">Role</label>
                        <div className="input-wrapper">
                            <select
                                id="role"
                                value={role}
                                onChange={(e) => setRole(e.target.value)}
                                required
                            >
                                <option value="">Select a role</option>
                                <option value="RESIDENT">Resident</option>
                                <option value="CAREGIVER">Caregiver</option>
                                <option value="ADMIN">Admin</option>
                            </select>
                        </div>

                        {/* ⭐ Email */}
                        <label htmlFor="email">Email Address</label>
                        <div className="input-wrapper">
                            <input
                                id="email"
                                type="email"
                                value={email}
                                onChange={(e) => setEmail(e.target.value)}
                                required
                            />
                        </div>

                        {message && (
                            <p style={{color: "red", marginTop: 10}}>{message}</p>
                        )}

                        <button
                            type="submit"
                            className="login-btn"
                            disabled={loading}
                        >
                            {loading ? <span className="spinner"></span> : "Send Request"}
                        </button>

                        <button
                            type="button"
                            className="create-btn"
                            onClick={() => navigate("/")}
                        >
                            Back
                        </button>
                    </form>
                </div>
            </div>
        );
    }