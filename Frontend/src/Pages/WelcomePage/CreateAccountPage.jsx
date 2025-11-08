import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { post } from "../../services/api";
import "./css/LoginPage.css"; // reuse same CSS as Login

export default function CreateAccountPage() {
    const navigate = useNavigate();
    const [name, setName] = useState("");
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleRequestAccess = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            if (!name.trim() || !email.trim()) {
                throw new Error("Please fill in all fields.");
            }

            // Dummy backend call (replace with your real endpoint later)
            await post("/accountCredential/request-access", { name, email });

            // Navigate to confirmation page
            navigate("/request-sent");
        } catch (err) {
            console.error("Request access error:", err);
            setError(err.message || "Something went wrong.");
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

                    {error && <p style={{ color: "red", marginTop: 10 }}>{error}</p>}

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
