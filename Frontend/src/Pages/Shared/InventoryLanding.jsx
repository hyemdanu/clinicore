import { useState } from 'react';
import '../CaregiverPortal/css/inventory-landing.css';
import MedicationInventory from './CaregiverMedicationInventory';
import ConsumablesInventory from './ConsumablesInventory';

export default function InventoryLanding() {
    const [activeView, setActiveView] = useState('medications');

    return (
        <div>
            <div className="inventory-toggle">
                <button
                    type="button"
                    className={`inventory-toggle-btn ${activeView === 'medications' ? 'active' : ''}`}
                    onClick={() => setActiveView('medications')}
                >
                    Medications
                </button>
                <button
                    type="button"
                    className={`inventory-toggle-btn ${activeView === 'consumables' ? 'active' : ''}`}
                    onClick={() => setActiveView('consumables')}
                >
                    Medical Consumables
                </button>
            </div>

            {activeView === 'medications' ? <MedicationInventory /> : <ConsumablesInventory />}
        </div>
    );
}
