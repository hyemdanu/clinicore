package com.clinicore.project.service;

import com.clinicore.project.dto.MedicationInventoryDTO;
import com.clinicore.project.dto.ResidentFullDTO;
import com.clinicore.project.entity.*;
import com.clinicore.project.repository.*;
import com.clinicore.project.util.MedicationScheduleUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ResidentService {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ResidentGeneralRepository residentGeneralRepository;

    @Autowired
    private MedicalProfileRepository medicalProfileRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private MedicationInventoryRepository medicationInventoryRepository;

    @Autowired
    private AllergyRepository allergyRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private ResidentCaregiverRepository residentCaregiverRepository;

    /**
     * Get all residents with their full details
     * this includes user profile, resident info, medical profile, services, capability, records, and medications
     */
    @Transactional(readOnly = true)
    public List<ResidentFullDTO> getAllResidentsWithFullDetails() {
        // get all profiles with resident role
        List<UserProfile> residentProfiles = userProfileRepository.findByRole(UserProfile.Role.RESIDENT);

        // map each profile to ResidentFullDTO
        return residentProfiles.stream()
                .map(this::mapToResidentFullDTO)
                .collect(Collectors.toList());
    }

    /**
     * get single resident full details by ID
     */
    @Transactional(readOnly = true)
    public ResidentFullDTO getResidentFullDetailsById(Long residentId) {
        UserProfile userProfile = userProfileRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Resident not found with id: " + residentId));

        if (userProfile.getRole() != UserProfile.Role.RESIDENT) {
            throw new RuntimeException("User with id " + residentId + " is not a resident");
        }

        return mapToResidentFullDTO(userProfile);
    }

    /**
     * map all residents details to ResidentFullDTO
     */
    private ResidentFullDTO mapToResidentFullDTO(UserProfile userProfile) {
        ResidentFullDTO dto = new ResidentFullDTO();

        // map basic resident fields
        dto.setId(userProfile.getId());
        dto.setEmail(userProfile.getEmail());
        dto.setFirstName(userProfile.getFirstName());
        dto.setLastName(userProfile.getLastName());
        dto.setGender(userProfile.getGender());
        dto.setBirthday(userProfile.getBirthday());
        dto.setContactNumber(userProfile.getContactNumber());

        // map resident-specific fields (details that are in resident_general table)
        // cus other roles like staff/admin won't have these fields
        Resident resident = userProfile.getResident();
        if (resident != null) {
            dto.setEmergencyContactName(resident.getEmergencyContactName());
            dto.setEmergencyContactNumber(resident.getEmergencyContactNumber());
            dto.setResidentNotes(resident.getNotes());

            // resolve assigned caregivers for this resident
            List<ResidentCaregiver> assignments = residentCaregiverRepository.findById_ResidentId(userProfile.getId());
            List<ResidentFullDTO.AssignedCaregiverDTO> caregiverDTOs = assignments.stream()
                    .map(a -> new ResidentFullDTO.AssignedCaregiverDTO(
                            a.getCaregiver().getUserProfile().getId(),
                            a.getCaregiver().getUserProfile().getFirstName() + " " + a.getCaregiver().getUserProfile().getLastName()
                    ))
                    .collect(Collectors.toList());
            dto.setAssignedCaregivers(caregiverDTOs);
        }

        // map medical profile and related entities
        MedicalProfile medicalProfile = medicalProfileRepository.findById(userProfile.getId()).orElse(null);
        if (medicalProfile != null) {
            // DTO for medical profile
            ResidentFullDTO.MedicalProfileDTO medicalProfileDTO = new ResidentFullDTO.MedicalProfileDTO();
            medicalProfileDTO.setInsurance(medicalProfile.getInsurance());
            medicalProfileDTO.setNotes(medicalProfile.getNotes());
            dto.setMedicalProfile(medicalProfileDTO);

            // DTO for medical services
            MedicalServices medicalServices = medicalProfile.getMedicalServices();
            if (medicalServices != null) {
                ResidentFullDTO.MedicalServicesDTO servicesDTO = new ResidentFullDTO.MedicalServicesDTO();
                servicesDTO.setHospiceAgency(medicalServices.getHospiceAgency());
                servicesDTO.setPreferredHospital(medicalServices.getPreferredHospital());
                servicesDTO.setPreferredPharmacy(medicalServices.getPreferredPharmacy());
                servicesDTO.setHomeHealthAgency(medicalServices.getHomeHealthAgency());
                servicesDTO.setMortuary(medicalServices.getMortuary());
                servicesDTO.setDnrPolst(medicalServices.getDnrPolst());
                servicesDTO.setHospice(medicalServices.getHospice());
                servicesDTO.setHomeHealth(medicalServices.getHomeHealth());
                dto.setMedicalServices(servicesDTO);
            }

            // DTO for capabilities
            Capability capability = medicalProfile.getCapability();
            if (capability != null) {
                ResidentFullDTO.CapabilityDTO capabilityDTO = new ResidentFullDTO.CapabilityDTO();
                capabilityDTO.setVerbal(capability.getVerbal());
                capabilityDTO.setSelfMedicates(capability.getSelfMedicates());
                capabilityDTO.setIncontinenceStatus(formatIncontinenceStatus(capability.getIncontinenceStatus()));
                capabilityDTO.setMobilityStatus(formatMobilityStatus(capability.getMobilityStatus()));
                dto.setCapability(capabilityDTO);
            }

            // DTO for medical records
            MedicalRecord medicalRecord = medicalProfile.getMedicalRecord();
            ResidentFullDTO.MedicalRecordDTO recordDTO = new ResidentFullDTO.MedicalRecordDTO();
            if (medicalRecord != null) {
                recordDTO.setAllergies(medicalRecord.getAllergyList());
                recordDTO.setDiagnoses(medicalRecord.getDiagnosisList());
                recordDTO.setNotes(medicalRecord.getNotes());
            }

            // fetch allergies from allergy table
            List<Allergy> allergies = allergyRepository.findByResidentId(userProfile.getId());
            recordDTO.setAllergyDetails(allergies.stream()
                    .map(this::mapAllergyToDTO)
                    .collect(Collectors.toList()));

            // fetch diagnoses from diagnosis table
            List<Diagnosis> diagnoses = diagnosisRepository.findByResidentId(userProfile.getId());
            recordDTO.setDiagnosisDetails(diagnoses.stream()
                    .map(this::mapDiagnosisToDTO)
                    .collect(Collectors.toList()));

            dto.setMedicalRecord(recordDTO);

            // DTO for medications
            List<Medication> medications = medicalProfile.getMedications();
            if (medications != null && !medications.isEmpty()) {
                dto.setMedications(medications.stream().map(this::mapMedicationToDTO).collect(Collectors.toList()));
            } else {
                dto.setMedications(new ArrayList<>());
            }
        }

        return dto;
    }

    /**
     * incontinence status to readable string
     */
    private String formatIncontinenceStatus(Capability.IncontinenceStatus status) {
        if (status == null) return null;
        switch (status) {
            case CONTINENT:
                return "Continent";
            case INCONTINENT_URINE:
                return "Incontinent (Urine)";
            case INCONTINENT_BOWELS:
                return "Incontinent (Bowels)";
            case INCONTINENT_BOTH:
                return "Incontinent (Both)";
            default:
                return status.toString();
        }
    }

    /**
     * mobility status to readable string
     */
    private String formatMobilityStatus(Capability.MobilityStatus status) {
        if (status == null) return null;
        switch (status) {
            case WALKS_WITHOUT_ASSISTANCE:
                return "Walks Without Assistance";
            case WALKS_WITH_ASSISTANCE:
                return "Walks With Assistance";
            case WHEELCHAIR:
                return "Wheelchair";
            case BEDRIDDEN:
                return "Bedridden";
            default:
                return status.toString();
        }
    }

    /**
     * intake status to readable string
     */
    private String formatIntakeStatus(Medication.IntakeStatus status) {
        if (status == null) return "Pending";
        switch (status) {
            case ADMINISTERED:
                return "Administered";
            case WITHHELD:
                return "Withheld";
            case MISSED:
                return "Missed";
            case PENDING:
                return "Pending";
            default:
                return status.toString();
        }
    }

    /**
     * create a new medication for a resident
     */
    @Transactional
    public ResidentFullDTO.MedicationDTO createMedication(Long residentId, ResidentFullDTO.MedicationDTO medicationDTO) {
        MedicalProfile medicalProfile = medicalProfileRepository.findById(residentId)
                .orElseThrow(() -> new RuntimeException("Medical profile not found for resident with id: " + residentId));

        Medication medication = new Medication();
        medication.setMedicalProfile(medicalProfile);

        // link to inventory if available
        if (medicationDTO.getMedicationInventoryId() != null) {
            MedicationInventory inventory = medicationInventoryRepository.findById(medicationDTO.getMedicationInventoryId())
                    .orElseThrow(() -> new RuntimeException("Medication not found in inventory"));
            medication.setMedicationInventory(inventory);
            medication.setMedicationName(inventory.getItem().getName());
            medication.setDosage(medicationDTO.getDosage() != null ? medicationDTO.getDosage() : inventory.getDosagePerServing());
        } else {
            medication.setMedicationName(medicationDTO.getName());
            medication.setDosage(medicationDTO.getDosage());
        }

        medication.setFrequency(medicationDTO.getSchedule());
        medication.setNotes(medicationDTO.getNotes());
        medication.setIntakeStatus(Medication.IntakeStatus.PENDING);

        return mapMedicationToDTO(medicationRepository.save(medication));
    }

    /**
     * update medication status
     */
    @Transactional
    public ResidentFullDTO.MedicationDTO updateMedicationStatus(Long medicationId, String status) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + medicationId));

        Medication.IntakeStatus intakeStatus;
        try {
            intakeStatus = Medication.IntakeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }

        medication.setIntakeStatus(intakeStatus);

        // update administration time and inventory if administered
        if (intakeStatus == Medication.IntakeStatus.ADMINISTERED) {
            medication.setLastAdministeredAt(LocalDateTime.now());

            // minus 1 from inventory if tracked
            if (medication.getMedicationInventory() != null) {
                Item item = medication.getMedicationInventory().getItem();
                if (item.getQuantity() > 0) {
                    item.setQuantity(item.getQuantity() - 1);
                }
            }
        }

        return mapMedicationToDTO(medicationRepository.save(medication));
    }

    /**
     * get all available medications from inventory
     */
    public List<MedicationInventoryDTO> getAvailableMedications() {
        return medicationInventoryRepository.findAllOrderedByName().stream()
                .map(MedicationInventoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private ResidentFullDTO.MedicationDTO mapMedicationToDTO(Medication med) {
        ResidentFullDTO.MedicationDTO dto = new ResidentFullDTO.MedicationDTO();
        dto.setId(med.getId());
        dto.setMedicationInventoryId(med.getMedicationInventory() != null ? med.getMedicationInventory().getId() : null);
        dto.setName(med.getMedicationName());
        dto.setDosage(med.getDosage());
        dto.setSchedule(med.getFrequency());
        dto.setNotes(med.getNotes());
        dto.setIntakeStatus(formatIntakeStatus(med.getIntakeStatus()));
        dto.setLastAdministeredAt(med.getLastAdministeredAt() != null ? med.getLastAdministeredAt().toString() : null);

        // get inventory quantity
        if (med.getMedicationInventory() != null && med.getMedicationInventory().getItem() != null) {
            dto.setInventoryQuantity(med.getMedicationInventory().getItem().getQuantity());
        }

        // calculate next dose time to see if its missed/overdue
        if (med.getLastAdministeredAt() != null && med.getFrequency() != null) {
            LocalDateTime nextDose = MedicationScheduleUtil.calculateNextDoseTime(med.getLastAdministeredAt(), med.getFrequency());
            dto.setNextDoseTime(nextDose != null ? nextDose.toString() : null);
            dto.setIsOverdue(MedicationScheduleUtil.isOverdue(nextDose));
        }

        return dto;
    }

    /**
     * map allergy entity to AllergyDTO
     */
    private ResidentFullDTO.AllergyDTO mapAllergyToDTO(Allergy allergy) {
        ResidentFullDTO.AllergyDTO dto = new ResidentFullDTO.AllergyDTO();
        dto.setId(allergy.getId());
        dto.setResidentId(allergy.getResidentId());
        dto.setAllergyType(allergy.getAllergyType());
        dto.setSeverity(allergy.getSeverity());
        dto.setNotes(allergy.getNotes());
        return dto;
    }

    /**
     * map diagnosis entity to DiagnosisDTO
     */
    private ResidentFullDTO.DiagnosisDTO mapDiagnosisToDTO(Diagnosis diagnosis) {
        ResidentFullDTO.DiagnosisDTO dto = new ResidentFullDTO.DiagnosisDTO();
        dto.setId(diagnosis.getId());
        dto.setResidentId(diagnosis.getResidentId());
        dto.setDiagnosis(diagnosis.getDiagnosis());
        dto.setNotes(diagnosis.getNotes());
        return dto;
    }

    /**
     * create allergy for resident
     */
    @Transactional
    public ResidentFullDTO.AllergyDTO createAllergy(Long residentId, ResidentFullDTO.AllergyDTO allergyDTO) {
        Allergy allergy = new Allergy();
        allergy.setResidentId(residentId);
        allergy.setAllergyType(allergyDTO.getAllergyType());
        allergy.setSeverity(allergyDTO.getSeverity());
        allergy.setNotes(allergyDTO.getNotes());
        return mapAllergyToDTO(allergyRepository.save(allergy));
    }

    /**
     * delete allergy
     */
    @Transactional
    public void deleteAllergy(Long allergyId) {
        allergyRepository.deleteById(allergyId);
    }

    /**
     * create diagnosis for resident
     */
    @Transactional
    public ResidentFullDTO.DiagnosisDTO createDiagnosis(Long residentId, ResidentFullDTO.DiagnosisDTO diagnosisDTO) {
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setResidentId(residentId);
        diagnosis.setDiagnosis(diagnosisDTO.getDiagnosis());
        diagnosis.setNotes(diagnosisDTO.getNotes());
        return mapDiagnosisToDTO(diagnosisRepository.save(diagnosis));
    }

    /**
     * delete diagnosis
     */
    @Transactional
    public void deleteDiagnosis(Long diagnosisId) {
        diagnosisRepository.deleteById(diagnosisId);
    }

    /**
     * update resident medical profile
     */
    @Transactional
    public void updateMedicalProfile(Long residentId, Map<String, String> updates) {
        if (updates == null || updates.isEmpty()) {
            throw new RuntimeException("No updates provided");
        }

        // Find or create medical profile
        MedicalProfile medicalProfile = medicalProfileRepository.findById(residentId).orElse(null);

        if (medicalProfile == null) {
            // Create new medical profile if it doesn't exist
            medicalProfile = new MedicalProfile();
            medicalProfile.setResidentId(residentId);
        }

        if (updates.containsKey("insurance")) {
            String insurance = updates.get("insurance");
            medicalProfile.setInsurance(insurance != null && !insurance.trim().isEmpty() ? insurance : null);
        }
        if (updates.containsKey("notes")) {
            String notes = updates.get("notes");
            medicalProfile.setNotes(notes != null && !notes.trim().isEmpty() ? notes : null);
        }

        medicalProfileRepository.save(medicalProfile);
    }

    /**
     * updated medical services
     */
    @Transactional
    public void updateMedicalServices(Long residentId, Map<String, Object> updates) {
        // find/create medical profile
        MedicalProfile medicalProfile = medicalProfileRepository.findById(residentId).orElse(null);
        if (medicalProfile == null) {
            medicalProfile = new MedicalProfile();
            medicalProfile.setResidentId(residentId);
            medicalProfile = medicalProfileRepository.save(medicalProfile);
        }

        // find/create medical services
        MedicalServices services = medicalProfile.getMedicalServices();
        if (services == null) {
            services = new MedicalServices();
            services.setResidentId(residentId);
            medicalProfile.setMedicalServices(services);
        }

        if (updates.containsKey("dnrPolst")) {
            services.setDnrPolst((String) updates.get("dnrPolst"));
        }
        if (updates.containsKey("hospice")) {
            services.setHospice((Boolean) updates.get("hospice"));
        }
        if (updates.containsKey("hospiceAgency")) {
            services.setHospiceAgency((String) updates.get("hospiceAgency"));
        }
        if (updates.containsKey("preferredHospital")) {
            services.setPreferredHospital((String) updates.get("preferredHospital"));
        }
        if (updates.containsKey("preferredPharmacy")) {
            services.setPreferredPharmacy((String) updates.get("preferredPharmacy"));
        }
        if (updates.containsKey("homeHealth")) {
            services.setHomeHealth((Boolean) updates.get("homeHealth"));
        }
        if (updates.containsKey("homeHealthAgency")) {
            services.setHomeHealthAgency((String) updates.get("homeHealthAgency"));
        }
        if (updates.containsKey("mortuary")) {
            services.setMortuary((String) updates.get("mortuary"));
        }

        medicalProfileRepository.save(medicalProfile);
    }

    /**
     * update resident capabilities
     */
    @Transactional
    public void updateCapabilities(Long residentId, Map<String, Object> updates) {
        // find/create medical profile
        MedicalProfile medicalProfile = medicalProfileRepository.findById(residentId).orElse(null);
        if (medicalProfile == null) {
            medicalProfile = new MedicalProfile();
            medicalProfile.setResidentId(residentId);
            medicalProfile = medicalProfileRepository.save(medicalProfile);
        }

        // find/create capability
        Capability capability = medicalProfile.getCapability();
        if (capability == null) {
            capability = new Capability();
            capability.setResidentId(residentId);
            medicalProfile.setCapability(capability);
        }

        if (updates.containsKey("mobilityStatus")) {
            String mobilityStr = (String) updates.get("mobilityStatus");
            if (mobilityStr != null && !mobilityStr.isEmpty()) {
                capability.setMobilityStatus(Capability.MobilityStatus.valueOf(mobilityStr.toUpperCase().replace(" ", "_")));
            }
        }
        if (updates.containsKey("incontinenceStatus")) {
            String incontinenceStr = (String) updates.get("incontinenceStatus");
            if (incontinenceStr != null && !incontinenceStr.isEmpty()) {
                capability.setIncontinenceStatus(Capability.IncontinenceStatus.valueOf(incontinenceStr.toUpperCase().replace(" ", "_").replace("(", "").replace(")", "")));
            }
        }
        if (updates.containsKey("selfMedicates")) {
            capability.setSelfMedicates((Boolean) updates.get("selfMedicates"));
        }
        if (updates.containsKey("verbal")) {
            capability.setVerbal((Boolean) updates.get("verbal"));
        }

        medicalProfileRepository.save(medicalProfile);
    }

    /**
     * delete medication
     */
    @Transactional
    public void deleteMedication(Long medicationId) {
        medicationRepository.deleteById(medicationId);
    }

    /**
     * update medication details
     */
    @Transactional
    public void updateMedication(Long medicationId, Map<String, String> updates) {
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new RuntimeException("Medication not found with id: " + medicationId));

        if (updates.containsKey("dosage")) {
            medication.setDosage(updates.get("dosage"));
        }
        if (updates.containsKey("schedule")) {
            medication.setFrequency(updates.get("schedule"));
        }
        if (updates.containsKey("notes")) {
            medication.setNotes(updates.get("notes"));
        }

        medicationRepository.save(medication);
    }
}
