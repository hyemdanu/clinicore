package com.clinicore.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    private List<AssignedCaregiverDTO> assignedCaregivers; // adding assigned caregivers to the resident dto

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

        @NotBlank(message = "Medication name is required")
        @Size(max = 255, message = "Medication name must be under 255 characters")
        private String name;

        @Size(max = 255, message = "Dosage must be under 255 characters")
        private String dosage;

        @Size(max = 255, message = "Schedule must be under 255 characters")
        private String schedule;

        private Integer inventoryQuantity;

        @Size(max = 1000, message = "Notes must be under 1000 characters")
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

        @NotBlank(message = "Allergy type is required")
        @Size(max = 255, message = "Allergy type must be under 255 characters")
        private String allergyType;

        private Integer severity;

        @Size(max = 1000, message = "Notes must be under 1000 characters")
        private String notes;
    }

    // diagnosis details
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisDTO {
        private Long id;
        private Long residentId;

        @NotBlank(message = "Diagnosis is required")
        @Size(max = 500, message = "Diagnosis must be under 500 characters")
        private String diagnosis;

        @Size(max = 1000, message = "Notes must be under 1000 characters")
        private String notes;
    }

    // assigned caregiver info
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssignedCaregiverDTO {
        private Long caregiverId;
        private String caregiverName;
    }
}
