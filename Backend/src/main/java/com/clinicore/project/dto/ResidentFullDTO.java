package com.clinicore.project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

// complete resident info - everything about a resident in one DTO
// includes: profile, emergency contacts, medical info, capabilities, allergies, diagnoses, meds
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResidentFullDTO {
    // basic profile
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate birthday;
    private String contactNumber;

    // resident-specific
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String residentNotes;

    // all the medical stuff
    private MedicalProfileDTO medicalProfile;
    private MedicalServicesDTO medicalServices;
    private CapabilityDTO capability;
    private MedicalRecordDTO medicalRecord;
    private List<MedicationDTO> medications;

    // insurance and medical notes
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalProfileDTO {
        private String insurance;
        private String notes;
    }

    // healthcare providers and preferences
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalServicesDTO {
        private String hospiceAgency;
        private String preferredHospital;
        private String preferredPharmacy;
        private String homeHealthAgency;
        private String mortuary;
        private String dnrPolst;
        private Boolean hospice;
        private Boolean homeHealth;
    }

    // what they can do functionally
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapabilityDTO {
        private Boolean verbal;
        private Boolean selfMedicates;
        private String incontinenceStatus;
        private String mobilityStatus;
    }

    // allergies and diagnoses
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalRecordDTO {
        private List<String> allergies;
        private List<String> diagnoses;
        private String notes;
        private List<AllergyDTO> allergyDetails;
        private List<DiagnosisDTO> diagnosisDetails;
    }

    // medication info for a resident
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicationDTO {
        private Long id;
        private Long medicationInventoryId;
        private String name;
        private String dosage;
        private String schedule;
        private Integer inventoryQuantity;
        private String notes;
        private String intakeStatus;
        private String lastAdministeredAt;
        private String nextDoseTime;
        private Boolean isOverdue;
    }

    // allergy details with severity
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AllergyDTO {
        private Long id;
        private Long residentId;
        private String allergyType;
        private Integer severity;
        private String notes;
    }

    // diagnosis details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisDTO {
        private Long id;
        private Long residentId;
        private String diagnosis;
        private String notes;
    }
}
