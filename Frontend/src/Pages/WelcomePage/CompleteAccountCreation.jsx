import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { post } from "../../services/api";
import "./css/LoginPage.css";

export default function CompleteAccountCreation() {
    const navigate = useNavigate();

    const [step, setStep] = useState(1); // 1 = verify code, 2 = complete account
    const [email, setEmail] = useState("");
    const [activationCode, setActivationCode] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    // Step 2: Account Completion Form
    const [accountData, setAccountData] = useState({
        requestId: null,
        firstName: "",
        lastName: "",
        role: "",
        username: "",
        password: "",
        confirmPassword: "",
        gender: "",
        birthday: "",
        contactNumber: "",
        // Resident-specific fields
        emergencyContactName: "",
        emergencyContactNumber: "",
        // Caregiver-specific fields
        caregiverNotes: "",
        // Resident notes
        residentNotes: ""
    });

    // Step 1: Verify activation code
    const handleVerifyActivationCode = async (e) => {
        e.preventDefault();
        setMessage("");

        setLoading(true);

        try {

            // Call backend to verify activation code
            const response = await post("/accountCredential/verify-activation-code", {
                email,
                activationCode
            });

            if (response && response.success) {
                setAccountData(prev => ({
                    ...prev,
                    requestId: response.requestId,
                    firstName: response.firstName,
                    lastName: response.lastName,
                    role: response.role
                }));
                setStep(2);
            } else {
                setMessage(response?.message || "Verification failed. Please try again.");
            }
        } catch (err) {
            setMessage("Unable to verify account. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    // Step 2: Complete account creation
    const handleCompleteAccount = async (e) => {
        e.preventDefault();
        setMessage("");
        setLoading(true);

        try {
            if (!accountData.username.trim()) {
                setMessage("Username is required");
                setLoading(false);
                return;
            }
            if (!accountData.password) {
                setMessage("Password is required");
                setLoading(false);
                return;
            }
            if (accountData.password !== accountData.confirmPassword) {
                setMessage("Passwords do not match");
                setLoading(false);
                return;
            }
            if (!accountData.gender) {
                setMessage("Gender is required");
                setLoading(false);
                return;
            }
            if (!accountData.birthday) {
                setMessage("Birthday is required");
                setLoading(false);
                return;
            }
            if (!accountData.contactNumber.trim()) {
                setMessage("Contact number is required");
                setLoading(false);
                return;
            }

            if (accountData.role === "RESIDENT") {
                if (!accountData.emergencyContactName.trim()) {
                    setMessage("Emergency contact name is required");
                    setLoading(false);
                    return;
                }
                if (!accountData.emergencyContactNumber.trim()) {
                    setMessage("Emergency contact number is required");
                    setLoading(false);
                    return;
                }
            }

            const requestPayload = {
                requestId: accountData.requestId,
                username: accountData.username,
                password: accountData.password,
                gender: accountData.gender,
                birthday: accountData.birthday,
                contactNumber: accountData.contactNumber,
                role: accountData.role
            };

            if (accountData.role === "RESIDENT") {
                requestPayload.emergencyContactName = accountData.emergencyContactName;
                requestPayload.emergencyContactNumber = accountData.emergencyContactNumber;
                requestPayload.notes = accountData.residentNotes;
            } else if (accountData.role === "CAREGIVER") {
                requestPayload.notes = accountData.caregiverNotes;
            }

            const response = await post("/accountCredential/create-account", requestPayload);

            if (response && response.userId) {
                navigate("/", {
                    state: {
                        message: "Account created successfully! Please log in.",
                        success: true
                    }
                });
            } else {
                setMessage(response?.error || "Account creation failed. Please try again.");
            }
        } catch (err) {
            setMessage(err.message || "Failed to create account. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    // Handle input changes for account data
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setAccountData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    // Handle number-only input changes (strips any non-digit characters)
    const handleNumberInputChange = (e) => {
        const { name, value } = e.target;
        const numbersOnly = value.replace(/\D/g, "");
        setAccountData(prev => ({
            ...prev,
            [name]: numbersOnly
        }));
    };

    // Render role-specific form fields
    const renderRoleSpecificFields = () => {
        if (accountData.role === "RESIDENT") {
            return (
                <>
                    <h3 style={{ marginTop: "20px", marginBottom: "15px", color: "#333", fontSize: "16px" }}>
                        Resident Information
                    </h3>

                    {/* Emergency Contact Name */}
                    <label htmlFor="emergencyContactName">Emergency Contact Name *</label>
                    <div className="input-wrapper">
                        <input
                            id="emergencyContactName"
                            type="text"
                            name="emergencyContactName"
                            value={accountData.emergencyContactName}
                            onChange={handleInputChange}
                            placeholder="Full name of emergency contact"
                            required
                        />
                    </div>

                    {/* Emergency Contact Number */}
                    <label htmlFor="emergencyContactNumber">Emergency Contact Number *</label>
                    <div className="input-wrapper">
                        <input
                            id="emergencyContactNumber"
                            type="tel"
                            name="emergencyContactNumber"
                            value={accountData.emergencyContactNumber}
                            onChange={handleNumberInputChange}
                            placeholder="e.g. 9161234567"
                            required
                        />
                    </div>

                    {/* Resident Notes */}
                    <label htmlFor="residentNotes">Additional Notes</label>
                    <div className="input-wrapper">
                        <textarea
                            id="residentNotes"
                            name="residentNotes"
                            value={accountData.residentNotes}
                            onChange={handleInputChange}
                            placeholder="Any additional medical or personal notes (optional)"
                            rows="4"
                            style={{ padding: "10px", fontFamily: "inherit" }}
                        />
                    </div>
                </>
            );
        } else if (accountData.role === "CAREGIVER") {
            return (
                <>
                    <h3 style={{ marginTop: "20px", marginBottom: "15px", color: "#333", fontSize: "16px" }}>
                        Caregiver Information
                    </h3>

                    {/* Caregiver Notes */}
                    <label htmlFor="caregiverNotes">Professional Notes</label>
                    <div className="input-wrapper">
                        <textarea
                            id="caregiverNotes"
                            name="caregiverNotes"
                            value={accountData.caregiverNotes}
                            onChange={handleInputChange}
                            placeholder="Certifications, specializations, or other relevant information (optional)"
                            rows="4"
                            style={{ padding: "10px", fontFamily: "inherit" }}
                        />
                    </div>
                </>
            );
        } else if (accountData.role === "ADMIN") {
            return (
                <p style={{ marginTop: "20px", color: "#666", fontSize: "14px" }}>
                    Admin accounts will be created with full system access.
                </p>
            );
        }
        return null;
    };

    return (
        <div className="login-container">
            <h1 className="title">
                Clini<span className="core">Core</span>
            </h1>

            <div className="container-box">
                {step === 1 ? (
                    // Step 1: Verify Activation Code
                    <>
                        <h2>Activate Your Account</h2>
                        <p style={{ color: "#666", marginBottom: "20px", fontSize: "14px" }}>
                            Enter your email and the activation code you received to proceed
                        </p>

                        <form onSubmit={handleVerifyActivationCode}>
                            {/* Email Input */}
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

                            {/* Activation Code Input */}
                            <label htmlFor="activationCode">Activation Code</label>
                            <div className="input-wrapper">
                                <input
                                    id="activationCode"
                                    type="text"
                                    value={activationCode}
                                    onChange={(e) => setActivationCode(e.target.value)}
                                    placeholder="Enter your activation code"
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
                                {loading ? <span className="spinner"></span> : "Verify Code"}
                            </button>

                            <button
                                type="button"
                                className="create-btn"
                                onClick={() => navigate("/")}
                            >
                                Back
                            </button>
                        </form>
                    </>
                ) : (
                    // Step 2: Complete Account Creation
                    <>
                        <h2>Complete Your Account</h2>
                        <p style={{ color: "#666", marginBottom: "20px", fontSize: "14px" }}>
                            Fill in your account details to complete registration
                        </p>

                        <form onSubmit={handleCompleteAccount}>
                            {/* Display-only fields */}
                            <div style={{ backgroundColor: "#f5f5f5", padding: "15px", borderRadius: "8px", marginBottom: "20px" }}>
                                <p style={{ margin: "5px 0", color: "#666" }}>
                                    <strong>Name:</strong> {accountData.firstName} {accountData.lastName}
                                </p>
                                <p style={{ margin: "5px 0", color: "#666" }}>
                                    <strong>Email:</strong> {email}
                                </p>
                                <p style={{ margin: "5px 0", color: "#666" }}>
                                    <strong>Role:</strong> {accountData.role}
                                </p>
                            </div>

                            <h3 style={{ marginBottom: "15px", color: "#333", fontSize: "16px" }}>
                                Account Credentials
                            </h3>

                            {/* Username */}
                            <label htmlFor="username">Username *</label>
                            <div className="input-wrapper">
                                <input
                                    id="username"
                                    type="text"
                                    name="username"
                                    value={accountData.username}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            {/* Password */}
                            <label htmlFor="password">Password *</label>
                            <div className="input-wrapper">
                                <input
                                    id="password"
                                    type="password"
                                    name="password"
                                    value={accountData.password}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            {/* Confirm Password */}
                            <label htmlFor="confirmPassword">Confirm Password *</label>
                            <div className="input-wrapper">
                                <input
                                    id="confirmPassword"
                                    type="password"
                                    name="confirmPassword"
                                    value={accountData.confirmPassword}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            <h3 style={{ marginTop: "20px", marginBottom: "15px", color: "#333", fontSize: "16px" }}>
                                Personal Information
                            </h3>

                            {/* Gender */}
                            <label htmlFor="gender">Gender *</label>
                            <div className="input-wrapper">
                                <select
                                    id="gender"
                                    name="gender"
                                    value={accountData.gender}
                                    onChange={handleInputChange}
                                    required
                                >
                                    <option value="">Select Gender</option>
                                    <option value="Male">Male</option>
                                    <option value="Female">Female</option>
                                    <option value="Other">Other</option>
                                </select>
                            </div>

                            {/* Birthday */}
                            <label htmlFor="birthday">Birthday *</label>
                            <div className="input-wrapper">
                                <input
                                    id="birthday"
                                    type="date"
                                    name="birthday"
                                    value={accountData.birthday}
                                    onChange={handleInputChange}
                                    required
                                />
                            </div>

                            {/* Contact Number */}
                            <label htmlFor="contactNumber">Contact Number *</label>
                            <div className="input-wrapper">
                                <input
                                    id="contactNumber"
                                    type="tel"
                                    name="contactNumber"
                                    value={accountData.contactNumber}
                                    onChange={handleNumberInputChange}
                                    placeholder="e.g. 9161234567"
                                    required
                                />
                            </div>

                            {/* Role-Specific Fields */}
                            {renderRoleSpecificFields()}

                            {message && (
                                <p style={{ color: "red", marginTop: 10 }}>{message}</p>
                            )}

                            <button
                                type="submit"
                                className="login-btn"
                                disabled={loading}
                                style={{ marginTop: "20px" }}
                            >
                                {loading ? <span className="spinner"></span> : "Create Account"}
                            </button>

                            <button
                                type="button"
                                className="create-btn"
                                onClick={() => {
                                    setStep(1);
                                    setAccountData({
                                        requestId: null,
                                        firstName: "",
                                        lastName: "",
                                        role: "",
                                        username: "",
                                        password: "",
                                        confirmPassword: "",
                                        gender: "",
                                        birthday: "",
                                        contactNumber: "",
                                        emergencyContactName: "",
                                        emergencyContactNumber: "",
                                        caregiverNotes: "",
                                        residentNotes: ""
                                    });
                                    setMessage("");
                                }}
                            >
                                Back
                            </button>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
}