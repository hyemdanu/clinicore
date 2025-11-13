import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { post } from "../../services/api";
import "./css/LoginPage.css"; // reuse same CSS

export default function CreateAccountPage() {
    const navigate = useNavigate();

    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [message, setMessage] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRequestAccess = async (e) => {
        e.preventDefault();
        setMessage("");
        setLoading(true);

        try {
            // validate input
            if (!name.trim() || !email.trim()) {
                setMessage("Please fill in all fields.");
                return;
            }

            // ⭐ Call backend (same pattern as Forgot Password page)
            const response = await post("/accountCredential/request-access", {
                name,
                email
            });

            // Backend should return { message: "Request received" }
            if (response && response.message === "Request received") {
                navigate("/request-sent");
            } else {
                setMessage("Unable to submit request. Please try again.");
            }

        } catch (err) {
            console.error("Request access error:", err);
            setMessage("Unable to submit request. Please try again.");
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
                <h2>Create Account</h2>

                <form onSubmit={handleRequestAccess}>
                    <label htmlFor="name">Full Name</label>
                    <div className="input-wrapper">
                        <input
                            id="name"
                            type="text"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            required
                        />
                    </div>

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
                        <p style={{ color: "red", marginTop: 10 }}>{message}</p>
                    )}

                    <button
                        type="submit"
                        className="login-btn"
                        disabled={loading}
                    >
                        {loading ? <span className="spinner"></span> : "Request Access"}
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
