import { useNavigate } from "react-router-dom";
import "./css/LoginPage.css";

export default function RequestSentPage() {
    const navigate = useNavigate();

    return (
        <div className="login-container">
            <h1 className="title">
                Clini<span className="core">Core</span>
            </h1>

            <div className="container-box">
                <h2>Request Sent</h2>

                <p style={{ marginTop: "1rem", textAlign: "center" }}>
                    Your request has been received by the admin team.
                    <br />
                    They will contact you with further details about your account soon.
                </p>

                <button
                    type="button"
                    className="create-btn"
                    onClick={() => navigate("/")}
                >
                    Back
                </button>
            </div>
        </div>
    );
}
