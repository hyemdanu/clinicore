import { useNavigate } from "react-router-dom";
import "./css/LoginPage.css";

// login page
export default function LoginPage() {
    const navigate = useNavigate();

    const handleLogin = (e) => {
        e.preventDefault();
        navigate("/home");
    };

    // login container
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
                        <span className="input-icon">✉</span>
                        <input id="username" type="text" placeholder="Name" required />
                    </div>
                    <a href="#" className="link">
                        Forgot User ID
                    </a>

                    <label htmlFor="password">Password</label>
                    <div className="input-wrapper">
                        <span className="input-icon">✉</span>
                        <input id="password" type="password" placeholder="Password" required />
                    </div>
                    <a href="#" className="link">
                        Forget password?
                    </a>

                    <button type="submit" className="login-btn">
                        Log in
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
