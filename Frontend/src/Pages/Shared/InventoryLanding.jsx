import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Header from '../../Components/Header.jsx';
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
                    <div
                        className="inventory-card"
                        onClick={handleConsumablesClick}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && handleConsumablesClick()}
                    >
                        <div className="card-icon-wrapper purple">
                            <img
                                src={consumablesIcon}
                                alt="Medical Consumables"
                                className="card-icon"
                            />
                        </div>
                        <h2 className="card-title">Medical<br/>Consumables</h2>
                    </div>

                    {/* Medications Card */}
                    <div
                        className="inventory-card"
                        onClick={handleMedicationsClick}
                        role="button"
                        tabIndex={0}
                        onKeyDown={(e) => (e.key === 'Enter' || e.key === ' ') && handleMedicationsClick()}
                    >
                        <div className="card-icon-wrapper pink">
                            <img
                                src={medicationsIcon}
                                alt="Medications"
                                className="card-icon"
                            />
                        </div>
                        <h2 className="card-title">Medications</h2>
                    </div>

                </div>

            </div>

        </div>
    );
}