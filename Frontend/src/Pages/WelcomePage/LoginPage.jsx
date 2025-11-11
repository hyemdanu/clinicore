import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { post } from "../../services/api";
import "./css/LoginPage.css";

export default function LoginPage() {
    const navigate = useNavigate();

    // state variables for username and password
    // syntax: const [stateVariable, setStateFunction] = useState(initialState);
    // react updates when state functions are called
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleLogin = async (e) => {
        // this prevents the default form submission behavior
        // necessary for react to handle ui rather than browser
        e.preventDefault();

        // reset stuff
        setError("");

        // set loading to true to show spinner
        // ui friendly so user dont keep clicking and fuck it up
        setLoading(true);

        try {

            // send post request to backend
            // these params are the same as the ones in the backend
            const data = await post("/accountCredential/login", {
                username,
                passwordHash: password
            });

            // Store user data in localStorage for session management
            localStorage.setItem('currentUser', JSON.stringify({
                id: data.id,
                username: data.username,
                role: data.role
            }));

            // Redirect by role
            switch (data.role) {
                case "ADMIN":
                    navigate("/admin"); // go to admin page
                    break;
                case "CAREGIVER":
                    navigate("/caregiver"); // go to caregiver page
                    break;
                case "RESIDENT":
                    navigate("/resident");  // go to resident page
                    break;
            }

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

                    <label htmlFor="username">Username</label>
                    <div className="input-wrapper">
                        <input
                            id="username"
                            type="text"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                            required
                        />
                    </div>

                    <a className="link" href="/forgot-userid">Forgot User ID</a>

                    <label htmlFor="password">Password</label>
                    <div className="input-wrapper">
                        <input
                            id="password"
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </div>

                    <a className="link" href="/forgot-password">Forget password?</a>

                    {error && (
                        <p style={{ color: "red", marginTop: 10 }}>{error}</p>
                    )}

                    <button
                        type="submit"
                        className="login-btn"
                        disabled={loading}
                    >
                        {loading ? <span className="spinner"></span> : "Log in"}
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
