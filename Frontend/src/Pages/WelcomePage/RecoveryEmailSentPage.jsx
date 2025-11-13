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

                    If your email ID exists in our records, we will send a recovery email with steps to reset your login information.
                    <br />
                    Please allow 2-5 minutes for the email to arrive.
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
