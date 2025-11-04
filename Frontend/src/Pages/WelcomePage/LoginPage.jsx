import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { post } from "../../services/api";     // ✅ correct import path
import "./css/LoginPage.css";

export default function LoginPage() {
    const navigate = useNavigate();

    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleLogin = async (e) => {
        e.preventDefault();
        setError("");
        setLoading(true);

        try {
            // ✅ No hashing – backend expects raw stored string ("hash_01", "hash_02", etc.)
            const data = await post("/accountCredential/login", {
                username,
                passwordHash: password
            });

            // ✅ Save authenticated user session
            localStorage.setItem("clinicoreUser", JSON.stringify(data));

            navigate("/home");// this navigates to perspective dashboard depending on log in
        } catch (err) {
            console.error("Login error:", err);
            setError("Invalid username or password.");
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
                <h2>Log in</h2>

                <form onSubmit={handleLogin}>
                    <label htmlFor="username">User ID</label>
                    <div className="input-wrapper">
                        <span className="input-icon">👤</span>
                        <input
                            id="username"
                            type="text"
                            placeholder="Name"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>

                    <a className="link" href="#">Forgot User ID</a>

                    <label htmlFor="password">Password</label>
                    <div className="input-wrapper">
                        <span className="input-icon">🔒</span>
                        <input
                            id="password"
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <a className="link" href="#">Forget password?</a>

                    {error && (
                        <p style={{ color: "red", marginTop: 10 }}>{error}</p>
                    )}

                    <button
                        type="submit"
                        className="login-btn"
                        disabled={loading}
                    >
                        {loading ? "Signing in..." : "Log in"}
                    </button>

                    <button
                        type="button"
                        className="create-btn"
                        onClick={() => navigate("/create-account")}
                    >
                        Create an Account
                    </button>
                </form>
            </div>
        </div>
    );
}
