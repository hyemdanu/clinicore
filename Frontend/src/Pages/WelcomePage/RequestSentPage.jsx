import { useLocation, useNavigate } from "react-router-dom";
import "./css/LoginPage.css";

export default function RequestSentPage() {
    const navigate = useNavigate();
    const location = useLocation();

    // message from backend or fallback
    const message =
        location.state?.message ??
        "Error: Request status unknown. Please try again later.";

    return (
        <div className="login-container">
            <h1 className="title">
                Clini<span className="core">Core</span>
            </h1>

            <div className="container-box">
                <h2>Request Status</h2>

                <p style={{ marginTop: "1rem", textAlign: "center" }}>
                    {message}
                </p>

                <button
                    type="button"
                    className="create-btn"
                    onClick={() => navigate("/")}
                >
                    Back to Login Page
                </button>
            </div>
        </div>
    );
}
