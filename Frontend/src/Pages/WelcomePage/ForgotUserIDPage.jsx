import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { post } from "../../services/api";
import "./css/LoginPage.css"; // reuse the same CSS

export default function ForgotUserIdPage() {
    const navigate = useNavigate();
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSendEmail = async (e) => {
        e.preventDefault();
        setMessage("");
        setLoading(true);

        try {
            const response = await post("/api/accountCredential/forgot-userid", { email });

            if (response && response.message === "Email verified successfully.") {
                // go to recovery page
                navigate("/request-sent");
            } else {
                setMessage("Email not found. Please try again.");
            }
        } catch (err) {
            console.error("Error:", err);
            setMessage("Unable to send email. Please try again.");
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
                <h2>Forgot User ID</h2>

                <form onSubmit={handleSendEmail}>
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
                        <p style={{ color: message.includes("Unable") ? "red" : "green" }}>
                            {message}
                        </p>
                    )}

                    <button
                        type="submit"
                        className="login-btn"
                        disabled={loading}
                    >
                        {loading ? <span className="spinner"></span> : "Send Email"}
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
