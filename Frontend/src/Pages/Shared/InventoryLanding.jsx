import '../CaregiverPortal/css/inventory-landing.css';
import consumablesIcon from '../../assets/icons/consumablesIcon.png';
import medicationsIcon from '../../assets/icons/medicationsIcon.png';

export default function InventoryLanding({ onNavigate }) {

    const handleMedicationsClick = () => {
        onNavigate('medication-inventory');
    };

    const handleConsumablesClick = () => {
        onNavigate('consumables-inventory');
    };

    return (
        <div className="inventory-landing-container">

            <div className="landing-content">

                <div className="inventory-cards-container">

                    {/* Medical Consumables Card */}
                    <button
                        type="button"
                        className="inventory-card btn-reset"
                        onClick={handleConsumablesClick}
                        aria-label="Go to medical consumables inventory"
                    >
                        <div className="card-icon-wrapper purple">
                            <img
                                src={consumablesIcon}
                                alt="Medical Consumables"
                                className="card-icon"
                            />
                        </div>
                        <h2 className="card-title">Medical<br/>Consumables</h2>
                    </button>

                    {/* Medications Card */}
                    <button
                        type="button"
                        className="inventory-card btn-reset"
                        onClick={handleMedicationsClick}
                        aria-label="Go to medication inventory"
                    >
                        <div className="card-icon-wrapper pink">
                            <img
                                src={medicationsIcon}
                                alt="Medications"
                                className="card-icon"
                            />
                        </div>
                        <h2 className="card-title">Medications</h2>
                    </button>

                </div>

            </div>

        </div>
    );
}
